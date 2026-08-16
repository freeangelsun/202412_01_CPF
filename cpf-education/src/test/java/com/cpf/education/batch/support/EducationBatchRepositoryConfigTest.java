package com.cpf.education.batch.support;
import com.cpf.education.batch.support.config.EducationBatchRepositoryConfig;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.mock.env.MockEnvironment;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class EducationBatchRepositoryConfigTest {

    @Test
    void usesBatMetadataResources() {
        DataSource educationBatchPlatformDataSource = mock(DataSource.class);
        PlatformTransactionManager educationBatchPlatformTransactionManager = mock(PlatformTransactionManager.class);
        TestableConfig config = new TestableConfig(educationBatchPlatformDataSource, educationBatchPlatformTransactionManager);

        assertThat(config.dataSource()).isSameAs(educationBatchPlatformDataSource);
        assertThat(config.transactionManager()).isSameAs(educationBatchPlatformTransactionManager);
    }

    private static final class TestableConfig extends EducationBatchRepositoryConfig {
        private TestableConfig(
                DataSource educationBatchPlatformDataSource,
                PlatformTransactionManager educationBatchPlatformTransactionManager) {
            super(educationBatchPlatformDataSource, educationBatchPlatformTransactionManager, new MockEnvironment());
        }

        private DataSource dataSource() {
            return getDataSource();
        }

        private PlatformTransactionManager transactionManager() {
            return getTransactionManager();
        }
    }
}
