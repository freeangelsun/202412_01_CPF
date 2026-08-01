import { MutationObserver } from "@tanstack/vue-query";
import { CpfOrvalError, cpfOrvalRequest } from "./orval-mutator";
import { cpfOperationDescriptors, resolveCpfOperation, type CpfOperationId } from "../generated/cpf-operation-contract";
import { cpfQueryClient } from "./queryClient";


const CLIENT_ACTOR_FIELDS = new Set(["requestUser", "actorId", "operatorIdOverride"]);
function csrfToken(): string {
  const entry=document.cookie.split(";").map(v=>v.trim()).find(v=>v.startsWith("XSRF-TOKEN="));
  return entry?decodeURIComponent(entry.substring("XSRF-TOKEN=".length)):"";
}
function headers(operationId:string, extra:HeadersInit={}):Headers {
  const value=new Headers(extra);
  if(value.has("Authorization"))throw new Error("BZA BFF는 Browser Bearer Token을 허용하지 않습니다.");
  value.set("X-CPF-Operation-Id",operationId);
  const csrf=csrfToken();if(csrf&&!value.has("X-XSRF-TOKEN"))value.set("X-XSRF-TOKEN",csrf);
  return value;
}
function assertNoClientActor(value:unknown,path="$",visited=new WeakSet<object>()):void {
  if(value===null||value===undefined||typeof value!=="object")return;if(visited.has(value as object))return;visited.add(value as object);
  if(Array.isArray(value)){value.forEach((item,index)=>assertNoClientActor(item,`${path}[${index}]`,visited));return;}
  for(const [key,child] of Object.entries(value as Record<string,unknown>)){if(CLIENT_ACTOR_FIELDS.has(key))throw new Error(`Browser actor field is forbidden: ${path}.${key}`);assertNoClientActor(child,`${path}.${key}`,visited);}
}

export interface CpfGeneratedRequestConfig {
  url: string; method: string; headers?: HeadersInit; data?: unknown; params?: Record<string, unknown>; signal?: AbortSignal;
}
function convert(error: unknown): never {
  if (error instanceof CpfOrvalError) throw error;
  throw error;
}
export async function bzaQuery<T = unknown>(url: string, params?: Record<string, unknown>): Promise<T> {
  const target = new URL(url, window.location.origin);
  if(target.origin!==window.location.origin)throw new Error("BZA API target must be same-origin");
  Object.entries(params || {}).forEach(([key, value]) => { if (value !== undefined && value !== null) target.searchParams.set(key, String(value)); });
  const relative = target.pathname + target.search;
  const operation = resolveCpfOperation("GET", relative);
  try {
    return await cpfQueryClient.fetchQuery<T>({
      queryKey: ["cpf", operation.operationId, target.pathname, target.search],
      queryFn: () => cpfOrvalRequest<T>({ url: relative, method: "GET", headers: headers(operation.operationId) })
    });
  } catch (error) { return convert(error); }
}
export async function bzaMutation<T = unknown>(url: string, method: "POST" | "PUT" | "PATCH" | "DELETE", body?: unknown): Promise<T> {
  assertNoClientActor(body);
  const target = new URL(url, window.location.origin);
  if(target.origin!==window.location.origin)throw new Error("BZA API target must be same-origin");
  const relative = target.pathname + target.search;
  const operation = resolveCpfOperation(method, relative);
  const observer = new MutationObserver<T, unknown, unknown, unknown>(cpfQueryClient, {
    mutationKey: ["cpf", operation.operationId],
    mutationFn: () => cpfOrvalRequest<T>({ url: relative, method, data: body, headers: headers(operation.operationId,{ "Content-Type":"application/json" }) })
  });
  try {
    const result = await observer.mutate(undefined);
    await cpfQueryClient.invalidateQueries({ queryKey: ["cpf"] });
    return result;
  } catch (error) { return convert(error); } finally { observer.reset(); }
}
export async function cpfGeneratedRequest<T = unknown>(config: CpfGeneratedRequestConfig): Promise<T> {
  const method = config.method.trim().toUpperCase();
  const target = new URL(config.url, window.location.origin);
  Object.entries(config.params || {}).forEach(([key, value]) => { if (value !== undefined && value !== null) target.searchParams.set(key, String(value)); });
  const relative = target.pathname + target.search;
  if (method === "GET") return bzaQuery<T>(relative);
  if (!["POST", "PUT", "PATCH", "DELETE"].includes(method)) throw new Error(`Unsupported generated BZA method: ${method}`);
  return bzaMutation<T>(relative, method as "POST" | "PUT" | "PATCH" | "DELETE", config.data);
}
export async function bzaApi<T = unknown>(url: string, options: RequestInit = {}): Promise<T> {
  const method = String(options.method || "GET").toUpperCase();
  let body: unknown = options.body; if (typeof body === "string") { try { body = JSON.parse(body); } catch { /* keep string */ } }
  return cpfGeneratedRequest<T>({ url, method, data: body, headers: options.headers, signal: options.signal || undefined });
}

export interface CpfOperationInvokeOptions {
  path?: Record<string, string | number>;
  query?: Record<string, unknown>;
  body?: unknown;
}
function renderOperationPath(template: string, values: Record<string, string | number> = {}): string {
  return template.replace(/\{([^}]+)\}/g, (_, name: string) => {
    const value=values[name];
    if(value===undefined||value===null||String(value).trim()==="")throw new Error(`Missing path parameter: ${name}`);
    return encodeURIComponent(String(value));
  });
}
export async function bzaInvokeOperation<T = unknown>(operationId: CpfOperationId, options: CpfOperationInvokeOptions = {}): Promise<T> {
  const descriptor=cpfOperationDescriptors.find(value=>value.operationId===operationId);
  if(!descriptor)throw new Error(`BZA operation is not registered: ${operationId}`);
  const target=new URL(renderOperationPath(descriptor.template,options.path),window.location.origin);
  Object.entries(options.query||{}).forEach(([key,value])=>{
    if(CLIENT_ACTOR_FIELDS.has(key))throw new Error(`Browser actor query field is forbidden: ${key}`);
    if(value!==undefined&&value!==null&&String(value).trim()!=="")target.searchParams.set(key,String(value));
  });
  const relative=target.pathname+target.search;
  if(descriptor.method==="GET")return bzaQuery<T>(relative);
  return bzaMutation<T>(relative,descriptor.method as "POST"|"PUT"|"PATCH"|"DELETE",options.body);
}
