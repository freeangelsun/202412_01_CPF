import fs from "node:fs";
import path from "node:path";
import crypto from "node:crypto";

const root = process.cwd();
const sourcePath = path.resolve(root, process.argv[2] || process.env.CPF_OPENAPI_FILE || "openapi/cpf-openapi.json");
const outputPath = path.resolve(root, process.argv[3] || sourcePath);
if (!fs.existsSync(sourcePath)) throw new Error(`OpenAPI source missing: ${sourcePath}`);

const spec = JSON.parse(fs.readFileSync(sourcePath, "utf8"));
const paths = spec.paths || {};
const components = spec.components ||= {};
const schemas = components.schemas ||= {};

function ensureOperation(route, method, operationId, summary) {
  const item = paths[route] ||= {};
  const key = method.toLowerCase();
  const existing = item[key];
  if (existing && existing.operationId !== operationId) {
    throw new Error(`OpenAPI route collision: ${method.toUpperCase()} ${route} existing=${existing.operationId}`);
  }
  item[key] ||= {
    operationId,
    summary,
    responses: {
      "200": {
        description: "Controller source contract response",
        content: { "application/json": { schema: { $ref: "#/components/schemas/CpfControllerSourceResponse" } } }
      }
    }
  };
}

function operation(operationId) {
  for (const [route, item] of Object.entries(paths)) {
    for (const [method, value] of Object.entries(item || {})) {
      if (value?.operationId === operationId) return { route, method, value };
    }
  }
  throw new Error(`OpenAPI operation missing: ${operationId}`);
}
function parameter(name, location, schema, required = false, description = undefined) {
  return { name, in: location, required, ...(description ? { description } : {}), schema };
}
const query = (name, schema, required = false, description = undefined) => parameter(name, "query", schema, required, description);
const header = (name, schema, required = false, description = undefined) => parameter(name, "header", schema, required, description);
function requestBody(schemaRef) {
  return { required: true, content: { "application/json": { schema: { $ref: `#/components/schemas/${schemaRef}` } } } };
}
function apply(operationId, contract) {
  const located = operation(operationId);
  const target = located.value;
  const declaredPath = (target.parameters || []).filter(value => value?.in === "path");
  const declaredByName = new Map(declaredPath.map(value => [value.name, value]));
  const preservedPath = [...located.route.matchAll(/\{([^{}]+)\}/g)].map(match => declaredByName.get(match[1]) || {
    name: match[1], in: "path", required: true, schema: { type: "string" }
  });
  const extras = contract.parameters || [];
  const mergedByKey = new Map();
  for (const value of [...preservedPath, ...extras]) mergedByKey.set(`${value.in}:${value.name}`, value);
  const merged = [...mergedByKey.values()];
  if (merged.length) target.parameters = merged;
  else delete target.parameters;
  if (contract.requestBody) target.requestBody = requestBody(contract.requestBody);
  else delete target.requestBody;
  target["x-cpf-contract-source"] = "ADM_CONTROLLER_EXPLICIT";
}
function objectSchema(required, properties, description) {
  return { type: "object", additionalProperties: false, required, properties, description };
}
const reasonProperty = { type: "string", minLength: 1, maxLength: 500 };
const pageParameters = [
  query("query", { type: "string", default: "" }, false, "Search keyword"),
  query("page", { type: "integer", format: "int32", minimum: 0, default: 0 }, false, "Zero-based page"),
  query("size", { type: "integer", format: "int32", minimum: 1, maximum: 500, default: 50 }, false, "Page size")
];
const riskConfirmed = header(
  "X-CPF-Risk-Confirmed",
  { type: "string", enum: ["confirmed"] },
  true,
  "Required explicit confirmation for a high-risk operation"
);

schemas.AdmReliabilityActionRequest = objectSchema(
  ["reason"],
  { targetStatus: { type: "string", maxLength: 100 }, reason: reasonProperty },
  "ADM reliability action. The authenticated operator is resolved from the server session; requestUser is not accepted."
);
schemas.AdmCacheEvictKeyRequest = objectSchema(
  ["namespace", "key", "version", "reason"],
  {
    tenantId: { type: "string", maxLength: 200 },
    namespace: { type: "string", minLength: 1, maxLength: 300 },
    key: { type: "string", minLength: 1, maxLength: 1000 },
    version: { type: "integer", format: "int64", minimum: 0 },
    reason: reasonProperty
  },
  "Audited cache key eviction request. The authenticated operator is resolved from the server session."
);
schemas.AdmCacheEvictNamespaceRequest = objectSchema(
  ["namespace", "version", "reason"],
  {
    tenantId: { type: "string", maxLength: 200 },
    namespace: { type: "string", minLength: 1, maxLength: 300 },
    version: { type: "integer", format: "int64", minimum: 0 },
    reason: reasonProperty
  },
  "Audited cache namespace eviction request. The authenticated operator is resolved from the server session."
);
schemas.AdmCacheControlRequest = objectSchema(
  ["reason"],
  { reason: reasonProperty },
  "Audited cache reconcile request. The authenticated operator is resolved from the server session."
);


