import { computed, reactive } from "vue";
import { canonicalBzaMenuCode } from "../../shared/bzaPermissionManifest";
import { bzaApi } from "../../shared/cpfApi";
import {
  getBzaAuthMe,
  postBzaAuthLogin,
  postBzaAuthLogout,
  postBzaPasswordChange
} from "../../generated/cpf-api";

export interface BzaOperator {
  loginId?: string; operatorName?: string; menus?: string[]; buttons?: string[];
  passwordChangeRequiredYn?: string; [key: string]: unknown;
}
interface SessionState { operator: BzaOperator | null; loaded: boolean; busy: boolean; message: string; }
export const bzaSession = reactive<SessionState>({ operator: null, loaded: false, busy: false, message: "" });
export const authenticated = computed(() => Boolean(bzaSession.loaded && bzaSession.operator?.loginId));
let pendingLoginOperationId: string | null = null;

export function clearBzaSession(): void { bzaSession.operator = null; bzaSession.loaded = false; bzaSession.message = ""; }

export { bzaApi };

export async function loginBza(loginId: string, password: string): Promise<void> {
  clearBzaSession(); bzaSession.busy = true;
  const operationId = pendingLoginOperationId || crypto.randomUUID(); pendingLoginOperationId = operationId;
  try {
    const result = await postBzaAuthLogin<{ operator?: BzaOperator }>({ data: { loginId, password, operationId } });
    if (!result.operator?.loginId) throw new Error("서버 세션이 생성되지 않았습니다.");
    pendingLoginOperationId = null; bzaSession.operator = result.operator; bzaSession.loaded = true;
  } catch (error) {
    const status = (error as any)?.status; if (status && status >= 400 && status < 500) pendingLoginOperationId = null; throw error;
  } finally { bzaSession.busy = false; }
}
export async function loadBzaOperator(): Promise<void> { bzaSession.operator = await getBzaAuthMe<BzaOperator>(); bzaSession.loaded = true; }
export async function restoreBzaSession(): Promise<void> { try { await loadBzaOperator(); } catch { clearBzaSession(); } }
export async function logoutBza(): Promise<void> { try { await postBzaAuthLogout(); } finally { clearBzaSession(); } }
export async function changeBzaPassword(currentPassword: string, newPassword: string, newPasswordConfirm: string): Promise<void> {
  await postBzaPasswordChange({ data: { currentPassword, newPassword, newPasswordConfirm } });
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
