export type MaintenanceActionType = "DRAIN" | "DISABLE" | "RESUME";
export interface MaintenanceAction { serviceId: string; endpointCode: string; instanceId: string; action: MaintenanceActionType; reason: string; }
const ACTIONS = new Set<MaintenanceActionType>(["DRAIN", "DISABLE", "RESUME"]);
function required(value: unknown, label: string): string { const text = String(value ?? "").trim(); if (!text) throw new Error(`${label}을(를) 입력하세요.`); return text; }
export function validateMaintenanceAction(value: MaintenanceAction): MaintenanceAction {
  const reason = required(value.reason, "감사 사유");
  if (reason.length < 5) throw new Error("감사 사유는 5자 이상 입력하세요.");
  if (!ACTIONS.has(value.action)) throw new Error(`지원하지 않는 점검 명령입니다: ${value.action}`);
  return { serviceId: required(value.serviceId, "Service"), endpointCode: required(value.endpointCode, "Endpoint"), instanceId: required(value.instanceId, "Instance"), action: value.action, reason };
}
