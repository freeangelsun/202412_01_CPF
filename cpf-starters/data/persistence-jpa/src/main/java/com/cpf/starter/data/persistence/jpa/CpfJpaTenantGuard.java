package com.cpf.starter.data.persistence.jpa;

import com.cpf.core.api.persistence.CpfPersistenceContext;
import java.util.Objects;

/** Multi-tenant Domain이 Repository 경계에서 현재 tenant와 aggregate tenant를 fail-closed로 대조할 때 사용합니다. */
public final class CpfJpaTenantGuard {
    private CpfJpaTenantGuard() { }
    public static void requireCurrentTenant(CpfPersistenceContext context, String entityTenantId) {
        Objects.requireNonNull(context, "context");
        if (context.tenantId() == null || context.tenantId().isBlank()) throw new IllegalStateException("현재 tenant context가 없습니다.");
        if (!context.tenantId().equals(entityTenantId)) throw new SecurityException("현재 tenant와 persistence 대상 tenant가 일치하지 않습니다.");
    }
}
