package com.cpf.reference.batch.operation;

import com.cpf.reference.edu.runtime.consumer.EduBusinessConsumerResult;
import com.cpf.reference.edu.runtime.consumer.EduConsumerBinding;
import com.cpf.reference.edu.runtime.consumer.EduConsumerType;
import com.cpf.reference.edu.runtime.model.EduExecutionCommand;
import com.cpf.reference.edu.runtime.model.EduFailurePoint;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SpringBatchEduBusinessConsumerTest {

    @Test
    void startsNamedJobThroughJobOperatorWithCpfParameters() throws Exception {
        ApplicationContext context = mock(ApplicationContext.class);
        JobOperator operator = mock(JobOperator.class);
        Job job = mock(Job.class);
        JobExecution execution = mock(JobExecution.class);
        when(context.getBean("cpfEduChunkJob", Job.class)).thenReturn(job);
        when(job.getName()).thenReturn("CPF_EDU_CHUNK_JOB");
        when(operator.start(eq(job), any(JobParameters.class))).thenReturn(execution);
        when(execution.getStatus()).thenReturn(BatchStatus.COMPLETED);
        when(execution.getId()).thenReturn(42L);

        EduConsumerBinding binding = new EduConsumerBinding(
                "EDU-BAT-001",
                EduConsumerType.SPRING_BATCH,
                "cpf-reference",
                "cpfEduChunkJob",
                "run",
                "JobOperator",
                "batch.run",
                "",
                30,
                List.of("memberId"));
        EduExecutionCommand command = new EduExecutionCommand(
                "business-1",
                "idempotency-1",
                0,
                "tester",
                Set.of("CPF_EDU_OPERATOR"),
                "REF",
                "education",
                "request-1",
                "trace-1",
                Map.of("memberId", "M001"),
                EduFailurePoint.NONE,
                false,
                false);

        EduBusinessConsumerResult result =
                new SpringBatchEduBusinessConsumer(context, operator).invoke(binding, command, 7L);

        assertThat(result.code()).isEqualTo("SPRING_BATCH_COMPLETED");
        assertThat(result.data()).containsEntry("jobExecutionId", 42L);
        verify(operator).start(eq(job), any(JobParameters.class));
    }
}
