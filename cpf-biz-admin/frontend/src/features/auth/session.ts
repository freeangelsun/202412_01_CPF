import { computed, reactive } from "vue";

export interface BzaOperator {
  loginId?: string;
  operatorName?: string;
  menus?: string[];
  buttons?: string[];
  passwordChangeRequiredYn?: string;
  [key: string]: unknown;
}

interface SessionState {
  accessToken: string | null;
  refreshToken: string | null;
  operator: BzaOperator | null;
  busy: boolean;
  message: string;
}

export const bzaSession = reactive<SessionState>({
  accessToken: sessionStorage.getItem("bza.accessToken"),
  refreshToken: sessionStorage.getItem("bza.refreshToken"),
  operator: null,
  busy: false,
  message: ""
});

export const authenticated = computed(() => Boolean(bzaSession.accessToken && bzaSession.operator));

function setTokens(result: Record<string, unknown>): void {
  bzaSession.accessToken = typeof result.accessToken === "string" ? result.accessToken : null;
  if (typeof result.refreshToken === "string" && result.refreshToken) {
    bzaSession.refreshToken = result.refreshToken;
  }
  if (bzaSession.accessToken) sessionStorage.setItem("bza.accessToken", bzaSession.accessToken);
  if (bzaSession.refreshToken) sessionStorage.setItem("bza.refreshToken", bzaSession.refreshToken);
}

export function clearBzaSession(): void {
  sessionStorage.removeItem("bza.accessToken");
  sessionStorage.removeItem("bza.refreshToken");
  bzaSession.accessToken = null;
  bzaSession.refreshToken = null;
  bzaSession.operator = null;
}

async function raw<T>(url: string, options: RequestInit = {}): Promise<T> {
  const response = await fetch(url, options);
  const text = await response.text();
  let data: unknown = {};
  try {
    data = text ? JSON.parse(text) : {};
  } catch {
    data = { message: text };
  }
  if (!response.ok) {
    const message = typeof data === "object" && data && "message" in data ? String((data as { message?: unknown }).message ?? "") : "";
    const error = new Error(message || `요청 실패 (${response.status})`) as Error & { status?: number };
    error.status = response.status;
    throw error;
  }
  return data as T;
}

export async function bzaApi<T = unknown>(url: string, options: RequestInit = {}, retry = true): Promise<T> {
  const headers = new Headers(options.headers || {});
  if (!(options.body instanceof FormData) && !headers.has("Content-Type")) headers.set("Content-Type", "application/json");
  if (bzaSession.accessToken) headers.set("Authorization", `Bearer ${bzaSession.accessToken}`);
  try {
    return await raw<T>(url, { ...options, headers });
  } catch (error) {
    const httpError = error as Error & { status?: number };
    if (httpError.status === 401 && retry && bzaSession.refreshToken) {
      const refreshed = await raw<Record<string, unknown>>("/api/bza/auth/refresh", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ refreshToken: bzaSession.refreshToken })
      });
      setTokens(refreshed);
      return bzaApi<T>(url, options, false);
    }
    throw error;
  }
}

export async function loginBza(loginId: string, password: string): Promise<void> {
  bzaSession.busy = true;
  bzaSession.message = "";
  try {
    const result = await raw<Record<string, unknown>>("/api/bza/auth/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ loginId, password })
    });
    setTokens(result);
    bzaSession.operator = (result.operator || null) as BzaOperator | null;
    if (!bzaSession.operator) await loadBzaOperator();
  } finally {
    bzaSession.busy = false;
  }
}

export async function loadBzaOperator(): Promise<void> {
  bzaSession.operator = await bzaApi<BzaOperator>("/api/bza/auth/me");
}

export async function restoreBzaSession(): Promise<void> {
  if (!bzaSession.accessToken) return;
  try {
    await loadBzaOperator();
  } catch {
    clearBzaSession();
  }
}

export async function logoutBza(): Promise<void> {
  try {
    if (bzaSession.accessToken) {
      await bzaApi("/api/bza/auth/logout", {
        method: "POST",
        body: JSON.stringify({ refreshToken: bzaSession.refreshToken })
      });
    }
  } finally {
    clearBzaSession();
  }
}

export async function changeBzaPassword(currentPassword: string, newPassword: string, newPasswordConfirm: string): Promise<void> {
  await bzaApi("/api/bza/auth/password/change", {
    method: "POST",
    body: JSON.stringify({ currentPassword, newPassword, newPasswordConfirm })
  });
  clearBzaSession();
}

function normalizeBzaMenuCode(value: string): string {
  const normalized = value.trim().toUpperCase();
  return normalized.startsWith("BZA_") ? normalized.substring(4) : normalized;
}

export function hasBzaMenu(menuCode: string): boolean {
  const required = normalizeBzaMenuCode(menuCode);
  if (required === "DASHBOARD") return true;
  return (bzaSession.operator?.menus || []).some(value => normalizeBzaMenuCode(String(value)) === required);
}

export function hasBzaPermission(menuCode: string, actionCode: string): boolean {
  const requiredMenu = normalizeBzaMenuCode(menuCode);
  const requiredAction = actionCode.trim().toUpperCase();
  return (bzaSession.operator?.buttons || []).some(value => {
    const [storedMenu = "", storedAction = ""] = String(value).split(":", 2);
    return normalizeBzaMenuCode(storedMenu) === requiredMenu
      && (storedAction.toUpperCase() === requiredAction || storedAction.toUpperCase() === "ALL");
  });
}
