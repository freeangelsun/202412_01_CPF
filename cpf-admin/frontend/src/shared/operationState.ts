import { CpfApiError } from "./cpfApi";

export type AdmFailureKind =
  | "unauthenticated"
  | "forbidden"
  | "not-found"
  | "conflict"
  | "rate-limited"
  | "partial"
  | "unavailable"
  | "server-error"
  | "network"
  | "unknown";

export interface AdmFailureState {
  kind: AdmFailureKind;
  status: number;
  title: string;
  message: string;
  correlationId: string;
  retryable: boolean;
  payload?: unknown;
}

function stringValue(value: unknown): string {
  return typeof value === "string" ? value.trim() : "";
}

export function correlationIdOf(payload: unknown): string {
  if (!payload || typeof payload !== "object") return "";
  const row = payload as Record<string, unknown>;
  return stringValue(row.correlationId) || stringValue(row.transactionId) || stringValue(row.traceId);
}

export function classifyAdmFailure(error: unknown): AdmFailureState {
  if (error instanceof CpfApiError) {
    const payload = error.payload;
    const correlationId = correlationIdOf(payload);
    const serverMessage = payload && typeof payload === "object"
      ? stringValue((payload as Record<string, unknown>).message) || stringValue((payload as Record<string, unknown>).detail)
      : "";
    const common = { status: error.status, correlationId, payload };
    switch (error.status) {
      case 401: return { ...common, kind: "unauthenticated", title: "세션이 만료되었습니다.", message: serverMessage || "다시 로그인한 뒤 작업을 재개하세요.", retryable: false };
      case 403: return { ...common, kind: "forbidden", title: "권한이 없습니다.", message: serverMessage || "메뉴·버튼 권한과 데이터 범위를 확인하세요.", retryable: false };
      case 404: return { ...common, kind: "not-found", title: "대상을 찾을 수 없습니다.", message: serverMessage || "이미 변경되었거나 삭제된 대상인지 확인하세요.", retryable: false };
      case 409: return { ...common, kind: "conflict", title: "동시 변경 충돌입니다.", message: serverMessage || "최신 상태를 다시 조회한 뒤 재시도하세요.", retryable: true };
      case 429: return { ...common, kind: "rate-limited", title: "요청이 제한되었습니다.", message: serverMessage || "잠시 후 재시도하세요.", retryable: true };
      case 503: return { ...common, kind: "unavailable", title: "Owner Runtime을 사용할 수 없습니다.", message: serverMessage || "부분 실패·연결 상태·재시도 가능 여부를 확인하세요.", retryable: true };
      default:
        if (error.status >= 500) return { ...common, kind: "server-error", title: "서버 처리 중 오류가 발생했습니다.", message: serverMessage || error.message, retryable: true };
        return { ...common, kind: "unknown", title: "요청을 처리하지 못했습니다.", message: serverMessage || error.message, retryable: false };
    }
  }
  if (error instanceof TypeError) return { kind: "network", status: 0, title: "네트워크 연결을 확인하세요.", message: error.message, correlationId: "", retryable: true };
  return { kind: "unknown", status: 0, title: "예상하지 못한 오류가 발생했습니다.", message: error instanceof Error ? error.message : String(error), correlationId: "", retryable: false };
}

export function maskOperationalValue(value: unknown): string {
  const text = value === undefined || value === null ? "" : String(value);
  if (!text) return "-";
  if (/password|secret|token|authorization|credential/i.test(text)) return "***";
  if (/^[^@\s]+@[^@\s]+$/.test(text)) {
    const [local, domain] = text.split("@");
    return `${local.slice(0, 2)}***@${domain}`;
  }
  if (/^\d{2,3}-?\d{3,4}-?\d{4}$/.test(text)) return text.replace(/\d(?=\d{4})/g, "*");
  return text;
}
