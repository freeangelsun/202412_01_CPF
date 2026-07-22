package com.cpf.batch.config;

import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * BAT의 Spring Batch JobRepository를 CPF DB에 고정합니다.
 *
 * <p>Spring Batch 원천 메타는 <code>BATCH_*</code> 테이블에 저장하고, CPF 운영 관제 메타는
 * CPF 공통 API가 <code>cpf_batch_*</code> 테이블에 별도로 기록합니다.</p>
 */
@Configuration
public class BatBatchRepositoryConfig extends DefaultBatchConfiguration {
    private final DataSource cpfDataSource;
    private final PlatformTransactionManager cpfTransactionManager;

    public BatBatchRepositoryConfig(
            @Qualifier("cpfDataSource") DataSource cpfDataSource,
            @Qualifier("cpfTransactionManager") PlatformTransactionManager cpfTransactionManager) {
        this.cpfDataSource = cpfDataSource;
        this.cpfTransactionManager = cpfTransactionManager;
    }

    @Override
    protected DataSource getDataSource() {
        return cpfDataSource;
    }

    @Override
    protected PlatformTransactionManager getTransactionManager() {
        return cpfTransactionManager;
    }

    @Override
    protected String getDatabaseType() {
        return "MYSQL";
    }
}
