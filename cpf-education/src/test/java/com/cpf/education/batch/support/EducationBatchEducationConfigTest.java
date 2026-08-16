package com.cpf.education.batch.support;
import com.cpf.batch.api.CpfBatchJob;
import com.cpf.education.batch.support.config.EducationBatchEducationConfig;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.transaction.PlatformTransactionManager;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class EducationBatchEducationConfigTest {

    @Test
    void declaresStableTaskletChunkAndRetryJobContracts() {
        Map<String, CpfBatchJob> contracts = Arrays.stream(EducationBatchEducationConfig.class.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(CpfBatchJob.class))
                .collect(Collectors.toMap(Method::getName, method -> method.getAnnotation(CpfBatchJob.class)));

        assertThat(contracts).hasSize(3);
        assertThat(contracts.get("cpfEduTaskletJob").id()).isEqualTo("BEDUAA0003");
        assertThat(contracts.get("cpfEduChunkJob").id()).isEqualTo("BEDUAA0001");
        assertThat(contracts.get("cpfEduRetryJob").id()).isEqualTo("BEDUAA0002");
        assertThat(contracts.values())
                .extracting(CpfBatchJob::ownerDomain)
                .containsOnly("EDU");
    }

    @Test
    void buildsAllEducationJobsAndStepsWithExpectedNames() {
        EducationBatchEducationConfig config = new EducationBatchEducationConfig();
        JobRepository repository = mock(JobRepository.class);
        PlatformTransactionManager transactionManager = mock(PlatformTransactionManager.class);

        Step taskletStep = config.cpfEduTaskletStep(repository, transactionManager);
        Step chunkStep = config.cpfEduChunkStep(repository, transactionManager);
        Step retryStep = config.cpfEduRetryStep(repository, transactionManager);

        assertThat(Map.of(
                taskletStep.getName(), taskletStep,
                chunkStep.getName(), chunkStep,
                retryStep.getName(), retryStep))
                .containsKeys("CPF_EDU_TASKLET_STEP", "CPF_EDU_CHUNK_STEP", "CPF_EDU_RETRY_STEP");

        assertJobName(config.cpfEduTaskletJob(repository, taskletStep), "CPF_EDU_TASKLET_JOB");
        assertJobName(config.cpfEduChunkJob(repository, chunkStep), "CPF_EDU_CHUNK_JOB");
        assertJobName(config.cpfEduRetryJob(repository, retryStep), "CPF_EDU_RETRY_JOB");
    }

    private void assertJobName(Job job, String expectedName) {
        assertThat(job.getName()).isEqualTo(expectedName);
    }
}
