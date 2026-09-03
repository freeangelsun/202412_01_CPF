package com.cpf.web.runtime;

import com.cpf.foundation.id.spi.CpfExecutionIdGenerator;
import com.cpf.foundation.id.spi.CpfTransactionIdGenerator;
import com.cpf.foundation.time.spi.CpfBusinessDateProvider;
import com.cpf.foundation.runtime.CpfRuntimeMetadata;
import com.cpf.foundation.tracking.CpfSubjectCollector;
import org.springframework.beans.factory.ObjectProvider;
import com.cpf.web.context.CpfConfiguredIngressTrustResolver;
import com.cpf.web.context.CpfDefaultHeaderFailureRecorder;
import com.cpf.web.context.CpfHeaderFailureRecorder;
import com.cpf.web.context.CpfHeaderPolicyProperties;
import com.cpf.web.context.CpfHeaderPolicyRegistry;
import com.cpf.web.context.CpfHttpInboundContextAdapter;
import com.cpf.web.context.CpfHttpIngressTrustResolver;
import com.cpf.web.context.CpfHttpOutboundContextAdapter;
import com.cpf.web.context.CpfRuntimeIdentity;
import com.cpf.web.context.CpfTrustedProxyClientIpResolver;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration;
import org.springframework.boot.actuate.endpoint.web.PathMappedEndpoints;
import jakarta.servlet.Filter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;

/** Installs the canonical HTTP context/trust boundary for the Web Profile. */
@AutoConfiguration(after = WebEndpointAutoConfiguration.class)
@ConditionalOnClass(Filter.class)
@EnableConfigurationProperties({CpfHeaderPolicyProperties.class, CpfWebContextProperties.class})
public class CpfWebContextAutoConfiguration {
    @Bean @ConditionalOnMissingBean
    // SystemCode 가 없는 Component(ADM/Gateway/Channel Front)는 정본 ChannelCode 로 lineage 를
    // 구성한다. 그 선언을 Runtime Identity 에 함께 전달한다(Harness 30.11).
    CpfRuntimeIdentity cpfRuntimeIdentity(CpfRuntimeMetadata runtime, Environment environment) {
        return CpfRuntimeIdentity.from(runtime,
                environment == null ? null : environment.getProperty(CpfRuntimeIdentity.CHANNEL_CODE_PROPERTY));
    }

    @Bean @ConditionalOnMissingBean
    CpfHeaderPolicyRegistry cpfHeaderPolicyRegistry(CpfHeaderPolicyProperties properties) { return new CpfHeaderPolicyRegistry(properties); }

    @Bean @ConditionalOnMissingBean
    CpfHttpIngressTrustResolver cpfHttpIngressTrustResolver(Environment environment) { return new CpfConfiguredIngressTrustResolver(environment); }

    @Bean @ConditionalOnMissingBean
    CpfTrustedProxyClientIpResolver cpfTrustedProxyClientIpResolver(Environment environment) { return new CpfTrustedProxyClientIpResolver(environment); }

    @Bean @ConditionalOnMissingBean
    CpfHeaderFailureRecorder cpfHeaderFailureRecorder(ApplicationEventPublisher publisher) { return new CpfDefaultHeaderFailureRecorder(publisher); }

    @Bean @ConditionalOnMissingBean
    CpfHttpInboundContextAdapter cpfHttpInboundContextAdapter(CpfTransactionIdGenerator tx, CpfExecutionIdGenerator ex) {
        return new CpfHttpInboundContextAdapter(tx, ex);
    }

    @Bean @ConditionalOnMissingBean
    CpfHttpOutboundContextAdapter cpfHttpOutboundContextAdapter(CpfRuntimeIdentity runtime, CpfHeaderPolicyRegistry policies) {
        return new CpfHttpOutboundContextAdapter(runtime, policies);
    }

    @Bean @ConditionalOnMissingBean
    CpfWebContextFilter cpfWebContextFilter(CpfHttpInboundContextAdapter inbound, CpfBusinessDateProvider dates,
            CpfTransactionIdGenerator transactionIds, CpfHttpIngressTrustResolver trustResolver,
            CpfTrustedProxyClientIpResolver clientIpResolver, CpfHeaderPolicyRegistry policies,
            CpfHeaderFailureRecorder failures, CpfRuntimeIdentity runtime,
            ObjectProvider<CpfSubjectCollector> subjectCollector, Environment environment,
            ObjectProvider<PathMappedEndpoints> pathMappedEndpoints,
            CpfWebContextProperties webContextProperties) {
        PathMappedEndpoints managementPaths = pathMappedEndpoints.getIfAvailable();
        java.util.ArrayList<String> managementRoots = new java.util.ArrayList<>();
        if (managementPaths != null) managementRoots.addAll(managementPaths.getAllRootPaths());
        // OpenAPI 문서 표면은 actuator 와 같은 범주다. 업무 거래가 아니므로 Canonical System6 를
        // 요구하지 않는다. 기본 면제가 없으면 Runtime OpenAPI 계약 검증(/v3/api-docs 조회)이
        // CPF_HEADER_FAILURE 로 400 을 받는다. 실제로 Gateway 가 그 이유로 실패했고, 같은 표면을
        // 노출하는 ADM/1-WAS/Domain 도 동일하게 막힌다. Runtime 별 YML 로 중복 선언하지 않고
        // 이 표면의 Owner 인 web starter 가 한 곳에서 면제한다.
        managementRoots.add(environment.getProperty("cpf.openapi.webmvc.api-docs-path", "/v3/api-docs"));
        managementRoots.add(environment.getProperty("springdoc.swagger-ui.path", "/swagger-ui"));
        managementRoots.add("/swagger-ui.html");
        managementRoots.addAll(webContextProperties.getManagementRootPaths());
        return new CpfWebContextFilter(inbound, dates, transactionIds, trustResolver, clientIpResolver, policies, failures, runtime,
                subjectCollector.getIfAvailable(),
                environment.getProperty("management.endpoints.web.base-path", "/actuator"),
                managementRoots);
    }

    @Bean
    FilterRegistrationBean<CpfWebContextFilter> cpfWebContextFilterRegistration(CpfWebContextFilter filter) {
        var registration = new FilterRegistrationBean<>(filter);
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 20);
        registration.setName("cpfWebContextFilter");
        return registration;
    }
}
