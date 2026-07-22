package cpf.xyz.batch.config;

import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * XYZ 교육 배치가 PFW DB의 Spring Batch 표준 저장소를 사용하도록 지정합니다.
 *
 * <p>CPF 로컬 실행은 PFW/CMN datasource를 함께 로딩하므로 Spring Batch가 사용할
 * datasource를 명확히 지정해야 합니다. 이 설정은 EDU Job 실행 이력이
 * pfwDB의 BATCH_* 테이블에 적재되도록 고정합니다.</p>
 */
@Configuration
public class XyzBatchRepositoryConfig extends DefaultBatchConfiguration {
    private final DataSource pfwDataSource;
    private final PlatformTransactionManager pfwTransactionManager;

    public XyzBatchRepositoryConfig(
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
