import { MutationObserver } from "@tanstack/vue-query";
import { CpfOrvalError, cpfOrvalRequest, type CpfOrvalResponse } from "./orval-mutator";
import { cpfOperationDescriptors, resolveCpfOperation, type CpfOperationId } from "../generated/cpf-operation-contract";
import { cpfQueryClient } from "./queryClient";
import { assertNoProtectedCpfHeaders, defaultHeaders } from "./clientHeaders";

export class CpfApiError extends Error {
  constructor(public readonly status: number, message: string, public readonly payload: unknown) {
    super(message); this.name = "CpfApiError";
  }
}
const CLIENT_ACTOR_FIELDS = new Set(["requestuser", "requestedby", "actorid", "operatorid", "operatoridoverride"]);
const SURFACE = "BZA";
function csrfToken(): string {
  const entry = document.cookie.split(";").map(value => value.trim()).find(value => value.startsWith("XSRF-TOKEN="));
  return entry ? decodeURIComponent(entry.substring("XSRF-TOKEN=".length)) : "";
}
export function createBzaHeaders(extraHeaders: HeadersInit = {}): Headers {
  const headers = new Headers(defaultHeaders);
  new Headers(extraHeaders).forEach((value, key) => headers.set(key, value));
  assertNoProtectedCpfHeaders(headers);
  const csrf = csrfToken(); if (csrf && !headers.has("X-XSRF-TOKEN")) headers.set("X-XSRF-TOKEN", csrf);
  if (headers.has("Authorization")) throw new Error("BZA BFF는 Browser Bearer Token을 허용하지 않습니다.");
  return headers;
}
function actorKey(value: string): boolean { return CLIENT_ACTOR_FIELDS.has(value.trim().toLowerCase()); }
function assertSameOrigin(target: URL): void {
  if (target.origin !== window.location.origin) throw new Error(`${SURFACE} BFF target must be same-origin`);
}
function assertNoClientActor(value: unknown, path = "$", visited = new WeakSet<object>()): void {
  if (value === null || value === undefined) return;
  if (typeof value === "string") {
    const trimmed = value.trim();
    if (!trimmed) return;
    const jsonLike = trimmed.startsWith("{") || trimmed.startsWith("[");
    if (path !== "$" && !jsonLike) return;
    try {
      const parsed = JSON.parse(trimmed) as unknown;
      if (parsed === null || typeof parsed !== "object") {
        if (path === "$") throw new Error(`${SURFACE} raw string body is forbidden; send a typed JSON object`);
        return;
      }
      assertNoClientActor(parsed, path, visited);
    } catch (error) {
      if (error instanceof SyntaxError) {
        const kind = path === "$" ? "raw string body" : `malformed JSON payload at ${path}`;
        throw new Error(`${SURFACE} ${kind} is forbidden; send a typed JSON object`);
      }
      throw error;
    }
    return;
  }
  if (typeof URLSearchParams !== "undefined" && value instanceof URLSearchParams) {
    for (const [key, child] of value.entries()) {
      if (actorKey(key)) throw new Error(`Browser actor field is forbidden: ${path}.${key}`);
      const trimmed = child.trim();
      if (trimmed.startsWith("{") || trimmed.startsWith("[")) assertNoClientActor(trimmed, `${path}.${key}`, visited);
    }
    return;
  }
  if (typeof FormData !== "undefined" && value instanceof FormData) {
    for (const [key, child] of value.entries()) {
      if (actorKey(key)) throw new Error(`Browser actor field is forbidden: ${path}.${key}`);
      if (typeof child === "string") {
        const trimmed = child.trim();
        if (trimmed.startsWith("{") || trimmed.startsWith("[")) assertNoClientActor(trimmed, `${path}.${key}`, visited);
      }
    }
    return;
  }
  if (typeof Blob !== "undefined" && value instanceof Blob) {
    throw new Error(`${SURFACE} Blob body is forbidden for privileged BFF mutations`);
  }
  if (typeof value !== "object") return;
  if (visited.has(value as object)) return;
  visited.add(value as object);
  if (Array.isArray(value)) { value.forEach((item, index) => assertNoClientActor(item, `${path}[${index}]`, visited)); return; }
  for (const [key, child] of Object.entries(value as Record<string, unknown>)) {
    if (actorKey(key)) throw new Error(`Browser actor field is forbidden: ${path}.${key}`);
    assertNoClientActor(child, `${path}.${key}`, visited);
  }
}
function assertNoClientActorQuery(target: URL): void {
  assertSameOrigin(target);
  for (const key of target.searchParams.keys()) {
    if (actorKey(key)) throw new Error(`Browser actor query field is forbidden: ${key}`);
  }
}
function convert(error: unknown): never {
  if (error instanceof CpfOrvalError) throw new CpfApiError(error.status, error.message, error.payload);
  throw error;
}
async function cpfOrvalPayload<T>(config: Parameters<typeof cpfOrvalRequest>[0]): Promise<T> {
  const response = await cpfOrvalRequest<CpfOrvalResponse<T>>(config);
  return response.data;
}
export async function bzaQuery<T = unknown>(url: string, params?: Record<string, unknown>): Promise<T> {
  const target = new URL(url, window.location.origin);
  assertNoClientActorQuery(target);
  Object.entries(params || {}).forEach(([key, value]) => {
    if (actorKey(key)) throw new Error(`Browser actor query field is forbidden: ${key}`);
    if (value !== undefined && value !== null && String(value).trim() !== "") target.searchParams.set(key, String(value));
  });
  const relative = target.pathname + target.search;
  const operation = resolveCpfOperation("GET", relative);
  try {
    return await cpfQueryClient.fetchQuery<T>({
      queryKey: ["cpf", operation.operationId, target.pathname, target.search],
      queryFn: () => cpfOrvalPayload<T>({ url: relative, method: "GET", headers: createBzaHeaders({}) })
    });
  } catch (error) { return convert(error); }
}
function isNativeMutationBody(value: unknown): value is FormData | URLSearchParams {
  return (typeof FormData !== "undefined" && value instanceof FormData)
    || (typeof URLSearchParams !== "undefined" && value instanceof URLSearchParams);
}

