package com.cpf.batch.execution;

import com.cpf.batch.api.annotation.CpfBatchJob;
import com.cpf.batch.spi.BatchStepHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

/** CPF Batch Annotation Golden Path를 기존 BatchStepHandler Runtime에 연결합니다. */
@AutoConfiguration(before = CpfBatchExecutionAutoConfiguration.class)
@ConditionalOnClass({CpfBatchJob.class, BatchStepHandler.class})
@ConditionalOnProperty(prefix = "cpf.batch.annotation", name = "enabled", havingValue = "true", matchIfMissing = true)
public class CpfBatchAnnotationAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    CpfAnnotatedBatchStepHandler cpfAnnotatedBatchStepHandler(ApplicationContext context) {
        return new CpfAnnotatedBatchStepHandler(context);
    }
}
