import fs from 'node:fs';
import path from 'node:path';

const root = process.cwd();
const spec = JSON.parse(fs.readFileSync(path.join(root, 'openapi/cpf-openapi.json'), 'utf8'));
const failures = [];
const requireFields = (operationId, fields) => {
  let operation;
  for (const item of Object.values(spec.paths || {})) {
    for (const value of Object.values(item || {})) if (value?.operationId === operationId) operation = value;
  }
  if (!operation) { failures.push(`${operationId}: operation missing`); return; }
  const schema = operation.requestBody?.content?.['application/json']?.schema;
  if (!schema?.$ref) { failures.push(`${operationId}: named request schema ref required`); return; }
  const name = schema.$ref.split('/').pop();
  const model = spec.components?.schemas?.[name];
  if (!model || model.additionalProperties !== false) failures.push(`${operationId}: closed typed request schema required (${name})`);
  const required = new Set(model?.required || []);
  for (const field of fields) {
    if (!Object.hasOwn(model?.properties || {}, field)) failures.push(`${operationId}: request field missing ${field}`);
    if (!required.has(field)) failures.push(`${operationId}: required field missing ${field}`);
  }
};
requireFields('admRetentionRunPause', ['expectedVersion','reason']);
requireFields('admRetentionRunResume', ['expectedVersion','reason']);
requireFields('admRetentionPolicyPause', ['expectedVersion','reason']);
requireFields('admRetentionPolicyResume', ['expectedVersion','reason']);
requireFields('admRetentionRunNow', ['reason']);
requireFields('admRetentionPolicySave', ['policyId','target','rowVersion','reason']);
requireFields('admRetentionPreview', ['target','action','cutoff','reason','limit']);
requireFields('admBatchJobDefinitionTransition', ['expectedRowVersion','targetState','reason']);

const api = fs.readFileSync(path.join(root, 'src/features/batch-runtime-control/api.ts'), 'utf8');
for (const [fn, operation] of [
  ['pauseRetentionRun','admRetentionRunPause'],
  ['resumeRetentionRun','admRetentionRunResume'],
  ['pauseRetentionPolicy','admRetentionPolicyPause'],
  ['resumeRetentionPolicy','admRetentionPolicyResume'],
]) {
  const body = api.match(new RegExp(`export\\s+async\\s+function\\s+${fn}\\([^]*?\\n\\}`, 'm'))?.[0] || '';
  if (!body) failures.push(`${fn}: wrapper missing`);
  if (!body.includes(`${operation}<`) || !body.includes('data:') || !body.includes('expectedVersion') || !body.includes('requiredReason(reason)')) {
    failures.push(`${fn}: required generated data {expectedVersion, reason} call-shape missing`);
  }
}

const workbench = fs.readFileSync(path.join(root, 'src/features/batch-runtime-control/RetentionWorkbench.vue'), 'utf8');
for (const operation of ['admRetentionRunPause','admRetentionRunResume','admRetentionPolicyPause','admRetentionPolicyResume']) {
  const calls = [...workbench.matchAll(new RegExp(`${operation}\\(([^;]+)\\)`, 'g'))].map(v=>v[1]);
  if (!calls.length) failures.push(`${operation}: actual RetentionWorkbench consumer missing`);
  else if (!calls.some(call => call.includes('data:') && call.includes('expectedVersion') && call.includes('reason'))) {
    failures.push(`${operation}: Workbench required data {expectedVersion, reason} missing`);
  }
}

const controllerRoot = path.resolve(root, '../src/main/java/com/cpf/admin/opr');
const walk = dir => fs.readdirSync(dir,{withFileTypes:true}).flatMap(e=>e.isDirectory()?walk(path.join(dir,e.name)):[path.join(dir,e.name)]);
const raw = walk(controllerRoot).filter(f=>f.endsWith('.java')).filter(f=>/@RequestBody\s+Map\s*<\s*String\s*,\s*Object\s*>/.test(fs.readFileSync(f,'utf8')));
if (raw.length) failures.push(`raw ADM @RequestBody Map remains: ${raw.map(f=>path.relative(root,f)).join(', ')}`);

if (failures.length) { console.error(failures.join('\n')); process.exit(1); }
console.log('[CPF][FRONTEND][PASS] retention/job typed request + actual consumer call-shape contract');
