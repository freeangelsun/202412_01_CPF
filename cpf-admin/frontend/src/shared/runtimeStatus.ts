export type CpfRuntimeStatus = "ACTIVE" | "DEGRADED" | "OFFLINE" | "UNKNOWN" | "MAINTENANCE";

/**
 * ADM 전체에서 Runtime/Server 상태를 동일 의미로 표시하기 위한 정규화 함수입니다.
 * UNKNOWN을 OFFLINE으로 단정하지 않고, stale/freshness가 불명확하면 UNKNOWN을 유지합니다.
 */
export function normalizeRuntimeStatus(value: unknown, stale = false): CpfRuntimeStatus {
  if (stale) return "UNKNOWN";
  const state = String(value ?? "").trim().toUpperCase();
  if (["UP", "READY", "HEALTHY", "OK", "ONLINE", "ACTIVE", "RUNNING", "AVAILABLE", "PASS"].includes(state)) return "ACTIVE";
  if (["DEGRADED", "WARN", "WARNING", "PARTIAL"].includes(state)) return "DEGRADED";
  if (["DOWN", "OFFLINE", "STOPPED", "FAILED", "UNAVAILABLE"].includes(state)) return "OFFLINE";
  if (["MAINTENANCE", "DRAINING"].includes(state)) return "MAINTENANCE";
  return "UNKNOWN";
}

export function runtimeStatusClass(status: CpfRuntimeStatus): "success" | "warning" | "danger" | "info" {
  if (status === "ACTIVE") return "success";
  if (status === "DEGRADED" || status === "MAINTENANCE") return "warning";
  if (status === "OFFLINE") return "danger";
  return "info";
}
