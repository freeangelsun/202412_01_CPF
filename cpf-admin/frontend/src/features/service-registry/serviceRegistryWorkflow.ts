export type ServiceRegistryTargetKind = "service" | "endpoint" | "instance";

export type RegistryDeleteOperation =
  | "admServiceRegistryDeleteService"
  | "admServiceRegistryDeleteEndpoint"
  | "admServiceRegistryDeleteInstance";

export function operationForRegistryTarget(kind: ServiceRegistryTargetKind): RegistryDeleteOperation {
  if (kind === "service") return "admServiceRegistryDeleteService";
  if (kind === "endpoint") return "admServiceRegistryDeleteEndpoint";
  return "admServiceRegistryDeleteInstance";
}

export interface RegistryDeleteRequest { kind: ServiceRegistryTargetKind; targetId: string; expectedVersion: number; reason: string; }
export type RegistryDeleteValidation = { ok: true } | { ok: false; message: string };

export function validateRegistryDeleteRequest(request: RegistryDeleteRequest): RegistryDeleteValidation {
  if (!request.targetId.trim()) return { ok: false, message: "삭제 대상 ID가 필요합니다." };
  if (!Number.isInteger(request.expectedVersion) || request.expectedVersion < 0) return { ok: false, message: "유효한 Version이 필요합니다." };
  if (request.reason.trim().length < 5) return { ok: false, message: "5자 이상의 삭제 사유가 필요합니다." };
  return { ok: true };
}
