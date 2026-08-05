import fs from "node:fs";
import path from "node:path";

const root = process.cwd();
const input = path.resolve(root, process.argv[2] || process.env.CPF_OPENAPI_FILE || "openapi/cpf-openapi.json");
if (!fs.existsSync(input)) throw new Error(`OpenAPI input missing: ${input}`);
const spec = JSON.parse(fs.readFileSync(input, "utf8"));
const schemas = spec.components?.schemas || {};
const refs = [];
function visit(value, location = "$") {
  if (Array.isArray(value)) {
    value.forEach((item, index) => visit(item, `${location}[${index}]`));
    return;
  }
  if (!value || typeof value !== "object") return;
  if (typeof value.$ref === "string") refs.push({ ref: value.$ref, location });
  for (const [key, child] of Object.entries(value)) visit(child, `${location}.${key}`);
}
visit(spec);
const unresolved = refs.filter(({ ref }) => ref.startsWith("#/components/schemas/") && !schemas[ref.split("/").pop()]);
const contaminated = Object.keys(schemas).filter(name => /Controller$/.test(name) || /[(){};]/.test(name));
const operations = [];
for (const [route, item] of Object.entries(spec.paths || {})) {
  for (const [method, operation] of Object.entries(item || {})) {
    if (operation && typeof operation === "object" && operation.operationId) operations.push({ route, method, id: operation.operationId });
  }
}
const counts = new Map();
operations.forEach(({ id }) => counts.set(id, (counts.get(id) || 0) + 1));
const duplicateIds = [...counts.entries()].filter(([, count]) => count > 1).map(([id]) => id);
const failures = [];
for (const value of unresolved) failures.push(`unresolved schema ref ${value.ref} at ${value.location}`);
for (const name of contaminated) failures.push(`contaminated schema name ${name}`);
for (const id of duplicateIds) failures.push(`duplicate operationId ${id}`);
if (failures.length) {
  console.error(`[CPF][OPENAPI][FAIL] schema-reference contract failures=${failures.length}`);
  failures.slice(0, 100).forEach(value => console.error(` - ${value}`));
  process.exit(1);
}
console.log(`[CPF][OPENAPI][PASS] schema-reference contract operations=${operations.length} schemas=${Object.keys(schemas).length} refs=${refs.length}`);
