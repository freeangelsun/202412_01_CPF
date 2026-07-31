import fs from "node:fs";
import path from "node:path";
const file = path.resolve(process.env.CPF_OPENAPI_FILE || process.argv[2] || "openapi/cpf-openapi.json");
const spec = JSON.parse(fs.readFileSync(file, "utf8"));
if (spec.openapi?.split('.')[0] !== '3') throw new Error("OpenAPI 3.x 문서가 아닙니다.");
if (spec["x-cpf-source-sha"] || spec["x-cpf-result-sha"]) throw new Error("Tracked OpenAPI에 Git SHA를 기록할 수 없습니다.");
if (spec["x-cpf-export-origin"] !== "BACKEND_RUNTIME") throw new Error("수작업 Snapshot은 금지됩니다: x-cpf-export-origin=BACKEND_RUNTIME 필요");
const ids = new Set();
let operations = 0;
let nonAuth = 0;
const methods = new Set(["get","post","put","patch","delete","head","options","trace"]);
for (const [url, pathItem] of Object.entries(spec.paths || {})) {
  for (const [method, operation] of Object.entries(pathItem || {})) {
    if (!methods.has(method)) continue;
    operations++;
    if (!String(url).includes("/auth/")) nonAuth++;
    if (!operation.operationId || ids.has(operation.operationId)) throw new Error(`operationId 누락/중복: ${method.toUpperCase()} ${url}`);
    ids.add(operation.operationId);
    if (!operation.responses || Object.keys(operation.responses).length === 0) throw new Error(`응답 계약 누락: ${operation.operationId}`);
    const success = Object.keys(operation.responses).some(code => /^2\d\d$/.test(code));
    if (!success) throw new Error(`2xx 응답 누락: ${operation.operationId}`);
  }
}
if (operations < 1 || nonAuth < 1) throw new Error(`제품 API 전체 Export가 아닙니다: operations=${operations}, nonAuth=${nonAuth}`);
if (Number(spec["x-cpf-openapi-operation-count"]) !== operations) throw new Error("OpenAPI operation count metadata drift");
console.log(`[CPF][OPENAPI][PASS] operations=${operations} nonAuth=${nonAuth}`);
