import assert from "node:assert/strict";
import fs from "node:fs";
import os from "node:os";
import path from "node:path";
import { pathToFileURL } from "node:url";
import { spawnSync } from "node:child_process";

const root = process.cwd();
const sourcePath = path.join(root, "src/app/methods/referenceMethods.ts");
const source = fs.readFileSync(sourcePath, "utf8");
const temp = fs.mkdtempSync(path.join(os.tmpdir(), "cpf-cache-consumer-"));
const transformed = source
  .replace('../../generated/orval/cpf-api', './mock-generated')
  .replace('../../generated/orval/model', './mock-model');
fs.writeFileSync(path.join(temp, "referenceMethods.ts"), transformed);
fs.writeFileSync(path.join(temp, "mock-model.ts"), `
export interface AdmCacheReconcileRequest { reason: string; }
export interface AdmCacheEvictKeyRequest { tenantId?: string; namespace: string; key: string; version: number; reason: string; }
export interface AdmCacheEvictNamespaceRequest { tenantId?: string; namespace: string; version: number; reason: string; }
export interface AdmNotificationRuleRequest { eventType: string; eventSubType?: string; channelCode: string; templateCode?: string; severity: string; receiverGroup?: string; useYn: string; reason: string; }
export interface AdmNotificationTestSendRequest { targetType: string; targetId: string; receiver: string; message: string; reason: string; }
export interface CommonCodeRequest { codeId?: number; parentId?: number; codeKey: string; codeValue: string; description?: string; useYn?: "Y" | "N"; reason: string; }
export interface CommonConfigRequest { configId?: number; configKey: string; configValue: string; configType?: "STRING" | "NUMBER" | "BOOLEAN" | "JSON"; description?: string; encryptedYn?: "Y" | "N"; useYn?: "Y" | "N"; reason: string; }
export interface CommonResponseCodeRequest { responseCode: string; messageCode: string; resultType: "S" | "E"; moduleId: string; responseGroup: string; sequenceNo: string; httpStatus: number; description?: string; useYn?: "Y" | "N"; }
export interface CommonMessageRequest { messageId?: number; messageCode?: string; messageKey?: string; locale: string; messageFormatType?: "FIXED" | "INDEXED"; externalMessage?: string; internalMessage?: string; messageValue?: string; parameterCount?: number; parameterSample?: string; description?: string; useYn?: "Y" | "N"; reason: string; }
`);
const operations = [
  "admCodeCreateCode", "admCodeDeleteCode", "admCodeFindCode", "admCodeFindCodes", "admCodeUpdateCode",
  "admConfigCreateConfig", "admConfigDeleteConfig", "admConfigFindConfig", "admConfigFindConfigs", "admConfigUpdateConfig",
  "admResponseCodeCreate", "admResponseCodeDelete", "admResponseCodeFindAll", "admResponseCodeFindOne", "admResponseCodeUpdate",
  "admCacheEvictKey", "admCacheEvictNamespace", "admCacheReconcile", "admCacheRefresh", "admCacheSummary",
  "admMessageCreateMessage", "admMessageDeleteMessage", "admMessageFindMessage", "admMessageFindMessages", "admMessageUpdateMessage",
  "admNotificationCancelDelivery", "admNotificationDisableRule", "admNotificationFindDeliveryAttempts", "admNotificationFindDeliveryLogs",
  "admNotificationFindDlq", "admNotificationFindRule", "admNotificationFindRules", "admNotificationRetryDelivery",
  "admNotificationSaveRule", "admNotificationSendTest", "admNotificationUpdateRule"
];
fs.writeFileSync(path.join(temp, "mock-generated.ts"), `
export const calls: Array<{name:string,args:unknown[]}> = [];
const responses = new Map<string, unknown>();
export function setResponse(name:string, value:unknown):void { responses.set(name, value); }
export function reset():void { calls.length = 0; responses.clear(); }
function invoke(name:string,args:unknown[]):Promise<unknown>{ calls.push({name,args}); return Promise.resolve(responses.has(name) ? responses.get(name) : {data:{operation:name},status:200,headers:new Headers()}); }
${operations.map(name => `export const ${name} = (...args:unknown[]) => invoke(${JSON.stringify(name)}, args);`).join("\n")}
`);
const compile = spawnSync(process.execPath, [
  path.join(root,"node_modules","typescript","bin","tsc"), path.join(temp, "referenceMethods.ts"), path.join(temp, "mock-generated.ts"), path.join(temp, "mock-model.ts"),
  "--target", "ES2022", "--module", "ES2022", "--moduleResolution", "Bundler", "--lib", "ES2022,DOM,DOM.Iterable",
  "--skipLibCheck", "--noImplicitAny", "false", "--outDir", temp
], { cwd: root, encoding: "utf8" });
if (compile.status !== 0) throw new Error(`Cache consumer compile failed:\n${compile.stdout}\n${compile.stderr}`);
for (const name of ["referenceMethods", "mock-generated"]) fs.renameSync(path.join(temp, `${name}.js`), path.join(temp, `${name}.mjs`));
let js = fs.readFileSync(path.join(temp, "referenceMethods.mjs"), "utf8").replace('./mock-generated', './mock-generated.mjs');
fs.writeFileSync(path.join(temp, "referenceMethods.mjs"), js);
const api = await import(pathToFileURL(path.join(temp, "mock-generated.mjs")).href);
const { referenceMethods } = await import(pathToFileURL(path.join(temp, "referenceMethods.mjs")).href);
const envelope = data => ({ data, status: 200, headers: new Headers() });
function context() {
  return Object.assign({
    uiMessage: "", cacheResult: null, cacheReason: "audited reason",
    cacheControl: { tenantId: "TENANT", namespace: "users", key: "42", version: 3 },
    requireReason(value) { return Boolean(String(value || "").trim()); }
  }, referenceMethods);
}
function find(name) { return api.calls.filter(call => call.name === name); }

