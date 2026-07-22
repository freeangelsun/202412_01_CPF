package com.cpf.batch.operation;

import com.cpf.core.common.batch.CpfBatchFileLogWriter;
import com.cpf.core.common.batch.CpfBatchRuntimeListener;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BatHealthControllerTest {

    @Test
    void healthDoesNotExposeSensitiveValues() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        BatSmokeOperationService operationService = mock(BatSmokeOperationService.class);
        BatSmokeExecutionRegistry registry = new BatSmokeExecutionRegistry();
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        ObjectProvider<CpfBatchFileLogWriter> fileLogWriterProvider = beanFactory.getBeanProvider(CpfBatchFileLogWriter.class);
        ObjectProvider<CpfBatchRuntimeListener> runtimeListenerProvider = beanFactory.getBeanProvider(CpfBatchRuntimeListener.class);
        BatHealthController controller = new BatHealthController(
                jdbcTemplate,
                new StandardEnvironment(),
                operationService,
                registry,
                fileLogWriterProvider,
                runtimeListenerProvider);

        when(jdbcTemplate.queryForObject("SELECT 1", Integer.class)).thenReturn(1);

        ResponseEntity<Map<String, Object>> response = controller.health();

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsEntry("status", "UP");
        assertThat(response.getBody()).doesNotContainKeys("password", "secret");
        assertThat(response.getBody()).containsKeys("serverInstanceId", "workerId", "database", "smoke");
    }
}
