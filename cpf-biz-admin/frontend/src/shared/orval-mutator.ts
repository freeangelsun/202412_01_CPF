export interface CpfOrvalRequestConfig {
  url: string;
  method: string;
  headers?: HeadersInit;
  data?: unknown;
  params?: Record<string, unknown>;
  signal?: AbortSignal;
}

export class CpfOrvalError extends Error {
  constructor(
    public readonly status: number,
    message: string,
    public readonly payload: unknown
  ) {
    super(message);
    this.name = "CpfOrvalError";
  }
}

export interface CpfBffSessionRecovery {
  epoch(): number;
  refresh(expectedEpoch: number): Promise<void>;
  clear(expectedEpoch: number): void;
}

let bffSessionRecovery: CpfBffSessionRecovery | null = null;
const refreshFlights = new Map<number, Promise<void>>();

/** Auth bootstrap이 소유한 서버 세션 회복 계약을 canonical HTTP mutator에 연결합니다. */
export function configureCpfBffSessionRecovery(recovery: CpfBffSessionRecovery): void {
  bffSessionRecovery = recovery;
  refreshFlights.clear();
}

function csrfToken(): string {
  const entry = document.cookie
    .split(";")
    .map(value => value.trim())
    .find(value => value.startsWith("XSRF-TOKEN="));
  return entry ? decodeURIComponent(entry.substring("XSRF-TOKEN=".length)) : "";
}

function requestBody(data: unknown, headers: Headers): BodyInit | undefined {
  if (data === undefined || data === null) return undefined;
  if (
    typeof data === "string" ||
    data instanceof FormData ||
    data instanceof Blob ||
    data instanceof URLSearchParams ||
    data instanceof ArrayBuffer ||
    ArrayBuffer.isView(data)
  ) {
    return data as BodyInit;
  }
  if (!headers.has("Content-Type")) headers.set("Content-Type", "application/json");
  return JSON.stringify(data);
}

async function responsePayload(response: Response): Promise<unknown> {
  if (response.status === 204) return undefined;
  const contentType = response.headers.get("Content-Type") || "";
  if (contentType.includes("application/json")) return response.json();
  if (contentType.startsWith("text/")) return response.text();
  return response.blob();
}

function isCredentialIssueRequest(pathname: string): boolean {
  return pathname.endsWith("/auth/login")
    || pathname.endsWith("/auth/refresh")
    || pathname.endsWith("/auth/logout");
}

async function discard(response: Response): Promise<void> {
  if (response.body) await response.body.cancel();
}

function refreshOnce(recovery: CpfBffSessionRecovery, epoch: number): Promise<void> {
  const existing = refreshFlights.get(epoch);
  if (existing) return existing;
  const created = recovery.refresh(epoch).finally(() => {
    if (refreshFlights.get(epoch) === created) refreshFlights.delete(epoch);
  });
  refreshFlights.set(epoch, created);
  return created;
}

function normalizeRequest(
  configOrUrl: CpfOrvalRequestConfig | string,
  requestOptions?: RequestInit
): CpfOrvalRequestConfig {
  if (typeof configOrUrl !== "string") return configOrUrl;
  return {
    url: configOrUrl,
    method: requestOptions?.method || "GET",
    headers: requestOptions?.headers,
    data: requestOptions?.body ?? undefined,
    signal: requestOptions?.signal ?? undefined
  };
}

export async function cpfOrvalRequest<T>(
  configOrUrl: CpfOrvalRequestConfig | string,
  requestOptions?: RequestInit
): Promise<T> {
  const config = normalizeRequest(configOrUrl, requestOptions);
  const url = new URL(config.url, window.location.origin);
  if (url.origin !== window.location.origin) throw new Error("CPF BFF request must be same-origin");
  Object.entries(config.params || {}).forEach(([key, value]) => {
    if (value !== undefined && value !== null) url.searchParams.set(key, String(value));
  });
  url.searchParams.delete("requestUser");
  url.searchParams.delete("actorId");

  const headers = new Headers(config.headers);
  const csrf = csrfToken();
  if (csrf && !headers.has("X-XSRF-TOKEN")) headers.set("X-XSRF-TOKEN", csrf);
  if (headers.has("Authorization")) throw new Error("Browser Bearer Token 금지");

  const body = requestBody(config.data, headers);
  const execute = () => fetch(url, {
      method: config.method,
      headers,
      body,
      signal: config.signal,
      credentials: "include",
      cache: "no-store",
      redirect: "error"
    });

  let response = await execute();
  const recovery = bffSessionRecovery;
  const canRecover = response.status === 401
    && recovery !== null
    && !isCredentialIssueRequest(url.pathname);
  if (canRecover) {
    const epoch = recovery.epoch();
    await discard(response);
    try {
      await refreshOnce(recovery, epoch);
    } catch (failure) {
      recovery.clear(epoch);
      throw failure;
    }
    if (recovery.epoch() !== epoch) {
      throw new Error("이미 종료된 BZA 세션의 refresh 응답은 적용할 수 없습니다.");
    }
    response = await execute();
    if (response.status === 401) recovery.clear(epoch);
  }
  const payload = await responsePayload(response);
  if (!response.ok) {
    const message = typeof payload === "object" && payload && "message" in payload
      ? String((payload as { message?: unknown }).message || `HTTP ${response.status}`)
      : typeof payload === "string" && payload
        ? payload
        : `HTTP ${response.status}`;
    throw new CpfOrvalError(response.status, message, payload);
  }
  return payload as T;
}

export default cpfOrvalRequest;
