import { readFileSync, readdirSync, statSync } from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const scriptDir = path.dirname(fileURLToPath(import.meta.url));
const repoRoot = path.resolve(scriptDir, '..');

const scanRoots = [
  'frontend',
  'backend',
  'scripts',
  'tests',
];

const skipDirNames = new Set([
  'node_modules',
  'dist',
  'artifacts',
  'backend/target',
  'playwright-report',
  'test-results',
]);

const allowedFiles = new Set([
  'frontend/ui-prompt.js',
]);

const scannedExtensions = new Set([
  '.html',
  '.java',
  '.js',
  '.json',
  '.mjs',
]);

const explicitNativePattern = /\b(?:window|globalThis|self)\.(alert|confirm|prompt)\s*\(/g;
const bareNativePattern = /(^|[^\w$.])(alert|confirm|prompt)\s*\(/g;

function toPosix(value) {
  return value.split(path.sep).join('/');
}

function shouldSkip(relativePath) {
  const normalized = toPosix(relativePath);
  return normalized.split('/').some((segment) => skipDirNames.has(segment));
}

function walk(relativePath, files) {
  if (shouldSkip(relativePath)) return;
  const absolutePath = path.join(repoRoot, relativePath);
  const stats = statSync(absolutePath);

  if (stats.isDirectory()) {
    for (const childName of readdirSync(absolutePath)) {
      const childRelativePath = path.join(relativePath, childName);
      walk(childRelativePath, files);
    }
    return;
  }

  if (!scannedExtensions.has(path.extname(relativePath))) {
    return;
  }

  files.push(toPosix(relativePath));
}

function collectFiles() {
  const files = [];
  for (const root of scanRoots) {
    walk(root, files);
  }
  return files.sort();
}

function lineNumberAt(text, index) {
  return text.slice(0, index).split(/\r?\n/).length;
}

function findViolations(relativePath) {
  if (allowedFiles.has(relativePath)) {
    return [];
  }

  const absolutePath = path.join(repoRoot, relativePath);
  const text = readFileSync(absolutePath, 'utf8');
  const violations = [];

  for (const match of text.matchAll(explicitNativePattern)) {
    violations.push({
      file: relativePath,
      line: lineNumberAt(text, match.index),
      api: match[1],
      kind: 'explicit global call',
    });
  }

  for (const match of text.matchAll(bareNativePattern)) {
    const api = match[2];
    const index = Number(match.index) + String(match[1] || '').length;
    const line = lineNumberAt(text, index);
    const lineText = text.split(/\r?\n/)[line - 1] || '';
    const trimmedLine = lineText.trim();

    if (
      trimmedLine.startsWith(`function ${api}(`)
      || trimmedLine.startsWith(`const ${api} =`)
      || trimmedLine.startsWith(`let ${api} =`)
      || trimmedLine.startsWith(`var ${api} =`)
      || trimmedLine.includes(`window.AppPrompt.${api}`)
    ) {
      continue;
    }

    violations.push({
      file: relativePath,
      line,
      api,
      kind: 'bare native-style call',
    });
  }

  return violations;
}

const files = collectFiles();
const violations = files.flatMap(findViolations);

if (violations.length) {
  console.error('Native browser prompt usage is not allowed. Use window.AppPrompt instead.');
  for (const violation of violations) {
    console.error(`- ${violation.file}:${violation.line} -> ${violation.kind} to ${violation.api}()`);
  }
  process.exitCode = 1;
} else {
  console.log(`Prompt policy check passed across ${files.length} source files.`);
}
