import fs from "node:fs";
import path from "node:path";

const root = process.cwd();
const routesPath = path.join(root, "src/app/routes.ts");
const routesDir = path.join(root, "src/app/routes");
const specPath = path.resolve(process.env.CPF_OPENAPI_FILE || "openapi/cpf-openapi.json");
const generatedDir = path.join(root, "src/generated");
if (!fs.existsSync(routesPath)) throw new Error(`ADM route registry missing: ${routesPath}`);
if (!fs.existsSync(specPath)) throw new Error(`OpenAPI source missing: ${specPath}`);

const routeFiles = [routesPath];
if (fs.existsSync(routesDir)) {
  for (const entry of fs.readdirSync(routesDir).filter(name => name.endsWith(".ts") && name !== "types.ts").sort()) {
    routeFiles.push(path.join(routesDir, entry));
  }
}
const routesText = routeFiles.map(file => fs.readFileSync(file, "utf8")).join("\n");
const spec = JSON.parse(fs.readFileSync(specPath, "utf8"));
const methods = new Set(["get", "post", "put", "patch", "delete", "head", "options", "trace"]);
const openApiIds = new Set();
for (const item of Object.values(spec.paths || {})) {
  for (const [method, operation] of Object.entries(item || {})) {
    if (methods.has(method) && operation?.operationId) openApiIds.add(operation.operationId);
  }
}

// The route capability registry is the single route/menu/operation source of truth. Consumer
// discovery is deliberately NOT merged here: doing so previously let a stale/generic consumer
// redefine the generated contract. Actual callsite coverage is enforced independently by
// verify-operation-consumer.mjs.
const pattern = /^\s*"([^"]+)": \{ routeId: "([^"]+)", path: "([^"]+)", menuId: "([^"]+)".*?expectedOperationIds: \[(.*?)\], component:/gm;
const contracts = new Map();
let match;
const failures = [];
while ((match = pattern.exec(routesText))) {
  const key = match[1];
  const routeId = match[2];
  if (key !== routeId) failures.push(`${key}: registry key differs from routeId=${routeId}`);
  if (contracts.has(routeId)) failures.push(`${routeId}: duplicate routeId`);
  const ids = [...match[5].matchAll(/"([^"]+)"/g)].map(value => value[1]);
  if (new Set(ids).size !== ids.length) failures.push(`${routeId}: duplicate expectedOperationIds`);
  for (const operationId of ids) {
    if (!openApiIds.has(operationId)) failures.push(`${routeId}: expected operation missing from runtime OpenAPI: ${operationId}`);
  }
  contracts.set(routeId, [...ids].sort());
}
const declaredRouteCount = [...routesText.matchAll(/routeId:\s*"[^"]+"/g)].length;
if (contracts.size === 0) failures.push("ADM route registry contains no routes");
if (contracts.size !== declaredRouteCount) failures.push(`ADM route parser coverage mismatch: parsed=${contracts.size} declared=${declaredRouteCount}`);
if (failures.length) throw new Error(failures.join("\n"));

fs.mkdirSync(generatedDir, { recursive: true });
const rows = [...contracts.entries()]
  .map(([routeId, operationIds]) => `  ${JSON.stringify(routeId)}: ${JSON.stringify(operationIds)}`)
  .join(",\n");
const output = `// Generated from ADM capability registry and canonical runtime OpenAPI.\n// Actual callsite coverage is verified separately; registry-only presence is never consumer evidence.\nexport const admRouteOperationContract = {\n${rows}\n} as const;\nexport type AdmRouteOperationContract = typeof admRouteOperationContract;\n`;
fs.writeFileSync(path.join(generatedDir, "adm-route-operation-contract.ts"), output, "utf8");
console.log(`[CPF][FRONTEND][PASS] route operation contract routes=${contracts.size} files=${routeFiles.length}`);