api.reset(); api.setResponse("admCacheSummary", envelope({ ready: true }));
let ctx = context(); await ctx.loadCacheSummary();
assert.deepEqual(find("admCacheSummary")[0].args, []);

api.reset(); api.setResponse("admCacheRefresh", envelope({ operation: "REFRESH" }));
ctx = context(); await ctx.refreshCache("MESSAGE");
assert.deepEqual(find("admCacheRefresh")[0].args, [{ target: "MESSAGE", reason: "audited reason" }]);

api.reset(); api.setResponse("admCacheEvictKey", envelope({ operation: "EVICT_KEY" })); api.setResponse("admCacheSummary", envelope({ ready: true }));
ctx = context(); await ctx.evictCacheKey();
let body = find("admCacheEvictKey")[0].args[0];
assert.deepEqual(body, { tenantId: "TENANT", namespace: "users", key: "42", version: 3, reason: "audited reason" });
assert.equal(Object.hasOwn(body, "requestUser"), false);

api.reset(); api.setResponse("admCacheEvictNamespace", envelope({ operation: "EVICT_NAMESPACE" })); api.setResponse("admCacheSummary", envelope({ ready: true }));
ctx = context(); await ctx.evictCacheNamespace();
body = find("admCacheEvictNamespace")[0].args[0];
assert.deepEqual(body, { tenantId: "TENANT", namespace: "users", version: 3, reason: "audited reason" });

api.reset(); api.setResponse("admCacheReconcile", envelope({ operation: "RECONCILE" })); api.setResponse("admCacheSummary", envelope({ ready: true }));
ctx = context(); await ctx.reconcileCache();
assert.deepEqual(find("admCacheReconcile")[0].args, [{ reason: "audited reason" }]);

api.reset(); ctx = context(); await ctx.refreshCache("UNSUPPORTED");
assert.equal(find("admCacheRefresh").length, 0);
ctx.cacheControl.version = -1; await ctx.evictCacheKey();
assert.equal(find("admCacheEvictKey").length, 0);
ctx.cacheControl.version = 1; ctx.cacheReason = ""; await ctx.reconcileCache();
assert.equal(find("admCacheReconcile").length, 0);

const rawCalls = source.match(/(?:getJson|sendJson)\(\s*[`'"]\/adm\/api\/cache/g) || [];
assert.equal(rawCalls.length, 0, "cache consumer must not use raw URL helpers");
console.log(`[CPF][FRONTEND][PASS] cache generated-client runtime operations=5 rawUrl=0 actorSpoof=blocked invalidVersion=blocked`);
