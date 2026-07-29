import { createTransactionId, defaultHeaders, isValidTransactionId } from "./transaction";

export const ADM_ACCESS_TOKEN_STORAGE_KEY = "admAccessToken";

export class CpfApiError extends Error {
  status: number;
  payload: any;

  constructor(status: number, message: string, payload: any) {
    super(message);
    this.name = "CpfApiError";
    this.status = status;
    this.payload = payload;
  }
}

export function getAdmAccessToken(): string {
  const token = typeof sessionStorage === "undefined"
    ? ""
    : sessionStorage.getItem(ADM_ACCESS_TOKEN_STORAGE_KEY) || "";
  removeLegacyPersistentToken();
  return token;
}

export function setAdmAccessToken(token: string): void {
  if (typeof sessionStorage !== "undefined") {
    const normalized = token.trim();
    if (normalized) sessionStorage.setItem(ADM_ACCESS_TOKEN_STORAGE_KEY, normalized);
    else sessionStorage.removeItem(ADM_ACCESS_TOKEN_STORAGE_KEY);
  }
  removeLegacyPersistentToken();
}

export function clearAdmAccessToken(): void {
  if (typeof sessionStorage !== "undefined") {
    sessionStorage.removeItem(ADM_ACCESS_TOKEN_STORAGE_KEY);
  }
  removeLegacyPersistentToken();
}

/**
 * ADM의 Options API mixin과 Composition API 화면이 공유하는 유일한 요청 Header 조립기입니다.
 */
export function createAdmHeaders(
  extraHeaders: HeadersInit = {},
  explicitToken?: string
): Headers {
  const headers = new Headers(defaultHeaders);
  new Headers(extraHeaders).forEach((value, key) => headers.set(key, value));

  const token = explicitToken === undefined ? getAdmAccessToken() : explicitToken.trim();
  if (token && !headers.has("Authorization")) {
    headers.set("Authorization", `Bearer ${token}`);
  }

  if (!isValidTransactionId(headers.get("X-Transaction-Id"))) {
    headers.set("X-Transaction-Id", createTransactionId());
  }
  return headers;
}

export async function admApi<T = any>(url: string, options: RequestInit = {}): Promise<T> {
  const headers = createAdmHeaders(options.headers);
  if (!headers.has("Content-Type") && options.body) {
    headers.set("Content-Type", "application/json");
  }

  const response = await fetch(url, { ...options, headers });
  const text = await response.text();
  let payload: any = text;
  try {
    payload = text ? JSON.parse(text) : undefined;
  } catch {
    // JSON이 아닌 다운로드/진단 응답은 원문을 유지합니다.
  }
  if (!response.ok) {
    throw new CpfApiError(
      response.status,
      payload?.message || text || `HTTP ${response.status}`,
      payload
    );
  }
  return payload as T;
}

export const cpfApi = admApi;

function removeLegacyPersistentToken(): void {
  if (typeof localStorage !== "undefined") {
    localStorage.removeItem(ADM_ACCESS_TOKEN_STORAGE_KEY);
  }
}
