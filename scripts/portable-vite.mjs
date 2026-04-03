import { spawnSync } from 'node:child_process';
import path from 'node:path';
import { ensurePortableNodeModules, expectedFrontendNativePackages, repoRoot } from './ensure-runtime.mjs';

const viteArgs = process.argv.slice(2);

ensurePortableNodeModules({
  requiredPackages: ['vite', 'rollup', 'esbuild'],
  requiredNativePackages: expectedFrontendNativePackages(),
});

const result = spawnSync(process.execPath, [path.join(repoRoot, 'node_modules', 'vite', 'bin', 'vite.js'), ...viteArgs], {
  cwd: repoRoot,
  stdio: 'inherit',
});

if (result.error) {
  throw result.error;
}

process.exit(result.status ?? 1);
