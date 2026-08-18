import assert from "node:assert/strict";
import fs from "node:fs";
import os from "node:os";
import path from "node:path";
import { pathToFileURL } from "node:url";
import { spawnSync } from "node:child_process";

const root = process.cwd();
const sourcePath = path.join(root, "src/app/methods/referenceMethods.ts");
const source = fs.readFileSync(sourcePath, "utf8");
if (!source.includes('../../generated/orval/cpf-api')) throw new Error("Message generated-client import missing");
const temp = fs.mkdtempSync(path.join(os.tmpdir(), "cpf-message-consumer-"));
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
  "admApprovalRequest",
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
const compile = spawnSync(process.execPath, [
  path.join(root,"node_modules","typescript","bin","tsc"), "--ignoreConfig", path.join(temp, "referenceMethods.ts"), path.join(temp, "mock-generated.ts"), path.join(temp, "mock-model.ts"),
  "--target", "ES2022", "--module", "ES2022", "--moduleResolution", "Bundler", "--lib", "ES2022,DOM,DOM.Iterable",
  "--skipLibCheck", "--noImplicitAny", "false", "--outDir", temp
], { cwd: root, encoding: "utf8" });
if (compile.status !== 0) throw new Error(`Message consumer compile failed:\n${compile.stdout}\n${compile.stderr}`);
for (const name of ["referenceMethods", "mock-generated"]) {
  fs.renameSync(path.join(temp, `${name}.js`), path.join(temp, `${name}.mjs`));
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
    messageResult: null,
    messageForm: {
      messageId: null, messageCode: "MCPF900001", messageKey: "", locale: "ko-KR", messageFormatType: "FIXED",
      externalMessage: "external", internalMessage: "internal", messageValue: "", parameterCount: 0,
      parameterSample: "", description: "description", useYn: "Y", requestUser: "browser-spoof", reason: "audited reason"
    },
    requireReason(value) { return Boolean(String(value || "").trim()); }
  }, referenceMethods);
}
function find(name) { return api.calls.filter(call => call.name === name); }

api.reset();
api.setResponse("admMessageFindMessages", envelope([{ messageId: 1 }]));
let ctx = context();
await ctx.loadMessages();
assert.deepEqual(find("admMessageFindMessages")[0].args, []);
assert.equal(ctx.messageResult.length, 1);

api.reset();
api.setResponse("admMessageFindMessage", envelope({ messageId: 7 }));
ctx = context(); ctx.messageForm.messageId = 7;
await ctx.loadMessageDetail();
assert.deepEqual(find("admMessageFindMessage")[0].args, [7]);
assert.equal(ctx.messageResult.messageId, 7);

for (const [id, operation] of [[null, "admMessageCreateMessage"], [9, "admMessageUpdateMessage"]]) {
  api.reset(); api.setResponse(operation, envelope({ messageId: id || 10 }));
  ctx = context(); ctx.messageForm.messageId = id;
  await ctx[id ? "updateMessage" : "createMessage"]();
  const call = find(operation)[0];
  assert.ok(call, operation);
  const body = id ? call.args[1] : call.args[0];
  assert.equal(body.messageCode, "MCPF900001");
  assert.equal(body.locale, "ko-KR");
  assert.equal(body.reason, "audited reason");
  assert.equal(Object.hasOwn(body, "requestUser"), false, "browser actor must be excluded");
}

api.reset(); api.setResponse("admMessageDeleteMessage", envelope([{ messageId: 9, useYn: "N" }]));
ctx = context(); ctx.messageForm.messageId = 9;
await ctx.deleteMessage();
assert.deepEqual(find("admMessageDeleteMessage")[0].args, [9, { reason: "audited reason" }]);
assert.equal(ctx.messageResult[0].useYn, "N");

api.reset(); ctx = context(); ctx.messageForm.messageId = 9; ctx.messageForm.reason = "";
await ctx.deleteMessage();
assert.equal(find("admMessageDeleteMessage").length, 0, "missing audit reason must fail closed");

api.reset(); api.setResponse("admMessageFindMessages", [{ messageId: 1 }]);
ctx = context();
await assert.rejects(() => ctx.loadMessages(), /response contract mismatch/);

const rawCalls = source.match(/(?:getJson|sendJson)\(\s*[`'"]\/adm\/api\/messages/g) || [];
assert.equal(rawCalls.length, 0, "message consumer must not use raw URL helpers");
console.log(`[CPF][FRONTEND][PASS] message generated-client runtime operations=5 rawUrl=0 actorSpoof=blocked`);
