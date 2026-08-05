export interface CpfOrvalRequestConfig {
  url: string;
  method: string;
  headers?: HeadersInit;
  data?: unknown;
  params?: Record<string, unknown>;
  signal?: AbortSignal;
}

export type CpfOrvalRequestOptions = Partial<Omit<CpfOrvalRequestConfig, "url">>;

/** Options exposed to generated operation callers. Method, body, and query shape are fixed by OpenAPI. */
export type CpfOrvalGeneratedRequestOptions = Pick<CpfOrvalRequestConfig, "headers" | "signal">;

export interface CpfOrvalResponse<T> {
  data: T;
  status: number;
  headers: Headers;
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

const CLIENT_ACTOR_FIELDS = new Set(["requestuser", "requestedby", "actorid", "operatorid", "operatoridoverride"]);
function actorKey(value: string): boolean { return CLIENT_ACTOR_FIELDS.has(value.trim().toLowerCase()); }
function allowTopLevelOperatorIdentity(target: URL): boolean {
  return target.pathname === "/adm/api/auth/login"
    || target.pathname === "/adm/api/operators"
    || target.pathname.startsWith("/adm/api/operators/");
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
    if (!trimmed.startsWith("{") && !trimmed.startsWith("[")) {
      if (path === "$") throw new Error("CPF privileged BFF raw string body is forbidden; send a typed JSON value");
      return;
    }
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
  if (typeof Blob !== "undefined" && value instanceof Blob) {
    throw new Error("CPF privileged BFF Blob body is forbidden; generated contracts are JSON-only");
  }
  if (value instanceof ArrayBuffer || ArrayBuffer.isView(value)) {
    throw new Error("CPF privileged BFF binary body is forbidden; generated contracts are JSON-only");
  }
  if (typeof value !== "object") return;
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
  if (contentType.includes("application/json") || contentType.includes("+json")) return response.json();
  if (contentType.startsWith("text/")) return response.text();
  return response.blob();
}

function normalizeRequest(
  configOrUrl: CpfOrvalRequestConfig | string,
  requestOptions?: CpfOrvalRequestOptions
): CpfOrvalRequestConfig {
  if (typeof configOrUrl !== "string") return configOrUrl;
  return {
    url: configOrUrl,
    method: requestOptions?.method || "GET",
    headers: requestOptions?.headers,
    data: requestOptions?.data,
    params: requestOptions?.params,
    signal: requestOptions?.signal
  };
}

export async function cpfOrvalRequest<T>(
  configOrUrl: CpfOrvalRequestConfig | string,
  requestOptions?: CpfOrvalRequestOptions
): Promise<T> {
  const config = normalizeRequest(configOrUrl, requestOptions);
  const method = config.method.trim().toUpperCase();
  if (!["GET", "POST", "PUT", "PATCH", "DELETE"].includes(method)) {
    throw new Error(`Unsupported CPF generated method: ${method || "<empty>"}`);
  }
  if (method === "GET" && config.data !== undefined && config.data !== null) {
    throw new Error("GET request body is forbidden by the generated OpenAPI contract");
  }
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

  const response = await fetch(url, {
    method,
    headers,
    body: requestBody(config.data, headers),
    signal: config.signal,
    credentials: "include",
    cache: "no-store",
    redirect: "error"
  });
  const payload = await responsePayload(response);
  if (!response.ok) {
    const message = typeof payload === "object" && payload && "message" in payload
      ? String((payload as { message?: unknown }).message || `HTTP ${response.status}`)
      : typeof payload === "string" && payload
        ? payload
        : `HTTP ${response.status}`;
    throw new CpfOrvalError(response.status, message, payload);
  }
  return {
    data: payload,
    status: response.status,
    headers: response.headers
  } as T;
}

export default cpfOrvalRequest;
