export async function admApi<T = any>(url: string, options: RequestInit = {}): Promise<T> {
  const token = localStorage.getItem("admAccessToken") || "";
  const headers = new Headers(options.headers || {});
  if (!headers.has("Content-Type") && options.body) headers.set("Content-Type", "application/json");
  if (token) headers.set("Authorization", `Bearer ${token}`);
  if (!headers.has("X-Transaction-Id")) headers.set("X-Transaction-Id", `OADM-UI-${Date.now()}`);
  const response = await fetch(url, { ...options, headers });
  if (!response.ok) {
    const body = await response.text();
    throw new Error(body || `HTTP ${response.status}`);
  }
  if (response.status === 204) return undefined as T;
  return response.json() as Promise<T>;
}
