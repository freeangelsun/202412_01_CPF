package com.cpf.core.api.database;

/**
 * Owner Module에 맞는 {@link CpfVendorSqlCatalog}를 공급하는 Public SPI입니다.
 * 구현은 Runtime Config의 공식 DB Vendor를 해석하되 미지원/미준비 Vendor를 다른 Pack으로 대체하지 않습니다.
 * Provider와 반환 Catalog는 다중 Thread/다중 인스턴스에서 동일 Config에 대해 결정적인 결과를 제공해야 합니다.
 */
@FunctionalInterface
public interface CpfVendorSqlCatalogProvider {
    /** moduleCode의 Runtime Query Catalog를 반환하며 미등록 Module 또는 미준비 Vendor는 즉시 실패합니다. */
    CpfVendorSqlCatalog forModule(String moduleCode);
}
