package com.cpf.batch.scheduler;

import com.github.kagkarlsson.scheduler.Scheduler;
import com.github.kagkarlsson.scheduler.task.helper.RecurringTask;
import com.github.kagkarlsson.scheduler.task.helper.Tasks;
import com.github.kagkarlsson.scheduler.task.schedule.FixedDelay;
import java.time.Duration;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** db-scheduler는 Trigger 시각·Claim·다중 인스턴스 실행만 소유하고 실제 업무 실행은 Spring Batch로 위임합니다. */
@Configuration(proxyBeanMethods = false)
public class DbSchedulerPrimaryConfiguration {
    @Bean
    RecurringTask<Void> cpfBatchDispatchTask(
            SchedulerDispatchService service,
            @Value("${cpf.batch.scheduler.dispatch-ms:1000}") long delayMs) {
        return Tasks.recurring(
                        "cpf-batch-trigger-dispatch",
                        FixedDelay.of(Duration.ofMillis(Math.max(250, delayMs))))
                .execute((instance, context) -> service.dispatchDue());
    }

    @Bean
    Scheduler cpfDbScheduler(
            DataSource dataSource,
            RecurringTask<Void> cpfBatchDispatchTask,
            @Value("${cpf.batch.scheduler.threads:2}") int threads) {
        return Scheduler.create(dataSource, cpfBatchDispatchTask)
                .threads(Math.max(1, threads))
                .build();
    }

    @Bean
    SmartLifecycle cpfDbSchedulerLifecycle(Scheduler scheduler) {
        return new SmartLifecycle() {
            private volatile boolean running;

            @Override
            public void start() {
                scheduler.start();
                running = true;
            }

            @Override
            public void stop() {
                scheduler.stop();
                running = false;
            }

            @Override
            public boolean isRunning() {
                return running;
            }

            @Override
            public int getPhase() {
                return Integer.MAX_VALUE - 100;
            }

            @Override
            public boolean isAutoStartup() {
                return true;
            }
        };
    }
}
