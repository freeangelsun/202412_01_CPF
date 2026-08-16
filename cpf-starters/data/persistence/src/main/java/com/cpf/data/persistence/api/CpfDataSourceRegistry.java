package com.cpf.data.persistence.api;

import javax.sql.DataSource;

/**
 * 논리 Database role을 실제 DataSource로 해석하는 Data Capability 계약입니다.
 * Provider가 없거나 다중 DataSource가 모호하면 fallback하지 않고 실패해야 합니다.
 */
public interface CpfDataSourceRegistry {
    DataSource require(CpfDatabaseRole role);
}
