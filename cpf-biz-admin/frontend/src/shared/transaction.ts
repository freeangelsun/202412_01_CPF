/**
 * CPF BZA 브라우저가 독립 거래를 최초 기동할 때 사용하는 표준 transactionId 생성기입니다.
 * 서버에서 전달받은 transactionId가 있으면 호출 계층이 그 값을 우선 승계해야 합니다.
 *
 * 규격: yyyyMMddHHmmssSSS(17) + SystemCode(3) + wasId(7) + sequence(7) = 34자리.
 */
export const defaultHeaders: Readonly<Record<string, string>> = Object.freeze({
  "X-Request-Type": "INQUIRY",
  "X-Original-Channel-Code": "BZA",
  "X-Channel-Code": "BZA",
  "X-User-Id": "bza-ui",
  "X-Client-App-Id": "cpf-bza-ui",
  "X-Client-Version": "1.0.0",
  "X-Caller-Service": "bza-ui"
});

export function createTransactionId(systemCode = "BZA", wasId = "bzaUI01"): string {
  const normalizedSystemCode = normalizeFixedAlphaNumeric(systemCode, 3, "BZA").toUpperCase();
  const normalizedWasId = normalizeFixedAlphaNumeric(wasId, 7, "bzaUI01");
  const now = new Date();
  const pad = (value: number, size: number) => String(value).padStart(size, "0");
  const timestamp = [
    now.getFullYear(),
    pad(now.getMonth() + 1, 2),
    pad(now.getDate(), 2),
    pad(now.getHours(), 2),
    pad(now.getMinutes(), 2),
    pad(now.getSeconds(), 2),
    pad(now.getMilliseconds(), 3)
  ].join("");
  const sequence = pad(secureSequence(), 7);
  return `${timestamp}${normalizedSystemCode}${normalizedWasId}${sequence}`;
}

export function isValidTransactionId(value: string | null | undefined): value is string {
  return typeof value === "string" && /^\d{17}[A-Z0-9]{3}[A-Za-z0-9]{7}\d{7}$/.test(value);
}

function secureSequence(): number {
  const values = new Uint32Array(1);
  globalThis.crypto.getRandomValues(values);
  return values[0] % 9_999_999 + 1;
}

function normalizeFixedAlphaNumeric(value: string, length: number, fallback: string): string {
  const normalized = (value || fallback).replace(/[^A-Za-z0-9]/g, "");
  if (normalized.length !== length) {
    throw new Error(`CPF transactionId component must be ${length} alphanumeric characters.`);
  }
  return normalized;
}
