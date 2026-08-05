import fs from "node:fs";
import path from "node:path";
const root=process.cwd();
const specPath=path.join(root,"openapi/cpf-openapi.json");
if(!fs.existsSync(specPath))throw new Error("OpenAPI missing");
const openapi=JSON.parse(fs.readFileSync(specPath,"utf8"));
const defaultScope=openapi["x-cpf-export-origin"]==="CONTROLLER_SOURCE_PRE_RUNTIME"?"source":"full";
const verificationScope=String(process.env.CPF_CONSUMER_SCOPE||defaultScope).toLowerCase();
if(!["full","source","changed"].includes(verificationScope))throw new Error(`Unsupported CPF_CONSUMER_SCOPE=${verificationScope}`);
if(verificationScope==="full"&&openapi["x-cpf-export-origin"]!=="BACKEND_RUNTIME")throw new Error("Full consumer closure requires canonical BACKEND_RUNTIME OpenAPI");
if(verificationScope==="source"&&(openapi["x-cpf-export-origin"]!=="CONTROLLER_SOURCE_PRE_RUNTIME"||openapi["x-cpf-release-eligible"]!==false))throw new Error("Source consumer closure requires release-ineligible CONTROLLER_SOURCE_PRE_RUNTIME OpenAPI");
const methods=["get","post","put","patch","delete","head","options","trace"];
const operations=[];
for(const [template,item] of Object.entries(openapi.paths||{}))for(const method of methods){const operationId=item?.[method]?.operationId;if(operationId)operations.push({method:method.toUpperCase(),template,operationId});}
const failures=[];const ids=operations.map(v=>v.operationId);
if(!operations.length)failures.push("OpenAPI has no operations");
if(new Set(ids).size!==ids.length)failures.push("OpenAPI operationId values are not unique");
const requiredFiles=["src/generated/cpf-operation-contract.ts","src/generated/cpf-api.ts","src/generated/orval/cpf-api.ts","src/shared/cpfApi.ts","src/shared/queryClient.ts"];
for(const rel of requiredFiles)if(!fs.existsSync(path.join(root,rel)))failures.push(`missing ${rel}`);
const read=rel=>fs.existsSync(path.join(root,rel))?fs.readFileSync(path.join(root,rel),"utf8"):"";
const contract=read(requiredFiles[0]),compatibility=read(requiredFiles[1]),orval=read(requiredFiles[2]),shared=read(requiredFiles[3]),mutator=read("src/shared/orval-mutator.ts");
for(const operation of operations){if(!contract.includes(JSON.stringify(operation.operationId)))failures.push(`operation contract missing ${operation.operationId}`);if(!compatibility.includes(`function ${operation.operationId}<`))failures.push(`compatibility client missing ${operation.operationId}`);}
if(!orval.includes("@tanstack/vue-query"))failures.push("Orval client is not generated with TanStack Vue Query");
for(const token of ["resolveCpfOperation","cpfQueryClient","MutationObserver","credentials: \"include\"","redirect: \"error\""])if(!`${shared}\n${mutator}`.includes(token))failures.push(`shared API owner missing ${token}`);
function walk(dir){const out=[];if(!fs.existsSync(dir))return out;for(const entry of fs.readdirSync(dir,{withFileTypes:true})){const absolute=path.join(dir,entry.name);if(entry.isDirectory())out.push(...walk(absolute));else if(/\.(?:ts|vue)$/.test(entry.name))out.push(absolute);}return out;}
function templateRegex(template){const escaped=template.replace(/[.*+?^${}()|[\]\\]/g,"\\$&").replace(/\\\{[^/]+\\\}/g,"[^/]+");return new RegExp(`^${escaped}$`);}
function normalizeSourceTemplate(raw){return raw.replace(/\$\{[^}]+\}/g,"x").split("?")[0];}
const consumed=new Set();
function match(method,raw,rel){const pathname=normalizeSourceTemplate(raw);const found=operations.find(op=>op.method===method&&templateRegex(op.template).test(pathname));if(!found)failures.push(`${rel}: privileged API is absent from OpenAPI: ${method} ${raw}`);else consumed.add(found.operationId);}
const patterns=[
 {re:/\b(?:adm|bza)Query(?:<[^>]+>)?\s*\(\s*([`"'])(\/[^`"']+)\1/g,method:"GET"},
 {re:/\b(?:adm|bza)Mutation(?:<[^>]+>)?\s*\(\s*([`"'])(\/[^`"']+)\1\s*,\s*([`"'])(POST|PUT|PATCH|DELETE)\3/g,method:null},
 {re:/\bthis\.getJson(?:<[^>]+>)?\s*\(\s*([`"'])(\/[^`"']+)\1/g,method:"GET"},
 {re:/\bthis\.sendJson(?:<[^>]+>)?\s*\(\s*([`"'])(\/[^`"']+)\1\s*,\s*([`"'])(POST|PUT|PATCH|DELETE)\3/g,method:null},
 {re:/\bthis\.rawResponse(?:<[^>]+>)?\s*\(\s*([`"'])(\/[^`"']+)\1(?:\s*,\s*([`"'])(GET|POST|PUT|PATCH|DELETE)\3)?/g,method:"RAW"},
];

function extractCall(text,start){
 let depth=0,quote=null,escaped=false;
 for(let i=start;i<text.length;i++){
   const ch=text[i];
   if(quote){if(escaped)escaped=false;else if(ch==="\\")escaped=true;else if(ch===quote)quote=null;continue;}
   if(ch==='"'||ch==="'"||ch==='`'){quote=ch;continue;}
   if(ch==='(')depth++; else if(ch===')'){depth--;if(depth===0)return text.slice(start,i+1);}
 }
 return text.slice(start,Math.min(text.length,start+1200));
}
function inferWrapperCalls(text,rel){
 const wrapper=/\b(?:request|admApi|bzaApi)(?:<[^>]+>)?\s*\(\s*([`"'])(\/(?:adm\/api|api\/bza)\/[^`"']+)\1/g;
 for(const item of text.matchAll(wrapper)){
   const raw=item[2]; const callStart=(item.index||0)+item[0].indexOf('('); const call=extractCall(text,callStart);
   const explicit=call.match(/\bmethod\s*:\s*([`"'])(POST|PUT|PATCH|DELETE|GET)\1/i);
   match(explicit?explicit[2].toUpperCase():"GET",raw,rel);
 }
}
for(const file of walk(path.join(root,"src"))){const rel=path.relative(root,file).replaceAll("\\","/");if(rel.startsWith("src/generated/"))continue;const text=fs.readFileSync(file,"utf8");if(/\bfetch\s*\(/.test(text)&&/\/(?:adm\/api|api\/bza)\b/.test(text)&&!["src/shared/cpfApi.ts","src/shared/orval-mutator.ts"].includes(rel))failures.push(`${rel}: direct privileged API fetch is forbidden`);if(/\b(?:axios|XMLHttpRequest)\b/.test(text))failures.push(`${rel}: direct HTTP client usage is forbidden`);
 for(const pattern of patterns){for(const matchValue of text.matchAll(pattern.re)){const raw=matchValue[2];if(!/^\/(?:adm\/api|api\/bza)\//.test(raw))continue;const method=pattern.method===null?matchValue[4]:(pattern.method==="RAW"?(matchValue[4]||"GET"):pattern.method);if(method!=="DYNAMIC")match(method,raw,rel);}}
 inferWrapperCalls(text,rel);
 for(const invoked of text.matchAll(/\badmInvokeOperation(?:<[^>]+>)?\s*\(\s*["']([^"']+)["']/g)){
   const operationId=invoked[1];
   if(!ids.includes(operationId))failures.push(`${rel}: unknown generated operation invocation ${operationId}`);
   else consumed.add(operationId);
 }
 for(const stream of text.matchAll(/\bnew\s+EventSource\s*\(\s*([`"'])(\/adm\/api\/[^`"']+)\1/g))match("GET",stream[2],rel);
}
const routeRegistry=read("src/app/routes.ts");
const appSource=read("src/App.vue");
const workbenchSource=read("src/components/RouteOperationWorkbench.vue");
const hasRouteWorkbench=appSource.includes("RouteOperationWorkbench")&&/(?:adm|bza)InvokeOperation/.test(workbenchSource)&&workbenchSource.includes("cpfOperationDescriptors");
if(!hasRouteWorkbench)failures.push("route operation workbench is not wired to the generated operation contract");
if(hasRouteWorkbench){
  for(const block of routeRegistry.matchAll(/expectedOperationIds\s*:\s*\[([^\]]*)\]/gs)){
    for(const value of block[1].matchAll(/["']([^"']+)["']/g)){
      const operationId=value[1];
      if(!ids.includes(operationId))failures.push(`route operation registry references unknown operation: ${operationId}`);
      else consumed.add(operationId);
    }
  }
}
const waiversPath=path.join(root,"openapi/cpf-consumer-waivers.json");let waived=new Set();if(fs.existsSync(waiversPath)){const waivers=JSON.parse(fs.readFileSync(waiversPath,"utf8"));for(const waiver of waivers.waivers||[]){if(!waiver.operationId||!waiver.owner||!waiver.reason||!waiver.expiresOn)failures.push("invalid consumer waiver");else waived.add(waiver.operationId);}}
const publicPrefix=String(openapi["x-cpf-product-module"]).toUpperCase()==="ADM"?"/adm/api/":"/api/bza/";
const internalPrefixes=["/adm/api/auth/","/api/bza/auth/"];
if(verificationScope!=="changed")for(const operation of operations){if(!operation.template.startsWith(publicPrefix)||internalPrefixes.some(prefix=>operation.template.startsWith(prefix)))continue;if(!consumed.has(operation.operationId)&&!waived.has(operation.operationId))failures.push(`public operation has no real frontend consumer or approved waiver: ${operation.operationId} ${operation.method} ${operation.template}`);}
if(failures.length){console.error(failures.join("\n"));process.exit(1);}console.log(`[CPF][FRONTEND][PASS] operation consumer closure scope=${verificationScope} operations=${operations.length} consumed=${consumed.size} waived=${waived.size}`);
