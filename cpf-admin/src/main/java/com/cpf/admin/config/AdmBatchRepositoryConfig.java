package com.cpf.admin.config;

import com.cpf.core.api.database.CpfDatabaseVendor;
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * ADM 배치 관제가 BAT DB의 Spring Batch 메타 테이블을 사용하도록 고정합니다.
 *
 * <p>ADM은 CPF, BAT, ADM, MBR datasource를 함께 로딩하므로 Spring Batch 자동설정이
 * 단일 datasource를 추론할 수 없습니다. 배치 실행 메타는 BAT 운영 표준에 따라 batDB의
 * BATCH_* 테이블에 적재되도록 명시합니다.</p>
 */
@Configuration
public class AdmBatchRepositoryConfig extends DefaultBatchConfiguration {
    private final DataSource batDataSource;
    private final PlatformTransactionManager batTransactionManager;
    private final String databaseType;

    public AdmBatchRepositoryConfig(
            @Qualifier("batDataSource") DataSource batDataSource,
            @Qualifier("batTransactionManager") PlatformTransactionManager batTransactionManager,
            Environment environment) {
        this.batDataSource = batDataSource;
        this.batTransactionManager = batTransactionManager;
        this.databaseType = CpfDatabaseVendor.from(
                environment.getProperty("cpf.db.vendor", "mariadb"))
                .springBatchDatabaseType();
    }

    @Override
    protected DataSource getDataSource() {
        return batDataSource;
    }

    @Override
    protected PlatformTransactionManager getTransactionManager() {
        return batTransactionManager;
    }

    @Override
    protected String getDatabaseType() {
        return databaseType;
    }
}
