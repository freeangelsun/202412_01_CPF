export function requireDangerousOperationReason(value: string, minimumLength = 5): string {
  const normalized = value.trim();
  if (normalized.length < minimumLength) {
    throw new Error(`위험 조치 사유는 ${minimumLength}자 이상 입력해야 합니다.`);
  }
  return normalized;
}

export function validatePathValues(pathNames: readonly string[], values: Record<string, string>): Record<string, string> {
  const path: Record<string, string> = {};
  for (const name of pathNames) {
    const value = String(values[name] || "").trim();
    if (!value) throw new Error(`필수 Path 값 ${name}을(를) 입력하세요.`);
    path[name] = value;
  }
  return path;
}

export function attachDangerousReason(
  query: Record<string, unknown>,
  body: Record<string, unknown> | undefined,
  reason: string
): { query: Record<string, unknown>; body: Record<string, unknown> } {
  const normalized = requireDangerousOperationReason(reason);
  const nextQuery = { ...query };
  const nextBody = { ...(body || {}) };
  if (!("reason" in nextQuery) && !("reason" in nextBody)) nextBody.reason = normalized;
  return { query: nextQuery, body: nextBody };
}
