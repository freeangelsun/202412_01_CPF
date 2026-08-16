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
if (!routes) throw new Error("Fixture route registry is empty");
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
if (generatedRoutes !== routes) throw new Error(`Generated route contract count drift: expected=${routes} actual=${generatedRoutes}`);
console.log(`[CPF][FRONTEND][PASS] route operation contract fixture routes=${generatedRoutes} explicitOperations=${operationIds.size}`);

// Negative fixture: a route-bound page may use the single global ADM store, but the store action
// registry must not make every privileged URL appear as a consumer of that route. Direct route
// component calls remain discoverable.
const boundaryFixture = fs.mkdtempSync(path.join(os.tmpdir(), "cpf-route-boundary-"));
const boundaryApp = path.join(boundaryFixture, "src/app");
fs.mkdirSync(boundaryApp, { recursive: true });
fs.writeFileSync(path.join(boundaryApp, "routes.ts"), `
export const admCapabilityRegistry = {
  "routeA": { routeId: "routeA", path: "/a", menuId: "a", riskLevel: "LOW", expectedOperationIds: ["opA"], component: defineAsyncComponent(() => import("../features/a/PageA.vue")) },
  "routeB": { routeId: "routeB", path: "/b", menuId: "b", riskLevel: "LOW", expectedOperationIds: [], component: defineAsyncComponent(() => import("../features/b/PageB.vue")) }
};
`, "utf8");
fs.mkdirSync(path.join(boundaryFixture, "src/features/a"), { recursive: true });
fs.mkdirSync(path.join(boundaryFixture, "src/features/b"), { recursive: true });
fs.mkdirSync(path.join(boundaryFixture, "src/stores"), { recursive: true });
fs.writeFileSync(path.join(boundaryFixture, "src/features/a/PageA.vue"), `
<script setup lang="ts">import { useAdmConsolePage } from "../../app/useAdmConsolePage"; useAdmConsolePage();</script>
<template><section /></template>
`, "utf8");
fs.writeFileSync(path.join(boundaryFixture, "src/features/b/PageB.vue"), `
<script setup lang="ts">const load = () => admQuery("/adm/api/direct"); void load;</script>
<template><section /></template>
`, "utf8");
fs.writeFileSync(path.join(boundaryApp, "useAdmConsolePage.ts"), `
import { useAdmConsoleStore } from "../stores/admConsoleStore";
export function useAdmConsolePage(){ return useAdmConsoleStore(); }
`, "utf8");
fs.writeFileSync(path.join(boundaryFixture, "src/stores/admConsoleStore.ts"), `
export function useAdmConsoleStore(){ return { load: () => admQuery("/adm/api/unrelated") }; }
`, "utf8");
const boundarySpec = path.join(boundaryFixture, "openapi.json");
fs.writeFileSync(boundarySpec, JSON.stringify({
  openapi: "3.0.3",
  paths: {
    "/adm/api/a": { get: { operationId: "opA", responses: { 200: { description: "OK" } } } },
    "/adm/api/direct": { get: { operationId: "opDirect", responses: { 200: { description: "OK" } } } },
    "/adm/api/unrelated": { get: { operationId: "opUnrelated", responses: { 200: { description: "OK" } } } }
  }
}), "utf8");
fs.mkdirSync(path.join(boundaryFixture, "scripts"), { recursive: true });
fs.copyFileSync(path.join(sourceRoot, "scripts/write-route-operation-contract.mjs"), path.join(boundaryFixture, "scripts/write-route-operation-contract.mjs"));
const boundaryResult = spawnSync(process.execPath, ["scripts/write-route-operation-contract.mjs"], {
  cwd: boundaryFixture,
  env: { ...process.env, CPF_OPENAPI_FILE: boundarySpec, CPF_EXPECTED_ADM_ROUTE_COUNT: "2" },
  encoding: "utf8"
});
if (boundaryResult.status !== 0) throw new Error(`${boundaryResult.stdout}\n${boundaryResult.stderr}`);
const boundaryGenerated = fs.readFileSync(path.join(boundaryFixture, "src/generated/adm-route-operation-contract.ts"), "utf8");
function routeIds(routeId) {
  const found = boundaryGenerated.match(new RegExp(`^  ${JSON.stringify(routeId)}: (\\[[^\\n]*\\])`, "m"));
  if (!found) throw new Error(`Boundary fixture route missing: ${routeId}`);
  return JSON.parse(found[1]);
}
const routeAIds = routeIds("routeA");
const routeBIds = routeIds("routeB");
if (routeAIds.length !== 1 || routeAIds[0] !== "opA") {
  throw new Error(`Global store operation leakage: ${JSON.stringify(routeAIds)}`);
}
if (routeBIds.length !== 0) {
  throw new Error(`Direct consumer must not redefine route registry contract: ${JSON.stringify(routeBIds)}`);
}
console.log("[CPF][FRONTEND][PASS] route registry remains authoritative; global/direct consumer discovery cannot redefine it");
