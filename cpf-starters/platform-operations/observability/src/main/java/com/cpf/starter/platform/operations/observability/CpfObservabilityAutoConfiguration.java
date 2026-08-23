package com.cpf.starter.platform.operations.observability;

import io.micrometer.observation.ObservationRegistry;
import com.cpf.foundation.context.CpfContextProjectionRegistry;
import com.cpf.platform.operations.observability.api.logging.CpfStructuredLogger;
import com.cpf.platform.operations.observability.internal.logging.DefaultCpfStructuredLogger;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(ObservationRegistry.class)
public class CpfObservabilityAutoConfiguration {
    @Bean @ConditionalOnMissingBean
    CpfObservationSupport cpfObservationSupport(ObservationRegistry registry) { return new CpfObservationSupport(registry); }
    @Bean @ConditionalOnMissingBean CpfMdcContextProjection cpfMdcContextProjection(){return new CpfMdcContextProjection();}
    @Bean @ConditionalOnMissingBean(CpfStructuredLogger.class)
    CpfStructuredLogger cpfStructuredLogger(){ return new DefaultCpfStructuredLogger(); }
    @Bean @ConditionalOnMissingBean CpfTraceContextProjection cpfTraceContextProjection(){return new CpfTraceContextProjection();}
    @Bean @ConditionalOnMissingBean CpfObservabilityContextProjection cpfObservabilityContextProjection(
            CpfMdcContextProjection mdc,CpfTraceContextProjection trace,CpfContextProjectionRegistry projections){
        return new CpfObservabilityContextProjection(mdc, trace, projections);
    }
}
