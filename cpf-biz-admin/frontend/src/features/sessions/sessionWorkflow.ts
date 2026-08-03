export function requireSessionRevokeReason(value: string, minimumLength = 5): string {
  const normalized = value.trim();
  if (normalized.length < minimumLength) {
    throw new Error(`세션 폐기 사유는 ${minimumLength}자 이상 입력해야 합니다.`);
  }
  return normalized;
}

export function requireSessionId(value: unknown): string {
  const normalized = String(value || "").trim();
  if (!normalized) throw new Error("폐기할 세션 식별자가 없습니다.");
  return normalized;
}
