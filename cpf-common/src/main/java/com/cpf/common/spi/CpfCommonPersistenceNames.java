package com.cpf.common.spi;

/** Common Product와 Starter runtime 사이에서 공유하는 안정적인 Bean 이름 계약입니다. */
public final class CpfCommonPersistenceNames {
    public static final String DATA_SOURCE_BEAN = "cpfCommonDataSource";
    public static final String JDBC_TEMPLATE_BEAN = "cpfCommonJdbcTemplate";
    public static final String NAMED_JDBC_TEMPLATE_BEAN = "cpfCommonNamedJdbcTemplate";
    public static final String TX_MANAGER_BEAN = "cpfCommonTransactionManager";
    private CpfCommonPersistenceNames() { }
}
