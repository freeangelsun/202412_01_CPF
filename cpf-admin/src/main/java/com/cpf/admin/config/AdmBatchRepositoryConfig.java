package com.cpf.admin.config;

import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * ADM 배치 관제가 CPF DB의 Spring Batch 메타 테이블을 사용하도록 고정합니다.
 *
 * <p>ADM은 CPF, CMN, ADM, MBR datasource를 함께 로딩하므로 Spring Batch 자동설정이
 * 단일 datasource를 추론할 수 없습니다. 배치 실행 메타는 CPF 운영 표준에 따라 cpfDB의
 * BATCH_* 테이블에 적재되도록 명시합니다.</p>
 */
@Configuration
public class AdmBatchRepositoryConfig extends DefaultBatchConfiguration {
    private final DataSource cpfDataSource;
    private final PlatformTransactionManager cpfTransactionManager;

    public AdmBatchRepositoryConfig(
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
