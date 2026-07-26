package com.cpf.batch.worker;

import com.cpf.batch.api.*;
import com.cpf.batch.spi.BusinessJobProvider;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.*;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.*;

@Configuration
@ConditionalOnProperty(name="cpf.batch.diagnostic.enabled",havingValue="true")
public class DiagnosticJobConfiguration {
    public static final String JOB_ID="CPF_BAT_DIAGNOSTIC_JOB";

    @Bean("cpfBatDiagnosticStep")
    Step diagnosticStep(JobRepository repository,PlatformTransactionManager tx) {
        return new StepBuilder("cpfBatDiagnosticStep",repository)
            .tasklet((contribution,context)->{
                Map<String,Object> p=context.getStepContext().getJobParameters();
                int iterations=Integer.parseInt(Objects.toString(p.getOrDefault("iterations","1")));
                long sleepMs=Long.parseLong(Objects.toString(p.getOrDefault("sleepMs","50")));
                for(int i=0;i<Math.max(1,iterations);i++) {
                    if(Thread.currentThread().isInterrupted()) throw new InterruptedException("diagnostic job interrupted");
                    Thread.sleep(Math.max(0,sleepMs));
                }
                return RepeatStatus.FINISHED;
            },tx).build();
    }

    @Bean("cpfBatDiagnosticJob")
    Job diagnosticJob(JobRepository repository,@Qualifier("cpfBatDiagnosticStep") Step step) {
        return new JobBuilder(JOB_ID,repository).start(step).build();
    }

    @Bean
    BusinessJobProvider diagnosticJobPackProvider(@Qualifier("cpfBatDiagnosticJob") Job job) {
        return new BusinessJobProvider() {
            public JobPackManifest manifest() {
                return new JobPackManifest("CPF-BAT-DIAGNOSTIC","BAT","com.cpf.batch:cpf-batch-worker","runtime",
                    "RUNTIME_VERIFICATION_ONLY",null,"[1.0.0,2.0.0)",List.of("CPF_BAT_DIAGNOSTIC"),
                    List.of(new JobPackManifest.JobDefinition(JOB_ID,"BAT multi-instance diagnostic",true,
                        List.of(new BatchParameterDefinition("iterations","LONG",false,"1",false,false),
                                new BatchParameterDefinition("sleepMs","LONG",false,"50",false,false)),null,null)),
                    Map.of("scope","local-test-only"));
            }
            public Object resolveJob(String jobId) {
                if(!JOB_ID.equals(jobId)) throw new IllegalArgumentException("Unknown diagnostic jobId");
                return job;
            }
        };
    }
}