schemas.CommonCodeRequest = objectSchema(
  ["codeKey", "codeValue", "reason"],
  {
    codeId: { type: "integer", format: "int64", minimum: 1 },
    parentId: { type: "integer", format: "int64", minimum: 1 },
    codeKey: { type: "string", minLength: 1, maxLength: 200 },
    codeValue: { type: "string", minLength: 1, maxLength: 500 },
    description: { type: "string", maxLength: 1000 },
    useYn: { type: "string", enum: ["Y", "N"], default: "Y" },
    reason: reasonProperty
  },
  "Common code create/update input. The authenticated operator is resolved from the server session; requestUser is not accepted."
);
schemas.CommonConfigRequest = objectSchema(
  ["configKey", "configValue", "reason"],
  {
    configId: { type: "integer", format: "int64", minimum: 1 },
    configKey: { type: "string", minLength: 1, maxLength: 300 },
    configValue: { type: "string", minLength: 1, maxLength: 4000 },
    configType: { type: "string", enum: ["STRING", "NUMBER", "BOOLEAN", "JSON"], default: "STRING" },
    description: { type: "string", maxLength: 1000 },
    encryptedYn: { type: "string", enum: ["Y", "N"], default: "N" },
    useYn: { type: "string", enum: ["Y", "N"], default: "Y" },
    reason: reasonProperty
  },
  "Common configuration create/update input. The authenticated operator is resolved from the server session; requestUser is not accepted."
);
schemas.CommonResponseCodeRequest = objectSchema(
  ["responseCode", "messageCode", "resultType", "moduleId", "responseGroup", "sequenceNo", "httpStatus"],
  {
    responseCode: { type: "string", pattern: "^[SE][A-Z]{3}[0-9]{6}$", maxLength: 10 },
    messageCode: { type: "string", pattern: "^M[A-Z]{3}[0-9]{6}$", maxLength: 10 },
    resultType: { type: "string", enum: ["S", "E"] },
    moduleId: { type: "string", pattern: "^[A-Z]{3}$" },
    responseGroup: { type: "string", pattern: "^[0-9]{2}$" },
    sequenceNo: { type: "string", pattern: "^[0-9]{4}$" },
    httpStatus: { type: "integer", format: "int32", minimum: 100, maximum: 599 },
    description: { type: "string", maxLength: 1000 },
    useYn: { type: "string", enum: ["Y", "N"], default: "Y" }
  },
  "Response-code create/update input. The authenticated operator is resolved from the server session; requestUser is not accepted."
);

schemas.AdmNotificationRuleRequest = objectSchema(
  ["eventType", "reason"],
  {
    eventType: { type: "string", minLength: 1, maxLength: 100 },
    eventSubType: { type: "string", maxLength: 100 },
    channelCode: { type: "string", minLength: 1, maxLength: 50, default: "ADM" },
    templateCode: { type: "string", maxLength: 100 },
    severity: { type: "string", enum: ["TRACE", "DEBUG", "INFO", "WARN", "ERROR", "CRITICAL"], default: "WARN" },
    receiverGroup: { type: "string", maxLength: 100 },
    useYn: { type: "string", enum: ["Y", "N"], default: "Y" },
    reason: reasonProperty
  },
  "ADM notification rule input. The authenticated operator is resolved from the server session; requestUser is not accepted."
);
schemas.AdmNotificationTestSendRequest = objectSchema(
  ["targetType", "targetId", "receiver", "message", "reason"],
  {
    targetType: { type: "string", minLength: 1, maxLength: 50 },
    targetId: { type: "string", minLength: 1, maxLength: 200 },
    receiver: { type: "string", minLength: 1, maxLength: 500 },
    message: { type: "string", minLength: 1, maxLength: 4000 },
    reason: reasonProperty
  },
  "ADM notification test-send input. The authenticated operator is resolved from the server session; requestUser is not accepted."
);

