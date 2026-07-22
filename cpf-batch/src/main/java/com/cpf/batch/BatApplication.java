package com.cpf.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import com.cpf.batch.worker.BatWorkerProperties;

/**
 * CPF BAT 독립 실행 애플리케이션입니다.
 *
 * <p>BAT는 CPF가 제공하는 Batch 공통 API를 사용해 실제 Job과 Step을 실행하는 worker입니다.
 * ADM은 BAT의 실행 상태와 이력을 관제하고, CPF는 공통 실행 Facade와 운영 메타 표준을 제공합니다.</p>
 */
@SpringBootApplication(scanBasePackages = {"com.cpf.batch", "com.cpf.core"})
@EnableScheduling
@EnableConfigurationProperties(BatWorkerProperties.class)
public class BatApplication {

    public static void main(String[] args) {
        SpringApplication.run(BatApplication.class, args);
    }
}
