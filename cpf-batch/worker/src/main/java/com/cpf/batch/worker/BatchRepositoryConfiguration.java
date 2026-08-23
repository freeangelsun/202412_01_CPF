package com.cpf.batch.worker;

import com.cpf.data.persistence.api.database.CpfDatabaseVendor;
import org.springframework.batch.core.configuration.support.JdbcDefaultBatchConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/** Spring Batch 6 JDBC JobRepository를 canonical CPF Platform DB의 BAT_SB_* tables에 연결합니다. */
@Configuration
public class BatchRepositoryConfiguration extends JdbcDefaultBatchConfiguration {
    static final String TABLE_PREFIX = "BAT_SB_";

    private final DataSource dataSource;
    private final PlatformTransactionManager transactionManager;
    private final String databaseType;

    public BatchRepositoryConfiguration(
            @Qualifier("batDataSource") DataSource dataSource,
            @Qualifier("batTransactionManager") PlatformTransactionManager transactionManager,
            Environment environment) {
        this.dataSource = dataSource;
        this.transactionManager = transactionManager;
        this.databaseType = CpfDatabaseVendor.from(
                environment.getProperty("cpf.db.vendor", "mariadb")).springBatchDatabaseType();
    }

    @Override
    protected DataSource getDataSource() {
        return dataSource;
    }

    @Override
    protected PlatformTransactionManager getTransactionManager() {
        return transactionManager;
    }

    @Override
    protected String getDatabaseType() {
        return databaseType;
    }

    @Override
    protected String getTablePrefix() {
        return TABLE_PREFIX;
    }
}
