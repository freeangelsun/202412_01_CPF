import { beforeEach, describe, expect, it, vi } from "vitest";

vi.mock("../../generated/orval/cpf-api", () => ({
  admApprovalRequest: vi.fn(),
  admCacheSummary: vi.fn()
}));

import { admApprovalRequest } from "../../generated/orval/cpf-api";
import { referenceMethods } from "./referenceMethods";

const api = vi.mocked({ admApprovalRequest });
const response = (data: unknown) => ({ data, status: 200, headers: {} });

function context() {
  return {
    cacheReason: "운영 캐시 조치 승인 사유",
    cacheControl: { tenantId: "TENANT", namespace: "users", key: "42", version: 3 },
    cacheResult: null,
    uiMessage: "",
    setMessage: referenceMethods.setMessage,
    requireReason: (reason: string) => reason.trim().length >= 8,
    cacheVersionValid: referenceMethods.cacheVersionValid,
    requestCacheApproval: referenceMethods.requestCacheApproval,
    evictCacheKey: referenceMethods.evictCacheKey,
    evictCacheNamespace: referenceMethods.evictCacheNamespace,
    reconcileCache: referenceMethods.reconcileCache,
    refreshCache: referenceMethods.refreshCache
  } as any;
}

describe("cache dangerous action approval boundary", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    api.admApprovalRequest.mockResolvedValue(response({ approvalRequestId: 91 }) as any);
  });

  it("requests approval for key eviction with immutable CAS snapshot", async () => {
    const ctx = context();
    await referenceMethods.evictCacheKey.call(ctx);
    expect(api.admApprovalRequest).toHaveBeenCalledTimes(1);
    const request = api.admApprovalRequest.mock.calls[0][0] as any;
    expect(request.ownerModule).toBe("CPF-DATA-CACHE");
    expect(request.ownerCommand).toBe("CACHE_EVICT_KEY");
    expect(request.targetId).toBe("TENANT:users:42");
    expect(JSON.parse(request.payloadSnapshot)).toEqual({ tenantId: "TENANT", namespace: "users", key: "42", version: 3 });
    expect(ctx.uiMessage).toContain("독립 승인");
  });

  it("requests approval for namespace eviction and reconcile", async () => {
    const ctx = context();
    await referenceMethods.evictCacheNamespace.call(ctx);
    await referenceMethods.reconcileCache.call(ctx);
    expect(api.admApprovalRequest).toHaveBeenCalledTimes(2);
    expect((api.admApprovalRequest.mock.calls[0][0] as any).ownerCommand).toBe("CACHE_EVICT_NAMESPACE");
    expect((api.admApprovalRequest.mock.calls[1][0] as any).ownerCommand).toBe("CACHE_RECONCILE");
  });

  it("requests approval for refresh and rejects invalid version before request", async () => {
    const ctx = context();
    await referenceMethods.refreshCache.call(ctx, "ALL");
    expect((api.admApprovalRequest.mock.calls[0][0] as any).ownerCommand).toBe("CACHE_REFRESH");
    ctx.cacheControl.version = -1;
    await referenceMethods.evictCacheKey.call(ctx);
    expect(api.admApprovalRequest).toHaveBeenCalledTimes(1);
  });
});
