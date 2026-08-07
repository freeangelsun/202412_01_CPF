import fs from "node:fs";
import path from "node:path";
import {fileURLToPath} from "node:url";
import os from "node:os";
import crypto from "node:crypto";
import {spawnSync} from "node:child_process";
const root=path.resolve(path.dirname(fileURLToPath(import.meta.url)),"..");
const pkg=JSON.parse(fs.readFileSync(path.join(root,"package.json"),"utf8"));
const scripts=pkg.scripts||{};
if(scripts["validate:openapi"]!=="npm run validate:openapi:source") throw new Error("Default OpenAPI scope must be source");
if(!scripts["validate:openapi:source"]?.includes("--scope=source")) throw new Error("Source OpenAPI scope is missing");
if(!scripts["validate:openapi:release"]?.includes("--scope=release")) throw new Error("Release runtime gate is missing");
const verify=scripts.verify||"";
for(const required of ["generate:api","verify:generated","verify:consumer","lint","typecheck","test","build:prod"]) if(!verify.includes(required)) throw new Error(`verify lifecycle missing ${required}`);
if(verify.indexOf("generate:api")>verify.indexOf("verify:generated")||verify.indexOf("verify:generated")>verify.indexOf("verify:consumer")) throw new Error("Generated client lifecycle order is invalid");
const validator=fs.readFileSync(path.join(root,"scripts/validate-openapi.mjs"),"utf8");
if(!validator.includes('option("scope")')) throw new Error("OpenAPI validator must accept an explicit scope");
const enrich=fs.readFileSync(path.join(root,"scripts/enrich-adm-openapi-contract.mjs"),"utf8");
for(const forbidden of ["writeFileSync", "components.schemas[", ".requestBody =", ".responses =", ".security ="]) {
  if(enrich.includes(forbidden)) throw new Error(`OpenAPI enrichment must be validation-only: forbidden semantic writer ${forbidden}`);
}
const source=JSON.parse(fs.readFileSync(path.join(root,"openapi/cpf-openapi.json"),"utf8"));
let target=null;
for(const item of Object.values(source.paths||{})) for(const value of Object.values(item||{})) {
  if(value?.operationId==="admApprovalExecute") target=value;
}
if(!target?.responses?.["503"]) throw new Error("Mutation baseline requires admApprovalExecute response 503");
const tempDir=fs.mkdtempSync(path.join(os.tmpdir(),"cpf-openapi-lifecycle-"));
try {
  const mutated=structuredClone(source);
  let mutatedTarget=null;
  for(const item of Object.values(mutated.paths||{})) for(const value of Object.values(item||{})) {
    if(value?.operationId==="admApprovalExecute") mutatedTarget=value;
  }
  delete mutatedTarget.responses["503"];
  const mutationPath=path.join(tempDir,"cpf-openapi.json");
  fs.writeFileSync(mutationPath,JSON.stringify(mutated));
  const before=crypto.createHash("sha256").update(fs.readFileSync(mutationPath)).digest("hex");
  const run=spawnSync(process.execPath,[path.join(root,"scripts/enrich-adm-openapi-contract.mjs"),mutationPath],{cwd:root,encoding:"utf8"});
  const after=crypto.createHash("sha256").update(fs.readFileSync(mutationPath)).digest("hex");
  if(run.status===0) throw new Error("OpenAPI semantic mutation was incorrectly repaired/accepted by enrichment");
  if(before!==after) throw new Error("Validation-only enrichment mutated the failing OpenAPI fixture");
} finally {
  fs.rmSync(tempDir,{recursive:true,force:true});
}
console.log("[CPF][OPENAPI][LIFECYCLE][PASS] validationOnly=true mutationDetected=true");
