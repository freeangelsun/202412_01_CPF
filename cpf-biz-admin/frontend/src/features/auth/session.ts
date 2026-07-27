import { computed, reactive } from "vue";
import { canonicalBzaMenuCode } from "../../shared/bzaPermissionManifest";

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

let refreshInFlight: Promise<void> | null = null;
let sessionGeneration = 0;
let pendingLoginOperationId: string | null = null;

function setTokens(result: Record<string, unknown>): void {
  bzaSession.accessToken = typeof result.accessToken === "string" && result.accessToken
    ? result.accessToken
    : null;
  bzaSession.refreshToken = typeof result.refreshToken === "string" && result.refreshToken
    ? result.refreshToken
    : null;
  if (bzaSession.accessToken) {
    sessionStorage.setItem("bza.accessToken", bzaSession.accessToken);
  } else {
    sessionStorage.removeItem("bza.accessToken");
  }
  if (bzaSession.refreshToken) {
    sessionStorage.setItem("bza.refreshToken", bzaSession.refreshToken);
  } else {
    sessionStorage.removeItem("bza.refreshToken");
  }
}

export function clearBzaSession(): void {
  sessionGeneration += 1;
  sessionStorage.removeItem("bza.accessToken");
  sessionStorage.removeItem("bza.refreshToken");
  bzaSession.accessToken = null;
  bzaSession.refreshToken = null;
  bzaSession.operator = null;
  bzaSession.message = "";
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

async function refreshBzaTokensSingleFlight(): Promise<void> {
  if (!bzaSession.refreshToken) {
    clearBzaSession();
    throw new Error("refresh token이 없습니다.");
  }
  if (!refreshInFlight) {
    const refreshToken = bzaSession.refreshToken;
    const refreshGeneration = sessionGeneration;
    refreshInFlight = raw<Record<string, unknown>>("/api/bza/auth/refresh", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ refreshToken })
    }).then(result => {
      if (refreshGeneration !== sessionGeneration) {
        throw new Error("이미 종료된 BZA 세션의 refresh 응답입니다.");
      }
      setTokens(result);
      if (!bzaSession.accessToken || !bzaSession.refreshToken) {
        throw new Error("token rotation 응답이 불완전합니다.");
      }
    }).catch(error => {
      if (refreshGeneration === sessionGeneration) {
        clearBzaSession();
      }
      throw error;
    }).finally(() => {
      refreshInFlight = null;
    });
  }
  await refreshInFlight;
}

export async function bzaApi<T = unknown>(url: string, options: RequestInit = {}, retry = true): Promise<T> {
  const headers = new Headers(options.headers || {});
  if (!(options.body instanceof FormData) && !headers.has("Content-Type")) headers.set("Content-Type", "application/json");
  const requestAccessToken = bzaSession.accessToken;
  if (requestAccessToken) headers.set("Authorization", `Bearer ${requestAccessToken}`);
  try {
    return await raw<T>(url, { ...options, headers });
  } catch (error) {
    const httpError = error as Error & { status?: number };
    if (httpError.status === 401 && retry && bzaSession.refreshToken) {
      if (requestAccessToken && bzaSession.accessToken && requestAccessToken !== bzaSession.accessToken) {
        return bzaApi<T>(url, options, false);
      }
      await refreshBzaTokensSingleFlight();
      return bzaApi<T>(url, options, false);
    }
    if (httpError.status === 401) clearBzaSession();
    throw error;
  }
}

export async function loginBza(loginId: string, password: string): Promise<void> {
  clearBzaSession();
  bzaSession.busy = true;
  bzaSession.message = "";
  const operationId = pendingLoginOperationId || crypto.randomUUID();
  pendingLoginOperationId = operationId;
  try {
    const result = await raw<Record<string, unknown>>("/api/bza/auth/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ loginId, password, operationId })
    });
    pendingLoginOperationId = null;
    setTokens(result);
    if (!bzaSession.accessToken || !bzaSession.refreshToken) {
      clearBzaSession();
      throw new Error("로그인 token 응답이 불완전합니다.");
    }
    bzaSession.operator = (result.operator || null) as BzaOperator | null;
    if (!bzaSession.operator) await loadBzaOperator();
  } catch (error) {
    const httpError = error as Error & { status?: number };
    // 4xx는 서버가 요청 결과를 확정한 것이므로 다음 시도는 새 operationId를 사용합니다.
    // 통신실패/5xx는 결과불명일 수 있어 동일 operationId를 재사용합니다.
    if (httpError.status && httpError.status >= 400 && httpError.status < 500) {
      pendingLoginOperationId = null;
    }
    throw error;
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
  return canonicalBzaMenuCode(value);
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
