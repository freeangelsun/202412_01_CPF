/** Browser-owned request metadata. CPF transaction/system/caller/target values are server-owned. */
export const defaultHeaders: Readonly<Record<string, string>> = Object.freeze({
  "X-Request-Type": "INQUIRY",
  "X-Client-Id": "cpf-bza-ui",
  "X-Client-Version": "1.0.0"
});

/** Framework-protected canonical transaction headers must never be authored by browser code. */
export const protectedCpfTransactionHeaders = Object.freeze([
  "X-Transaction-Id",
  "X-Original-System-Code",
  "X-System-Code",
  "X-Caller-System-Code",
  "X-Target-System-Code",
  "X-Target-Operation-Id"
] as const);

export function assertNoProtectedCpfHeaders(headers: Headers): void {
  for (const name of protectedCpfTransactionHeaders) {
    if (headers.has(name)) throw new Error(`Browser must not set CPF protected header: ${name}`);
  }
}
