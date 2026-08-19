package com.cpf.data.persistence.mybatis.config;

/**
 * @deprecated Common Product Service no longer owns a separate MyBatis runtime. Canonical Common
 * persistence uses cpfDB through the JDBC runtime owned by cpf-starter-common. Retained only as a
 * source-compatibility marker until stale-file cleanup.
 */
@Deprecated(forRemoval = false)
/** Common은 canonical cpfDB/JDBC 관리 경로를 사용하며 별도 CMN MyBatis 런타임을 만들지 않는 호환성 표식입니다. */
public final class CmnMyBatisConfig {
    private CmnMyBatisConfig() { }
}
