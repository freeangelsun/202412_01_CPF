import { admApprovalRequest, admServiceRegistryFindInstances } from "../generated/cpf-api";

export type ServiceInstanceAction = "DRAIN" | "DISABLE" | "RESUME";

export interface ServiceInstanceApprovalInput {
  serviceId: string;
  endpointCode: string;
  instanceId: string;
  action: ServiceInstanceAction | string;
  reason: string;
  expectedVersion?: number;
}

export interface ServiceInstanceApprovalResult {
  approvalRequestId: string;
  expectedVersion: number;
  raw: Record<string, unknown>;
}

function required(value: unknown, label: string): string {
  const text = String(value ?? "").trim();
  if (!text) throw new Error(`${label}을(를) 입력하세요.`);
  if (text.includes("@")) throw new Error(`${label}에는 @ 문자를 사용할 수 없습니다.`);
  return text;
}

function rowsOf(value: unknown): Array<Record<string, unknown>> {
  if (Array.isArray(value)) return value.filter((row): row is Record<string, unknown> => !!row && typeof row === "object");
  if (!value || typeof value !== "object") return [];
  const row = value as Record<string, unknown>;
  for (const key of ["instances", "items", "content", "rows"]) {
    if (Array.isArray(row[key])) return (row[key] as unknown[]).filter((item): item is Record<string, unknown> => !!item && typeof item === "object");
  }
  return [];
}

async function resolveVersion(input: ServiceInstanceApprovalInput): Promise<number> {
  if (Number.isInteger(input.expectedVersion) && Number(input.expectedVersion) >= 0) return Number(input.expectedVersion);
  const response = await admServiceRegistryFindInstances<unknown>({
    query: { serviceId: input.serviceId, endpointCode: input.endpointCode, limit: 1000 }
  });
  const matched = rowsOf(response).find(row => String(row.instanceId ?? "") === input.instanceId);
  const version = Number(matched?.version);
  if (!Number.isInteger(version) || version < 0) {
    throw new Error("Service Instance의 현재 version을 확인할 수 없습니다. 최신 상태를 다시 조회하세요.");
  }
  return version;
}

function requestIdOf(value: Record<string, unknown>): string {
  for (const key of ["requestId", "id", "approvalRequestId"]) {
    const candidate = value[key];
    if (candidate !== undefined && candidate !== null && String(candidate).trim()) return String(candidate).trim();
  }
  return "";
}

/**
 * Service Registry Instance의 고위험 상태 변경을 즉시 실행하지 않고 Approval Owner 경계에 요청합니다.
 * 요청 시점의 version을 target snapshot에 고정하여 승인 중 대상이 변경되면 Owner Adapter가 fail-fast 합니다.
 */
export async function requestServiceInstanceApproval(input: ServiceInstanceApprovalInput): Promise<ServiceInstanceApprovalResult> {
  const serviceId = required(input.serviceId, "Service ID");
  const endpointCode = required(input.endpointCode, "Endpoint Code");
  const instanceId = required(input.instanceId, "Instance ID");
  const reason = String(input.reason ?? "").trim();
  if (reason.length < 8) throw new Error("승인 요청 사유는 8자 이상 입력하세요.");
  const action = String(input.action ?? "").trim().toUpperCase() as ServiceInstanceAction;
  if (!["DRAIN", "DISABLE", "RESUME"].includes(action)) throw new Error(`지원하지 않는 Service Instance 조치입니다: ${input.action}`);
  const expectedVersion = await resolveVersion({ ...input, serviceId, endpointCode, instanceId, reason, action });
  const ownerCommand = `SERVICE_INSTANCE_${action}`;
  const targetId = `${serviceId}@${endpointCode}@${instanceId}@${expectedVersion}`;
  const payloadSnapshot = JSON.stringify({
    action,
    endpointCode,
    expectedVersion,
    instanceId,
    serviceId
  });
  const raw = await admApprovalRequest<Record<string, unknown>>({ data: {
    requestKey: `SR-${crypto.randomUUID()}`,
    actionType: ownerCommand,
    ownerModule: "CPF-INTEGRATION",
    ownerCommand,
    targetType: "SERVICE_INSTANCE",
    targetId,
    payloadSnapshot,
    reason
  }});
  return { approvalRequestId: requestIdOf(raw), expectedVersion, raw };
}

export type ServiceRegistryDeleteKind = "service" | "endpoint" | "instance";

/** Service Registry 삭제도 즉시 실행하지 않고 Approval Owner Command로 요청합니다. */
export async function requestServiceRegistryDeleteApproval(
  kind: ServiceRegistryDeleteKind,
  targetId: string,
  expectedVersion: number,
  reasonValue: string,
): Promise<ServiceInstanceApprovalResult> {
  const id = required(targetId, "삭제 대상 ID");
  if (!Number.isInteger(expectedVersion) || expectedVersion < 0) throw new Error("최신 대상 version을 확인한 뒤 다시 시도하세요.");
  const reason = String(reasonValue ?? "").trim();
  if (reason.length < 8) throw new Error("승인 요청 사유는 8자 이상 입력하세요.");
  const upper = kind.toUpperCase();
  const ownerCommand = `SERVICE_REGISTRY_${upper}_DELETE`;
  const targetType = `SERVICE_REGISTRY_${upper}`;
  const requestKey = `SR-DEL-${crypto.randomUUID()}`;
  const raw = await admApprovalRequest<Record<string, unknown>>({ data: {
    requestKey,
    actionType: ownerCommand,
    ownerModule: "CPF-INTEGRATION",
    ownerCommand,
    targetType,
    targetId: `${id}@${expectedVersion}`,
    payloadSnapshot: JSON.stringify({ kind, targetId: id, expectedVersion }),
    reason,
  }});
  return { approvalRequestId: requestIdOf(raw), expectedVersion, raw };
}
