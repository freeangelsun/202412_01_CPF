package com.cpf.reference.batch.config;

import com.cpf.core.api.database.CpfDatabaseVendor;
import org.springframework.batch.core.configuration.support.JdbcDefaultBatchConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * REF 교육 배치가 BAT DB의 Spring Batch 표준 저장소를 사용하도록 지정합니다.
 *
 * <p>REF 로컬 실행은 CPF/REF/BAT datasource를 함께 로딩하므로 Spring Batch가 사용할
 * datasource를 명확히 지정해야 합니다. 이 설정은 EDU Job 실행 이력이
 * batDB의 BATCH_* 테이블에 적재되도록 고정합니다.</p>
 */
@Configuration
@ConditionalOnProperty(prefix = "cpf.reference.edu.batch", name = "local-runtime-enabled", havingValue = "true", matchIfMissing = false)
public class ReferenceBatchRepositoryConfig extends JdbcDefaultBatchConfiguration {
    private final DataSource batDataSource;
    private final PlatformTransactionManager batTransactionManager;
    private final String databaseType;

    public ReferenceBatchRepositoryConfig(
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
