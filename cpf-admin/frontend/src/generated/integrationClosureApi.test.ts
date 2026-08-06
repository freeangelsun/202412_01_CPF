import { afterEach, describe, expect, it, vi } from "vitest";
import { integrationClosureApi } from "./integrationClosureApi";

afterEach(() => vi.unstubAllGlobals());

describe("integration closure generated client", () => {
  it("creates an immutable approval request without an approved flag", async () => {
    const fetchMock = vi.fn().mockResolvedValue({ ok: true, json: async () => ({ approvalRequestId: 77 }) });
    vi.stubGlobal("fetch", fetchMock);

    await integrationClosureApi.requestCorrectionApproval("DQ-1", {
      expectedVersion: 3,
      idempotencyKey: "idem-1",
      reason: "fix invalid name",
      corrected: { name: "Kim" }
    });

    const [url, init] = fetchMock.mock.calls[0];
    expect(url).toContain("/data-quality/quarantine/DQ-1/correction-approvals");
    const body = JSON.parse(String(init.body));
    expect(body.approved).toBeUndefined();
    expect(body).toMatchObject({ expectedVersion: 3, idempotencyKey: "idem-1" });
  });

  it("executes using approval id and reason only", async () => {
    const fetchMock = vi.fn().mockResolvedValue({ ok: true, json: async () => ({ approvalStatus: "COMPLETED" }) });
    vi.stubGlobal("fetch", fetchMock);

    await integrationClosureApi.executeCorrectionApproval(77, { reason: "execute approved correction" });

    const [url, init] = fetchMock.mock.calls[0];
    expect(url).toContain("/data-quality/correction-approvals/77/execute");
    expect(JSON.parse(String(init.body))).toEqual({ reason: "execute approved correction" });
  });
});
