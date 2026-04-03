[CmdletBinding()]
param(
    [string]$ServerHost = $env:CLUB_PORTAL_DEPLOY_HOST,
    [string]$ServerUser = $env:CLUB_PORTAL_DEPLOY_USER,
    [string]$SshKey = $env:CLUB_PORTAL_DEPLOY_SSH_KEY,
    [string]$RemoteDeployDir = $env:CLUB_PORTAL_DEPLOY_DIR,
    [switch]$SkipBuild,
    [switch]$SkipTests,
    [switch]$UploadOnly
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
$backendDir = Join-Path $repoRoot "backend"
$deployDir = Join-Path $repoRoot "deploy"
$artifactsDir = Join-Path $repoRoot "artifacts\deploy"
$schemaPath = Join-Path $deployDir "mysql_schema.sql"
$migrationDir = Join-Path $deployDir "migrations"
$backendJar = Join-Path $backendDir "target\club-portal-backend-1.0-SNAPSHOT.jar"
$archiveName = "dist-deploy-{0}.tgz" -f (Get-Date -Format "yyyyMMddHHmmss")
$archivePath = Join-Path $artifactsDir $archiveName
$npmExecutable = if ($IsWindows -or $env:OS -eq "Windows_NT") { "npm.cmd" } else { "npm" }
$nodeExecutable = if (Get-Command node -ErrorAction SilentlyContinue) { "node" } else { "node.exe" }

function Assert-RequiredValue {
    param(
        [string]$Name,
        [string]$Value,
        [string]$EnvironmentVariable
    )

    if ([string]::IsNullOrWhiteSpace($Value)) {
        throw "Missing required value: $Name. Pass -$Name or set $EnvironmentVariable."
    }
}

Assert-RequiredValue -Name "ServerHost" -Value $ServerHost -EnvironmentVariable "CLUB_PORTAL_DEPLOY_HOST"
Assert-RequiredValue -Name "ServerUser" -Value $ServerUser -EnvironmentVariable "CLUB_PORTAL_DEPLOY_USER"
Assert-RequiredValue -Name "SshKey" -Value $SshKey -EnvironmentVariable "CLUB_PORTAL_DEPLOY_SSH_KEY"

if ([string]::IsNullOrWhiteSpace($RemoteDeployDir)) {
    $RemoteDeployDir = "/home/$ServerUser/deploy"
}

$remoteArchivePath = "$remoteDeployDir/$archiveName"
$remoteTarget = "$ServerUser@$ServerHost"

function Write-Step {
    param([string]$Message)
    Write-Host ""
    Write-Host $Message -ForegroundColor Yellow
}

function Invoke-Checked {
    param(
        [string]$FilePath,
        [string[]]$Arguments,
        [string]$FailureMessage
    )

    & $FilePath @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw $FailureMessage
    }
}

if (-not (Test-Path $SshKey)) {
    throw "SSH key not found: $SshKey"
}

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "  Club Portal upload script (parameterized)" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "Server: $remoteTarget" -ForegroundColor White
if ($SkipTests) {
    Write-Host "Safety checks: tests will be skipped because -SkipTests was provided." -ForegroundColor Yellow
}
else {
    Write-Host "Safety checks: tests will run before packaging." -ForegroundColor White
}