schemas.CommonMessageRequest = {
  ...objectSchema(
    ["locale", "reason"],
    {
      messageId: { type: "integer", format: "int64", minimum: 1 },
      messageCode: { type: "string", minLength: 1, maxLength: 100 },
      messageKey: { type: "string", minLength: 1, maxLength: 100 },
      locale: { type: "string", minLength: 1, maxLength: 20 },
      messageFormatType: { type: "string", enum: ["FIXED", "INDEXED"], default: "FIXED" },
      externalMessage: { type: "string", maxLength: 4000 },
      internalMessage: { type: "string", maxLength: 4000 },
      messageValue: { type: "string", maxLength: 4000 },
      parameterCount: { type: "integer", format: "int32", minimum: 0, default: 0 },
      parameterSample: { type: "string", maxLength: 4000 },
      description: { type: "string", maxLength: 1000 },
      useYn: { type: "string", enum: ["Y", "N"], default: "Y" },
      reason: reasonProperty
    },
    "ADM message create/update input. The authenticated operator is resolved from the server session; requestUser is not accepted."
  ),
  anyOf: [{ required: ["messageCode"] }, { required: ["messageKey"] }]
};

schemas.AdmFeatureFlagEvaluateRequest = objectSchema(
  ["flagKey", "valueType", "value"],
  {
    flagKey: { type: "string", minLength: 1, maxLength: 200 },
    valueType: { type: "string", enum: ["BOOLEAN", "STRING", "INTEGER", "DECIMAL", "NUMBER"] },
    value: { type: "string", minLength: 1, maxLength: 4000 },
    targetingKey: { type: "string", maxLength: 500 },
    attributes: { type: "object", additionalProperties: { type: "string" } }
  },
  "Typed Feature Flag evaluation request."
);
schemas.AdmFeatureFlagOverrideRequest = objectSchema(
  ["flagKey", "valueType", "value", "expiresAt", "reason"],
  {
    flagKey: { type: "string", minLength: 1, maxLength: 200 },
    valueType: { type: "string", enum: ["BOOLEAN", "STRING", "INTEGER", "DECIMAL", "NUMBER"] },
    value: { type: "string", minLength: 1, maxLength: 4000 },
    expiresAt: { type: "string", format: "date-time" },
    reason: reasonProperty
  },
  "Feature Flag override request requiring a separate approval."
);
schemas.AdmFeatureFlagDecisionRequest = objectSchema(["reason"], { reason: reasonProperty }, "Feature Flag approval or revoke reason.");
schemas.AdmFeatureFlagKillSwitchRequest = objectSchema(
  ["enabled", "reason"],
  { enabled: { type: "boolean" }, reason: reasonProperty },
  "Feature Flag kill-switch command."
);
schemas.AdmResiliencePolicyRequest = objectSchema(
  ["operationId", "timeoutMs", "maxAttempts", "retryBackoffMs", "circuitFailureThreshold", "circuitOpenMs", "bulkheadMaxConcurrent", "rateLimitPermits", "rateLimitWindowMs", "idempotent", "unknownResultReconcileEnabled", "reason"],
  {
    operationId: { type: "string", minLength: 1, maxLength: 300 },
    timeoutMs: { type: "integer", format: "int64", minimum: 1 },
    maxAttempts: { type: "integer", format: "int32", minimum: 1 },
    retryBackoffMs: { type: "integer", format: "int64", minimum: 0 },
    circuitFailureThreshold: { type: "integer", format: "int32", minimum: 1 },
    circuitOpenMs: { type: "integer", format: "int64", minimum: 1 },
    bulkheadMaxConcurrent: { type: "integer", format: "int32", minimum: 1 },
    rateLimitPermits: { type: "integer", format: "int32", minimum: 1 },
    rateLimitWindowMs: { type: "integer", format: "int64", minimum: 1 },
    idempotent: { type: "boolean" },
    unknownResultReconcileEnabled: { type: "boolean" },
    reason: reasonProperty
  },
  "Resilience policy change request requiring a separate approval."
);
schemas.AdmResilienceDecisionRequest = objectSchema(["reason"], { reason: reasonProperty }, "Resilience policy approval or rejection reason.");
schemas.AdmOpenApiRefreshRequest = objectSchema(
  ["reason"],
  { reason: reasonProperty },
  "Audited OpenAPI route inventory refresh request. The authenticated operator is resolved from the server session."
);

