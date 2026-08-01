import { MutationObserver } from "@tanstack/vue-query";
import { CpfOrvalError, cpfOrvalRequest } from "./orval-mutator";
import { cpfOperationDescriptors, resolveCpfOperation, type CpfOperationId } from "../generated/cpf-operation-contract";
import { cpfQueryClient } from "./queryClient";
import { createTransactionId, defaultHeaders, isValidTransactionId } from "./transaction";

export class CpfApiError extends Error {
  constructor(public readonly status: number, message: string, public readonly payload: unknown) {
    super(message); this.name = "CpfApiError";
  }
}
const CLIENT_ACTOR_FIELDS = new Set(["requestUser", "requestedBy", "actorId", "operatorIdOverride"]);
function csrfToken(): string {
  const entry = document.cookie.split(";").map(value => value.trim()).find(value => value.startsWith("XSRF-TOKEN="));
  return entry ? decodeURIComponent(entry.substring("XSRF-TOKEN=".length)) : "";
}
export function createAdmHeaders(extraHeaders: HeadersInit = {}): Headers {
  const headers = new Headers(defaultHeaders);
  new Headers(extraHeaders).forEach((value, key) => headers.set(key, value));
  if (!isValidTransactionId(headers.get("X-Transaction-Id"))) headers.set("X-Transaction-Id", createTransactionId());
  const csrf = csrfToken(); if (csrf && !headers.has("X-XSRF-TOKEN")) headers.set("X-XSRF-TOKEN", csrf);
  if (headers.has("Authorization")) throw new Error("ADM BFF는 Browser Bearer Token을 허용하지 않습니다.");
  return headers;
}
function assertNoClientActor(value: unknown, path = "$", visited = new WeakSet<object>()): void {
  if (value === null || value === undefined || typeof value !== "object") return;
  if (visited.has(value as object)) return; visited.add(value as object);
  if (Array.isArray(value)) { value.forEach((item, index) => assertNoClientActor(item, `${path}[${index}]`, visited)); return; }
  for (const [key, child] of Object.entries(value as Record<string, unknown>)) {
    if (CLIENT_ACTOR_FIELDS.has(key)) throw new Error(`Browser actor field is forbidden: ${path}.${key}`);
    assertNoClientActor(child, `${path}.${key}`, visited);
  }
}
function convert(error: unknown): never {
  if (error instanceof CpfOrvalError) throw new CpfApiError(error.status, error.message, error.payload);
  throw error;
}
export async function admQuery<T = unknown>(url: string, params?: Record<string, unknown>): Promise<T> {
  const target = new URL(url, window.location.origin);
  Object.entries(params || {}).forEach(([key, value]) => {
    if (CLIENT_ACTOR_FIELDS.has(key)) throw new Error(`Browser actor query field is forbidden: ${key}`);
    if (value !== undefined && value !== null && String(value).trim() !== "") target.searchParams.set(key, String(value));
  });
  const relative = target.pathname + target.search;
  const operation = resolveCpfOperation("GET", relative);
  try {
    return await cpfQueryClient.fetchQuery<T>({
      queryKey: ["cpf", operation.operationId, target.pathname, target.search],
      queryFn: () => cpfOrvalRequest<T>({ url: relative, method: "GET", headers: createAdmHeaders({ "X-CPF-Operation-Id": operation.operationId }) })
    });
  } catch (error) { return convert(error); }
}
export async function admMutation<T = unknown>(url: string, method: "POST" | "PUT" | "PATCH" | "DELETE", body?: unknown): Promise<T> {
  assertNoClientActor(body);
  const target = new URL(url, window.location.origin);
  const relative = target.pathname + target.search;
  const operation = resolveCpfOperation(method, relative);
  const observer = new MutationObserver<T, unknown, unknown, unknown>(cpfQueryClient, {
    mutationKey: ["cpf", operation.operationId],
    mutationFn: () => cpfOrvalRequest<T>({ url: relative, method, headers: createAdmHeaders({ "Content-Type": "application/json", "X-CPF-Operation-Id": operation.operationId }), data: body })
  });
  try {
    const result = await observer.mutate(undefined);
    await cpfQueryClient.invalidateQueries({ queryKey: ["cpf"] });
    return result;
  } catch (error) { return convert(error); } finally { observer.reset(); }
}


