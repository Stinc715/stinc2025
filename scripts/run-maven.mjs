import { spawnSync } from 'node:child_process';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const scriptDir = path.dirname(fileURLToPath(import.meta.url));
const repoRoot = path.resolve(scriptDir, '..');
const args = process.argv.slice(2);

const result =
  process.platform === 'win32'
    ? spawnSync(path.join(repoRoot, 'mvnw.cmd'), args, {
        cwd: repoRoot,
        stdio: 'inherit',
        shell: true,
      })
    : spawnSync('sh', [path.join(repoRoot, 'mvnw'), ...args], {
        cwd: repoRoot,
        stdio: 'inherit',
      });

if (result.error) {
  throw result.error;
}

process.exit(result.status ?? 1);
