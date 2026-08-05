import { beforeEach, describe, expect, it, vi } from "vitest";

const api = vi.hoisted(() => ({
  admCacheEvictKey: vi.fn(),
  admCacheEvictNamespace: vi.fn(),
  admCacheReconcile: vi.fn(),
  admCacheRefresh: vi.fn(),
  admCacheSummary: vi.fn(),
  admMessageCreateMessage: vi.fn(),
  admMessageDeleteMessage: vi.fn(),
  admMessageFindMessage: vi.fn(),
  admMessageFindMessages: vi.fn(),
  admMessageUpdateMessage: vi.fn(),
  admNotificationCancelDelivery: vi.fn(),
  admNotificationDisableRule: vi.fn(),
  admNotificationFindDeliveryAttempts: vi.fn(),
  admNotificationFindDeliveryLogs: vi.fn(),
  admNotificationFindDlq: vi.fn(),
  admNotificationFindRule: vi.fn(),
  admNotificationFindRules: vi.fn(),
  admNotificationRetryDelivery: vi.fn(),
  admNotificationSaveRule: vi.fn(),
  admNotificationSendTest: vi.fn(),
  admNotificationUpdateRule: vi.fn()
}));

vi.mock("../../generated/orval/cpf-api", () => api);

import { referenceMethods } from "./referenceMethods";

function response<T>(data: T) {
  return { data, status: 200 as const, headers: new Headers() };
}

function context() {
  return {
    cacheResult: null,
    cacheReason: "운영 캐시 조치",
    cacheControl: { tenantId: "TENANT", namespace: "users", key: "42", version: 3 },
    requireReason: vi.fn().mockReturnValue(true),
    setMessage: vi.fn(),
    cacheVersionValid: referenceMethods.cacheVersionValid,
    loadCacheSummary: vi.fn()
  } as any;
}

describe("cache route generated-client actions", () => {
  beforeEach(() => vi.clearAllMocks());

  it("loads summary and refreshes an allowlisted target", async () => {
    api.admCacheSummary.mockResolvedValue(response({ ready: true }));
    api.admCacheRefresh.mockResolvedValue(response({ operation: "REFRESH" }));
    const ctx = context();

    await referenceMethods.loadCacheSummary.call(ctx);
    expect(api.admCacheSummary).toHaveBeenCalledWith();
    await referenceMethods.refreshCache.call(ctx, "MESSAGE");
    expect(api.admCacheRefresh).toHaveBeenCalledWith({ target: "MESSAGE", reason: "운영 캐시 조치" });
  });

  it("evicts a key with CAS version and without browser actor identity", async () => {
    api.admCacheEvictKey.mockResolvedValue(response({ operation: "EVICT_KEY" }));
    const ctx = context();

    await referenceMethods.evictCacheKey.call(ctx);

    const body = api.admCacheEvictKey.mock.calls[0][0];
    expect(body).toEqual({ tenantId: "TENANT", namespace: "users", key: "42", version: 3, reason: "운영 캐시 조치" });
    expect(body).not.toHaveProperty("requestUser");
  });

  it("evicts a namespace and reconciles through typed request bodies", async () => {
    api.admCacheEvictNamespace.mockResolvedValue(response({ operation: "EVICT_NAMESPACE" }));
    api.admCacheReconcile.mockResolvedValue(response({ operation: "RECONCILE" }));
    const ctx = context();

    await referenceMethods.evictCacheNamespace.call(ctx);
    expect(api.admCacheEvictNamespace).toHaveBeenCalledWith({
      tenantId: "TENANT", namespace: "users", version: 3, reason: "운영 캐시 조치"
    });
    await referenceMethods.reconcileCache.call(ctx);
    expect(api.admCacheReconcile).toHaveBeenCalledWith({ reason: "운영 캐시 조치" });
  });

  it("fails closed for invalid target, missing reason, or negative version", async () => {
    const ctx = context();
    await referenceMethods.refreshCache.call(ctx, "UNSUPPORTED");
    expect(api.admCacheRefresh).not.toHaveBeenCalled();

    ctx.cacheControl.version = -1;
    await referenceMethods.evictCacheKey.call(ctx);
    expect(api.admCacheEvictKey).not.toHaveBeenCalled();
    expect(ctx.setMessage).toHaveBeenCalledWith("캐시 버전은 0 이상의 정수여야 합니다.");

    ctx.cacheControl.version = 1;
    ctx.requireReason.mockReturnValue(false);
    await referenceMethods.reconcileCache.call(ctx);
    expect(api.admCacheReconcile).not.toHaveBeenCalled();
  });
});