// Canonical public types referenced by controller-local request records.
schemas.RecoveryTarget = { type: "string", enum: ["TRANSACTION", "SEGMENT"], description: "Trace recovery target." };
schemas.CpfLogLevel = { type: "string", enum: ["TRACE", "DEBUG", "INFO", "WARN", "ERROR"], description: "CPF log level." };
schemas.CpfTabularFormat = { type: "string", enum: ["CSV", "XLSX"], description: "Supported tabular file format." };
schemas.InstanceCommand = { type: "string", enum: ["DRAIN", "DISABLE", "RESUME"], description: "Service instance state command." };
schemas.UnknownResolution = {
  type: "string",
  enum: ["SIDE_EFFECT_NOT_APPLIED", "SIDE_EFFECT_APPLIED", "SIDE_EFFECT_COMPENSATED"],
  description: "Operator-confirmed UNKNOWN row resolution."
};
schemas.CpfRuntimePayload = {
  type: "object",
  additionalProperties: true,
  description: "Canonical JSON object payload for a runtime-control change."
};
schemas.CpfRuntimeTargetSelector = objectSchema(
  ["includeDraining", "includeMaintenance", "allowAll"],
  {
    environment: { type: "string" }, serviceId: { type: "string" }, groupId: { type: "string" },
    instanceIds: { type: "array", items: { type: "string" } },
    excludeInstanceIds: { type: "array", items: { type: "string" } },
    labels: { type: "object", additionalProperties: { type: "string" } },
    zone: { type: "string" }, cell: { type: "string" },
    includeDraining: { type: "boolean" }, includeMaintenance: { type: "boolean" }, allowAll: { type: "boolean" }
  },
  "Runtime control-plane target selector."
);
schemas.CpfRuntimeActualState = objectSchema(
  ["actualVersion"],
  {
    changeType: { type: "string" }, actualVersion: { type: "integer", format: "int64" },
    actualHash: { type: "string" }, sourceDeliveryId: { type: "string" }
  },
  "Runtime agent actual state report."
);

ensureOperation("/adm/api/openapi/status", "get", "admOpenApiStatus", "OpenAPI Web MVC status");
ensureOperation("/adm/api/openapi/refresh", "post", "admOpenApiRefresh", "Refresh OpenAPI Web MVC route inventory");

const limit = query("limit", { type: "integer", format: "int32", minimum: 1, maximum: 500, default: 100 }, false, "Maximum number of rows");
const expectedVersion = query("expectedVersion", { type: "integer", format: "int64", minimum: 0 }, true, "Optimistic-lock version");
const reason = query("reason", { type: "string", minLength: 1, maxLength: 500 }, true, "Audited operation reason");

apply("admCacheSummary", {});
apply("admCacheRefresh", {
  parameters: [
    query("target", { type: "string", enum: ["ALL", "CODE", "MESSAGE", "RESPONSE_CODE", "CONFIG"], default: "ALL" }, false),
    reason
  ]
});
apply("admCacheEvictKey", { requestBody: "AdmCacheEvictKeyRequest" });
apply("admCacheEvictNamespace", { requestBody: "AdmCacheEvictNamespaceRequest" });
apply("admCacheReconcile", { requestBody: "AdmCacheControlRequest" });

apply("admNotificationFindRules", { parameters: [limit] });
apply("admNotificationFindRule", {});
apply("admNotificationSaveRule", { requestBody: "AdmNotificationRuleRequest" });
apply("admNotificationUpdateRule", { requestBody: "AdmNotificationRuleRequest" });
apply("admNotificationDisableRule", { parameters: [reason] });
apply("admNotificationFindDeliveryLogs", { parameters: [limit] });
apply("admNotificationFindDlq", { parameters: [limit] });
apply("admNotificationFindDeliveryAttempts", { parameters: [limit] });
apply("admNotificationSendTest", { requestBody: "AdmNotificationTestSendRequest" });
apply("admNotificationRetryDelivery", { parameters: [expectedVersion, reason] });
apply("admNotificationCancelDelivery", { parameters: [expectedVersion, reason] });

