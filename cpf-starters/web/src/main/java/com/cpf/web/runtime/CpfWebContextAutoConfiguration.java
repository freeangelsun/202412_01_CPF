package com.cpf.web.runtime;

import com.cpf.foundation.id.spi.CpfExecutionIdGenerator;
import com.cpf.foundation.id.spi.CpfTransactionIdGenerator;
import com.cpf.foundation.time.spi.CpfBusinessDateProvider;
import com.cpf.foundation.runtime.CpfRuntimeMetadata;
import com.cpf.core.api.tracking.CpfSubjectTrackingOperations;
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
@AutoConfiguration
@ConditionalOnClass(Filter.class)
@EnableConfigurationProperties(CpfHeaderPolicyProperties.class)
public class CpfWebContextAutoConfiguration {
    @Bean @ConditionalOnMissingBean
    CpfRuntimeIdentity cpfRuntimeIdentity(CpfRuntimeMetadata runtime) { return CpfRuntimeIdentity.from(runtime); }

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
            ObjectProvider<CpfSubjectTrackingOperations> subjectTracking) {
        return new CpfWebContextFilter(inbound, dates, transactionIds, trustResolver, clientIpResolver, policies, failures, runtime,
                subjectTracking.getIfAvailable());
    }

    @Bean
    FilterRegistrationBean<CpfWebContextFilter> cpfWebContextFilterRegistration(CpfWebContextFilter filter) {
        var registration = new FilterRegistrationBean<>(filter);
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 20);
        registration.setName("cpfWebContextFilter");
        return registration;
    }
}
