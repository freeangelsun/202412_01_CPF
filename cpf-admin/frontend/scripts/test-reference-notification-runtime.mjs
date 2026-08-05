import assert from "node:assert/strict";
import fs from "node:fs";
import os from "node:os";
import path from "node:path";
import { pathToFileURL } from "node:url";
import { spawnSync } from "node:child_process";

const root = process.cwd();
const sourcePath = path.join(root, "src/app/methods/referenceMethods.ts");
const source = fs.readFileSync(sourcePath, "utf8");
if (!source.includes('../../generated/orval/cpf-api')) throw new Error("Notification generated-client import missing");
const temp = fs.mkdtempSync(path.join(os.tmpdir(), "cpf-notification-consumer-"));
const transformed = source
  .replace('../../generated/orval/cpf-api', './mock-generated')
  .replace('../../generated/orval/model', './mock-model');
fs.writeFileSync(path.join(temp, "referenceMethods.ts"), transformed);
fs.writeFileSync(path.join(temp, "mock-model.ts"), `
export interface AdmCacheControlRequest { reason: string; }
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
  "admNotificationCancelDelivery", "admNotificationDisableRule", "admNotificationFindDeliveryAttempts",
  "admNotificationFindDeliveryLogs", "admNotificationFindDlq", "admNotificationFindRule",
  "admNotificationFindRules", "admNotificationRetryDelivery", "admNotificationSaveRule",
  "admNotificationSendTest", "admNotificationUpdateRule"
];
fs.writeFileSync(path.join(temp, "mock-generated.ts"), `
export const calls: Array<{name:string,args:unknown[]}> = [];
const responses = new Map<string, unknown>();
export function setResponse(name:string, value:unknown):void { responses.set(name, value); }
export function reset():void { calls.length = 0; responses.clear(); }
function invoke(name:string,args:unknown[]):Promise<unknown>{ calls.push({name,args}); return Promise.resolve(responses.has(name) ? responses.get(name) : {data:{operation:name},status:200,headers:new Headers()}); }
${operations.map(name => `export const ${name} = (...args:unknown[]) => invoke(${JSON.stringify(name)}, args);`).join("\n")}
`);
const compile = spawnSync(process.platform === "win32" ? "npx.cmd" : "npx", [
  "--no-install", "tsc", path.join(temp, "referenceMethods.ts"), path.join(temp, "mock-generated.ts"), path.join(temp, "mock-model.ts"),
  "--target", "ES2022", "--module", "ES2022", "--moduleResolution", "Bundler", "--lib", "ES2022,DOM,DOM.Iterable",
  "--skipLibCheck", "--noImplicitAny", "false", "--outDir", temp
], { cwd: root, encoding: "utf8" });
if (compile.status !== 0) throw new Error(`Notification consumer compile failed:\n${compile.stdout}\n${compile.stderr}`);
for (const name of ["referenceMethods", "mock-generated"]) {
  const js = path.join(temp, `${name}.js`);
  fs.renameSync(js, path.join(temp, `${name}.mjs`));
}
let js = fs.readFileSync(path.join(temp, "referenceMethods.mjs"), "utf8");
js = js.replace('./mock-generated', './mock-generated.mjs');
fs.writeFileSync(path.join(temp, "referenceMethods.mjs"), js);
const api = await import(pathToFileURL(path.join(temp, "mock-generated.mjs")).href);
const { referenceMethods } = await import(pathToFileURL(path.join(temp, "referenceMethods.mjs")).href);
const envelope = data => ({ data, status: 200, headers: new Headers() });
function context() {
  return Object.assign({
    uiMessage: "",
    notificationResult: {},
    selectedNotificationDelivery: null,
    notificationForm: {
      ruleId: null, eventType: "BATCH", eventSubType: "FAILED", channelCode: "ADM", templateCode: "BATCH_FAILED",
      severity: "WARN", receiverGroup: "ADM_OPERATOR", useYn: "Y", targetType: "ADM_TEST", targetId: "TEST",
      receiver: "ADM_OPERATOR", message: "test", reason: "audited reason"
    },
    notificationDeliveryForm: { deliveryId: null, expectedVersion: null, deliveryStatus: "", operationId: "", reason: "audited reason" },
    requireReason(value) { return Boolean(String(value || "").trim()); }
  }, referenceMethods);
}
function find(name) { return api.calls.filter(call => call.name === name); }

api.reset();
api.setResponse("admNotificationFindRules", envelope([{ ruleId: 1 }]));
api.setResponse("admNotificationFindDeliveryLogs", envelope([{ deliveryId: 2, version: 3, deliveryStatus: "DLQ" }]));
let ctx = context();
await ctx.loadNotifications();
assert.deepEqual(find("admNotificationFindRules")[0].args, [{ limit: 100 }]);
assert.deepEqual(find("admNotificationFindDeliveryLogs")[0].args, [{ limit: 50 }]);
assert.equal(ctx.notificationResult.rules.length, 1);

api.reset();
api.setResponse("admNotificationFindRule", envelope({ ruleId: 17, eventType: "ONLINE", useYn: "Y" }));
ctx = context(); ctx.notificationForm.ruleId = 17;
await ctx.loadNotificationRuleDetail();
assert.deepEqual(find("admNotificationFindRule")[0].args, [17]);
assert.equal(ctx.notificationForm.eventType, "ONLINE");

api.reset();
api.setResponse("admNotificationFindDlq", envelope([{ deliveryId: 9 }]));
ctx = context(); await ctx.loadNotificationDlq();
assert.deepEqual(find("admNotificationFindDlq")[0].args, [{ limit: 100 }]);
assert.equal(ctx.selectedNotificationDelivery, null);

api.reset();
api.setResponse("admNotificationFindDeliveryAttempts", envelope([{ attemptNo: 1 }]));
ctx = context();
await ctx.selectNotificationDelivery({ deliveryId: 9, version: 4, deliveryStatus: "DLQ", operationId: "op-1" });
assert.deepEqual(find("admNotificationFindDeliveryAttempts")[0].args, [9, { limit: 100 }]);
assert.equal(ctx.notificationDeliveryForm.expectedVersion, 4);

for (const [ruleId, expectedOperation] of [[null, "admNotificationSaveRule"], [23, "admNotificationUpdateRule"]]) {
  api.reset();
  api.setResponse(expectedOperation, envelope({ ruleId: ruleId || 33 }));
  api.setResponse("admNotificationFindRules", envelope([]));
  api.setResponse("admNotificationFindDeliveryLogs", envelope([]));
  ctx = context(); ctx.notificationForm.ruleId = ruleId;
  await ctx.saveNotificationRule();
  const call = find(expectedOperation)[0];
  assert.ok(call, expectedOperation);
  const body = expectedOperation === "admNotificationUpdateRule" ? call.args[1] : call.args[0];
  assert.equal(body.eventType, "BATCH");
  assert.equal(body.reason, "audited reason");
  assert.equal(Object.hasOwn(body, "requestUser"), false);
}

api.reset();
api.setResponse("admNotificationDisableRule", envelope({ disabled: true }));
api.setResponse("admNotificationFindRules", envelope([])); api.setResponse("admNotificationFindDeliveryLogs", envelope([]));
ctx = context(); ctx.notificationForm.ruleId = 5; await ctx.disableNotificationRule();
assert.deepEqual(find("admNotificationDisableRule")[0].args, [5, { reason: "audited reason" }]);

api.reset();
api.setResponse("admNotificationSendTest", envelope({ deliveryId: 51 }));
api.setResponse("admNotificationFindRules", envelope([])); api.setResponse("admNotificationFindDeliveryLogs", envelope([]));
ctx = context(); ctx.notificationForm.ruleId = 7; await ctx.sendNotificationTest();
const testBody = find("admNotificationSendTest")[0].args[1];
assert.equal(testBody.reason, "audited reason");
assert.equal(Object.hasOwn(testBody, "requestUser"), false);

for (const [action, status, operation] of [["retry", "DLQ", "admNotificationRetryDelivery"], ["cancel", "READY", "admNotificationCancelDelivery"]]) {
  api.reset(); api.setResponse(operation, envelope({ status: action.toUpperCase() }));
  api.setResponse("admNotificationFindRules", envelope([])); api.setResponse("admNotificationFindDeliveryLogs", envelope([]));
  ctx = context(); Object.assign(ctx.notificationDeliveryForm, { deliveryId: 41, expectedVersion: 7, deliveryStatus: status });
  await ctx[action === "retry" ? "retryNotificationDelivery" : "cancelNotificationDelivery"]();
  assert.deepEqual(find(operation)[0].args, [41, { expectedVersion: 7, reason: "audited reason" }]);
}

api.reset();
ctx = context(); Object.assign(ctx.notificationDeliveryForm, { deliveryId: 41, expectedVersion: 7, deliveryStatus: "READY" });
await ctx.retryNotificationDelivery();
assert.equal(find("admNotificationRetryDelivery").length, 0, "invalid state must fail closed");

api.reset(); api.setResponse("admNotificationFindRule", { ruleId: 1 });
ctx = context(); ctx.notificationForm.ruleId = 1;
await assert.rejects(() => ctx.loadNotificationRuleDetail(), /response contract mismatch/);

const rawCalls = source.match(/(?:getJson|sendJson)\(\s*[`'"]\/adm\/api\/notifications/g) || [];
assert.equal(rawCalls.length, 0, "notification consumer must not use raw URL helpers");
console.log(`[CPF][FRONTEND][PASS] notification generated-client runtime operations=${operations.length} rawUrl=0`);