apply("admMessageFindMessages", {});
apply("admMessageFindMessage", { parameters: [parameter("messageId", "path", { type: "integer", format: "int64" }, true)] });
apply("admMessageCreateMessage", { requestBody: "CommonMessageRequest" });
apply("admMessageUpdateMessage", { parameters: [parameter("messageId", "path", { type: "integer", format: "int64" }, true)], requestBody: "CommonMessageRequest" });
apply("admMessageDeleteMessage", { parameters: [parameter("messageId", "path", { type: "integer", format: "int64" }, true), reason] });


apply("admCodeFindCodes", {});
apply("admCodeFindCode", { parameters: [parameter("codeId", "path", { type: "integer", format: "int64", minimum: 1 }, true)] });
apply("admCodeCreateCode", { requestBody: "CommonCodeRequest" });
apply("admCodeUpdateCode", { parameters: [parameter("codeId", "path", { type: "integer", format: "int64", minimum: 1 }, true)], requestBody: "CommonCodeRequest" });
apply("admCodeDeleteCode", { parameters: [parameter("codeId", "path", { type: "integer", format: "int64", minimum: 1 }, true), reason] });

apply("admConfigFindConfigs", {});
apply("admConfigFindConfig", { parameters: [parameter("configId", "path", { type: "integer", format: "int64", minimum: 1 }, true)] });
apply("admConfigCreateConfig", { requestBody: "CommonConfigRequest" });
apply("admConfigUpdateConfig", { parameters: [parameter("configId", "path", { type: "integer", format: "int64", minimum: 1 }, true)], requestBody: "CommonConfigRequest" });
apply("admConfigDeleteConfig", { parameters: [parameter("configId", "path", { type: "integer", format: "int64", minimum: 1 }, true), reason] });

apply("admResponseCodeFindAll", {});
apply("admResponseCodeFindOne", { parameters: [parameter("responseCode", "path", { type: "string", pattern: "^[SE][A-Z]{3}[0-9]{6}$" }, true)] });
apply("admResponseCodeCreate", { parameters: [reason], requestBody: "CommonResponseCodeRequest" });
apply("admResponseCodeUpdate", { parameters: [parameter("responseCode", "path", { type: "string", pattern: "^[SE][A-Z]{3}[0-9]{6}$" }, true), reason], requestBody: "CommonResponseCodeRequest" });
apply("admResponseCodeDelete", { parameters: [parameter("responseCode", "path", { type: "string", pattern: "^[SE][A-Z]{3}[0-9]{6}$" }, true), reason] });

apply("admFeatureFlagSearch", { parameters: pageParameters });
apply("admFeatureFlagFind", {});
apply("admFeatureFlagEvaluate", { requestBody: "AdmFeatureFlagEvaluateRequest" });
apply("admFeatureFlagRequestOverride", { requestBody: "AdmFeatureFlagOverrideRequest" });
apply("admFeatureFlagApproveOverride", { parameters: [riskConfirmed], requestBody: "AdmFeatureFlagDecisionRequest" });
apply("admFeatureFlagRevokeOverride", { parameters: [riskConfirmed], requestBody: "AdmFeatureFlagDecisionRequest" });
apply("admFeatureFlagSetKillSwitch", { parameters: [riskConfirmed], requestBody: "AdmFeatureFlagKillSwitchRequest" });

apply("admResiliencePolicySearch", { parameters: pageParameters });
apply("admResiliencePolicyFind", {});
apply("admResiliencePolicyRequest", { requestBody: "AdmResiliencePolicyRequest" });
apply("admResiliencePolicyApprove", { parameters: [riskConfirmed], requestBody: "AdmResilienceDecisionRequest" });
apply("admResiliencePolicyReject", { parameters: [riskConfirmed], requestBody: "AdmResilienceDecisionRequest" });

apply("admOpenApiStatus", {});
apply("admOpenApiRefresh", { parameters: [riskConfirmed], requestBody: "AdmOpenApiRefreshRequest" });

// The source DTO contains requestUser for legacy compatibility, but the controller ignores it and
// binds the authenticated operator. Keep the public generated contract free from actor spoofing.
apply("requestAdmBrokerDlqReplay", { requestBody: "AdmReliabilityActionRequest" });

spec["x-cpf-adm-explicit-contract-version"] = 6;
const canonical = JSON.stringify(spec, null, 2) + "\n";
fs.mkdirSync(path.dirname(outputPath), { recursive: true });
fs.writeFileSync(outputPath, canonical);
console.log(`[CPF][OPENAPI][PASS] explicit ADM contracts output=${path.relative(root, outputPath)} sha256=${crypto.createHash("sha256").update(canonical).digest("hex")}`);
