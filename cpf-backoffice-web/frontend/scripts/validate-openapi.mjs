import fs from "node:fs";
import path from "node:path";

function option(name) {
  const prefix = `--${name}=`;
  const hit = process.argv.slice(2).find(value => value.startsWith(prefix));
  return hit ? hit.slice(prefix.length) : undefined;
}

const positionalFile = process.argv.slice(2).find(value => !value.startsWith("--"));
const file = path.resolve(process.env.CPF_OPENAPI_FILE || option("file") || positionalFile || "openapi/cpf-openapi.json");
if (!fs.existsSync(file)) throw new Error(`OpenAPI file missing: ${file}`);
const spec = JSON.parse(fs.readFileSync(file, "utf8"));
if (spec.openapi?.split('.')[0] !== '3') throw new Error("OpenAPI 3.x 문서가 아닙니다.");
if (spec["x-cpf-source-sha"] || spec["x-cpf-result-sha"]) throw new Error("Tracked OpenAPI에 Git SHA를 기록할 수 없습니다.");
const inferredScope = spec["x-cpf-export-origin"] === "BACKEND_RUNTIME" ? "release" : "source";
const verificationScope = String(process.env.CPF_OPENAPI_SCOPE || option("scope") || inferredScope).toLowerCase();
if (!["release", "source"].includes(verificationScope)) throw new Error(`Unsupported CPF_OPENAPI_SCOPE=${verificationScope}`);
const expectedOrigin = verificationScope === "release" ? "BACKEND_RUNTIME" : "CONTROLLER_SOURCE_PRE_RUNTIME";
if (spec["x-cpf-export-origin"] !== expectedOrigin) throw new Error(`x-cpf-export-origin=${expectedOrigin} 필요`);
if (verificationScope === "source" && spec["x-cpf-release-eligible"] !== false) throw new Error("Source contract must be release-ineligible");
if (verificationScope === "release" && spec["x-cpf-release-eligible"] !== true) throw new Error("Runtime contract must be release-eligible");
if (Number(spec["x-cpf-canonical-schema-version"]) !== 5) throw new Error("canonical OpenAPI schemaVersion 5 필요");
const module = String(spec["x-cpf-product-module"] || "").toUpperCase();
// CPF canonical module code 는 MBW 다(CpfSystemCodes / canonicalize-cpf-openapi.py MODULE_PREFIX).
// Runtime 이 내보내는 문서는 MBW 이고, 아직 갱신되지 않은 tracked source 문서는 Backoffice 다.
// 둘 다 같은 Public prefix 를 가리키므로 canonical 코드와 legacy 표기를 함께 받는다.
const publicPrefix = module === "ADM" ? "/adm/api/"
  : (module === "MBW" || module === "BACKOFFICE") ? "/api/v1/backoffice/"
  : null;
if (!publicPrefix) throw new Error(`지원하지 않는 제품 Module: ${module}`);
const requiredErrors = ["401","403","404","409","429","500","503"];
const methods = new Set(["get","post","put","patch","delete","head","options","trace"]);
const ids = new Set(); let operations = 0; let publicOperations = 0;
function resolveParameter(value) {
  if (!value?.$ref) return value;
  const prefix = "#/components/parameters/";
  if (!value.$ref.startsWith(prefix)) return null;
  return spec.components?.parameters?.[value.$ref.slice(prefix.length)] || null;
}
for (const [url, pathItem] of Object.entries(spec.paths || {})) {
  for (const [method, operation] of Object.entries(pathItem || {})) {
    if (!methods.has(method)) continue;
    operations++;
    if (!operation?.operationId || ids.has(operation.operationId)) throw new Error(`operationId 누락/중복: ${method.toUpperCase()} ${url}`);
    ids.add(operation.operationId);
    const placeholders = new Set([...url.matchAll(/\{([^{}]+)\}/g)].map(match => match[1]));
    const parameters = [...(pathItem.parameters || []), ...(operation.parameters || [])]
      .map(resolveParameter).filter(Boolean);
    for (const name of placeholders) {
      if (!parameters.some(parameter => parameter.in === "path" && parameter.name === name && parameter.required === true)) {
        throw new Error(`필수 Path Parameter 선언 누락: ${operation.operationId}:${name}`);
      }
    }
    for (const parameter of parameters.filter(value => value.in === "path")) {
      if (!placeholders.has(parameter.name)) throw new Error(`Template에 없는 Path Parameter: ${operation.operationId}:${parameter.name}`);
    }
    const responses = operation.responses || {};
    const successCodes = Object.keys(responses).filter(code => /^2\d\d$/.test(code));
    if (!successCodes.length) throw new Error(`2xx 응답 누락: ${operation.operationId}`);
    for (const code of successCodes) {
      const content = responses[code]?.content || {};
      for (const media of Object.values(content)) {
        if (!media?.schema || Object.keys(media.schema).length === 0) throw new Error(`성공 DTO Schema 누락: ${operation.operationId}:${code}`);
      }
    }
    if (url.startsWith(publicPrefix)) {
      publicOperations++;
      if (verificationScope === "release") {
        if (!Array.isArray(operation.security) || !operation.security.some(value => Object.hasOwn(value,"cpfSession"))) throw new Error(`Session Security 누락: ${operation.operationId}`);
        for (const code of requiredErrors) if (!responses[code]) throw new Error(`${code} 응답 누락: ${operation.operationId}`);
      }
    }
  }
}
if (operations < 1 || publicOperations < 1) throw new Error(`제품 API 전체 Export가 아닙니다: operations=${operations}, public=${publicOperations}`);
if (Number(spec["x-cpf-openapi-operation-count"]) !== operations) throw new Error("OpenAPI operation count metadata drift");
if (Number(spec["x-cpf-public-operation-count"]) !== publicOperations) throw new Error("OpenAPI public operation count metadata drift");
if (verificationScope === "release") {
  const errorSchema = spec.components?.schemas?.CpfApiError;
  const sessionScheme = spec.components?.securitySchemes?.cpfSession;
  if (!errorSchema || !sessionScheme || sessionScheme.in !== "cookie") throw new Error("CpfApiError/cpfSession component contract missing");
}
console.log(`[CPF][OPENAPI][PASS] scope=${verificationScope} module=${module} operations=${operations} public=${publicOperations}`);
