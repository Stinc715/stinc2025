import { spawnSync } from 'node:child_process';
import path from 'node:path';
import { ensurePlaywrightChromium, repoRoot } from './ensure-runtime.mjs';

const playwrightArgs = process.argv.slice(2);

await ensurePlaywrightChromium();

const result = spawnSync(process.execPath, [path.join(repoRoot, 'node_modules', 'playwright', 'cli.js'), ...playwrightArgs], {
  cwd: repoRoot,
  stdio: 'inherit',
});

if (result.error) {
  throw result.error;
}

process.exit(result.status ?? 1);
