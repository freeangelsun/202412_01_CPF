import fs from "node:fs";
import path from "node:path";

const root = process.cwd();
const openapi = JSON.parse(fs.readFileSync(path.join(root, "openapi/cpf-openapi.json"), "utf8"));
const operations = [];
for (const [apiPath, item] of Object.entries(openapi.paths || {})) {
  for (const method of ["get", "post", "put", "patch", "delete", "head", "options"]) {
    const operationId = item?.[method]?.operationId;
    if (operationId) operations.push({ method: method.toUpperCase(), path: apiPath, operationId });
  }
}
const failures = [];
if (!operations.length) failures.push("OpenAPI has no operations");
const ids = operations.map((v) => v.operationId);
if (new Set(ids).size !== ids.length) failures.push("OpenAPI operationId values are not unique");

const requiredFiles = [
  "src/generated/cpf-operation-contract.ts",
  "src/generated/cpf-api.ts",
  "src/generated/orval/cpf-api.ts",
  "src/shared/cpfApi.ts",
  "src/shared/queryClient.ts",
];
for (const rel of requiredFiles) if (!fs.existsSync(path.join(root, rel))) failures.push(`missing ${rel}`);

const contract = fs.existsSync(path.join(root, requiredFiles[0])) ? fs.readFileSync(path.join(root, requiredFiles[0]), "utf8") : "";
const compatibility = fs.existsSync(path.join(root, requiredFiles[1])) ? fs.readFileSync(path.join(root, requiredFiles[1]), "utf8") : "";
const orval = fs.existsSync(path.join(root, requiredFiles[2])) ? fs.readFileSync(path.join(root, requiredFiles[2]), "utf8") : "";
const shared = fs.existsSync(path.join(root, requiredFiles[3])) ? fs.readFileSync(path.join(root, requiredFiles[3]), "utf8") : "";
const mutatorPath = path.join(root, "src/shared/orval-mutator.ts");
const mutator = fs.existsSync(mutatorPath) ? fs.readFileSync(mutatorPath, "utf8") : "";
const httpOwner = `${shared}\n${mutator}`;
for (const operation of operations) {
  if (!contract.includes(JSON.stringify(operation.operationId))) failures.push(`operation contract missing ${operation.operationId}`);
  if (!compatibility.includes(operation.operationId)) failures.push(`compatibility client missing ${operation.operationId}`);
}
if (!orval.includes("@tanstack/vue-query")) failures.push("Orval client is not generated with TanStack Vue Query");
for (const token of ["resolveCpfOperation", "cpfQueryClient", "MutationObserver", "credentials: \"include\"", "redirect: \"error\""]) {
  if (!httpOwner.includes(token)) failures.push(`shared API owner missing ${token}`);
}

function walk(dir) {
  const out = [];
  if (!fs.existsSync(dir)) return out;
  for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
    const absolute = path.join(dir, entry.name);
    if (entry.isDirectory()) out.push(...walk(absolute));
    else if (/\.(?:ts|vue)$/.test(entry.name)) out.push(absolute);
  }
  return out;
}
for (const file of walk(path.join(root, "src"))) {
  const rel = path.relative(root, file).replaceAll("\\", "/");
  if (rel === "src/shared/cpfApi.ts" || rel.startsWith("src/generated/")) continue;
  const text = fs.readFileSync(file, "utf8");
  if (/\bfetch\s*\(/.test(text) && /\/(?:adm\/api|api\/bza|bza\/api)\b/.test(text)) {
    failures.push(`${rel}: direct privileged API fetch is forbidden`);
  }
  if (/\b(?:axios|XMLHttpRequest)\b/.test(text)) failures.push(`${rel}: direct HTTP client usage is forbidden`);
}

if (failures.length) {
  console.error(failures.join("\n"));
  process.exit(1);
}
console.log(`[CPF][FRONTEND][PASS] operation consumer closure operations=${operations.length}`);
