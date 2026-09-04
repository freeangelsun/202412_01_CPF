import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";
import assert from "node:assert/strict";

const scriptDir = path.dirname(fileURLToPath(import.meta.url));
const frontend = path.resolve(scriptDir, "..");
const root = path.resolve(frontend, "..");
const read = rel => fs.readFileSync(path.resolve(root, rel), "utf8");
const readFront = rel => fs.readFileSync(path.resolve(frontend, rel), "utf8");

const store = readFront("src/stores/admSessionStore.ts");
assert.match(store, /allowedOperationIds:\s*\[\]\s+as\s+string\[\]/, "session must keep operation permissions separately");
assert.match(store, /hasOperation\(operationId:\s*string\)/, "session must expose hasOperation");
assert.match(store, /hasButton\(buttonId:\s*string\)/, "session must retain button identity");

const projection = read("src/main/java/com/cpf/admin/opr/service/AdmOperationPermissionProjectionService.java");
assert.match(projection, /ADM_API_PERMISSION/, "operation projection must use canonical API permission storage");
assert.match(projection, /ADM_ROLE_API_PERMISSION/, "operation projection must use canonical role API permission storage");
assert.match(projection, /getMethodAnnotation\(Operation\.class\)/, "operation projection must discover actual controller operationId");
assert.match(projection, /AdmApiPermissionPolicy\.evaluate/, "operation projection must share backend permission semantics");

const filter = read("src/main/java/com/cpf/admin/opr/filter/AdmApiAuthFilter.java");
assert.match(filter, /AdmApiPermissionPolicy\.evaluate/, "backend filter and projection must share permission policy");

for (const rel of [
  "src/main/java/com/cpf/admin/opr/dto/AdmLoginResponse.java",
  "src/main/java/com/cpf/admin/opr/dto/AdmCurrentSessionResponse.java"
]) assert.match(read(rel), /allowedOperationIds/, `${rel} must expose operation projection`);

for (const rel of [
  "src/generated/cpf-api.ts",
  "src/generated/orval/model/admLoginResponse.ts",
  "src/generated/orval/model/admCurrentSessionResponse.ts"
]) assert.match(readFront(rel), /allowedOperationIds/, `${rel} must stay generated from OpenAPI`);

const sourceFiles = [];
function walk(dir) {
  for (const e of fs.readdirSync(dir, {withFileTypes:true})) {
    const p = path.join(dir, e.name);
    if (e.isDirectory()) walk(p);
    else if (/\.(?:ts|tsx|vue)$/.test(e.name)) sourceFiles.push(p);
  }
}
walk(path.resolve(frontend, "src"));
const bad = [];
for (const file of sourceFiles) {
  const src = fs.readFileSync(file, "utf8");
  if (/hasButton\([^\n)]*\.operationId\)/.test(src) || /hasButton\(\s*["']adm[A-Z][^"']*["']\s*\)/.test(src)) {
    bad.push(path.relative(frontend, file));
  }
}
assert.deepEqual(bad, [], `OpenAPI operationId must not be treated as buttonId: ${bad.join(", ")}`);

const generator = readFront("scripts/generate-checked-client.mjs");
assert.match(generator, /!preRuntime\s*&&\s*!fs\.existsSync\(orvalCli\)/, "pre-runtime generation must not require Orval CLI");
console.log("[CPF][ADM][PASS] Menu/Button/API Permission/OpenAPI operation identities are separated and generated contract is synchronized");
