import { copyFileSync, mkdirSync, writeFileSync, existsSync } from 'node:fs';
import { spawnSync } from 'node:child_process';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const scriptDir = path.dirname(fileURLToPath(import.meta.url));
const repoRoot = path.resolve(scriptDir, '..');
const outputRoot = path.join(repoRoot, 'artifacts', 'submission');
const submissionDirName = 'club-portal-submission';
const stageDir = path.join(outputRoot, submissionDirName);
const verificationReportJsonPath = path.join(outputRoot, 'verification-report.json');
const verificationReportMdPath = path.join(outputRoot, 'verification-report.md');
const stagedVerificationReportJsonPath = path.join(stageDir, 'verification-report.json');
const stagedVerificationReportMdPath = path.join(stageDir, 'verification-report.md');

const requiredFiles = [
  'README.md',
  '.env.example',
  'SUBMISSION_MANIFEST.md',
  'frontend/desktop-consistency.css',
  'tests/frontend/desktop-visual.spec.mjs',
  'playwright.config.mjs',
  'docs/auth-session-loading-audit.md',
  'docs/button-system-audit.md',
  'docs/final-delivery-guide.md',
];

const forbiddenPaths = [
  '.git',
  '.vscode',
  'node_modules',
  'dist',
  'backend/target',
  'backend/.github',
  '.tmp_deploy_key.pem',
];

const verificationCommands = [
  {
    id: 'npm_ci',
    label: 'npm ci',
    displayCommand: 'npm ci',
    args: ['ci'],
  },
  {
    id: 'build',
    label: 'build',
    displayCommand: 'npm run build',
    args: ['run', 'build'],
  },
  {
    id: 'frontend_static',
    label: 'frontend tests',
    displayCommand: 'npm run test:frontend',
    args: ['run', 'test:frontend'],
  },
  {
    id: 'frontend_visual',
    label: 'visual tests',
    displayCommand: 'npm run test:frontend:visual',
    args: ['run', 'test:frontend:visual'],
  },
  {
    id: 'backend',
    label: 'backend tests',
    displayCommand: 'npm run test:backend',
    args: ['run', 'test:backend'],
  },
  {
    id: 'acceptance',
    label: 'acceptance',
    displayCommand: 'npm run test:acceptance',
    args: ['run', 'test:acceptance'],
  },
];

function toPosix(relativePath) {
  return relativePath.split(path.sep).join('/');
}

function runNpm(displayCommand, args, cwd) {
  const started = Date.now();
  const startedAt = new Date().toISOString();
  const result = spawnSync(displayCommand, {
    cwd,
    shell: true,
    stdio: 'inherit',
  });
  const finishedAt = new Date().toISOString();

  return {
    displayCommand,
    workingDirectory: cwd === repoRoot ? '.' : `artifacts/submission/${submissionDirName}`,
    exitCode: typeof result.status === 'number' ? result.status : null,
    status: result.status === 0 ? 'passed' : 'failed',
    durationMs: Date.now() - started,
    startedAt,
    finishedAt,
    error: result.error ? String(result.error.message || result.error) : null,
  };
}

function checkRequiredFiles() {
  return requiredFiles.map((relativePath) => ({
    path: relativePath,
    exists: existsSync(path.join(stageDir, relativePath)),
  }));
}

function checkForbiddenPaths() {
  return forbiddenPaths.map((relativePath) => ({
    path: relativePath,
    exists: existsSync(path.join(stageDir, relativePath)),
  }));
}

function formatResultCell(passed) {
  return passed ? 'PASS' : 'FAIL';
}

function renderTable(rows, headers) {
  const headerRow = `| ${headers.join(' | ')} |`;
  const dividerRow = `| ${headers.map(() => '---').join(' | ')} |`;
  const bodyRows = rows.map((row) => `| ${row.join(' | ')} |`);
  return [headerRow, dividerRow, ...bodyRows].join('\n');
}

