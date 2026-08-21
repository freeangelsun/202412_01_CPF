package com.cpf.education.contract;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.cpf.batch.api.annotation.CpfBatchJob;
import com.cpf.batch.api.annotation.CpfBatchStep;
import com.cpf.batch.spi.BatchStepHandler.BatchStepCommand;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Canonical EDU Batch 15개가 metadata-only 예제가 아니라 실제 Job -> Service 실행 경계를 가지는지 검증합니다.
 */
class BatchCanonical15ExecutableContractTest {

    private static final List<Class<?>> JOBS = List.of(
            com.cpf.education.batch.centercut.job.SettlementCenterCutJob.class,
            com.cpf.education.batch.chunk.job.CustomerMigrationChunkJob.class,
            com.cpf.education.batch.chunktransaction.job.CustomerChunkTransactionJob.class,
            com.cpf.education.batch.conditionalflow.job.CustomerConditionalFlowJob.class,
            com.cpf.education.batch.distributedworker.job.SettlementWorkerJob.class,
            com.cpf.education.batch.externalcall.job.ExternalSettlementJob.class,
            com.cpf.education.batch.flatfile.job.SettlementFileJob.class,
            com.cpf.education.batch.ondemand.job.MemberExportJob.class,
            com.cpf.education.batch.partition.job.MemberPartitionJob.class,
            com.cpf.education.batch.requiresnew.job.BatchAuditRequiresNewJob.class,
            com.cpf.education.batch.restart.job.BillingRestartJob.class,
            com.cpf.education.batch.scheduler.job.BusinessDateSchedulerJob.class,
            com.cpf.education.batch.shellcommand.job.ApprovedCommandJob.class,
            com.cpf.education.batch.steptransaction.job.SettlementStepTransactionJob.class,
            com.cpf.education.batch.tasklet.job.DailyCleanupTaskletJob.class
    );

    @Test
    void allCanonicalBatchJobsInvokeTheirServiceSteps() throws Exception {
        assertEquals(15, JOBS.size(), "Canonical Batch group count");
        BatchStepCommand command = mock(BatchStepCommand.class);
        int invokedSteps = 0;

        for (Class<?> jobType : JOBS) {
            assertNotNull(jobType.getAnnotation(CpfBatchJob.class), jobType.getName() + " must be @CpfBatchJob");
            Constructor<?>[] constructors = jobType.getConstructors();
            assertEquals(1, constructors.length, jobType.getName() + " must expose one public constructor");
            Constructor<?> constructor = constructors[0];
            assertEquals(1, constructor.getParameterCount(), jobType.getName() + " must delegate to one feature service");

            Class<?> serviceType = constructor.getParameterTypes()[0];
            Object service = mock(serviceType);
            Object job = constructor.newInstance(service);

            for (Method step : jobType.getDeclaredMethods()) {
                if (step.getAnnotation(CpfBatchStep.class) == null) continue;
                assertEquals(1, step.getParameterCount(), jobType.getName() + "." + step.getName() + " command boundary");
                assertEquals(BatchStepCommand.class, step.getParameterTypes()[0], jobType.getName() + "." + step.getName());

                step.invoke(job, command);
                Method serviceMethod = serviceType.getMethod(step.getName(), BatchStepCommand.class);
                Object verified = verify(service);
                serviceMethod.invoke(verified, command);
                invokedSteps++;
            }
        }

        // conditional-flow와 step-transaction은 2-step이며 나머지는 1-step입니다.
        assertEquals(17, invokedSteps, "Canonical Batch 15의 실제 Step 실행 수");
    }
}
