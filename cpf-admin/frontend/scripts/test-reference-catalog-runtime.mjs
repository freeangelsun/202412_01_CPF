import assert from "node:assert/strict";
import fs from "node:fs";
import os from "node:os";
import path from "node:path";
import { pathToFileURL } from "node:url";
import { spawnSync } from "node:child_process";

const root = process.cwd();
const source = fs.readFileSync(path.join(root, "src/app/methods/referenceMethods.ts"), "utf8");
const temp = fs.mkdtempSync(path.join(os.tmpdir(), "cpf-education-catalog-"));
fs.writeFileSync(path.join(temp, "referenceMethods.ts"), source
  .replace('../../generated/orval/cpf-api', './mock-generated')
  .replace('../../generated/orval/model', './mock-model'));
fs.writeFileSync(path.join(temp, "mock-model.ts"), `
export interface AdmCacheReconcileRequest { reason: string; }
export interface AdmCacheEvictKeyRequest { tenantId?: string; namespace: string; key: string; version: number; reason: string; }
export interface AdmCacheEvictNamespaceRequest { tenantId?: string; namespace: string; version: number; reason: string; }
export interface AdmNotificationRuleRequest { eventType: string; eventSubType?: string; channelCode: string; templateCode?: string; severity: string; receiverGroup?: string; useYn: string; reason: string; }
export interface AdmNotificationTestSendRequest { targetType: string; targetId: string; receiver: string; message: string; reason: string; }
export interface CommonMessageRequest { messageId?: number; messageCode?: string; messageKey?: string; locale: string; messageFormatType?: "FIXED" | "INDEXED"; externalMessage?: string; internalMessage?: string; messageValue?: string; parameterCount?: number; parameterSample?: string; description?: string; useYn?: "Y" | "N"; reason: string; }
export interface CommonCodeRequest { codeId?: number; parentId?: number; codeKey: string; codeValue: string; description?: string; useYn?: "Y" | "N"; reason: string; }
export interface CommonConfigRequest { configId?: number; configKey: string; configValue: string; configType?: "STRING" | "NUMBER" | "BOOLEAN" | "JSON"; description?: string; encryptedYn?: "Y" | "N"; useYn?: "Y" | "N"; reason: string; }
export interface CommonResponseCodeRequest { responseCode: string; messageCode: string; resultType: "S" | "E"; moduleId: string; responseGroup: string; sequenceNo: string; httpStatus: number; description?: string; useYn?: "Y" | "N"; }
`);
const operations = [
  "admApprovalRequest",
  "admCacheEvictKey", "admCacheEvictNamespace", "admCacheReconcile", "admCacheRefresh", "admCacheSummary",
  "admCodeCreateCode", "admCodeDeleteCode", "admCodeFindCode", "admCodeFindCodes", "admCodeUpdateCode",
  "admConfigCreateConfig", "admConfigDeleteConfig", "admConfigFindConfig", "admConfigFindConfigs", "admConfigUpdateConfig",
  "admResponseCodeCreate", "admResponseCodeDelete", "admResponseCodeFindAll", "admResponseCodeFindOne", "admResponseCodeUpdate",
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
  path.join(root,"node_modules","typescript","bin","tsc"), "--ignoreConfig", path.join(temp, "referenceMethods.ts"), path.join(temp, "mock-generated.ts"), path.join(temp, "mock-model.ts"),
  "--target", "ES2022", "--module", "ES2022", "--moduleResolution", "Bundler", "--lib", "ES2022,DOM,DOM.Iterable",
  "--skipLibCheck", "--noImplicitAny", "false", "--outDir", temp
], { cwd: root, encoding: "utf8" });
if (compile.status !== 0) throw new Error(`Reference catalog consumer compile failed:\n${compile.stdout}\n${compile.stderr}`);
for (const name of ["referenceMethods", "mock-generated"]) fs.renameSync(path.join(temp, `${name}.js`), path.join(temp, `${name}.mjs`));
fs.writeFileSync(path.join(temp, "referenceMethods.mjs"), fs.readFileSync(path.join(temp, "referenceMethods.mjs"), "utf8").replace('./mock-generated', './mock-generated.mjs'));
const api = await import(pathToFileURL(path.join(temp, "mock-generated.mjs")).href);
const { referenceMethods } = await import(pathToFileURL(path.join(temp, "referenceMethods.mjs")).href);
const envelope = data => ({ data, status: 200, headers: new Headers() });
const find = name => api.calls.filter(call => call.name === name);
function context() {
  return Object.assign({
    uiMessage: "", codeResult: null, configResult: null, responseCodeResult: null,
    codeForm: { codeId: 10, parentId: null, codeKey: "USER_STATUS", codeValue: "ACTIVE", description: "active", useYn: "Y", reason: "code reason", requestUser: "browser-spoof" },
    configForm: { configId: 20, configKey: "CPF.TEST", configValue: "value", configType: "STRING", description: "config", encryptedYn: "N", useYn: "Y", reason: "config reason", requestUser: "browser-spoof" },
    responseCodeReason: "response reason",
    responseCodeForm: { responseCode: "EEDU010001", messageCode: "MEDU010001", resultType: "E", moduleId: "EDU", responseGroup: "01", sequenceNo: "0001", httpStatus: 400, description: "error", useYn: "Y", requestUser: "browser-spoof" },
    requireReason(value) { return Boolean(String(value || "").trim()); }
  }, referenceMethods);
}

