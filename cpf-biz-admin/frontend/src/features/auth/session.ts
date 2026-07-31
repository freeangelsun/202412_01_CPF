import { computed, reactive } from "vue";
import { canonicalBzaMenuCode } from "../../shared/bzaPermissionManifest";

export interface BzaOperator {
  loginId?: string; operatorName?: string; menus?: string[]; buttons?: string[];
  passwordChangeRequiredYn?: string; [key: string]: unknown;
}
interface SessionState { operator: BzaOperator | null; loaded: boolean; busy: boolean; message: string; }
export const bzaSession = reactive<SessionState>({ operator: null, loaded: false, busy: false, message: "" });
export const authenticated = computed(() => Boolean(bzaSession.loaded && bzaSession.operator?.loginId));
let pendingLoginOperationId: string | null = null;

function csrfToken(): string {
  const entry = document.cookie.split(";").map(v => v.trim()).find(v => v.startsWith("XSRF-TOKEN="));
  return entry ? decodeURIComponent(entry.substring("XSRF-TOKEN=".length)) : "";
}
export function clearBzaSession(): void { bzaSession.operator = null; bzaSession.loaded = false; bzaSession.message = ""; }
async function raw<T>(url: string, options: RequestInit = {}): Promise<T> {
  const headers = new Headers(options.headers || {});
  if (!(options.body instanceof FormData) && options.body && !headers.has("Content-Type")) headers.set("Content-Type", "application/json");
  const csrf = csrfToken(); if (csrf) headers.set("X-XSRF-TOKEN", csrf);
  if (headers.has("Authorization")) throw new Error("BZA BFF는 Browser Bearer Token을 허용하지 않습니다.");
  const response = await fetch(url, { ...options, headers, credentials: "include", cache: "no-store" });
  const text = await response.text(); let data: any = {};
  try { data = text ? JSON.parse(text) : {}; } catch { data = { message: text }; }
  if (!response.ok) { const error = new Error(data?.message || `요청 실패 (${response.status})`) as Error & { status?: number }; error.status = response.status; throw error; }
  return data as T;
}
export async function bzaApi<T = unknown>(url: string, options: RequestInit = {}): Promise<T> {
  try { return await raw<T>(url, options); }
  catch (error) { if ((error as any)?.status === 401) clearBzaSession(); throw error; }
}
export async function loginBza(loginId: string, password: string): Promise<void> {
  clearBzaSession(); bzaSession.busy = true;
  const operationId = pendingLoginOperationId || crypto.randomUUID(); pendingLoginOperationId = operationId;
  try {
    const result = await raw<{ operator?: BzaOperator }>("/api/bza/auth/login", { method: "POST", body: JSON.stringify({ loginId, password, operationId }) });
    if (!result.operator?.loginId) throw new Error("서버 세션이 생성되지 않았습니다.");
    pendingLoginOperationId = null; bzaSession.operator = result.operator; bzaSession.loaded = true;
  } catch (error) {
    const status = (error as any)?.status; if (status && status >= 400 && status < 500) pendingLoginOperationId = null; throw error;
  } finally { bzaSession.busy = false; }
}
export async function loadBzaOperator(): Promise<void> { bzaSession.operator = await bzaApi<BzaOperator>("/api/bza/auth/me"); bzaSession.loaded = true; }
export async function restoreBzaSession(): Promise<void> { try { await loadBzaOperator(); } catch { clearBzaSession(); } }
export async function logoutBza(): Promise<void> { try { await bzaApi("/api/bza/auth/logout", { method: "POST" }); } finally { clearBzaSession(); } }
export async function changeBzaPassword(currentPassword: string, newPassword: string, newPasswordConfirm: string): Promise<void> {
  await bzaApi("/api/bza/auth/password/change", { method: "POST", body: JSON.stringify({ currentPassword, newPassword, newPasswordConfirm }) });
  clearBzaSession();
}
function normalizeBzaMenuCode(value: string): string { return canonicalBzaMenuCode(value); }
export function hasBzaMenu(menuCode: string): boolean {
  const required = normalizeBzaMenuCode(menuCode); if (required === "DASHBOARD") return true;
  return (bzaSession.operator?.menus || []).some(value => normalizeBzaMenuCode(String(value)) === required);
}
export function hasBzaPermission(menuCode: string, actionCode: string): boolean {
  const requiredMenu = normalizeBzaMenuCode(menuCode), requiredAction = actionCode.trim().toUpperCase();
  return (bzaSession.operator?.buttons || []).some(value => { const [m="",a=""] = String(value).split(":",2); return normalizeBzaMenuCode(m) === requiredMenu && [requiredAction,"ALL"].includes(a.toUpperCase()); });
}
