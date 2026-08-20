package com.cpf.local.runtime;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * CPF 개발자 기본 Local Runtime입니다.
 *
 * <p>기본 모드는 Gateway 없이 Core/Common/ADM/Backoffice/Generated Domain을 한 JVM·한 HTTP Port에 조립합니다.
 * Generated Domain의 online Component도 classpath descriptor로 자동 편입할 수 있습니다. Batch는 초기 프로젝트 구성에서 선택한 별도 Batch Runtime으로 조립합니다.
 * 운영과 분산 검증에서는 동일 Source를 별도 Process/Instance topology로 실행하므로 업무 코드는
 * Local/Remote 배치 위치를 알 필요가 없습니다.</p>
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