let ctx = context();
for (const [method, op, args, response] of [
  ["loadCodes", "admCodeFindCodes", [], []], ["loadCodeDetail", "admCodeFindCode", [10], {}],
  ["createCode", "admCodeCreateCode", null, {}], ["updateCode", "admCodeUpdateCode", null, {}], ["deleteCode", "admCodeDeleteCode", [10,{reason:"code reason"}], []],
  ["loadConfigs", "admConfigFindConfigs", [], []], ["loadConfigDetail", "admConfigFindConfig", [20], {}],
  ["createConfig", "admConfigCreateConfig", null, {}], ["updateConfig", "admConfigUpdateConfig", null, {}], ["deleteConfig", "admConfigDeleteConfig", [20,{reason:"config reason"}], []],
  ["loadResponseCodes", "admResponseCodeFindAll", [], {}], ["loadResponseCodeDetail", "admResponseCodeFindOne", ["EEDU010001"], {}],
  ["createResponseCode", "admResponseCodeCreate", null, {}], ["updateResponseCode", "admResponseCodeUpdate", null, {}], ["deleteResponseCode", "admResponseCodeDelete", ["EEDU010001",{reason:"response reason"}], {}]
]) {
  api.reset(); api.setResponse(op, envelope(response)); ctx = context(); await ctx[method]();
  const call = find(op)[0]; assert.ok(call, op);
  if (args) assert.deepEqual(call.args, args);
  for (const value of call.args) if (value && typeof value === "object") assert.equal(Object.hasOwn(value, "requestUser"), false, `${op} actor spoof`);
}
api.reset(); ctx=context(); await ctx.createCode();
let body=find("admCodeCreateCode")[0].args[0]; assert.equal(body.reason,"code reason"); assert.equal(Object.hasOwn(body,"requestUser"),false);
api.reset(); ctx=context(); await ctx.updateConfig();
body=find("admConfigUpdateConfig")[0].args[1]; assert.equal(body.reason,"config reason"); assert.equal(Object.hasOwn(body,"requestUser"),false);
api.reset(); ctx=context(); await ctx.createResponseCode();
let call=find("admResponseCodeCreate")[0]; assert.deepEqual(call.args[0],{reason:"response reason"}); assert.equal(Object.hasOwn(call.args[1],"requestUser"),false);
api.reset(); ctx=context(); ctx.configForm.encryptedYn="Y"; ctx.configForm.configValue="********"; await ctx.updateConfig();
assert.equal(find("admConfigUpdateConfig").length,0); assert.match(ctx.uiMessage,/마스킹/);
api.reset(); ctx=context(); ctx.responseCodeForm.httpStatus=999; await ctx.createResponseCode(); assert.equal(find("admResponseCodeCreate").length,0);
const raw = source.match(/(?:getJson|sendJson)\(\s*[`'"]\/adm\/api\/(?:codes|configs|response-codes)/g) || [];
assert.equal(raw.length,0);
console.log("[CPF][FRONTEND][PASS] reference catalogs generated-client runtime operations=15 rawUrl=0 actorSpoof=blocked maskedSecret=blocked invalidStatus=blocked");
