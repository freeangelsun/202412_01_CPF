package com.cpf.local.runtime;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Gateway·ADM·BZA와 선택한 Domain을 한 JVM·한 Port에서 실행하는 개발 전용 Runtime입니다.
 *
 * <p>Production 배포 Artifact가 아니며 {@code cpf.local.runtime.enabled=true}와 local 계열
 * Profile이 모두 확인될 때만 기동합니다. Batch는 {@code cpf-local-batch-runtime}에서 별도
 * Process로 실행합니다.</p>
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@EnableScheduling
@Import({CpfLocalRuntimeModules.class, CpfLocalDomainModuleRegistrar.class})
public class CpfLocalRuntimeApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(CpfLocalRuntimeApplication.class);
        application.addInitializers(new CpfLocalRuntimeSafetyGuard());
        application.run(args);
    }
}
