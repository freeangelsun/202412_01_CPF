package com.cpf.common.spi;

/** Common Product와 Starter runtime 사이에서 공유하는 안정적인 Bean 이름 계약입니다. */
public final class CpfCommonPersistenceNames {
    public static final String DATA_SOURCE_BEAN = "cpfCommonDataSource";
    public static final String JDBC_TEMPLATE_BEAN = "cpfCommonJdbcTemplate";
    public static final String NAMED_JDBC_TEMPLATE_BEAN = "cpfCommonNamedJdbcTemplate";
    public static final String TX_MANAGER_BEAN = "cpfCommonTransactionManager";
    /** Common effective-time, cache and audit consumers의 전용 override 지점입니다. */
    public static final String CLOCK_BEAN = "cpfCommonClock";
    /** CPF_PLATFORM_DB의 플랫폼 공통 DataSource alias입니다. */
    public static final String PLATFORM_DATA_SOURCE_BEAN = "cpfDataSource";
    /** CPF_PLATFORM_DB의 플랫폼 공통 JDBC alias입니다. */
    public static final String PLATFORM_JDBC_TEMPLATE_BEAN = "cpfJdbcTemplate";
    /** CPF_PLATFORM_DB의 플랫폼 공통 TransactionManager alias입니다. */
    public static final String PLATFORM_TX_MANAGER_BEAN = "cpfTransactionManager";
    private CpfCommonPersistenceNames() { }
}
