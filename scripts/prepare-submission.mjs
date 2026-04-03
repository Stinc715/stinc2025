import {
  chmodSync,
  copyFileSync,
  existsSync,
  lstatSync,
  mkdirSync,
  readdirSync,
  readFileSync,
  rmSync,
  statSync,
  writeFileSync,
} from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const scriptDir = path.dirname(fileURLToPath(import.meta.url));
const repoRoot = path.resolve(scriptDir, '..');
const outputRoot = path.join(repoRoot, 'artifacts', 'submission');
const submissionDirName = 'club-portal-submission';
const stageDir = path.join(outputRoot, submissionDirName);
const outputManifestPath = path.join(outputRoot, 'manifest.json');
const verificationReportJsonPath = path.join(outputRoot, 'verification-report.json');
const verificationReportMdPath = path.join(outputRoot, 'verification-report.md');
const submissionManifestPath = path.join(stageDir, 'SUBMISSION_MANIFEST.md');
const stagedVerificationReportJsonPath = path.join(stageDir, 'verification-report.json');
const stagedVerificationReportMdPath = path.join(stageDir, 'verification-report.md');

const includeTopLevelEntries = [
  '.env.example',
  '.gitattributes',
  '.gitignore',
  '.mvn',
  'README.md',
  'backend',
  'deploy',
  'docs',
  'frontend',
  'mvnw',
  'mvnw.cmd',
  'package-lock.json',
  'package.json',
  'playwright.config.mjs',
  'scripts',
  'tests',
  'vite.config.js',
];

const forbiddenDirNames = new Set([
  '.git',
  '.vscode',
  'artifacts',
  'backend/target',
  'dist',
  'node_modules',
  'playwright-report',
  'target',
  'test-results',
]);

const forbiddenPathPrefixes = [
  'backend/.github',
];

const forbiddenFileNames = new Set([
  '.tmp_deploy_key.pem',
]);

const forbiddenExtensions = [
  '.bak',
  '.log',
  '.pem',
  '.tgz',
  '.zip',
];

const acceptanceScope = [
  'Desktop only',
  'Chrome / Edge latest stable',
  'Browser zoom: 100%',
  'Windows is the main acceptance environment',
  'Fixed desktop viewport baselines: 1366x768, 1440x900, 1920x1080',
];

const knownRetainedItems = [
  '`auth-session.js` classic script warning is an intentional non-blocking warning.',
  'Complex-page `.btn` systems remain page-local on purpose and are not force-merged.',
];

const excludedContent = [
  '`.git/`',
  '`.vscode/`',
  '`node_modules/`',
  '`dist/`',
  '`backend/target/`',
  '`backend/.github/` internal upgrade/planning metadata',
  'private keys such as `.pem` files',
];

const verificationCommandSummary = [
  {
    id: 'build',
    label: 'build',
    command: 'npm run build',
  },
  {
    id: 'frontend_static',
    label: 'frontend tests',
    command: 'npm run test:frontend',
  },
  {
    id: 'frontend_visual',
    label: 'visual tests',
    command: 'npm run test:frontend:visual',
  },
  {
    id: 'backend',
    label: 'backend tests',
    command: 'npm run test:backend',
  },
  {
    id: 'acceptance',
    label: 'acceptance',
    command: 'npm run test:acceptance',
  },
];

function toPosix(relativePath) {
  return relativePath.split(path.sep).join('/');
}

function shouldIgnore(relativePath, isDirectory) {
  const normalized = toPosix(relativePath);
  if (!normalized) return false;

  const segments = normalized.split('/');
  const baseName = segments[segments.length - 1];

  if (forbiddenFileNames.has(baseName)) {
    return true;
  }

  if (segments.some((segment) => forbiddenDirNames.has(segment))) {
    return true;
  }

  if (forbiddenDirNames.has(normalized)) {
    return true;
  }

  if (forbiddenPathPrefixes.some((prefix) => normalized === prefix || normalized.startsWith(`${prefix}/`))) {
    return true;
  }

  if (!isDirectory && forbiddenExtensions.some((suffix) => baseName.endsWith(suffix))) {
    return true;
  }

  return false;
}

function copyEntry(sourcePath, destinationPath, relativePath = '') {
  const stats = lstatSync(sourcePath);
  if (shouldIgnore(relativePath, stats.isDirectory())) {
    return;
  }

  if (stats.isSymbolicLink()) {
    throw new Error(`Submission packaging does not support symbolic links: ${relativePath}`);
  }

  if (stats.isDirectory()) {
    mkdirSync(destinationPath, { recursive: true });
    for (const childName of readdirSync(sourcePath)) {
      const childSourcePath = path.join(sourcePath, childName);
      const childDestinationPath = path.join(destinationPath, childName);
      const childRelativePath = relativePath ? path.join(relativePath, childName) : childName;
      copyEntry(childSourcePath, childDestinationPath, childRelativePath);
    }
    return;
  }

  mkdirSync(path.dirname(destinationPath), { recursive: true });
  copyFileSync(sourcePath, destinationPath);
  chmodSync(destinationPath, stats.mode);
}