async function decodeMutationResponse<T>(response: Response): Promise<T> {
  const contentType = response.headers.get("content-type")?.toLowerCase() || "";
  let payload: unknown;
  if (response.status === 204) payload = undefined;
  else if (contentType.includes("application/json")) payload = await response.json();
  else {
    const text = await response.text();
    payload = text || undefined;
  }
  if (!response.ok) {
    const message = typeof payload === "object" && payload !== null && "message" in payload
      ? String((payload as { message?: unknown }).message || response.statusText)
      : String(payload || response.statusText || `HTTP ${response.status}`);
    throw new CpfApiError(response.status, message, payload);
  }
  return payload as T;
}

async function executeNativeMutation<T>(
  target: URL,
  method: "POST" | "PUT" | "PATCH" | "DELETE",
  body: FormData | URLSearchParams
): Promise<T> {
  const relative = target.pathname + target.search;
  const operation = resolveCpfOperation(method, relative);
  const headers = createBzaHeaders({});
  if (body instanceof URLSearchParams) headers.set("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
  const response = await fetch(relative, {
    method,
    headers,
    body,
    credentials: "include",
    cache: "no-store",
    redirect: "error"
  });
  return decodeMutationResponse<T>(response);
}

export async function bzaMutation<T = unknown>(url: string, method: "POST" | "PUT" | "PATCH" | "DELETE", body?: unknown): Promise<T> {
  assertNoClientActor(body);
  const target = new URL(url, window.location.origin);
  assertNoClientActorQuery(target);
  if (isNativeMutationBody(body)) {
    try {
      const result = await executeNativeMutation<T>(target, method, body);
      await cpfQueryClient.invalidateQueries({ queryKey: ["cpf"] });
      return result;
    } catch (error) { return convert(error); }
  }
  const relative = target.pathname + target.search;
  const operation = resolveCpfOperation(method, relative);
  const observer = new MutationObserver<T, unknown, unknown, unknown>(cpfQueryClient, {
    mutationKey: ["cpf", operation.operationId],
    mutationFn: () => cpfOrvalPayload<T>({ url: relative, method, headers: createBzaHeaders({ "Content-Type": "application/json" }), data: body })
  });
  try {
    const result = await observer.mutate(undefined);
    await cpfQueryClient.invalidateQueries({ queryKey: ["cpf"] });
    return result;
  } catch (error) { return convert(error); } finally { observer.reset(); }
}


export async function bzaRawResponse(
  url: string,
  method: "GET" | "POST" | "PUT" | "PATCH" | "DELETE" = "GET",
  body?: unknown,
  extraHeaders: HeadersInit = {}
): Promise<Response> {
  assertNoClientActor(body);
  const target = new URL(url, window.location.origin);
  assertNoClientActorQuery(target);
  assertSameOrigin(target);
  const operation = resolveCpfOperation(method, target.pathname + target.search);
  const headers = createBzaHeaders({ ...Object.fromEntries(new Headers(extraHeaders).entries()) });
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
  assertNoClientActorQuery(target);
  Object.entries(config.params || {}).forEach(([key, value]) => {
    if (actorKey(key)) throw new Error(`Browser actor query field is forbidden: ${key}`);
    if (value !== undefined && value !== null) target.searchParams.set(key, String(value));
  });
  assertNoClientActorQuery(target);
  const relative = target.pathname + target.search;
  if (method === "GET") return bzaQuery<T>(relative);
  if (!["POST", "PUT", "PATCH", "DELETE"].includes(method)) throw new Error(`Unsupported generated BZA method: ${method}`);
  return bzaMutation<T>(relative, method as "POST" | "PUT" | "PATCH" | "DELETE", config.data);
}

export async function bzaApi<T = unknown>(url: string, options: RequestInit = {}): Promise<T> {
  const method = String(options.method || "GET").toUpperCase();
  if (method === "GET") return bzaQuery<T>(url);
  if (!["POST", "PUT", "PATCH", "DELETE"].includes(method)) throw new Error(`Unsupported BZA method: ${method}`);
  let body: unknown = options.body; if (typeof body === "string") { try { body = JSON.parse(body); } catch { /* keep string */ } }
  return bzaMutation<T>(url, method as "POST" | "PUT" | "PATCH" | "DELETE", body);
}
export const cpfApi = bzaApi;

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
  assertNoClientActorQuery(target);
  Object.entries(options.query||{}).forEach(([key,value])=>{
    if(actorKey(key))throw new Error(`Browser actor query field is forbidden: ${key}`);
    if(value!==undefined&&value!==null&&String(value).trim()!=="")target.searchParams.set(key,String(value));
  });
  assertNoClientActorQuery(target);
  const relative=target.pathname+target.search;
  if(descriptor.method==="GET")return bzaQuery<T>(relative);
  return bzaMutation<T>(relative,descriptor.method as "POST"|"PUT"|"PATCH"|"DELETE",options.body);
}

/** @deprecated BZA consumers should use bza* exports. Kept for one migration window. */
export const createAdmHeaders = createBzaHeaders;
/** @deprecated */ export const admQuery = bzaQuery;
/** @deprecated */ export const admMutation = bzaMutation;
/** @deprecated */ export const admRawResponse = bzaRawResponse;
/** @deprecated */ export const admApi = bzaApi;
/** @deprecated */ export const admInvokeOperation = bzaInvokeOperation;
