package com.cpf.security.common;

import com.cpf.security.common.crypto.CmnCryptoService;
import com.cpf.security.common.token.CmnJwtService;
import com.cpf.security.common.token.CmnOAuthBearerTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * CMN 공통 보안 Service 를 Starter AutoConfiguration 으로 조립합니다.
 *
 * <p>증상 근거: ADM 을 단독 기동하면 {@code CmnCryptoService} 를, MBW 를 단독 기동하면
 * {@code CmnJwtService} 를 찾지 못해 기동에 실패했다. 이 Service 들은 {@code @Service} 로만
 * 선언돼 있어 소비 Runtime 이 {@code com.cpf.security.common} 을 직접 component scan 해야 했다.
 * 통합 Runtime 모듈만 그 package 를 스캔했기 때문에 단독 기동 경로에서는 존재하지 않았다.</p>
 *
 * <p>업무 Runtime 은 "CPF Framework 기능은 Public Starter 의 AutoConfiguration 으로 조립한다" 는
 * 계약을 따른다. Starter 의 Bean 을 소비자가 scan 해야만 생기는 구조는 그 계약을 깨뜨린다.</p>
 *
 * <p>이미 component scan 으로 등록된 환경(통합 Runtime 등)에서는
 * {@link ConditionalOnMissingBean} 으로 물러나므로 Bean 이 중복되지 않는다.</p>
 *
 * <p>되돌리면 재발할 증상: Platform/업무 Runtime 을 단독 기동할 때 CMN 보안 Service 를 찾지 못해
 * 기동 자체가 실패한다.</p>
 */
@AutoConfiguration
@ConditionalOnClass(ObjectMapper.class)
public class CpfSecurityCommonAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    CmnCryptoService cmnCryptoService() {
        return new CmnCryptoService();
    }

    @Bean
    @ConditionalOnMissingBean
    CmnJwtService cmnJwtService(ObjectMapper objectMapper, CmnCryptoService cryptoService) {
        return new CmnJwtService(objectMapper, cryptoService);
    }

    @Bean
    @ConditionalOnMissingBean
    CmnOAuthBearerTokenService cmnOAuthBearerTokenService(CmnJwtService jwtService) {
        return new CmnOAuthBearerTokenService(jwtService);
    }
}
