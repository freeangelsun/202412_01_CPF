import { createTransactionId, defaultHeaders, isValidTransactionId } from "./transaction";

export class CpfApiError extends Error {
  constructor(public readonly status: number, message: string, public readonly payload: unknown) {
    super(message); this.name = "CpfApiError";
  }
}

function csrfToken(): string {
  const entry = document.cookie.split(";").map(v => v.trim()).find(v => v.startsWith("XSRF-TOKEN="));
  return entry ? decodeURIComponent(entry.substring("XSRF-TOKEN=".length)) : "";
}

export function createAdmHeaders(extraHeaders: HeadersInit = {}): Headers {
  const headers = new Headers(defaultHeaders);
  new Headers(extraHeaders).forEach((value, key) => headers.set(key, value));
  if (!isValidTransactionId(headers.get("X-Transaction-Id"))) headers.set("X-Transaction-Id", createTransactionId());
  const csrf = csrfToken();
  if (csrf && !headers.has("X-XSRF-TOKEN")) headers.set("X-XSRF-TOKEN", csrf);
  if (headers.has("Authorization")) throw new Error("ADM BFF는 Browser Bearer Token을 허용하지 않습니다.");
  return headers;
}

export async function admApi<T = unknown>(url: string, options: RequestInit = {}): Promise<T> {
  const headers = createAdmHeaders(options.headers);
  if (!headers.has("Content-Type") && options.body) headers.set("Content-Type", "application/json");
  const response = await fetch(url, { ...options, headers, credentials: "include", cache: "no-store" });
  const text = await response.text();
  let payload: any = text;
  try { payload = text ? JSON.parse(text) : undefined; } catch { /* download/text response */ }
  if (!response.ok) throw new CpfApiError(response.status, payload?.message || text || `HTTP ${response.status}`, payload);
  return payload as T;
}
export const cpfApi = admApi;
