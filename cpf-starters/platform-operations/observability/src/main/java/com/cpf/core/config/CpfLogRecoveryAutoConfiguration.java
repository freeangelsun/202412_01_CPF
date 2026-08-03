package com.cpf.core.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * CPF DB 거래 로그 복구 worker의 주기 실행을 활성화합니다.
 */
@AutoConfiguration
@EnableScheduling
@ConditionalOnProperty(
        prefix = "cpf.logging.db-fallback",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
public class CpfLogRecoveryAutoConfiguration {
}
