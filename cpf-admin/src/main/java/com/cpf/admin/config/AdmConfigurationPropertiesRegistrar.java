package com.cpf.admin.config;

import com.cpf.admin.opr.parameter.AdmParameterReferenceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * ADM이 스스로 소유하는 {@code @ConfigurationProperties} 등록입니다.
 *
 * <p>ADM은 CPF가 제공하는 Framework/Platform 관리 기능이므로, ADM Component가 요구하는 설정 Bean은
 * ADM을 조립한 어떤 Runtime에서도 존재해야 합니다. 이 등록이 {@code AdmApplication}에만 있으면
 * 그 Main Class를 쓰지 않는 조립 Runtime(1-WAS 등)에서는 component-scan으로 올라온 ADM Bean이
 * {@code required a bean of type ... Properties} 로 기동을 실패시킵니다. 실제로
 * {@code AdmParameterReferenceCatalogAdapter}가 그 이유로 1-WAS 기동을 막았습니다.</p>
 *
 * <p>이 Configuration은 ADM Component와 같은 package tree에 있어 component-scan으로 함께 올라오므로,
 * 조립하는 Runtime이 ADM의 내부 요구사항을 알 필요가 없습니다.</p>
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({
        AdmBootstrapProperties.class,
        AdmPasswordPolicyProperties.class,
        AdmSecurityProperties.class,
        AdmParameterReferenceProperties.class})
public class AdmConfigurationPropertiesRegistrar {
}
