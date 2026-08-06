import { beforeEach, describe, expect, it } from "vitest";
import {
  APPROVAL_IDEMPOTENCY_STORAGE_KEY, approvalDraftFingerprint, clearApprovalIdempotency,
  markApprovalConfirmed, resolveApprovalIdempotency,
} from "./integrationClosureIdempotency";
const draft = { quarantineId: "DQ-1", expectedVersion: 3, reason: "correct", corrected: { z: 1.0, name: "Kim" } };
beforeEach(() => clearApprovalIdempotency());
describe("integration closure approval idempotency", () => {
  it("reuses one key for equivalent drafts, timeout retries, double clicks and remounts", async () => {
    const left = await approvalDraftFingerprint(draft);
    const right = await approvalDraftFingerprint({ ...draft, corrected: { name: "Kim", z: 1 } });
    expect(right).toBe(left);
    const first = resolveApprovalIdempotency(left, sessionStorage, () => "idem-0001");
    expect(resolveApprovalIdempotency(right, sessionStorage, () => "idem-0002").key).toBe(first.key);
  });
  it("rotates only when payload changes and preserves pending state after response loss", async () => {
    const first = await approvalDraftFingerprint(draft);
    resolveApprovalIdempotency(first, sessionStorage, () => "idem-pending");
    expect(resolveApprovalIdempotency(first, sessionStorage, () => "unused").key).toBe("idem-pending");
    const changed = await approvalDraftFingerprint({ ...draft, expectedVersion: 4 });
    expect(resolveApprovalIdempotency(changed, sessionStorage, () => "idem-new").key).toBe("idem-new");
  });
  it("blocks approval-id reuse after confirmed success until explicit new work", async () => {
    const fingerprint = await approvalDraftFingerprint(draft);
    const pending = resolveApprovalIdempotency(fingerprint, sessionStorage, () => "idem-confirmed");
    markApprovalConfirmed(fingerprint, pending.key, 77);
    expect(resolveApprovalIdempotency(fingerprint, sessionStorage, () => "duplicate"))
      .toMatchObject({ key: "idem-confirmed", state: "confirmed", approvalRequestId: 77 });
    clearApprovalIdempotency();
    expect(resolveApprovalIdempotency(fingerprint, sessionStorage, () => "idem-next").key).toBe("idem-next");
  });
  it("never stores corrected payload in browser storage", async () => {
    const fingerprint = await approvalDraftFingerprint({ ...draft, corrected: { accountNumber: "123-456-789" } });
    resolveApprovalIdempotency(fingerprint, sessionStorage, () => "idem-safe");
    const raw = sessionStorage.getItem(APPROVAL_IDEMPOTENCY_STORAGE_KEY) ?? "";
    expect(raw).not.toContain("123-456-789"); expect(raw).not.toContain("accountNumber");
  });
  it("rejects an invalid generated idempotency key before any request", async () => {
    const fingerprint = await approvalDraftFingerprint(draft);
    expect(() => resolveApprovalIdempotency(fingerprint, sessionStorage, () => "short"))
      .toThrow("Invalid idempotency key");
    expect(sessionStorage.getItem(APPROVAL_IDEMPOTENCY_STORAGE_KEY)).toBeNull();
  });
  it("fails closed on Unicode-normalized duplicate keys", async () => {
    const decomposed = "é".normalize("NFD");
    await expect(approvalDraftFingerprint({ ...draft, corrected: { "é": 1, [decomposed]: 2 } }))
      .rejects.toThrow("Duplicate draft key");
  });
});
