export type BreakGlassActionMode = "CLOSE" | "REVIEW_APPROVE" | "REVIEW_REJECT";

export function requireBreakGlassReason(value: string, minimumLength = 5): string {
  const normalized = value.trim();
  if (normalized.length < minimumLength) throw new Error(`사유는 ${minimumLength}자 이상 입력해야 합니다.`);
  return normalized;
}

export function validateBreakGlassRequest(scopeValue: string, reason: string, ttlMinutes: number): {
  scopeValue: string; reason: string; ttlMinutes: number;
} {
  const target = scopeValue.trim();
  if (!target) throw new Error("Break-glass 적용 대상을 입력하세요.");
  if (!Number.isInteger(ttlMinutes) || ttlMinutes < 1 || ttlMinutes > 30) {
    throw new Error("TTL은 1분 이상 30분 이하의 정수여야 합니다.");
  }
  return { scopeValue: target, reason: requireBreakGlassReason(reason), ttlMinutes };
}
