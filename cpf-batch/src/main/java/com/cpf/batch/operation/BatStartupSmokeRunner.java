package com.cpf.batch.operation;

import com.cpf.batch.job.smoke.BatSmokeJobConfig;
import com.cpf.batch.job.failure.BatFailureJobConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * BAT runtime smoke에서 애플리케이션 기동 직후 최소 Job을 실행합니다.
 */
@Component
@ConditionalOnProperty(prefix = "cpf.bat.smoke", name = "run-on-startup", havingValue = "true")
public class BatStartupSmokeRunner implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(BatStartupSmokeRunner.class);

    private final BatSmokeOperationService operationService;
    private final BatSmokeExecutionRegistry registry;
    private final boolean runFailureOnStartup;

    public BatStartupSmokeRunner(
            BatSmokeOperationService operationService,
            BatSmokeExecutionRegistry registry,
            org.springframework.core.env.Environment environment) {
        this.operationService = operationService;
        this.registry = registry;
        this.runFailureOnStartup = environment.getProperty(
                "cpf.bat.smoke.run-failure-on-startup",
                Boolean.class,
                false);
    }

    @Override
    public void run(ApplicationArguments args) {
        Map<String, Object> success = operationService.run(
                BatSmokeJobConfig.SMOKE_JOB_ID,
                "BAT startup smoke 정상 Job 실행");
        registry.recordSuccess(success);
        log.info("BAT startup smoke success result={}", success);

        if (runFailureOnStartup) {
            Map<String, Object> failure = operationService.run(
                    BatFailureJobConfig.JOB_ID,
                    "BAT startup smoke 실패 Job 실행");
            registry.recordFailure(failure);
            log.info("BAT startup smoke failure result={}", failure);
        }
    }
}