function walk(relativeDir = '') {
  const absoluteDir = path.join(stageDir, relativeDir);
  const items = [];
  for (const childName of readdirSync(absoluteDir)) {
    const childRelativePath = relativeDir ? path.join(relativeDir, childName) : childName;
    const childAbsolutePath = path.join(stageDir, childRelativePath);
    const childStats = statSync(childAbsolutePath);
    items.push(childRelativePath);
    if (childStats.isDirectory()) {
      items.push(...walk(childRelativePath));
    }
  }
  return items;
}

function formatVerificationStatus(report) {
  if (!report || report.summary?.success !== true) {
    return {
      header: 'Verification status: pending fresh verification',
      lines: verificationCommandSummary.map(
        ({ label, command }) => `- ${label}: pending (${command}; run \`npm run verify:submission\`)`
      ),
    };
  }

  const commandById = new Map((report.commands || []).map((command) => [command.id, command]));
  return {
    header: `Latest successful verification: ${report.generatedAt}`,
    lines: verificationCommandSummary.map(({ id, label, command }) => {
      const result = commandById.get(id);
      const status = result?.status === 'passed' ? 'passed' : 'not verified';
      return `- ${label}: ${status} via \`${command}\``;
    }),
  };
}

function readVerificationReport() {
  if (!existsSync(verificationReportJsonPath)) {
    return null;
  }

  try {
    const parsed = JSON.parse(readFileSync(verificationReportJsonPath, 'utf8'));
    if (parsed?.verificationDir !== `artifacts/submission/${submissionDirName}`) {
      return null;
    }
    return parsed;
  } catch {
    return null;
  }
}

function renderSubmissionManifest(createdAt) {
  const verificationReport = readVerificationReport();
  const verificationStatus = formatVerificationStatus(verificationReport);
  const verificationReportLine = verificationReport
    ? '- Verification report: `verification-report.md`'
    : '- Verification report: pending fresh verification (run `npm run verify:submission`)';

  return [
    '# Submission Manifest',
    '',
    `- Generated at: ${createdAt}`,
    `- Submission package: \`artifacts/submission/${submissionDirName}\``,
    `- Project version: \`${JSON.parse(readFileSync(path.join(repoRoot, 'package.json'), 'utf8')).version}\``,
    `- ${verificationStatus.header}`,
    verificationReportLine,
    '',
    '## Acceptance scope',
    '',
    ...acceptanceScope.map((item) => `- ${item}`),
    '',
    '## Verified command summary',
    '',
    ...verificationStatus.lines,
    '',
    '## Intentionally retained known items',
    '',
    ...knownRetainedItems.map((item) => `- ${item}`),
    '',
    '## Excluded from this package',
    '',
    ...excludedContent.map((item) => `- ${item}`),
    '',
  ].join('\n');
}

function syncVerificationArtifactsIntoStage() {
  if (existsSync(verificationReportMdPath)) {
    copyFileSync(verificationReportMdPath, stagedVerificationReportMdPath);
  }

  if (existsSync(verificationReportJsonPath)) {
    copyFileSync(verificationReportJsonPath, stagedVerificationReportJsonPath);
  }
}

mkdirSync(outputRoot, { recursive: true });
rmSync(stageDir, { recursive: true, force: true });
mkdirSync(stageDir, { recursive: true });

for (const entry of includeTopLevelEntries) {
  const sourcePath = path.join(repoRoot, entry);
  if (!existsSync(sourcePath)) {
    throw new Error(`Required entry is missing and cannot be packaged: ${entry}`);
  }

  copyEntry(sourcePath, path.join(stageDir, entry), entry);
}

const packagedEntries = walk().map(toPosix).sort();
for (const entry of packagedEntries) {
  const absolutePath = path.join(stageDir, entry);
  const entryStats = statSync(absolutePath);
  if (shouldIgnore(entry, entryStats.isDirectory())) {
    throw new Error(`Forbidden entry leaked into submission package: ${entry}`);
  }
}

for (const entry of includeTopLevelEntries) {
  if (!existsSync(path.join(stageDir, entry))) {
    throw new Error(`Expected submission entry is missing: ${entry}`);
  }
}

const createdAt = new Date().toISOString();

writeFileSync(
  submissionManifestPath,
  renderSubmissionManifest(createdAt),
  'utf8'
);

syncVerificationArtifactsIntoStage();

writeFileSync(
  outputManifestPath,
  `${JSON.stringify(
    {
      createdAt,
      stageDir: `artifacts/submission/${submissionDirName}`,
      includedTopLevelEntries: includeTopLevelEntries,
      forbiddenDirNames: [...forbiddenDirNames].sort(),
      forbiddenFileNames: [...forbiddenFileNames].sort(),
      forbiddenExtensions: [...forbiddenExtensions].sort(),
      submissionManifest: `artifacts/submission/${submissionDirName}/SUBMISSION_MANIFEST.md`,
      verificationReport: existsSync(stagedVerificationReportJsonPath)
        ? `artifacts/submission/${submissionDirName}/verification-report.json`
        : null,
    },
    null,
    2
  )}\n`,
  'utf8'
);

console.log(`Prepared clean submission tree at artifacts/submission/${submissionDirName}`);