Push-Location $repoRoot
try {
    if (-not $SkipBuild) {
        if (-not $SkipTests) {
            Write-Step "Step 1/5: running acceptance tests"
            Invoke-Checked -FilePath $npmExecutable -Arguments @("run", "test:acceptance") -FailureMessage "Acceptance test chain failed."

            Write-Step "Step 2/5: packaging backend with tests enabled"
            Invoke-Checked -FilePath $nodeExecutable -Arguments @("scripts/run-maven.mjs", "-f", "backend/pom.xml", "clean", "package") -FailureMessage "Backend package failed."
        }
        else {
            Write-Step "Step 1/5: building frontend"
            Invoke-Checked -FilePath $npmExecutable -Arguments @("run", "build") -FailureMessage "Frontend build failed."

            Write-Step "Step 2/5: packaging backend without tests"
            Invoke-Checked -FilePath $nodeExecutable -Arguments @("scripts/run-maven.mjs", "-f", "backend/pom.xml", "clean", "package", "-DskipTests") -FailureMessage "Backend package failed."
        }
    }

    if (-not (Test-Path $backendJar)) {
        throw "Backend JAR not found: $backendJar"
    }
    if (-not (Test-Path (Join-Path $repoRoot "dist"))) {
        throw "Frontend dist directory not found. Run the frontend build first."
    }

    Write-Step "Step 3/5: packaging frontend"
    New-Item -ItemType Directory -Force -Path $artifactsDir | Out-Null
    if (Test-Path $archivePath) {
        Remove-Item $archivePath -Force
    }
    Invoke-Checked -FilePath "tar" -Arguments @("-czf", $archivePath, "-C", (Join-Path $repoRoot "dist"), ".") -FailureMessage "Frontend archive creation failed."

    Write-Step "Step 4/5: uploading deploy assets"
    Invoke-Checked -FilePath "ssh" -Arguments @(
        "-o", "StrictHostKeyChecking=accept-new",
        "-i", $SshKey,
        $remoteTarget,
        "mkdir -p $remoteDeployDir/migrations"
    ) -FailureMessage "Failed to create remote deploy directories."

    $scpCommonArgs = @("-o", "StrictHostKeyChecking=accept-new", "-i", $SshKey)
    $uploadSpecs = @(
        @{ Local = $archivePath; Remote = "$remoteTarget`:$remoteDeployDir/" },
        @{ Local = $backendJar; Remote = "$remoteTarget`:$remoteDeployDir/club-portal-backend-1.0-SNAPSHOT.jar" },
        @{ Local = $schemaPath; Remote = "$remoteTarget`:$remoteDeployDir/mysql_schema.sql" },
        @{ Local = (Join-Path $deployDir "nginx.conf"); Remote = "$remoteTarget`:$remoteDeployDir/nginx.conf" },
        @{ Local = (Join-Path $deployDir "remote_release_frontend_only.sh"); Remote = "$remoteTarget`:$remoteDeployDir/remote_release_frontend_only.sh" },
        @{ Local = (Join-Path $deployDir "remote_release_full.sh"); Remote = "$remoteTarget`:$remoteDeployDir/remote_release_full.sh" },
        @{ Local = (Join-Path $deployDir "remote_apply_schema_from_env.sh"); Remote = "$remoteTarget`:$remoteDeployDir/remote_apply_schema_from_env.sh" },
        @{ Local = (Join-Path $deployDir "remote_deploy_backend.sh"); Remote = "$remoteTarget`:$remoteDeployDir/remote_deploy_backend.sh" }
    )

    foreach ($uploadSpec in $uploadSpecs) {
        Invoke-Checked -FilePath "scp" -Arguments ($scpCommonArgs + @($uploadSpec.Local, $uploadSpec.Remote)) -FailureMessage "Upload failed for $($uploadSpec.Local)"
    }

    $migrationFiles = Get-ChildItem -Path $migrationDir -Filter "*.sql" -File | Sort-Object Name
    foreach ($migrationFile in $migrationFiles) {
        Invoke-Checked -FilePath "scp" -Arguments ($scpCommonArgs + @($migrationFile.FullName, "$remoteTarget`:$remoteDeployDir/migrations/")) -FailureMessage "Upload failed for $($migrationFile.FullName)"
    }

    $remotePrepCommand = "chmod +x $remoteDeployDir/*.sh"
    Invoke-Checked -FilePath "ssh" -Arguments @(
        "-o", "StrictHostKeyChecking=accept-new",
        "-i", $SshKey,
        $remoteTarget,
        $remotePrepCommand
    ) -FailureMessage "Failed to prepare remote deploy scripts."

    if ($UploadOnly) {
        Write-Host ""
        Write-Host "Upload completed. Remote release was skipped because -UploadOnly was provided." -ForegroundColor Green
        Write-Host "Run this on the server to release manually:" -ForegroundColor White
        Write-Host "  cd $remoteDeployDir && ./remote_release_full.sh $remoteArchivePath" -ForegroundColor White
        exit 0
    }

    Write-Step "Step 5/5: releasing on the server"
    Invoke-Checked -FilePath "ssh" -Arguments @(
        "-o", "StrictHostKeyChecking=accept-new",
        "-i", $SshKey,
        $remoteTarget,
        "cd $remoteDeployDir && ./remote_release_full.sh $remoteArchivePath"
    ) -FailureMessage "Remote release failed."

    Write-Host ""
    Write-Host "Deployment completed successfully." -ForegroundColor Green
    Write-Host "Frontend archive: $archiveName" -ForegroundColor White
    Write-Host "Remote host: $remoteTarget" -ForegroundColor White
}
finally {
    Pop-Location
}
