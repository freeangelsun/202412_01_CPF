package cpf.bat.config;

import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * BAT의 Spring Batch JobRepository를 PFW DB에 고정합니다.
 *
 * <p>Spring Batch 원천 메타는 <code>BATCH_*</code> 테이블에 저장하고, CPF 운영 관제 메타는
 * PFW 공통 API가 <code>pfw_batch_*</code> 테이블에 별도로 기록합니다.</p>
 */
@Configuration
public class BatBatchRepositoryConfig extends DefaultBatchConfiguration {
    private final DataSource pfwDataSource;
    private final PlatformTransactionManager pfwTransactionManager;

    public BatBatchRepositoryConfig(
            @Qualifier("pfwDataSource") DataSource pfwDataSource,
            @Qualifier("pfwTransactionManager") PlatformTransactionManager pfwTransactionManager) {
        this.pfwDataSource = pfwDataSource;
        this.pfwTransactionManager = pfwTransactionManager;
    }

    @Override
    protected DataSource getDataSource() {
        return pfwDataSource;
    }

    @Override
    protected PlatformTransactionManager getTransactionManager() {
        return pfwTransactionManager;
    }

    @Override
    protected String getDatabaseType() {
        return "MYSQL";
    }
}
