export const defaultHeaders: Readonly<Record<string, string>> = Object.freeze({
  "X-Request-Type": "INQUIRY",
  "X-Original-Channel-Code": "ADM",
  "X-Channel-Code": "ADM",
  "X-User-Id": "admin-ui",
  "X-Client-App-Id": "cpf-adm-ui",
  "X-Client-Version": "1.0.0",
  "X-Caller-Service": "adm-ui"
});

export function createTransactionGlobalId(moduleId = "ADM", wasId = "admUI01"): string {
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
  return `${timestamp}${moduleId}${wasId}${sequence}`;
}
