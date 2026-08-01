import fs from "node:fs";
import os from "node:os";
import path from "node:path";
import { spawnSync } from "node:child_process";

const sourceRoot = process.cwd();
const fixture = fs.mkdtempSync(path.join(os.tmpdir(), "cpf-route-contract-"));
const appDir = path.join(fixture, "src/app");
fs.mkdirSync(appDir, { recursive: true });
const routesSource = fs.readFileSync(path.join(sourceRoot, "src/app/routes.ts"), "utf8");
fs.writeFileSync(path.join(appDir, "routes.ts"), routesSource, "utf8");
const routePattern = /^\s*"([^"]+)": \{ routeId: "\1"[\s\S]*?expectedOperationIds: \[([^\]]*)\][\s\S]*?import\("([^"]+)"\)/gm;
const operationIds = new Set();
let match; let routes = 0;
while ((match = routePattern.exec(routesSource))) {
  routes++;
  for (const value of match[2].matchAll(/"([^"]+)"/g)) operationIds.add(value[1]);
  const component = path.resolve(appDir, match[3]);
  fs.mkdirSync(path.dirname(component), { recursive: true });
  fs.writeFileSync(component.endsWith(".vue") ? component : `${component}.vue`, "<template><section /></template><script setup lang=\"ts\"></script>\n", "utf8");
}
if (routes !== 59) throw new Error(`Fixture route count drift: ${routes}`);
const paths = {};
let index = 0;
for (const operationId of [...operationIds].sort()) {
  paths[`/fixture/${index++}`] = { get: { operationId, responses: { 200: { description: "OK" } } } };
}
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
const generated = fs.readFileSync(path.join(fixture, "src/generated/adm-route-operation-contract.ts"), "utf8");
const generatedRoutes = (generated.match(/^  "[^"]+":/gm) || []).length;
if (generatedRoutes !== 59) throw new Error(`Generated route contract count drift: ${generatedRoutes}`);
console.log(`[CPF][FRONTEND][PASS] route operation contract fixture routes=${generatedRoutes} explicitOperations=${operationIds.size}`);
