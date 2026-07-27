package com.cpf.core.api.database;

import org.springframework.core.env.Environment;

/**
 * Vendor별 CPF Runtime SQL을 조회하는 공개 API입니다.
 *
 * <p>업무/관리/Generated Domain은 {@code com.cpf.core.common.*} 내부 구현을 직접 참조하지 않고
 * 이 공개 경계를 통해 Query Contract를 사용합니다.</p>
 */
public final class CpfVendorSqlCatalog {
    private final com.cpf.core.common.database.CpfVendorSqlCatalog delegate;

    private CpfVendorSqlCatalog(com.cpf.core.common.database.CpfVendorSqlCatalog delegate) {
        this.delegate = delegate;
    }

    public static CpfVendorSqlCatalog create(Environment environment, String moduleCode) {
        return new CpfVendorSqlCatalog(
                com.cpf.core.common.database.CpfVendorSqlCatalog.create(environment, moduleCode));
    }

    public String required(String statementKey) {
        return delegate.required(statementKey);
    }

    public String resourcePath(String statementKey) {
        return delegate.resourcePath(statementKey);
    }

    public String vendor() {
        return delegate.vendor();
    }
}
