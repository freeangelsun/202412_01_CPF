import { CpfOrvalError, cpfOrvalRequest } from "./orval-mutator";
import { createTransactionId, defaultHeaders, isValidTransactionId } from "./transaction";

export class CpfApiError extends Error {
  constructor(public readonly status: number, message: string, public readonly payload: unknown) {
    super(message);
    this.name = "CpfApiError";
  }
}

const CLIENT_ACTOR_FIELDS = new Set(["requestUser", "actorId", "operatorIdOverride"]);

function csrfToken(): string {
  const entry = document.cookie.split(";").map(value => value.trim()).find(value => value.startsWith("XSRF-TOKEN="));
  return entry ? decodeURIComponent(entry.substring("XSRF-TOKEN=".length)) : "";
}

export function createAdmHeaders(extraHeaders: HeadersInit = {}): Headers {
  const headers = new Headers(defaultHeaders);
  new Headers(extraHeaders).forEach((value, key) => headers.set(key, value));
  if (!isValidTransactionId(headers.get("X-Transaction-Id"))) {
    headers.set("X-Transaction-Id", createTransactionId());
  }
  const csrf = csrfToken();
  if (csrf && !headers.has("X-XSRF-TOKEN")) headers.set("X-XSRF-TOKEN", csrf);
  if (headers.has("Authorization")) throw new Error("ADM BFF는 Browser Bearer Token을 허용하지 않습니다.");
  return headers;
}

function assertNoClientActor(value: unknown, path = "$", visited = new WeakSet<object>()): void {
  if (value === null || value === undefined || typeof value !== "object") return;
  if (visited.has(value as object)) return;
  visited.add(value as object);
  if (Array.isArray(value)) {
    value.forEach((item, index) => assertNoClientActor(item, `${path}[${index}]`, visited));
    return;
  }
  for (const [key, child] of Object.entries(value as Record<string, unknown>)) {
    if (CLIENT_ACTOR_FIELDS.has(key)) {
      throw new Error(`Browser actor field is forbidden: ${path}.${key}`);
    }
    assertNoClientActor(child, `${path}.${key}`, visited);
  }
}

function decodeBody(body: BodyInit | null | undefined): unknown {
  if (typeof body !== "string") return body;
  try { return JSON.parse(body); } catch { return body; }
}

export async function admApi<T = unknown>(url: string, options: RequestInit = {}): Promise<T> {
  const data = decodeBody(options.body);
  assertNoClientActor(data);
  try {
    return await cpfOrvalRequest<T>({
      url,
      method: options.method || "GET",
      headers: createAdmHeaders(options.headers),
      data,
      signal: options.signal || undefined
    });
  } catch (error) {
    if (error instanceof CpfOrvalError) {
      throw new CpfApiError(error.status, error.message, error.payload);
    }
    throw error;
  }
}

export async function admQuery<T = unknown>(url: string, params?: Record<string, unknown>): Promise<T> {
  const target = new URL(url, window.location.origin);
  Object.entries(params || {}).forEach(([key, value]) => {
    if (CLIENT_ACTOR_FIELDS.has(key)) throw new Error(`Browser actor query field is forbidden: ${key}`);
    if (value !== undefined && value !== null && String(value).trim() !== "") {
      target.searchParams.set(key, String(value));
    }
  });
  return admApi<T>(target.pathname + target.search);
}

export async function admMutation<T = unknown>(
  url: string,
  method: "POST" | "PUT" | "PATCH" | "DELETE",
  body?: unknown
): Promise<T> {
  assertNoClientActor(body);
  return admApi<T>(url, {
    method,
    headers: { "Content-Type": "application/json" },
    body: body === undefined || body === null ? undefined : JSON.stringify(body)
  });
}

export const cpfApi = admApi;
