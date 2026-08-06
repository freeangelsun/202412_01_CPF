export interface ApprovalDraft {
  quarantineId: string;
  expectedVersion: number;
  reason: string;
  corrected: Record<string, unknown>;
}
export interface ApprovalIdempotencyState {
  fingerprint: string;
  key: string;
  state: "pending" | "confirmed";
  approvalRequestId?: number;
}
export const APPROVAL_IDEMPOTENCY_STORAGE_KEY = "cpf.adm.integrationClosure.approval.idempotency.v1";
function normalize(value: string): string { return value.normalize("NFC"); }
function compareCodePoint(left: string, right: string): number { return left < right ? -1 : left > right ? 1 : 0; }
function canonical(value: unknown): unknown {
  if (Array.isArray(value)) return value.map(canonical);
  if (value && typeof value === "object") {
    const entries = Object.entries(value as Record<string, unknown>)
      .map(([key, child]) => [normalize(key), child] as const)
      .sort(([left], [right]) => compareCodePoint(left, right));
    for (let index = 1; index < entries.length; index += 1) {
      if (entries[index - 1][0] === entries[index][0]) throw new Error("Duplicate draft key after Unicode normalization");
    }
    return Object.fromEntries(entries.map(([key, child]) => [key, canonical(child)]));
  }
  if (typeof value === "string") return normalize(value);
  if (typeof value === "number") {
    if (!Number.isFinite(value)) throw new Error("Non-finite numbers are not allowed");
    return Object.is(value, -0) ? 0 : value;
  }
  return value;
}
export async function approvalDraftFingerprint(draft: ApprovalDraft): Promise<string> {
  if (!globalThis.crypto?.subtle) throw new Error("Secure SHA-256 browser API is unavailable");
  const normalizedDraft = {
    quarantineId: draft.quarantineId.trim(), expectedVersion: draft.expectedVersion,
    reason: draft.reason.trim(), corrected: draft.corrected,
  };
  const bytes = new TextEncoder().encode(JSON.stringify(canonical(normalizedDraft)));
  const digest = await globalThis.crypto.subtle.digest("SHA-256", bytes);
  return Array.from(new Uint8Array(digest), byte => byte.toString(16).padStart(2, "0")).join("");
}
function parse(storage: Storage): ApprovalIdempotencyState | null {
  try {
    const raw = storage.getItem(APPROVAL_IDEMPOTENCY_STORAGE_KEY); if (!raw) return null;
    const state = JSON.parse(raw) as Partial<ApprovalIdempotencyState>;
    if (!/^[0-9a-f]{64}$/.test(String(state.fingerprint || "")) || typeof state.key !== "string"
        || state.key.length < 8 || !["pending", "confirmed"].includes(String(state.state))) return null;
    if (state.state === "confirmed" && (!Number.isSafeInteger(state.approvalRequestId) || Number(state.approvalRequestId) < 1)) return null;
    return state as ApprovalIdempotencyState;
  } catch { return null; }
}
function persist(storage: Storage, state: ApprovalIdempotencyState): ApprovalIdempotencyState {
  storage.setItem(APPROVAL_IDEMPOTENCY_STORAGE_KEY, JSON.stringify(state)); return state;
}
export function resolveApprovalIdempotency(
  fingerprint: string, storage: Storage = sessionStorage,
  keyFactory: () => string = () => globalThis.crypto.randomUUID(),
): ApprovalIdempotencyState {
  if (!/^[0-9a-f]{64}$/.test(fingerprint)) throw new Error("Invalid approval draft fingerprint");
  const existing = parse(storage); if (existing?.fingerprint === fingerprint) return existing;
  const key = keyFactory();
  if (typeof key !== "string" || key.trim().length < 8) throw new Error("Invalid idempotency key");
  return persist(storage, { fingerprint, key: key.trim(), state: "pending" });
}
export function markApprovalConfirmed(
  fingerprint: string, key: string, approvalRequestId: number, storage: Storage = sessionStorage,
): ApprovalIdempotencyState {
  const current = parse(storage);
  if (!current || current.fingerprint !== fingerprint || current.key !== key || current.state !== "pending") {
    throw new Error("Approval idempotency state changed before confirmation");
  }
  if (!Number.isSafeInteger(approvalRequestId) || approvalRequestId < 1) throw new Error("Invalid approval request id");
  return persist(storage, { fingerprint, key, state: "confirmed", approvalRequestId });
}
export function clearApprovalIdempotency(storage: Storage = sessionStorage): void {
  storage.removeItem(APPROVAL_IDEMPOTENCY_STORAGE_KEY);
}
