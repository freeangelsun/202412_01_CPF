package com.cpf.web.runtime;

import com.cpf.foundation.time.spi.CpfBusinessDateProvider;
import com.cpf.foundation.id.spi.CpfExecutionIdGenerator;
import com.cpf.foundation.id.spi.CpfTransactionIdGenerator;
import com.cpf.web.context.CpfHttpInboundContextAdapter;
import com.cpf.web.context.CpfHttpOutboundContextAdapter;
import jakarta.servlet.Filter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

/** HTTP root Context 경계를 설치하는 Web Profile AutoConfiguration입니다. */
@AutoConfiguration @ConditionalOnClass(Filter.class)
public class CpfWebContextAutoConfiguration {
    @Bean @ConditionalOnMissingBean CpfHttpInboundContextAdapter cpfHttpInboundContextAdapter(CpfTransactionIdGenerator tx,CpfExecutionIdGenerator ex){return new CpfHttpInboundContextAdapter(tx,ex);}
    @Bean @ConditionalOnMissingBean CpfHttpOutboundContextAdapter cpfHttpOutboundContextAdapter(){return new CpfHttpOutboundContextAdapter();}
    @Bean @ConditionalOnMissingBean CpfWebContextFilter cpfWebContextFilter(CpfHttpInboundContextAdapter inbound,CpfBusinessDateProvider dates){return new CpfWebContextFilter(inbound,dates);}
    @Bean FilterRegistrationBean<CpfWebContextFilter> cpfWebContextFilterRegistration(CpfWebContextFilter filter){var r=new FilterRegistrationBean<>(filter);r.setOrder(Ordered.HIGHEST_PRECEDENCE+20);r.setName("cpfWebContextFilter");return r;}
}
