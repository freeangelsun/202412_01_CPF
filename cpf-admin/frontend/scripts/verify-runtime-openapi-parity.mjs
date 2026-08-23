import fs from "node:fs";
import path from "node:path";
const sourcePath=path.resolve(process.argv[2] || "openapi/cpf-openapi.json");
const runtimePath=path.resolve(process.argv[3] || process.env.CPF_ADM_RUNTIME_OPENAPI_FILE || "");
if(!fs.existsSync(sourcePath)) throw new Error(`source OpenAPI missing: ${sourcePath}`);
if(!runtimePath || !fs.existsSync(runtimePath)) throw new Error(`runtime OpenAPI missing: ${runtimePath}`);
const source=JSON.parse(fs.readFileSync(sourcePath,"utf8")); const runtime=JSON.parse(fs.readFileSync(runtimePath,"utf8"));
if(source["x-cpf-export-origin"]!=="CONTROLLER_SOURCE_PRE_RUNTIME") throw new Error("source OpenAPI origin drift");
if(runtime["x-cpf-export-origin"]!=="BACKEND_RUNTIME" || runtime["x-cpf-release-eligible"]!==true) throw new Error("runtime OpenAPI is not release eligible");
const module=String(source["x-cpf-product-module"]||"").toUpperCase();
if(String(runtime["x-cpf-product-module"]||"").toUpperCase()!==module) throw new Error("runtime/source product module drift");
const prefix=module==="ADM"?"/adm/api/":null; if(!prefix) throw new Error(`unsupported module: ${module}`);
const methods=new Set(["get","post","put","patch","delete","head","options"]);
function tuples(spec){ const result=[]; for(const [route,item] of Object.entries(spec.paths||{})) for(const [method,op] of Object.entries(item||{})) if(methods.has(method)&&op?.operationId&&route.startsWith(prefix)) result.push(`${method.toUpperCase()} ${route} ${op.operationId}`); return result.sort(); }
const a=tuples(source), b=tuples(runtime); if(JSON.stringify(a)!==JSON.stringify(b)) { const bs=new Set(b), as=new Set(a); throw new Error(`runtime/source operation drift missingRuntime=${a.filter(x=>!bs.has(x)).join("|")} missingSource=${b.filter(x=>!as.has(x)).join("|")}`); }
const criticalSchemas=module==="ADM"?["CpfApiError","AdmApprovalRequestCreateRequest","AdmApprovalDecisionRequest","AdmDataQualityCorrectionApprovalRequest"]:["CpfApiError"];
for(const name of criticalSchemas){ if(source.components?.schemas?.[name] && !runtime.components?.schemas?.[name]) throw new Error(`runtime schema missing: ${name}`); }
console.log(`[CPF][${module}][OPENAPI][RUNTIME-PARITY][PASS] operations=${a.length}`);
