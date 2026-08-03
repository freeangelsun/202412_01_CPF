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

const CLIENT_ACTOR_FIELDS = new Set(["requestuser", "requestedby", "actorid", "operatorid", "operatoridoverride"]);
function actorKey(value: string): boolean { return CLIENT_ACTOR_FIELDS.has(value.trim().toLowerCase()); }
function allowTopLevelOperatorIdentity(_target: URL): boolean {
  // BZA는 운영자 자격증명 발급·관리 Owner가 아니다. Browser가 전달한 operatorId는 모든 경로에서 차단한다.
  return false;
}
function assertNoClientActor(
  value: unknown,
  path = "$",
  visited = new WeakSet<object>(),
  allowOperatorIdentity = false
): void {
  if (value === null || value === undefined) return;
  if (typeof value === "string") {
    const trimmed = value.trim();
    if (!trimmed.startsWith("{") && !trimmed.startsWith("[")) return;
    try { assertNoClientActor(JSON.parse(trimmed) as unknown, path, visited, allowOperatorIdentity); }
    catch (error) {
      if (error instanceof SyntaxError) throw new Error(`Malformed JSON actor payload is forbidden at ${path}`);
      throw error;
    }
    return;
  }
  if (typeof URLSearchParams !== "undefined" && value instanceof URLSearchParams) {
    for (const [key, child] of value.entries()) {
      if (actorKey(key)) throw new Error(`Browser actor field is forbidden: ${path}.${key}`);
      assertNoClientActor(child, `${path}.${key}`, visited, false);
    }
    return;
  }
  if (typeof FormData !== "undefined" && value instanceof FormData) {
    for (const [key, child] of value.entries()) {
      if (actorKey(key)) throw new Error(`Browser actor field is forbidden: ${path}.${key}`);
      if (typeof child === "string") assertNoClientActor(child, `${path}.${key}`, visited, false);
    }
    return;
  }
  if (typeof Blob !== "undefined" && value instanceof Blob) return;
  if (value instanceof ArrayBuffer || ArrayBuffer.isView(value) || typeof value !== "object") return;
  if (visited.has(value as object)) return;
  visited.add(value as object);
  if (Array.isArray(value)) {
    value.forEach((item, index) => assertNoClientActor(item, `${path}[${index}]`, visited, false));
    return;
  }
  for (const [key, child] of Object.entries(value as Record<string, unknown>)) {
    const topLevelOperatorIdentity = allowOperatorIdentity && path === "$" && key.trim().toLowerCase() === "operatorid";
    if (actorKey(key) && !topLevelOperatorIdentity) throw new Error(`Browser actor field is forbidden: ${path}.${key}`);
    assertNoClientActor(child, `${path}.${key}`, visited, false);
  }
}
function assertNoClientActorQuery(target: URL, params: Record<string, unknown> = {}): void {
  for (const key of target.searchParams.keys()) {
    if (actorKey(key)) throw new Error(`Browser actor query field is forbidden: ${key}`);
  }
  for (const [key, value] of Object.entries(params)) {
    if (actorKey(key)) throw new Error(`Browser actor query field is forbidden: ${key}`);
    assertNoClientActor(value, `$.query.${key}`);
  }
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
  assertNoClientActor(config.data, "$", new WeakSet<object>(), allowTopLevelOperatorIdentity(url));
  assertNoClientActorQuery(url, config.params || {});
  Object.entries(config.params || {}).forEach(([key, value]) => {
    if (value !== undefined && value !== null) url.searchParams.set(key, String(value));
  });

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
