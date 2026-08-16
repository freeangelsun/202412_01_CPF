import fs from "node:fs";
import os from "node:os";
import path from "node:path";
import { spawnSync } from "node:child_process";

const sourceRoot = process.cwd();
const fixture = fs.mkdtempSync(path.join(os.tmpdir(), "cpf-bza-route-contract-"));
const appDir = path.join(fixture, "src/app");
fs.mkdirSync(appDir, { recursive: true });
const routesSource = fs.readFileSync(path.join(sourceRoot, "src/app/routes.ts"), "utf8");
fs.writeFileSync(path.join(appDir, "routes.ts"), routesSource, "utf8");
const routePattern = /\{\s*id:\s*"([^"]+)"[\s\S]*?expectedOperationIds:\s*\[([^\]]*)\][\s\S]*?load:\s*\(\)\s*=>\s*import\("([^"]+)"\)\s*\}/g;
const operationIds = new Set();
let match;
let routes = 0;
let firstComponent = "";
while ((match = routePattern.exec(routesSource))) {
  routes++;
  for (const value of match[2].matchAll(/"([^"]+)"/g)) operationIds.add(value[1]);
  const component = path.resolve(appDir, match[3]);
  fs.mkdirSync(path.dirname(component), { recursive: true });
  const componentFile = component.endsWith(".vue") ? component : `${component}.vue`;
  if (!firstComponent) firstComponent = componentFile;
  fs.writeFileSync(componentFile, "<template><section /></template><script setup lang=\"ts\"></script>\n", "utf8");
}
if (routes !== 27) throw new Error(`Fixture route count drift: ${routes}`);
const sharedDir = path.join(fixture, "src/shared");
fs.mkdirSync(sharedDir, { recursive: true });
fs.writeFileSync(path.join(sharedDir, "cpfApi.ts"), "export const transportOnly = '/api/bza/transport-only';\n", "utf8");
fs.writeFileSync(firstComponent, "<template><section /></template><script setup lang=\"ts\">import '../../shared/cpfApi';</script>\n", "utf8");
const paths = {};
let index = 0;
for (const operationId of [...operationIds].sort()) {
  paths[`/fixture/${index++}`] = { get: { operationId, responses: { 200: { description: "OK" } } } };
}
paths["/api/bza/transport-only"] = { get: { operationId: "fixtureTransportOnly", responses: { 200: { description: "OK" } } } };
if (!Object.keys(paths).length) paths["/fixture/health"] = { get: { operationId: "fixtureHealth", responses: { 200: { description: "OK" } } } };
const specPath = path.join(fixture, "openapi.json");
fs.writeFileSync(specPath, JSON.stringify({ openapi: "3.0.3", paths }), "utf8");
fs.mkdirSync(path.join(fixture, "scripts"), { recursive: true });
fs.copyFileSync(path.join(sourceRoot, "scripts/write-route-operation-contract.mjs"), path.join(fixture, "scripts/write-route-operation-contract.mjs"));
const result = spawnSync(process.execPath, ["scripts/write-route-operation-contract.mjs"], {
  cwd: fixture,
  env: { ...process.env, CPF_OPENAPI_FILE: specPath },
  encoding: "utf8"
});
if (result.status !== 0) throw new Error(`${result.stdout}\n${result.stderr}`);
const generated = fs.readFileSync(path.join(fixture, "src/generated/bza-route-operation-contract.ts"), "utf8");
const generatedRoutes = (generated.match(/^  "[^"]+":/gm) || []).length;
if (generatedRoutes !== 27) throw new Error(`Generated route contract count drift: ${generatedRoutes}`);
if (generated.includes("fixtureTransportOnly")) throw new Error("Shared transport implementation leaked into a route operation contract");
console.log(`[CPF][FRONTEND][PASS] route operation contract fixture routes=${generatedRoutes} explicitOperations=${operationIds.size}`);
