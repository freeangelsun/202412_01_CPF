package com.cpf.core.api.tenant;

/** 요청 Thread 범위 Tenant Context. 비동기 전파는 명시 Adapter가 필요합니다. */
public final class CpfTenantContext {
    private static final ThreadLocal<String> CURRENT = new ThreadLocal<>();
    private CpfTenantContext() {}
    public static void set(String tenantId) {
        if (tenantId == null || tenantId.isBlank()) throw new IllegalArgumentException("tenantId는 필수입니다.");
        CURRENT.set(tenantId.trim());
    }
    public static String current() { return CURRENT.get(); }
    public static String require() {
        String tenantId = CURRENT.get();
        if (tenantId == null) throw new IllegalStateException("Tenant Context가 없습니다.");
        return tenantId;
    }
    public static void clear() { CURRENT.remove(); }
}
