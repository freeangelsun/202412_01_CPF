export type AttachmentActionMode = "RECHECK" | "SECURITY";

export function requireAuditReason(value: string, minimumLength = 5): string {
  const normalized = value.trim();
  if (normalized.length < minimumLength) {
    throw new Error(`조치 사유는 ${minimumLength}자 이상 입력해야 합니다.`);
  }
  return normalized;
}

export function safeAttachmentFilename(value: unknown, attachmentId: unknown): string {
  const fallback = `attachment-${String(attachmentId || "download")}`;
  const source = Array.from(String(value || fallback), (char) => {
    const code = char.charCodeAt(0);
    return code <= 0x1f || '\\/:*?"<>|'.includes(char) ? "_" : char;
  }).join("").trim();
  return source || fallback;
}

export function filenameFromContentDisposition(header: string | null, fallback: string): string {
  if (!header) return fallback;
  const encoded = header.match(/filename\*=UTF-8''([^;]+)/i)?.[1];
  if (encoded) {
    try { return safeAttachmentFilename(decodeURIComponent(encoded), fallback); }
    catch { return safeAttachmentFilename(encoded, fallback); }
  }
  const plain = header.match(/filename="?([^";]+)"?/i)?.[1];
  return safeAttachmentFilename(plain || fallback, fallback);
}
