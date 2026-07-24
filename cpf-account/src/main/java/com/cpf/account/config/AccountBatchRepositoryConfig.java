package com.cpf.account.config;

import com.cpf.core.api.database.CpfDatabaseVendor;
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * ACC의 Spring Batch 원천 메타 저장소를 BAT DB에 고정합니다.
 *
 * <p>Job/Step 실행 메타는 {@code batDB.BATCH_*}에 저장하고, ACC 업무 데이터 변경은
 * {@code accTransactionManager}를 사용합니다. 이 경계를 통해 ACC 업무 DB에 Spring Batch
 * 내부 테이블이 생성되거나 조회되는 것을 방지합니다.</p>
 */
@Configuration
public class AccountBatchRepositoryConfig extends DefaultBatchConfiguration {
    private final DataSource batDataSource;
    private final PlatformTransactionManager batTransactionManager;
    private final String databaseType;

    public AccountBatchRepositoryConfig(
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
