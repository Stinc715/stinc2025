import { spawnSync } from 'node:child_process';

const cmd = 'rg';
const args = [
  '-n',
  'window\\.confirm|window\\.alert|window\\.prompt',
  'frontend',
  'dist'
];

const res = spawnSync(cmd, args, { encoding: 'utf8' });

if (res.status === 1) {
  console.log('OK: no native browser prompts.');
  process.exit(0);
}

if (res.status === 0) {
  const output = String(res.stdout || '').trim();
  if (output) console.error(output);
  console.error('\nNative browser prompts found. Use window.AppPrompt instead.');
  process.exit(1);
}

if (res.error) {
  console.error(res.error.message);
  process.exit(2);
}

const stderr = String(res.stderr || '').trim();
if (stderr) console.error(stderr);
process.exit(typeof res.status === 'number' ? res.status : 2);
