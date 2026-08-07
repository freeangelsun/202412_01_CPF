import { beforeEach, describe, expect, it } from "vitest";
import {
  APPROVAL_IDEMPOTENCY_STORAGE_KEY, approvalDraftFingerprint, clearApprovalIdempotency,
  markApprovalConfirmed, resolveApprovalIdempotency,
} from "./integrationClosureIdempotency";
const draft = { quarantineId: "DQ-1", expectedVersion: 3, reason: "correct", corrected: { z: 1.0, name: "Kim" } };
beforeEach(() => { clearApprovalIdempotency(localStorage); sessionStorage.clear(); });
describe("integration closure approval idempotency", () => {
  it("preserves the original A key across A to B to A navigation", async () => {
    const a = await approvalDraftFingerprint(draft);
    const b = await approvalDraftFingerprint({ ...draft, quarantineId: "DQ-2" });
    expect(resolveApprovalIdempotency(a, localStorage, () => "idem-a-0001").key).toBe("idem-a-0001");
    expect(resolveApprovalIdempotency(b, localStorage, () => "idem-b-0001").key).toBe("idem-b-0001");
    expect(resolveApprovalIdempotency(a, localStorage, () => "idem-a-0002").key).toBe("idem-a-0001");
  });
  it("keeps independent pending keys for multiple drafts and remounts", async () => {
    const a = await approvalDraftFingerprint(draft);
    const b = await approvalDraftFingerprint({ ...draft, expectedVersion: 4 });
    resolveApprovalIdempotency(a, localStorage, () => "idem-a-pending");
    resolveApprovalIdempotency(b, localStorage, () => "idem-b-pending");
    expect(resolveApprovalIdempotency(a, localStorage, () => "unused-a").key).toBe("idem-a-pending");
    expect(resolveApprovalIdempotency(b, localStorage, () => "unused-b").key).toBe("idem-b-pending");
  });
  it("confirms one draft without overwriting another draft", async () => {
    const a = await approvalDraftFingerprint(draft);
    const b = await approvalDraftFingerprint({ ...draft, quarantineId: "DQ-2" });
    const pendingA = resolveApprovalIdempotency(a, localStorage, () => "idem-confirm-a");
    resolveApprovalIdempotency(b, localStorage, () => "idem-pending-b");
    markApprovalConfirmed(a, pendingA.key, 77);
    expect(resolveApprovalIdempotency(a, localStorage, () => "duplicate-a")).toMatchObject({ state: "confirmed", approvalRequestId: 77 });
    expect(resolveApprovalIdempotency(b, localStorage, () => "duplicate-b")).toMatchObject({ state: "pending", key: "idem-pending-b" });
  });
  it("clears only the explicitly completed draft when requested", async () => {
    const a = await approvalDraftFingerprint(draft);
    const b = await approvalDraftFingerprint({ ...draft, quarantineId: "DQ-2" });
    resolveApprovalIdempotency(a, localStorage, () => "idem-clear-a");
    resolveApprovalIdempotency(b, localStorage, () => "idem-keep-b");
    clearApprovalIdempotency(localStorage, a);
    expect(resolveApprovalIdempotency(a, localStorage, () => "idem-new-a").key).toBe("idem-new-a");
    expect(resolveApprovalIdempotency(b, localStorage, () => "unused-b").key).toBe("idem-keep-b");
  });

  it("uses the same deterministic key for simultaneous tabs before either write is observed", async () => {
    const fingerprint = await approvalDraftFingerprint(draft);
    const backing = new Map<string, string>();
    const tab = (): Storage => ({
      get length(){ return backing.size; }, clear(){ backing.clear(); },
      getItem(key){ return backing.get(key) ?? null; }, key(index){ return [...backing.keys()][index] ?? null; },
      removeItem(key){ backing.delete(key); }, setItem(key,value){ backing.set(key,String(value)); },
    });
    const tabA = tab(); const tabB = tab();
    const a = resolveApprovalIdempotency(fingerprint, tabA);
    backing.clear(); // emulate both tabs having read the same pre-write snapshot
    const b = resolveApprovalIdempotency(fingerprint, tabB);
    expect(a.key).toBe(b.key);
  });
  it("never stores corrected payload in browser storage", async () => {
    const fingerprint = await approvalDraftFingerprint({ ...draft, corrected: { accountNumber: "123-456-789" } });
    resolveApprovalIdempotency(fingerprint, localStorage, () => "idem-safe");
    const raw = localStorage.getItem(APPROVAL_IDEMPOTENCY_STORAGE_KEY) ?? "";
    expect(raw).not.toContain("123-456-789"); expect(raw).not.toContain("accountNumber");
  });

  it("rotates the deterministic key generation after pending TTL expiry", async () => {
    const fingerprint = await approvalDraftFingerprint(draft);
    localStorage.setItem(APPROVAL_IDEMPOTENCY_STORAGE_KEY, JSON.stringify({
      version: 3,
      entries: {
        [fingerprint]: { fingerprint, key: `dq-${fingerprint.slice(0, 48)}-0`, state: "pending", updatedAt: Date.now() - (25 * 60 * 60 * 1000) },
      },
      generations: { [fingerprint]: 0 },
    }));
    expect(resolveApprovalIdempotency(fingerprint, localStorage).key).toBe(`dq-${fingerprint.slice(0, 48)}-1`);
  });
  it("rotates the deterministic key generation after confirmed TTL expiry", async () => {
    const fingerprint = await approvalDraftFingerprint(draft);
    localStorage.setItem(APPROVAL_IDEMPOTENCY_STORAGE_KEY, JSON.stringify({
      version: 3,
      entries: {
        [fingerprint]: { fingerprint, key: `dq-${fingerprint.slice(0, 48)}-2`, state: "confirmed", approvalRequestId: 77, updatedAt: Date.now() - (8 * 24 * 60 * 60 * 1000) },
      },
      generations: { [fingerprint]: 2 },
    }));
    expect(resolveApprovalIdempotency(fingerprint, localStorage).key).toBe(`dq-${fingerprint.slice(0, 48)}-3`);
  });
  it("rejects unsafe integer input before hashing", async () => {
    await expect(approvalDraftFingerprint({ ...draft, corrected: { sequence: Number.MAX_SAFE_INTEGER + 1 } }))
      .rejects.toThrow("Unsafe integer");
  });
  it("fails closed on Unicode-normalized duplicate keys", async () => {
    const decomposed = "é".normalize("NFD");
    await expect(approvalDraftFingerprint({ ...draft, corrected: { "é": 1, [decomposed]: 2 } }))
      .rejects.toThrow("Duplicate draft key");
  });
});
