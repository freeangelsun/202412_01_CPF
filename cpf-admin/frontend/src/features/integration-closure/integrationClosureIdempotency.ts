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
  updatedAt: number;
}
interface ApprovalIdempotencyLedger {
  version: 3;
  entries: Record<string, ApprovalIdempotencyState>;
  generations: Record<string, number>;
}
export const APPROVAL_IDEMPOTENCY_STORAGE_KEY = "cpf.adm.integrationClosure.approval.idempotency.v3";
const PREVIOUS_STORAGE_KEY = "cpf.adm.integrationClosure.approval.idempotency.v2";
const LEGACY_STORAGE_KEY = "cpf.adm.integrationClosure.approval.idempotency.v1";
const MAX_ENTRIES = 64;
const PENDING_TTL_MS = 24 * 60 * 60 * 1000;
const CONFIRMED_TTL_MS = 7 * 24 * 60 * 60 * 1000;
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
    if (!Number.isSafeInteger(value) && Number.isInteger(value)) throw new Error("Unsafe integer is not allowed");
    return Object.is(value, -0) ? 0 : value;
  }
  return value;
}
export async function approvalDraftFingerprint(draft: ApprovalDraft): Promise<string> {
  if (!globalThis.crypto?.subtle) throw new Error("Secure SHA-256 browser API is unavailable");
  if (!Number.isSafeInteger(draft.expectedVersion) || draft.expectedVersion < 1) throw new Error("Invalid expected version");
  const normalizedDraft = {
    quarantineId: draft.quarantineId.trim(), expectedVersion: draft.expectedVersion,
    reason: draft.reason.trim(), corrected: draft.corrected,
  };
  const bytes = new TextEncoder().encode(JSON.stringify(canonical(normalizedDraft)));
  const digest = await globalThis.crypto.subtle.digest("SHA-256", bytes);
  return Array.from(new Uint8Array(digest), byte => byte.toString(16).padStart(2, "0")).join("");
}
function isState(value: unknown): value is ApprovalIdempotencyState {
  const state = value as Partial<ApprovalIdempotencyState>;
  return !!state && /^[0-9a-f]{64}$/.test(String(state.fingerprint || ""))
    && typeof state.key === "string" && state.key.length >= 8
    && ["pending", "confirmed"].includes(String(state.state))
    && Number.isSafeInteger(state.updatedAt) && Number(state.updatedAt) > 0
    && (state.state !== "confirmed" || (Number.isSafeInteger(state.approvalRequestId) && Number(state.approvalRequestId) > 0));
}
function parse(storage: Storage, now = Date.now()): ApprovalIdempotencyLedger {
  const empty: ApprovalIdempotencyLedger = { version: 3, entries: {}, generations: {} };
  try {
    const raw = storage.getItem(APPROVAL_IDEMPOTENCY_STORAGE_KEY);
    if (!raw) return empty;
    const parsed = JSON.parse(raw) as Partial<ApprovalIdempotencyLedger>;
    if (parsed.version !== 3 || !parsed.entries || typeof parsed.entries !== "object"
        || !parsed.generations || typeof parsed.generations !== "object") return empty;
    const generations = Object.fromEntries(Object.entries(parsed.generations).filter(([fingerprint, generation]) =>
      /^[0-9a-f]{64}$/.test(fingerprint) && Number.isSafeInteger(generation) && Number(generation) >= 0));
    const entries: Record<string, ApprovalIdempotencyState> = {};
    for (const [fingerprint, state] of Object.entries(parsed.entries)) {
      if (!isState(state) || state.fingerprint !== fingerprint) continue;
      const ttl = state.state === "pending" ? PENDING_TTL_MS : CONFIRMED_TTL_MS;
      if (now - state.updatedAt <= ttl) {
        entries[fingerprint] = state;
      } else {
        // Expiry starts a new idempotency generation. Reusing generation 0 after browser TTL
        // could replay an older server-side idempotency record even though the UI treats it as new.
        generations[fingerprint] = Math.max(Number(generations[fingerprint] ?? 0), 0) + 1;
      }
    }
    return { version: 3, entries, generations };
  } catch { return empty; }
}
function persist(storage: Storage, ledger: ApprovalIdempotencyLedger): ApprovalIdempotencyLedger {
  const entries = Object.fromEntries(Object.entries(ledger.entries)
    .sort(([, left], [, right]) => right.updatedAt - left.updatedAt).slice(0, MAX_ENTRIES));
  const normalized = { version: 3 as const, entries, generations: ledger.generations };
  storage.setItem(APPROVAL_IDEMPOTENCY_STORAGE_KEY, JSON.stringify(normalized));
  storage.removeItem(PREVIOUS_STORAGE_KEY);
  storage.removeItem(LEGACY_STORAGE_KEY);
  return normalized;
}
export function resolveApprovalIdempotency(
  fingerprint: string, storage: Storage = localStorage,
  keyFactory?: () => string,
): ApprovalIdempotencyState {
  if (!/^[0-9a-f]{64}$/.test(fingerprint)) throw new Error("Invalid approval draft fingerprint");
  const ledger = parse(storage);
  const existing = ledger.entries[fingerprint];
  if (existing) return existing;
  const generation = ledger.generations[fingerprint] ?? 0;
  // The default key is deterministic for a fingerprint generation, so two tabs racing before
  // either storage write still submit the same server idempotency key.
  const key = keyFactory ? keyFactory() : `dq-${fingerprint.slice(0, 48)}-${generation}`;
  if (typeof key !== "string" || key.trim().length < 8) throw new Error("Invalid idempotency key");
  const state: ApprovalIdempotencyState = { fingerprint, key: key.trim(), state: "pending", updatedAt: Date.now() };
  ledger.entries[fingerprint] = state;
  persist(storage, ledger);
  return state;
}
export function markApprovalConfirmed(
  fingerprint: string, key: string, approvalRequestId: number, storage: Storage = localStorage,
): ApprovalIdempotencyState {
  const ledger = parse(storage);
  const current = ledger.entries[fingerprint];
  if (!current || current.key !== key || current.state !== "pending") {
    throw new Error("Approval idempotency state changed before confirmation");
  }
  if (!Number.isSafeInteger(approvalRequestId) || approvalRequestId < 1) throw new Error("Invalid approval request id");
  const confirmed: ApprovalIdempotencyState = { ...current, state: "confirmed", approvalRequestId, updatedAt: Date.now() };
  ledger.entries[fingerprint] = confirmed;
  persist(storage, ledger);
  return confirmed;
}
export function clearApprovalIdempotency(storage: Storage = localStorage, fingerprint?: string): void {
  if (!fingerprint) {
    storage.removeItem(APPROVAL_IDEMPOTENCY_STORAGE_KEY);
    storage.removeItem(PREVIOUS_STORAGE_KEY);
    storage.removeItem(LEGACY_STORAGE_KEY);
    return;
  }
  const ledger = parse(storage);
  delete ledger.entries[fingerprint];
  ledger.generations[fingerprint] = (ledger.generations[fingerprint] ?? 0) + 1;
  persist(storage, ledger);
}
