#!/usr/bin/env node
'use strict';

const fs = require('fs');
const path = require('path');
const childProcess = require('child_process');
const { createRequire } = require('module');

const root = path.resolve(process.argv[2] || '.');

function loadTypeScript() {
  const candidates = [];
  for (const frontend of ['cpf-admin/frontend', 'cpf-backoffice-web/frontend']) {
    const packageJson = path.join(root, frontend, 'package.json');
    if (fs.existsSync(packageJson)) {
      try {
        const localRequire = createRequire(packageJson);
        return localRequire('typescript');
      } catch (error) {
        candidates.push(`${frontend}/node_modules`);
      }
    }
  }
  try {
    return require('typescript');
  } catch (error) {
    candidates.push('NODE_PATH/default require');
  }
  try {
    const globalRoot = childProcess.execFileSync('npm', ['root', '-g'], {
      encoding: 'utf8',
      stdio: ['ignore', 'pipe', 'ignore'],
    }).trim();
    if (globalRoot) return require(path.join(globalRoot, 'typescript'));
  } catch (error) {
    candidates.push('npm root -g');
  }
  throw new Error(`TypeScript compiler를 찾을 수 없습니다. npm ci 후 다시 실행하십시오. searched=${candidates.join(', ')}`);
}

let ts;
try {
  ts = loadTypeScript();
} catch (error) {
  console.error(`[FAIL] ${error.message}`);
  process.exit(2);
}

const files = [];
const errors = [];

function walk(dir) {
  for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
    const absolute = path.join(dir, entry.name);
    if (entry.isDirectory()) walk(absolute);
    else if (/\.(ts|vue)$/.test(entry.name)) files.push(absolute);
  }
}

for (const relative of ['cpf-admin/frontend/src', 'cpf-backoffice-web/frontend/src']) {
  const absolute = path.join(root, relative);
  if (fs.existsSync(absolute)) walk(absolute);
}

for (const file of files) {
  let source = fs.readFileSync(file, 'utf8');
  if (file.endsWith('.vue')) {
    const match = source.match(/<script\b(?:(?:"[^"]*")|(?:'[^']*')|[^>])*?>([\s\S]*?)<\/script>/i);
    if (!match) continue;
    source = match[1];
  }
  const output = ts.transpileModule(source, {
    compilerOptions: {
      target: ts.ScriptTarget.ES2022,
      module: ts.ModuleKind.ESNext,
    },
    reportDiagnostics: true,
    fileName: file,
  });
  for (const diagnostic of output.diagnostics || []) {
    if (diagnostic.category !== ts.DiagnosticCategory.Error) continue;
    const message = ts.flattenDiagnosticMessageText(diagnostic.messageText, ' ');
    const line = diagnostic.start == null ? '' : source.slice(0, diagnostic.start).split('\n').length;
    errors.push(`${path.relative(root, file)}:${line} ${message}`);
  }
}

for (const error of errors) console.error('[FAIL]', error);
console.log(`FRONTEND_SYNTAX=${errors.length ? 'FAIL' : 'PASS'} files=${files.length} errors=${errors.length}`);
process.exit(errors.length ? 1 : 0);
