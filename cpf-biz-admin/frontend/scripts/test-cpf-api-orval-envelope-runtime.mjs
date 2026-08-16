import assert from "node:assert/strict";
import fs from "node:fs";
import os from "node:os";
import path from "node:path";
import { pathToFileURL } from "node:url";
import { spawnSync } from "node:child_process";

const root = process.cwd();
const sourcePath = path.join(root, "src/shared/cpfApi.ts");
let source = fs.readFileSync(sourcePath, "utf8");
const temp = fs.mkdtempSync(path.join(os.tmpdir(), "cpf-bza-api-envelope-"));
source = source
  .replace('@tanstack/vue-query', './mock-vue-query')
  .replaceAll('./orval-mutator', './mock-orval-mutator')
  .replace('../generated/cpf-operation-contract', './mock-operation-contract')
  .replace('./queryClient', './mock-query-client')
  .replace('./transaction', './mock-transaction');
fs.writeFileSync(path.join(temp, "cpfApi.ts"), source);
fs.writeFileSync(path.join(temp, "mock-orval-mutator.ts"), `
export interface CpfOrvalRequestConfig { url:string; method:string; headers?:HeadersInit; data?:unknown; params?:Record<string,unknown>; signal?:AbortSignal; }
export interface CpfOrvalResponse<T> { data:T; status:number; headers:Headers; }
export class CpfOrvalError extends Error { constructor(public status:number,message:string,public payload:unknown){super(message);this.name="CpfOrvalError";} }
export const calls:Array<CpfOrvalRequestConfig> = [];
let next:unknown = {value:7}; let failure:CpfOrvalError|undefined;
export function setPayload(value:unknown):void{next=value;failure=undefined;}
export function setFailure(value:CpfOrvalError):void{failure=value;}
export async function cpfOrvalRequest<T>(config:CpfOrvalRequestConfig):Promise<T>{calls.push(config);if(failure)throw failure;return {data:next,status:200,headers:new Headers()} as T;}
`);
fs.writeFileSync(path.join(temp, "mock-vue-query.ts"), `
export class MutationObserver<T,_TError=unknown,_TVariables=unknown,_TContext=unknown>{ constructor(_client:unknown,private options:{mutationFn:()=>Promise<T>;mutationKey?:unknown}){} async mutate(_value:unknown):Promise<T>{return this.options.mutationFn();} reset():void{} }
`);
fs.writeFileSync(path.join(temp, "mock-operation-contract.ts"), `
export type CpfOperationId = string;
export const cpfOperationDescriptors=[{method:"GET",template:"/api/bza/example",operationId:"bzaExampleGet"},{method:"POST",template:"/api/bza/example",operationId:"bzaExamplePost"}] as const;
export function resolveCpfOperation(method:string,rawUrl:string){const pathname=new URL(rawUrl,"https://cpf.example").pathname;const value=cpfOperationDescriptors.find(v=>v.method===method&&v.template===pathname);if(!value)throw new Error("not registered");return value;}
`);
fs.writeFileSync(path.join(temp, "mock-query-client.ts"), `
export const cpfQueryClient={async fetchQuery<T>(options:{queryFn:()=>Promise<T>;queryKey?:unknown}):Promise<T>{return options.queryFn();},async invalidateQueries(_options?:unknown):Promise<void>{}};
`);
fs.writeFileSync(path.join(temp, "mock-transaction.ts"), `
export const defaultHeaders:HeadersInit={}; export function createTransactionId(){return "01ARZ3NDEKTSV4RRFFQ69G5FAV";} export function isValidTransactionId(value:unknown){return typeof value==="string"&&value.length>10;}
`);
const inputs = ["cpfApi.ts","mock-orval-mutator.ts","mock-vue-query.ts","mock-operation-contract.ts","mock-query-client.ts","mock-transaction.ts"].map(v=>path.join(temp,v));
const compile = spawnSync(process.execPath, [path.join(root,"node_modules","typescript","bin","tsc"),...inputs,"--target","ES2022","--module","ES2022","--moduleResolution","Bundler","--strict","--lib","ES2022,DOM,DOM.Iterable","--skipLibCheck","--outDir",temp], {cwd:root,encoding:"utf8"});
if(compile.status!==0)throw new Error(`cpfApi compile failed:\n${compile.stdout}\n${compile.stderr}`);
for(const name of ["cpfApi","mock-orval-mutator","mock-vue-query","mock-operation-contract","mock-query-client","mock-transaction"]){const js=path.join(temp,`${name}.js`);fs.renameSync(js,path.join(temp,`${name}.mjs`));}
let compiled=fs.readFileSync(path.join(temp,"cpfApi.mjs"),"utf8");
for(const name of ["mock-orval-mutator","mock-vue-query","mock-operation-contract","mock-query-client","mock-transaction"])compiled=compiled.replaceAll(`./${name}`,`./${name}.mjs`);
fs.writeFileSync(path.join(temp,"cpfApi.mjs"),compiled);
globalThis.window={location:{origin:"https://cpf.example"}};globalThis.document={cookie:"XSRF-TOKEN=csrf"};
const mutator=await import(pathToFileURL(path.join(temp,"mock-orval-mutator.mjs")).href);
const api=await import(pathToFileURL(path.join(temp,"cpfApi.mjs")).href);
mutator.setPayload({value:11});
const query=await api.bzaQuery("/api/bza/example",{limit:10});
assert.deepEqual(query,{value:11});
assert.equal(Object.hasOwn(query,"data"),false,"legacy query must unwrap Orval envelope");
assert.equal(mutator.calls.at(-1).method,"GET");
mutator.setPayload({saved:true});
const mutation=await api.bzaMutation("/api/bza/example","POST",{reason:"audit"});
assert.deepEqual(mutation,{saved:true});
assert.equal(Object.hasOwn(mutation,"data"),false,"legacy mutation must unwrap Orval envelope");
assert.deepEqual(mutator.calls.at(-1).data,{reason:"audit"});
mutator.setFailure(new mutator.CpfOrvalError(409,"conflict",{message:"conflict"}));
await assert.rejects(()=>api.bzaMutation("/api/bza/example","POST",{reason:"audit"}),error=>error.name==="CpfApiError"&&error.status===409&&error.message==="conflict");
console.log("[CPF][BZA][FRONTEND][PASS] legacy cpfApi unwraps Orval envelope for query/mutation and converts errors");
