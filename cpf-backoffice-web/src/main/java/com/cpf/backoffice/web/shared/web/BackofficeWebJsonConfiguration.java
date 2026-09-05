package com.cpf.backoffice.web.shared.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Backoffice Web 이 사용하는 Jackson 2 ObjectMapper 를 제공합니다.
 *
 * <p>증상 근거: 공개 배포본에서 Backoffice Web 을 기동하면
 * {@code required a bean of type com.fasterxml.jackson.databind.ObjectMapper} 로 실패했다.</p>
 *
 * <p>원인: Spring Boot 4 는 Jackson 3 를 자동구성하므로 Jackson 2 의 {@code ObjectMapper} Bean 은
 * 더 이상 만들어지지 않는다. 이 모듈의 Controller/Service 는 Jackson 2 API 를 직접 쓴다.</p>
 *
 * <p>이 모듈은 CPF Starter 를 소비하지 않는 독립 Channel Front 이므로, 프레임워크 의존을 늘리지 않고
 * 자기 사용 범위에 한해 직접 선언한다. 상위 구성이 Bean 을 제공하면 그쪽이 우선한다.</p>
 *
 * <p>되돌리면 재발할 증상: Channel Front 가 기동조차 하지 못해 공개 사용자가 업무 화면을 열 수 없다.</p>
 */
@Configuration(proxyBeanMethods = false)
public class BackofficeWebJsonConfiguration {

    @Bean
    @ConditionalOnMissingBean(ObjectMapper.class)
    ObjectMapper backofficeWebObjectMapper() {
        return new ObjectMapper().findAndRegisterModules();
    }
}
