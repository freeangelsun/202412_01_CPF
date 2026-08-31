package com.cpf.local.runtime;

import javax.sql.DataSource;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 통합 로컬 Runtime에서 CPF Platform 역할 DataSource를 기본 선택으로 지정합니다.
 *
 * <p>1-WAS는 ADM/Backoffice/Common/Platform/Customer DataSource를 한 ApplicationContext에
 * 함께 올립니다. 프레임워크 운영 데이터를 다루는 Starter 자동구성(리질리언스 정책, 메시징
 * 신뢰성, Feature Flag, 로그 정책, Subject Tracking, Runtime Health, 마스킹 정책, 세션 등)은
 * 단일 {@code DataSource}를 주입받도록 선언되어 있어, 후보가 여러 개인 이 구성에서는
 * 기동 자체가 실패합니다. 이 운영 데이터의 정본은 CPF Platform 스키마이므로 통합 실행
 * 단위가 그 역할을 기본값으로 밝혀 줍니다.</p>
 *
 * <p>Bean을 새로 만들지 않고 이미 등록된 {@code cpfPlatformDataSource} 정의에만 primary를
 * 표시하므로 DataSource 개수와 각 역할의 소유권은 그대로 유지됩니다. Platform 역할이
 * 비활성이거나 DataSource가 하나뿐인 구성에서는 아무 것도 바꾸지 않습니다.</p>
 */
@Configuration(proxyBeanMethods = false)
public class CpfLocalRuntimePlatformDataSourcePrimary {
    /** CPF 논리 Platform DB role의 canonical Bean 이름입니다. */
    static final String CPF_PLATFORM_DATA_SOURCE = "cpfPlatformDataSource";

    /** BeanFactoryPostProcessor는 조기 초기화를 피하려고 static @Bean으로 노출합니다. */
    @Bean
    static BeanFactoryPostProcessor cpfLocalRuntimePlatformDataSourcePrimary() {
        return beanFactory -> {
            if (beanFactory.getBeanNamesForType(DataSource.class, false, false).length <= 1) {
                return;
            }
            if (!beanFactory.containsBeanDefinition(CPF_PLATFORM_DATA_SOURCE)) {
                return;
            }
            beanFactory.getBeanDefinition(CPF_PLATFORM_DATA_SOURCE).setPrimary(true);
        };
    }
}
