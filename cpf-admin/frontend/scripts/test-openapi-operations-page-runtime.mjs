import assert from "node:assert/strict";
import fs from "node:fs";
import os from "node:os";
import path from "node:path";
import { pathToFileURL } from "node:url";
import { spawnSync } from "node:child_process";

const root = process.cwd();
const pagePath = path.join(root, "src/features/openapi-operations/OpenApiOperationsPage.vue");
const routePath = path.join(root, "src/app/routes.ts");
const page = fs.readFileSync(pagePath, "utf8");
const routeFiles = [routePath];
const routeDir = path.join(root, "src/app/routes");
if (fs.existsSync(routeDir)) {
  for (const entry of fs.readdirSync(routeDir).filter(name => name.endsWith(".ts") && name !== "types.ts").sort()) {
    routeFiles.push(path.join(routeDir, entry));
  }
}
const routes = routeFiles.map(file => fs.readFileSync(file, "utf8")).join("\n");
if (/(?:getJson|sendJson|rawResponse)\(\s*[`'"]\/adm\/api\/openapi/.test(page)) throw new Error("OpenAPI page raw URL bypass detected");
for (const token of ["admOpenApiStatus", "admOpenApiRefresh", '"X-CPF-Risk-Confirmed": "confirmed"', 'role="alert"', 'aria-live="polite"']) {
  if (!page.includes(token)) throw new Error(`OpenAPI page contract missing: ${token}`);
}
if (!routes.includes('"openApiOperations"') || !routes.includes('["admOpenApiStatus", "admOpenApiRefresh"]')) {
  throw new Error("OpenAPI route operation registry missing");
}
const match = page.match(/<script lang="ts">([\s\S]*?)<\/script>/);
if (!match) throw new Error("OpenAPI page TypeScript block missing");
const temp = fs.mkdtempSync(path.join(os.tmpdir(), "cpf-openapi-page-"));
fs.mkdirSync(path.join(temp, "node_modules/vue"), { recursive: true });
fs.writeFileSync(path.join(temp, "node_modules/vue/package.json"), JSON.stringify({ type: "module", exports: "./index.js" }));
fs.writeFileSync(path.join(temp, "node_modules/vue/index.js"), "export const defineComponent = value => value;\n");
fs.writeFileSync(path.join(temp, "vue.d.ts"), `declare module "vue" {\n  export function defineComponent<T>(value: T): T;\n}\n`);
const script = match[1]
  .replace('../../generated/cpf-api', './mock-generated')
  .replace('../../stores/admSessionStore', './mock-session')
  .replace('mounted() { void this.load(); },', 'mounted() {},');
fs.writeFileSync(path.join(temp, "page.ts"), script);
fs.writeFileSync(path.join(temp, "mock-session.ts"), `
export function useAdmSessionStore() { return { hasButton: (code:string) => code === "OPENAPI_REFRESH" }; }
`);
fs.writeFileSync(path.join(temp, "mock-generated.ts"), `
export const calls: Array<{name:string,args:unknown[]}> = [];
const responses = new Map<string, unknown>();
export function reset():void { calls.length = 0; responses.clear(); }
export function setResponse(name:string, value:unknown):void { responses.set(name, value); }
export function admOpenApiStatus<T=unknown>():Promise<T> { calls.push({name:"admOpenApiStatus",args:[]}); return Promise.resolve(responses.get("admOpenApiStatus") as T); }
export function admOpenApiRefresh<T=unknown>(options:unknown):Promise<T> { calls.push({name:"admOpenApiRefresh",args:[options]}); return Promise.resolve(responses.get("admOpenApiRefresh") as T); }
`);
const localTsc = path.join(root,"node_modules","typescript","bin","tsc");
const tscCommand = fs.existsSync(localTsc) ? process.execPath : "tsc";
const tscArgs = fs.existsSync(localTsc)
  ? [localTsc, "--ignoreConfig", "page.ts", "mock-generated.ts", "mock-session.ts", "vue.d.ts"]
  : ["page.ts", "mock-generated.ts", "mock-session.ts", "vue.d.ts"];
const tsc = spawnSync(tscCommand, [
  ...tscArgs,
  "--target", "ES2022", "--module", "ES2022", "--moduleResolution", "Bundler",
  "--strict", "--noImplicitThis", "false", "--skipLibCheck", "--outDir", temp
], { cwd: temp, encoding: "utf8" });
if (tsc.status !== 0) throw new Error(`OpenAPI page compile failed:\n${tsc.stdout}\n${tsc.stderr}`);
for (const name of ["page", "mock-generated", "mock-session"]) fs.renameSync(path.join(temp, `${name}.js`), path.join(temp, `${name}.mjs`));
let compiled = fs.readFileSync(path.join(temp, "page.mjs"), "utf8").replace('./mock-generated', './mock-generated.mjs').replace('./mock-session', './mock-session.mjs');
fs.writeFileSync(path.join(temp, "page.mjs"), compiled);
const api = await import(pathToFileURL(path.join(temp, "mock-generated.mjs")).href);
const component = (await import(pathToFileURL(path.join(temp, "page.mjs")).href)).default;
const vm = { ...component.setup(), ...component.data(), ...component.methods };
for (const [name, getter] of Object.entries(component.computed || {})) {
  Object.defineProperty(vm, name, { enumerable: true, get: () => getter.call(vm) });
}
const snapshot = { status:"UP", enabled:true, apiDocsEnabled:true, apiDocsPath:"/v3/api-docs", instanceId:"adm-1", operationCount:321, refreshedAt:"2026-08-05T00:00:00Z", refreshReason:"test", failureCode:"" };
api.reset(); api.setResponse("admOpenApiStatus", snapshot);
await vm.load.call(vm);
assert.equal(vm.snapshot.operationCount, 321);
api.reset(); api.setResponse("admOpenApiRefresh", snapshot);
vm.reason = "audited reason"; vm.confirmed = true;
await vm.refreshInventory.call(vm);
assert.deepEqual(api.calls[0], { name:"admOpenApiRefresh", args:[{ data:{reason:"audited reason"}, headers:{"X-CPF-Risk-Confirmed":"confirmed"} }] });
assert.equal(vm.reason, ""); assert.equal(vm.confirmed, false);
api.reset(); vm.reason = ""; vm.confirmed = true; await vm.refreshInventory.call(vm);
assert.equal(api.calls.length, 0, "missing reason must fail closed");
console.log("[CPF][FRONTEND][PASS] OpenAPI operations page generated-client workflow rawUrl=0 riskConfirm=required a11y=present");
