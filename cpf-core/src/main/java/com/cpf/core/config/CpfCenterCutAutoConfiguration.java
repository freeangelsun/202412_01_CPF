package com.cpf.core.config;

import com.cpf.core.common.batch.centercut.CpfCenterCutService;
import com.cpf.core.common.logging.TransactionIdGenerator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * center-cut 엔진을 사용하는 실행 모듈에 기본 서비스를 제공합니다.
 *
 * <p>업무 모듈이 전용 구현을 등록하면 해당 bean을 우선하며, 별도 구현이 없을 때만
 * CPF 거래 ID 생성기를 연결한 표준 서비스를 생성합니다.</p>
 */
@AutoConfiguration
public class CpfCenterCutAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(CpfCenterCutService.class)
    public CpfCenterCutService cpfCenterCutService(TransactionIdGenerator transactionIdGenerator) {
        return new CpfCenterCutService(transactionIdGenerator::generate);
    }
}
