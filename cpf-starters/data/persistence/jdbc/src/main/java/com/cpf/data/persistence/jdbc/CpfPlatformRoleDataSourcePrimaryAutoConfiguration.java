package com.cpf.data.persistence.jdbc;

import javax.sql.DataSource;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * CPF Platform role 을 쓰는 Runtime 에서 기본 DataSource/TransactionManager 를 밝힙니다.
 *
 * <p>증상 근거: Platform role 을 켠 ADM 을 단독 기동하면 {@code admDataSource},
 * {@code cpfCommonDataSource}, {@code cpfPlatformDataSource} 세 후보가 생기고, 단일 Bean 을
 * 주입받는 Starter 자동구성(Runtime Health, Spring Session JDBC 등)이
 * "required a single bean, but 3 were found" 로 기동을 실패시켰다.</p>
 *
 * <p>원인: 같은 판정이 One-WAS 통합 Runtime 모듈에만 있었다. 통합 Runtime 으로만 기동해 온
 * 탓에 Platform Runtime 단독 기동 경로에서는 이 기본 선택이 존재하지 않았다.</p>
 *
 * <p>Bean 을 새로 만들지 않고 이미 등록된 정의에 primary 만 표시하므로 DataSource 개수와 각
 * 역할의 소유권은 그대로다. 후보가 하나뿐이거나 Platform role 이 꺼져 있으면 아무 것도 바꾸지
 * 않는다.</p>
 *
 * <p>되돌리면 재발할 증상: Platform role 을 쓰는 Runtime 이 단독 기동에서 다시
 * "required a single bean" 으로 죽는다.</p>
 */
@AutoConfiguration(after = CpfJdbcRoleDataSourceAutoConfiguration.class)
@ConditionalOnProperty(
        prefix = "cpf.data.persistence.jdbc.role-datasources.cpf-platform-db",
        name = "enabled",
        havingValue = "true")
public class CpfPlatformRoleDataSourcePrimaryAutoConfiguration {

    /** CPF 논리 Platform DB role 의 canonical Bean 이름입니다. */
    static final String CPF_PLATFORM_DATA_SOURCE = "cpfPlatformDataSource";

    /** CPF 논리 Platform DB role 의 canonical TransactionManager Bean 이름입니다. */
    static final String CPF_PLATFORM_TRANSACTION_MANAGER = "cpfPlatformTransactionManager";

    /** BeanFactoryPostProcessor 는 조기 초기화를 피하려고 static @Bean 으로 노출합니다. */
    @Bean
    static BeanFactoryPostProcessor cpfPlatformRoleDataSourcePrimary() {
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

    /** 단일 {@code PlatformTransactionManager} 를 요구하는 자동구성도 같은 이유로 기본 선택이 필요하다. */
    @Bean
    static BeanFactoryPostProcessor cpfPlatformRoleTransactionManagerPrimary() {
        return beanFactory -> {
            if (beanFactory.getBeanNamesForType(PlatformTransactionManager.class, false, false).length <= 1) {
                return;
            }
            if (!beanFactory.containsBeanDefinition(CPF_PLATFORM_TRANSACTION_MANAGER)) {
                return;
            }
            beanFactory.getBeanDefinition(CPF_PLATFORM_TRANSACTION_MANAGER).setPrimary(true);
        };
    }
}
