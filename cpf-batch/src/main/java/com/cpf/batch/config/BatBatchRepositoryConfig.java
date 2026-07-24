package com.cpf.batch.config;

import com.cpf.core.api.database.CpfDatabaseVendor;
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * BAT의 Spring Batch JobRepository를 BAT DB에 고정합니다.
 *
 * <p>Spring Batch 원천 메타는 <code>BATCH_*</code> 테이블에 저장하고, BAT 운영 관제 메타는
 * <code>bat_*</code> 테이블에 별도로 기록합니다.</p>
 */
@Configuration
public class BatBatchRepositoryConfig extends DefaultBatchConfiguration {
    private final DataSource batDataSource;
    private final PlatformTransactionManager batTransactionManager;
    private final String databaseType;

    public BatBatchRepositoryConfig(
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