function writeReports(report) {
  mkdirSync(outputRoot, { recursive: true });
  writeFileSync(verificationReportJsonPath, `${JSON.stringify(report, null, 2)}\n`, 'utf8');

  const requiredTable = renderTable(
    report.requiredFiles.map((item) => [item.path, formatResultCell(item.exists)]),
    ['Required file', 'Result']
  );
  const forbiddenTable = renderTable(
    report.forbiddenPaths.map((item) => [item.path, item.exists ? 'FAIL' : 'PASS']),
    ['Forbidden path', 'Result']
  );
  const commandTable = renderTable(
    report.commands.map((command) => [
      command.displayCommand,
      command.workingDirectory,
      command.status.toUpperCase(),
      String(command.exitCode ?? ''),
      `${command.durationMs} ms`,
    ]),
    ['Command', 'Working directory', 'Result', 'Exit code', 'Duration']
  );

  const markdown = [
    '# Submission Verification Report',
    '',
    `- Verified at: ${report.generatedAt}`,
    `- Verification directory: ${report.verificationDir}`,
    `- Overall result: ${report.summary.success ? 'PASS' : 'FAIL'}`,
    `- Clean submission tree restored after verification: ${report.summary.restoredCleanSubmissionTree ? 'yes' : 'no'}`,
    '',
    '## Required files',
    '',
    requiredTable,
    '',
    '## Forbidden paths',
    '',
    forbiddenTable,
    '',
    '## Commands',
    '',
    commandTable,
    '',
    '## Notes',
    '',
    '- The report uses repo-relative paths only and does not include machine-specific absolute paths.',
    '- `npm run test:acceptance` reruns the frontend/backend/build chain inside the submission tree by design.',
    '- A PASS result assumes first-run network access was available for Playwright Chromium and the Maven distribution used by the Maven Wrapper, or that those caches already existed.',
    '- After those first-run downloads complete, later runs can reuse the local Playwright and Maven caches.',
    '- The `auth-session.js` classic-script build warning remains a known non-blocking warning during build and visual verification.',
    '',
  ].join('\n');

  writeFileSync(verificationReportMdPath, `${markdown}\n`, 'utf8');

  if (existsSync(stageDir)) {
    copyFileSync(verificationReportJsonPath, stagedVerificationReportJsonPath);
    copyFileSync(verificationReportMdPath, stagedVerificationReportMdPath);
  }
}

const report = {
  generatedAt: new Date().toISOString(),
  verificationDir: `artifacts/submission/${submissionDirName}`,
  requiredFiles: [],
  forbiddenPaths: [],
  commands: [],
  summary: {
    success: false,
    restoredCleanSubmissionTree: false,
  },
};

let continueStageCommands = true;

const packageResult = runNpm('npm run package:submission', ['run', 'package:submission'], repoRoot);
report.commands.push({
  id: 'package_submission',
  label: 'package:submission',
  ...packageResult,
});

if (packageResult.status === 'passed' && existsSync(stageDir)) {
  report.requiredFiles = checkRequiredFiles();
  report.forbiddenPaths = checkForbiddenPaths();
} else {
  continueStageCommands = false;
}

for (const command of verificationCommands) {
  if (!continueStageCommands) {
    report.commands.push({
      id: command.id,
      label: command.label,
      displayCommand: command.displayCommand,
      workingDirectory: `artifacts/submission/${submissionDirName}`,
      exitCode: null,
      status: 'skipped',
      durationMs: 0,
      startedAt: null,
      finishedAt: null,
      error: 'Skipped because packaging or a prior submission-tree command failed.',
    });
    continue;
  }

  const result = runNpm(command.displayCommand, command.args, stageDir);
  report.commands.push({
    id: command.id,
    label: command.label,
    ...result,
  });

  if (result.status !== 'passed') {
    continueStageCommands = false;
  }
}

const requiredFilesPassed =
  report.requiredFiles.length > 0 && report.requiredFiles.every((item) => item.exists);
const forbiddenPathsPassed =
  report.forbiddenPaths.length > 0 && report.forbiddenPaths.every((item) => !item.exists);
const commandStepsPassed = report.commands.every((command) => command.status === 'passed' || command.status === 'skipped')
  && report.commands
    .filter((command) => command.id !== 'restore_submission_tree')
    .every((command) => command.status === 'passed');

report.summary.success = requiredFilesPassed && forbiddenPathsPassed && commandStepsPassed;

writeReports(report);

const restoreResult = runNpm('npm run package:submission', ['run', 'package:submission'], repoRoot);
report.commands.push({
  id: 'restore_submission_tree',
  label: 'restore clean submission tree',
  ...restoreResult,
});
report.summary.restoredCleanSubmissionTree = restoreResult.status === 'passed';
report.summary.success = report.summary.success && report.summary.restoredCleanSubmissionTree;
report.generatedAt = new Date().toISOString();

writeReports(report);

const syncResult = runNpm('npm run package:submission', ['run', 'package:submission'], repoRoot);
report.commands.push({
  id: 'sync_submission_manifest',
  label: 'sync submission manifest with final verification report',
  ...syncResult,
});
report.summary.restoredCleanSubmissionTree =
  report.summary.restoredCleanSubmissionTree && syncResult.status === 'passed';
report.summary.success = report.summary.success && syncResult.status === 'passed';
report.generatedAt = new Date().toISOString();

writeReports(report);

if (!report.summary.success) {
  process.exitCode = 1;
}
