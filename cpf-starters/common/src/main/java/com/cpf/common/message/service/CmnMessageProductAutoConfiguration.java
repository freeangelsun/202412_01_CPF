package com.cpf.common.message.service;

import com.cpf.common.message.api.CpfErrorCatalogResolver;
import com.cpf.common.message.api.CpfErrorCatalogSignalSink;
import java.time.Clock;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * cpf-common 메시지 Product Service의 runtime 조립을 Starter 경계에서 담당합니다.
 *
 * <p>Product owner인 {@code cpf-common}은 업무 공통 API/서비스 구현만 보유하고,
 * 환경 조건과 Bean 선택은 이 AutoConfiguration이 책임집니다.</p>
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "cpf.common", name = "runtime-mode", havingValue = "product", matchIfMissing = true)
public class CmnMessageProductAutoConfiguration {

    @Bean
    @Primary
    @ConditionalOnMissingBean(CpfErrorCatalogResolver.class)
    @ConditionalOnBean({CmnErrorCatalogStore.class, CpfErrorCatalogSignalSink.class, CmnMessageArgumentPolicy.class})
    CpfErrorCatalogResolver cpfCommonErrorCatalogResolver(
            CmnErrorCatalogStore store,
            CpfErrorCatalogSignalSink signals,
            CmnMessageArgumentPolicy policy,
            Clock clock) {
        return new CmnCpfErrorCatalogResolver(store, signals, policy, clock);
    }
}
