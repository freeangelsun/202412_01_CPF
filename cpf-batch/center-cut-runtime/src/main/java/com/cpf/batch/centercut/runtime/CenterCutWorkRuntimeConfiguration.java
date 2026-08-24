package com.cpf.batch.centercut.runtime;

import com.cpf.batch.spi.CenterCutHandler;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalogProvider;
import com.cpf.foundation.id.spi.CpfExecutionIdGenerator;
import com.cpf.foundation.runtime.CpfRuntimeMetadata;
import com.cpf.integration.api.domaincall.CpfDomainClientRouter;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

/** 실제 Batch Worker가 선택하는 DB 기반 Center-Cut Work Runtime 구성입니다. */
@Configuration(proxyBeanMethods = false)
public class CenterCutWorkRuntimeConfiguration {
    @Bean
    JdbcCenterCutAdmissionControl cpfCenterCutAdmissionControl(
            JdbcTemplate jdbc, CpfVendorSqlCatalogProvider catalogs) {
        return new JdbcCenterCutAdmissionControl(jdbc, catalogs);
    }

    @Bean
    JdbcCenterCutClaimRepository cpfCenterCutClaimRepository(
            JdbcTemplate jdbc,
            JdbcCenterCutAdmissionControl admission,
            CpfVendorSqlCatalogProvider catalogs,
            PlatformTransactionManager transactions) {
        return new JdbcCenterCutClaimRepository(jdbc, admission, catalogs, transactions);
    }

    @Bean
    CpfDomainInvocationCenterCutHandler cpfDomainInvocationCenterCutHandler(
            CpfDomainClientRouter domains, ObjectMapper mapper) {
        return new CpfDomainInvocationCenterCutHandler(domains, mapper);
    }

    @Bean(destroyMethod = "close")
    CenterCutWorkProcessor cpfCenterCutWorkProcessor(
            JdbcCenterCutClaimRepository repository,
            List<CenterCutHandler> handlers,
            List<CenterCutWorkObserver> observers,
            CpfExecutionIdGenerator executionIds,
            CpfRuntimeMetadata runtimeMetadata) {
        return new CenterCutWorkProcessor(
                repository, handlers, observers, executionIds, runtimeMetadata.systemCode());
    }
}
