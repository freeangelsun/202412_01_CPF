import fs from 'node:fs';
import path from 'node:path';
const root = process.cwd();
const spec = JSON.parse(fs.readFileSync(path.resolve(root, process.env.CPF_OPENAPI_FILE || 'openapi/cpf-openapi.json'), 'utf8'));
const clientPath = path.resolve(root, process.env.CPF_COMPAT_CLIENT || 'src/generated/cpf-api.ts');
const source = fs.readFileSync(clientPath, 'utf8');
const methods = new Set(['post','put','patch','delete']);
const operationIds = [];
const mutationIds = [];
for (const item of Object.values(spec.paths || {})) {
  for (const [method, op] of Object.entries(item || {})) {
    if (!op?.operationId) continue;
    operationIds.push(op.operationId);
    if (methods.has(method.toLowerCase())) mutationIds.push(op.operationId);
  }
}
const failures = [];
if (!source.includes("CPF_CANONICAL_ORVAL_DELEGATE")) failures.push("orval-delegate-marker-missing");
if (!source.includes('from "./orval/cpf-api"')) failures.push("orval-delegate-import-missing");
if (source.includes("cpfGeneratedRequest")) failures.push("parallel-generic-transport-forbidden");
for (const id of operationIds) {
  if (!source.includes(`function ${id}<`)) failures.push(`missing-operation:${id}`);
  const symbol = id.charAt(0).toUpperCase() + id.slice(1);
  if (!source.includes(`${id} as orval${symbol}`) || !source.includes(`await orval${symbol}(`)) failures.push(`missing-orval-delegate:${id}`);
}
if (/data\?: unknown\b/.test(source) || /data:\s*unknown\b/.test(source)) failures.push('generic-unknown-body-contract');
for (const id of mutationIds) {
  const symbol = id.charAt(0).toUpperCase() + id.slice(1);
  const marker = `export type ${symbol}Body = `;
  if (!source.includes(marker)) failures.push(`missing-mutation-body-type:${id}`);
  const line = source.split('\n').find(v => v.startsWith(marker)) || '';
  if (/=\s*unknown;/.test(line)) failures.push(`unknown-mutation-body:${id}`);
}
const generatedCount = [...source.matchAll(/export async function ([A-Za-z0-9_$]+)</g)].length;
if (generatedCount !== operationIds.length) failures.push(`operation-count:${generatedCount}/${operationIds.length}`);
if (failures.length) {
  console.error(`CPF canonical compatibility client FAIL count=${failures.length}`);
  for (const failure of failures) console.error(` - ${failure}`);
  process.exit(1);
}
console.log(`CPF canonical compatibility client PASS operations=${operationIds.length} mutations=${mutationIds.length}`);
