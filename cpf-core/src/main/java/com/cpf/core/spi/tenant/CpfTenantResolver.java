package com.cpf.core.spi.tenant;

import jakarta.servlet.http.HttpServletRequest;

/** Host/Header/Token Claim 등에서 Tenant를 검증해 결정하는 확장 SPI. */
public interface CpfTenantResolver {
    String resolveTenantId(HttpServletRequest request);
}