export async function admRawResponse(
  url: string,
  method: "GET" | "POST" | "PUT" | "PATCH" | "DELETE" = "GET",
  body?: unknown,
  extraHeaders: HeadersInit = {}
): Promise<Response> {
  assertNoClientActor(body);
  const target = new URL(url, window.location.origin);
  if (target.origin !== window.location.origin) throw new Error("ADM download target must be same-origin");
  const operation = resolveCpfOperation(method, target.pathname + target.search);
  const headers = createAdmHeaders({ ...Object.fromEntries(new Headers(extraHeaders).entries()), "X-CPF-Operation-Id": operation.operationId });
  let requestBody: BodyInit | undefined;
  if (body !== undefined && body !== null) {
    if (typeof body === "string" || body instanceof FormData || body instanceof Blob || body instanceof URLSearchParams) requestBody = body;
    else { if (!headers.has("Content-Type")) headers.set("Content-Type", "application/json"); requestBody = JSON.stringify(body); }
  }
  return fetch(target.pathname + target.search, { method, headers, body: requestBody, credentials: "include", cache: "no-store", redirect: "error" });
}

export interface CpfGeneratedRequestConfig {
  url: string; method: string; headers?: HeadersInit; data?: unknown; params?: Record<string, unknown>; signal?: AbortSignal;
}
export async function cpfGeneratedRequest<T = unknown>(config: CpfGeneratedRequestConfig): Promise<T> {
  const method = config.method.trim().toUpperCase();
  const target = new URL(config.url, window.location.origin);
  Object.entries(config.params || {}).forEach(([key, value]) => {
    if (value !== undefined && value !== null) target.searchParams.set(key, String(value));
  });
  const relative = target.pathname + target.search;
  if (method === "GET") return admQuery<T>(relative);
  if (!["POST", "PUT", "PATCH", "DELETE"].includes(method)) throw new Error(`Unsupported generated ADM method: ${method}`);
  return admMutation<T>(relative, method as "POST" | "PUT" | "PATCH" | "DELETE", config.data);
}

export async function admApi<T = unknown>(url: string, options: RequestInit = {}): Promise<T> {
  const method = String(options.method || "GET").toUpperCase();
  if (method === "GET") return admQuery<T>(url);
  if (!["POST", "PUT", "PATCH", "DELETE"].includes(method)) throw new Error(`Unsupported ADM method: ${method}`);
  let body: unknown = options.body; if (typeof body === "string") { try { body = JSON.parse(body); } catch { /* keep string */ } }
  return admMutation<T>(url, method as "POST" | "PUT" | "PATCH" | "DELETE", body);
}
export const cpfApi = admApi;

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
export async function admInvokeOperation<T = unknown>(operationId: CpfOperationId, options: CpfOperationInvokeOptions = {}): Promise<T> {
  const descriptor=cpfOperationDescriptors.find(value=>value.operationId===operationId);
  if(!descriptor)throw new Error(`ADM operation is not registered: ${operationId}`);
  const target=new URL(renderOperationPath(descriptor.template,options.path),window.location.origin);
  Object.entries(options.query||{}).forEach(([key,value])=>{
    if(CLIENT_ACTOR_FIELDS.has(key))throw new Error(`Browser actor query field is forbidden: ${key}`);
    if(value!==undefined&&value!==null&&String(value).trim()!=="")target.searchParams.set(key,String(value));
  });
  const relative=target.pathname+target.search;
  if(descriptor.method==="GET")return admQuery<T>(relative);
  return admMutation<T>(relative,descriptor.method as "POST"|"PUT"|"PATCH"|"DELETE",options.body);
}
