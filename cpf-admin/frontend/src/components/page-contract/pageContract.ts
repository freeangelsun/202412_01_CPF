import type { RouteLocationNormalizedLoaded } from "vue-router";
import type { AdmCapabilityRoute, AdmRouteRiskLevel } from "../../app/routes";

export type AdmHttpFailureStatus = 401 | 403 | 404 | 409 | 429 | 500 | 503;

export interface AdmCommercialPageContract {
  routeId: string;
  menuId: string;
  ownerModule: string;
  riskLevel: AdmRouteRiskLevel;
  featureFlag: string;
  expectedOperationIds: readonly string[];
  requiresSearch: boolean;
  requiresServerPaging: boolean;
  requiresDetail: boolean;
  requiresActionConfirmation: boolean;
  preservesContext: readonly string[];
  supportedFailureStatuses: readonly AdmHttpFailureStatus[];
}

const PAGED_GROUPS = new Set(["online", "batch", "integration", "monitoring", "framework"]);
const DETAIL_GROUPS = new Set(["online", "batch", "integration", "monitoring", "framework"]);
const FAILURE_STATUSES: readonly AdmHttpFailureStatus[] = [401, 403, 404, 409, 429, 500, 503] as const;
const CONTEXT_KEYS = [
  "transactionId", "executionId", "correlationId", "from", "to", "filter",
  "tenant", "environment", "instanceId", "serviceId"
] as const;

export function commercialPageContractFor(capability: AdmCapabilityRoute): AdmCommercialPageContract {
  return Object.freeze({
    routeId: capability.routeId,
    menuId: capability.menuId,
    ownerModule: capability.ownerModule,
    riskLevel: capability.riskLevel,
    featureFlag: capability.featureFlag,
    expectedOperationIds: capability.expectedOperationIds,
    requiresSearch: PAGED_GROUPS.has(capability.group),
    requiresServerPaging: PAGED_GROUPS.has(capability.group),
    requiresDetail: DETAIL_GROUPS.has(capability.group),
    requiresActionConfirmation: capability.riskLevel === "HIGH" || capability.riskLevel === "CRITICAL",
    preservesContext: CONTEXT_KEYS,
    supportedFailureStatuses: FAILURE_STATUSES
  });
}

export function preservedRouteContext(route: RouteLocationNormalizedLoaded): Record<string, string> {
  const context: Record<string, string> = {};
  for (const key of CONTEXT_KEYS) {
    const raw = route.query[key];
    const value = Array.isArray(raw) ? raw[0] : raw;
    if (typeof value === "string" && value.trim()) context[key] = value.trim();
  }
  return context;
}

export interface AdmFailurePresentation {
  status: AdmHttpFailureStatus;
  title: string;
  message: string;
  retryable: boolean;
}

export function failurePresentation(error: unknown): AdmFailurePresentation {
  const candidate = error as { status?: unknown; message?: unknown } | null;
  const rawStatus = Number(candidate?.status ?? 500);
  const status = (FAILURE_STATUSES.includes(rawStatus as AdmHttpFailureStatus) ? rawStatus : 500) as AdmHttpFailureStatus;
  const messages: Record<AdmHttpFailureStatus, Omit<AdmFailurePresentation, "status">> = {
    401: { title: "인증이 만료되었습니다.", message: "다시 로그인한 뒤 같은 조회 조건으로 재시도하세요.", retryable: false },
    403: { title: "권한이 없습니다.", message: "메뉴와 Operation 권한 또는 승인 상태를 확인하세요.", retryable: false },
    404: { title: "대상을 찾을 수 없습니다.", message: "식별자와 환경·Tenant 범위를 확인하세요.", retryable: false },
    409: { title: "동시 변경 충돌입니다.", message: "최신 상태를 다시 조회한 뒤 사유와 버전을 확인하세요.", retryable: true },
    429: { title: "요청이 제한되었습니다.", message: "Retry-After 또는 운영 정책에 따라 재시도하세요.", retryable: true },
    500: { title: "운영 요청 처리에 실패했습니다.", message: "Correlation ID와 감사 로그를 확인하세요.", retryable: true },
    503: { title: "운영 기능을 사용할 수 없습니다.", message: "Owner 서비스·Feature Flag·배포 상태를 확인하세요.", retryable: true }
  };
  const base = messages[status];
  const explicit = typeof candidate?.message === "string" ? candidate.message.trim() : "";
  return { status, title: base.title, message: explicit || base.message, retryable: base.retryable };
}

export function confirmRiskAction(contract: AdmCommercialPageContract, action: string, reason: string): boolean {
  if (!contract.requiresActionConfirmation) return true;
  const normalizedReason = reason.trim();
  if (normalizedReason.length < 5) throw new Error("위험조치 사유는 5자 이상 입력해야 합니다.");
  return window.confirm(`[${contract.riskLevel}] ${action}\n사유: ${normalizedReason}\n실행 후 결과와 Audit ID를 반드시 확인하세요.`);
}
