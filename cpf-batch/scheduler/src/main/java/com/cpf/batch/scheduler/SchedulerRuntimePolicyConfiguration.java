package com.cpf.batch.scheduler;

import com.cpf.batch.runtime.BatchRuntimePolicy;
import com.cpf.batch.runtime.BatchRuntimePolicyApplier;
import com.cpf.core.api.runtimecontrol.CpfRuntimeChangeApplier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Scheduler instance가 실제 소비하는 schedule/calendar Runtime capability만 등록합니다. */
@Configuration
public class SchedulerRuntimePolicyConfiguration {
    @Bean(name = "batchScheduleRuntimeApplier")
    @ConditionalOnMissingBean(name = "batchScheduleRuntimeApplier")
    CpfRuntimeChangeApplier batchScheduleRuntimeApplier(BatchRuntimePolicy policy) {
        return new BatchRuntimePolicyApplier(BatchRuntimePolicyApplier.SCHEDULE, policy);
    }

    @Bean(name = "batchCalendarRuntimeApplier")
    @ConditionalOnMissingBean(name = "batchCalendarRuntimeApplier")
    CpfRuntimeChangeApplier batchCalendarRuntimeApplier(BatchRuntimePolicy policy) {
        return new BatchRuntimePolicyApplier(BatchRuntimePolicyApplier.CALENDAR, policy);
    }
}
