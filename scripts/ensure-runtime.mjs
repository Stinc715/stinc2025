import { spawnSync } from 'node:child_process';
import { createHash } from 'node:crypto';
import { existsSync, mkdirSync, readFileSync, writeFileSync } from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const scriptDir = path.dirname(fileURLToPath(import.meta.url));
export const repoRoot = path.resolve(scriptDir, '..');

const nodeModulesDir = path.join(repoRoot, 'node_modules');
const packageLockPath = path.join(repoRoot, 'package-lock.json');
const stampPath = path.join(nodeModulesDir, '.portable-runtime-stamp.json');

function npmCommand() {
  return process.platform === 'win32' ? 'npm.cmd' : 'npm';
}

function readJson(filePath) {
  if (!existsSync(filePath)) return null;
  try {
    return JSON.parse(readFileSync(filePath, 'utf8'));
  } catch {
    return null;
  }
}

function writeJson(filePath, value) {
  mkdirSync(path.dirname(filePath), { recursive: true });
  writeFileSync(filePath, `${JSON.stringify(value, null, 2)}\n`, 'utf8');
}

function lockHash() {
  if (!existsSync(packageLockPath)) return null;
  return createHash('sha256').update(readFileSync(packageLockPath)).digest('hex');
}

function detectLibc() {
  if (process.platform !== 'linux') return null;
  const report = process.report?.getReport?.();
  return report?.header?.glibcVersionRuntime ? 'gnu' : 'musl';
}

function currentRuntime() {
  return {
    platform: process.platform,
    arch: process.arch,
    libc: detectLibc(),
    lockHash: lockHash(),
  };
}

function packagePath(packageName) {
  return path.join(nodeModulesDir, ...packageName.split('/'));
}

export function expectedFrontendNativePackages(runtime = currentRuntime()) {
  const expected = [];

  if (runtime.platform === 'linux') {
    if (runtime.arch === 'x64') {
      expected.push('@esbuild/linux-x64');
      expected.push(`@rollup/rollup-linux-x64-${runtime.libc ?? 'gnu'}`);
    } else if (runtime.arch === 'arm64') {
      expected.push('@esbuild/linux-arm64');
      expected.push(`@rollup/rollup-linux-arm64-${runtime.libc ?? 'gnu'}`);
    } else if (runtime.arch === 'arm') {
      expected.push('@esbuild/linux-arm');
      expected.push(`@rollup/rollup-linux-arm-${runtime.libc === 'musl' ? 'musleabihf' : 'gnueabihf'}`);
    } else if (runtime.arch === 'ia32') {
      expected.push('@esbuild/linux-ia32');
    }
  } else if (runtime.platform === 'darwin') {
    if (runtime.arch === 'arm64') {
      expected.push('@esbuild/darwin-arm64');
      expected.push('@rollup/rollup-darwin-arm64');
    } else if (runtime.arch === 'x64') {
      expected.push('@esbuild/darwin-x64');
      expected.push('@rollup/rollup-darwin-x64');
    }
  } else if (runtime.platform === 'win32') {
    if (runtime.arch === 'arm64') {
      expected.push('@esbuild/win32-arm64');
      expected.push('@rollup/rollup-win32-arm64-msvc');
    } else if (runtime.arch === 'ia32') {
      expected.push('@esbuild/win32-ia32');
      expected.push('@rollup/rollup-win32-ia32-msvc');
    } else if (runtime.arch === 'x64') {
      expected.push('@esbuild/win32-x64');
      expected.push('@rollup/rollup-win32-x64-msvc');
    }
  }

  return expected.filter(Boolean);
}

function validatePackages(requiredPackages, requiredNativePackages) {
  if (!existsSync(nodeModulesDir)) {
    return 'node_modules directory is missing';
  }

  for (const packageName of requiredPackages) {
    if (!existsSync(packagePath(packageName))) {
      return `${packageName} is not installed`;
    }
  }

  for (const packageName of requiredNativePackages) {
    if (!existsSync(packagePath(packageName))) {
      return `missing platform dependency ${packageName}`;
    }
  }

  return null;
}

function validateStamp(runtime) {
  const stamp = readJson(stampPath);
  if (!stamp) {
    return 'dependency install metadata is missing';
  }

  if (
    stamp.platform !== runtime.platform ||
    stamp.arch !== runtime.arch ||
    stamp.libc !== runtime.libc ||
    stamp.lockHash !== runtime.lockHash
  ) {
    return 'dependency install metadata does not match this machine or lockfile';
  }

  return null;
}

function runCommand(command, args) {
  const result = spawnSync(command, args, {
    cwd: repoRoot,
    stdio: 'inherit',
    shell: process.platform === 'win32' && command.endsWith('.cmd'),
  });

  if (result.error) {
    throw result.error;
  }

  if ((result.status ?? 1) !== 0) {
    process.exit(result.status ?? 1);
  }
}

export function ensurePortableNodeModules({
  requiredPackages = [],
  requiredNativePackages = [],
} = {}) {
  const runtime = currentRuntime();
  const packageProblem = validatePackages(requiredPackages, requiredNativePackages);
  const stampProblem = packageProblem ? null : validateStamp(runtime);
  const installProblem = packageProblem || (stampProblem === 'dependency install metadata is missing' ? null : stampProblem);

  if (installProblem) {
    const target = [runtime.platform, runtime.arch, runtime.libc].filter(Boolean).join('/');
    const installVerb = existsSync(packageLockPath) ? 'ci' : 'install';
    console.log(`[portable-runtime] ${installProblem}; running npm ${installVerb} to rebuild dependencies for ${target}.`);
    runCommand(
      npmCommand(),
      existsSync(packageLockPath) ? ['ci', '--no-audit', '--no-fund'] : ['install', '--no-audit', '--no-fund']
    );
  }

  const verifiedRuntime = currentRuntime();
  const verifiedProblem = validatePackages(requiredPackages, requiredNativePackages);
  if (verifiedProblem) {
    throw new Error(`Dependency bootstrap failed: ${verifiedProblem}`);
  }

  writeJson(stampPath, {
    createdAt: new Date().toISOString(),
    ...verifiedRuntime,
  });

  return verifiedRuntime;
}

export async function ensurePlaywrightChromium() {
  ensurePortableNodeModules({
    requiredPackages: ['playwright', '@playwright/test', 'vite', 'rollup', 'esbuild'],
    requiredNativePackages: expectedFrontendNativePackages(),
  });

  let executablePath = '';
  try {
    const { chromium } = await import('playwright');
    executablePath = chromium.executablePath();
  } catch {
    executablePath = '';
  }

  if (executablePath && existsSync(executablePath)) {
    return executablePath;
  }

  console.log('[portable-runtime] Playwright Chromium is missing; running playwright install chromium.');
  runCommand(process.execPath, [path.join(repoRoot, 'node_modules', 'playwright', 'cli.js'), 'install', 'chromium']);

  const { chromium } = await import('playwright');
  const installedExecutablePath = chromium.executablePath();
  if (!installedExecutablePath || !existsSync(installedExecutablePath)) {
    throw new Error('Playwright Chromium install completed but the browser executable was not found.');
  }

  return installedExecutablePath;
}

if (process.argv[1] === fileURLToPath(import.meta.url)) {
  ensurePortableNodeModules({
    requiredPackages: ['vite', 'rollup', 'esbuild'],
    requiredNativePackages: expectedFrontendNativePackages(),
  });
}
