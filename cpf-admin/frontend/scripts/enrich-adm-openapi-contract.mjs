import fs from "node:fs";
import path from "node:path";
import crypto from "node:crypto";

// R6I: this script is deliberately validation-only. Runtime/controller OpenAPI is the
// contract owner; frontend tooling must never synthesize missing routes, schemas, security,
// request bodies or error responses.
const root = process.cwd();
const sourcePath = path.resolve(root, process.argv[2] || process.env.CPF_OPENAPI_FILE || "openapi/cpf-openapi.json");
const outputPath = path.resolve(root, process.argv[3] || sourcePath);
if (!fs.existsSync(sourcePath)) throw new Error(`OpenAPI source missing: ${sourcePath}`);
if (outputPath !== sourcePath) throw new Error("OpenAPI enrichment output is forbidden; runtime/controller contract is canonical");
const raw = fs.readFileSync(sourcePath);
const before = crypto.createHash("sha256").update(raw).digest("hex");
const spec = JSON.parse(raw.toString("utf8"));
const paths = spec.paths || {};
const methods = new Set(["get","post","put","patch","delete","head","options"]);
const requiredErrors = ["401","403","404","409","429","500","503"];

function operation(operationId) {
  for (const [route, item] of Object.entries(paths)) for (const [method, value] of Object.entries(item || {})) {
    if (methods.has(method) && value?.operationId === operationId) return { route, method, value };
  }
  throw new Error(`OpenAPI operation missing: ${operationId}`);
}
function requireOperation(route, method, operationId) {
  const value = paths[route]?.[method.toLowerCase()];
  if (!value) throw new Error(`Runtime/controller OpenAPI route missing: ${method.toUpperCase()} ${route}`);
  if (value.operationId !== operationId) throw new Error(`Runtime/controller OpenAPI operationId drift: ${method.toUpperCase()} ${route}`);
  return value;
}

if (Number(spec["x-cpf-canonical-schema-version"]) !== 5) throw new Error("canonical schema version 5 required");
if (!["CONTROLLER_SOURCE_PRE_RUNTIME","BACKEND_RUNTIME"].includes(spec["x-cpf-export-origin"])) throw new Error("runtime/controller OpenAPI origin marker missing");
let publicOperations = 0;
for (const [route, item] of Object.entries(paths)) for (const [method, value] of Object.entries(item || {})) {
  if (methods.has(method) && value?.operationId && route.startsWith("/adm/api/")) publicOperations += 1;
}
if (publicOperations < 1) throw new Error("ADM public operations missing");
// Critical high-risk operations must already carry their controller-derived security and error contract.
for (const target of [
  requireOperation("/adm/api/approvals/requests/{id}/execute", "post", "admApprovalExecute"),
  requireOperation("/adm/api/integration-closure/data-quality/quarantine/{id}/correction-approvals", "post", "admIntegrationDataQualityCorrectionApprovalRequest"),
  operation("admIntegrationDataQualityCorrectionExecute").value
]) {
  for (const status of requiredErrors) if (!target.responses?.[status]) throw new Error(`${target.operationId}: required response ${status} missing`);
  const security = Array.isArray(target.security) ? target.security : [];
  if (!security.some(entry => Object.hasOwn(entry,"admSessionCookie") || Object.hasOwn(entry,"cpfSession"))) throw new Error(`${target.operationId}: authenticated session security missing`);
  if (!security.some(entry => Object.hasOwn(entry,"admCsrfHeader") || Object.hasOwn(entry,"cpfCsrf"))) throw new Error(`${target.operationId}: CSRF security missing`);
}
const after = crypto.createHash("sha256").update(fs.readFileSync(sourcePath)).digest("hex");
if (before !== after) throw new Error("validation-only OpenAPI tool mutated its source");
console.log(`[CPF][ADM][OPENAPI][VALIDATION-ONLY][PASS] operations=${publicOperations} sha256=${after}`);
