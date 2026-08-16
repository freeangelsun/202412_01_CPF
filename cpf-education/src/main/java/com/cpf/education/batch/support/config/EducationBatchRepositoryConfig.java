package com.cpf.education.batch.support.config;
import com.cpf.data.persistence.api.database.CpfDatabaseVendor;
import org.springframework.batch.core.configuration.support.JdbcDefaultBatchConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * EDU 교육 배치가 CPF Platform DB의 Spring Batch 표준 저장소를 사용하도록 지정합니다.
 *
 * <p>EDU 로컬 실행은 CPF/EDU/BAT datasource를 함께 로딩하므로 Spring Batch가 사용할
 * datasource를 명확히 지정해야 합니다. 이 설정은 EDU Job 실행 이력이
 * cpfDB의 BATCH_* 테이블에 적재되도록 고정합니다.</p>
 */
@Configuration
@ConditionalOnProperty(prefix = "cpf.education.edu.batch", name = "local-runtime-enabled", havingValue = "true", matchIfMissing = false)
public class EducationBatchRepositoryConfig extends JdbcDefaultBatchConfiguration {
    private final DataSource educationBatchPlatformDataSource;
    private final PlatformTransactionManager educationBatchPlatformTransactionManager;
    private final String databaseType;

    /** EducationBatchRepositoryConfig 작업을 CPF 표준 계약에 따라 수행한다. */
    public EducationBatchRepositoryConfig(
            @Qualifier("educationBatchPlatformDataSource") DataSource educationBatchPlatformDataSource,
            @Qualifier("educationBatchPlatformTransactionManager") PlatformTransactionManager educationBatchPlatformTransactionManager,
            Environment environment) {
        this.educationBatchPlatformDataSource = educationBatchPlatformDataSource;
        this.educationBatchPlatformTransactionManager = educationBatchPlatformTransactionManager;
        this.databaseType = CpfDatabaseVendor.from(
                environment.getProperty("cpf.db.vendor", "mariadb"))
                .springBatchDatabaseType();
    }

    @Override
    protected DataSource getDataSource() {
        return educationBatchPlatformDataSource;
    }

    @Override
    protected PlatformTransactionManager getTransactionManager() {
        return educationBatchPlatformTransactionManager;
    }

    @Override
    protected String getDatabaseType() {
        return databaseType;
    }
}
