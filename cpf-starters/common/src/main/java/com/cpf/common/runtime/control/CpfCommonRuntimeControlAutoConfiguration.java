package com.cpf.common.runtime.control;

import com.cpf.common.runtime.cache.CpfCommonCacheRefresher;
import com.cpf.platform.operations.runtimecontrol.CpfRuntimeChangeApplier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

/** Common Product Service를 Platform Operations Runtime Control SPI에 연결합니다. */
@AutoConfiguration(afterName = "com.cpf.common.runtime.CpfCommonJdbcAutoConfiguration")
@ConditionalOnClass(CpfRuntimeChangeApplier.class)
public class CpfCommonRuntimeControlAutoConfiguration {
    @Bean(name="cpfCommonCodeRuntimeApplier")
    CpfRuntimeChangeApplier code(CpfCommonCacheRefresher r){ return new CpfCommonCacheRuntimeApplier("COMMON_CODE","codeCache",r); }
    @Bean(name="cpfCommonParameterRuntimeApplier")
    CpfRuntimeChangeApplier parameter(CpfCommonCacheRefresher r){ return new CpfCommonCacheRuntimeApplier("RUNTIME_CONFIG","configCache",r); }
    @Bean(name="cpfCommonMessageRuntimeApplier")
    CpfRuntimeChangeApplier message(CpfCommonCacheRefresher r){ return new CpfCommonCacheRuntimeApplier("MESSAGE_CATALOG","messageCache",r); }
    @Bean(name="cpfCommonResponseCodeRuntimeApplier")
    CpfRuntimeChangeApplier response(CpfCommonCacheRefresher r){ return new CpfCommonCacheRuntimeApplier("RESPONSE_CODE","responseCodeCache",r); }
}
