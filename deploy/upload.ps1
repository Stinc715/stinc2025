# Windows上传脚本 - Club Portal
# 使用方法: .\upload.ps1

$SERVER_IP = "13.40.74.21"
$SSH_KEY = "C:\Users\shenn\Sntc00715.pem"
$SERVER_USER = "ubuntu"

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "  Club Portal 上传脚本" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan

# 检查SSH密钥
if (-not (Test-Path $SSH_KEY)) {
    Write-Host "错误: SSH密钥未找到: $SSH_KEY" -ForegroundColor Red
    exit 1
}

# 1. 打包前端文件
Write-Host "`n步骤 1: 打包前端文件..." -ForegroundColor Yellow
$frontendZip = "frontend.zip"
if (Test-Path $frontendZip) { Remove-Item $frontendZip -Force }
Compress-Archive -Path "public\*" -DestinationPath $frontendZip
Write-Host "✅ 前端文件已打包: $frontendZip" -ForegroundColor Green

# 2. 编译后端
Write-Host "`n步骤 2: 编译后端..." -ForegroundColor Yellow
Set-Location backend
mvn clean package -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ 后端编译失败" -ForegroundColor Red
    exit 1
}
Set-Location ..
Write-Host "✅ 后端编译成功" -ForegroundColor Green

# 3. 上传文件到服务器
Write-Host "`n步骤 3: 上传文件到服务器..." -ForegroundColor Yellow

# 上传前端
Write-Host "  上传前端文件..." -ForegroundColor Cyan
scp -i $SSH_KEY $frontendZip "${SERVER_USER}@${SERVER_IP}:~/"

# 上传后端
Write-Host "  上传后端JAR..." -ForegroundColor Cyan
scp -i $SSH_KEY "backend\target\club-portal-backend-1.0-SNAPSHOT.jar" "${SERVER_USER}@${SERVER_IP}:~/"

# 上传部署脚本
Write-Host "  上传部署脚本..." -ForegroundColor Cyan
scp -i $SSH_KEY "deploy\deploy.sh" "${SERVER_USER}@${SERVER_IP}:~/"
scp -i $SSH_KEY "deploy\nginx.conf" "${SERVER_USER}@${SERVER_IP}:~/"

Write-Host "✅ 所有文件上传完成" -ForegroundColor Green

# 4. 在服务器上执行部署
Write-Host "`n步骤 4: 在服务器上执行部署..." -ForegroundColor Yellow
Write-Host "连接到服务器并执行部署脚本..." -ForegroundColor Cyan

ssh -i $SSH_KEY "${SERVER_USER}@${SERVER_IP}" @"
echo '解压前端文件...'
mkdir -p ~/deploy/public
unzip -o ~/frontend.zip -d ~/deploy/public

echo '创建部署目录...'
mkdir -p ~/deploy

echo '移动文件到部署目录...'
mv ~/deploy.sh ~/deploy/
mv ~/nginx.conf ~/deploy/
mv ~/club-portal-backend-1.0-SNAPSHOT.jar ~/deploy/backend.jar

echo '设置执行权限...'
chmod +x ~/deploy/deploy.sh

echo '准备就绪！现在可以执行: cd ~/deploy && sudo ./deploy.sh'
echo ''
echo '或者手动部署:'
echo '  1. 安装Nginx: sudo apt-get install nginx'
echo '  2. 安装Java: sudo apt-get install openjdk-17-jdk'
echo '  3. 复制前端: sudo cp -r ~/deploy/public/* /var/www/club-portal/'
echo '  4. 配置Nginx: sudo cp ~/deploy/nginx.conf /etc/nginx/sites-available/club-portal'
echo '  5. 启动后端: java -jar ~/deploy/backend.jar'
"@

Write-Host ""
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "  上传完成！" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "下一步操作:" -ForegroundColor Yellow
Write-Host "  1. 连接到服务器: ssh -i $SSH_KEY ${SERVER_USER}@${SERVER_IP}" -ForegroundColor White
Write-Host "  2. 执行部署: cd ~/deploy && sudo ./deploy.sh" -ForegroundColor White
Write-Host ""
Write-Host "或者现在立即部署? (Y/N)" -ForegroundColor Yellow
$response = Read-Host
if ($response -eq 'Y' -or $response -eq 'y') {
    Write-Host "开始自动部署..." -ForegroundColor Green
    ssh -i $SSH_KEY "${SERVER_USER}@${SERVER_IP}" "cd ~/deploy && sudo ./deploy.sh"
}
