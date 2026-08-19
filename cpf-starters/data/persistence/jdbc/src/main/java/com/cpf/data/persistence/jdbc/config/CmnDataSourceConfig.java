package com.cpf.data.persistence.jdbc.config;

/**
 * @deprecated Common runtime DataSource ownership moved to
 * {@code com.cpf.common.runtime.CpfCommonJdbcAutoConfiguration}, which resolves cpfDB through
 * {@code CpfDataSourceRegistry}. Retained only as a source-compatibility marker until stale-file cleanup.
 */
@Deprecated(forRemoval = false)
/** Common은 cpfDB 단일 런타임을 사용하며 별도 CMN DataSource를 만들지 않는 호환성 표식입니다. */
public final class CmnDataSourceConfig {
    private CmnDataSourceConfig() { }
}
