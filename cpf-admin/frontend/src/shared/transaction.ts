/**
 * CPF ADM 브라우저가 내부에서 독립 거래를 기동할 때 사용하는 표준 transactionId 생성기입니다.
 *
 * <p>서버가 이미 내려준 transactionId가 있는 후속 호출은 반드시 그 값을 승계하고,
 * 브라우저가 독립 거래를 최초 기동할 때만 이 함수로 새 값을 만듭니다.</p>
 *
 * 규격: yyyyMMddHHmmssSSS(17) + SystemCode(3) + wasId(7) + sequence(7) = 34자리.
 * 서버의 TransactionIdGenerator와 같은 길이/문자 계약을 유지합니다.
 */
export const defaultHeaders: Readonly<Record<string, string>> = Object.freeze({
  "X-Request-Type": "INQUIRY",
  "X-Original-Channel-Code": "ADM",
  "X-Channel-Code": "ADM",
  "X-User-Id": "admin-ui",
  "X-Client-App-Id": "cpf-adm-ui",
  "X-Client-Version": "1.0.0",
  "X-Caller-Service": "adm-ui"
});

export function createTransactionId(systemCode = "ADM", wasId = "admUI01"): string {
  const normalizedSystemCode = normalizeFixedAlphaNumeric(systemCode, 3, "ADM").toUpperCase();
  const normalizedWasId = normalizeFixedAlphaNumeric(wasId, 7, "admUI01");
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
  const sequence = pad(crypto.getRandomValues(new Uint32Array(1))[0] % 9_999_999 + 1, 7);
  return `${timestamp}${normalizedSystemCode}${normalizedWasId}${sequence}`;
}

export function isValidTransactionId(value: string | null | undefined): value is string {
  return typeof value === "string" && /^\d{17}[A-Z0-9]{3}[A-Za-z0-9]{7}\d{7}$/.test(value);
}

function normalizeFixedAlphaNumeric(value: string, length: number, fallback: string): string {
  const normalized = (value || fallback).replace(/[^A-Za-z0-9]/g, "");
  if (normalized.length !== length) {
    throw new Error(`CPF transactionId component must be ${length} alphanumeric characters.`);
  }
  return normalized;
}
