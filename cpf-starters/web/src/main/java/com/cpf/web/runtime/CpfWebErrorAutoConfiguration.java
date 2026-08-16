package com.cpf.web.runtime;

import com.cpf.web.error.CpfGlobalExceptionHandler;
import com.cpf.core.api.error.CpfResponseCodeResolver;
import com.cpf.core.api.error.DefaultCpfResponseCodeResolver;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/** Web Profile 표준 오류 Runtime을 자동 연결합니다. */
@AutoConfiguration
@Import(CpfGlobalExceptionHandler.class)
public class CpfWebErrorAutoConfiguration {
    @Bean @ConditionalOnMissingBean
    CpfResponseCodeResolver cpfResponseCodeResolver() { return new DefaultCpfResponseCodeResolver(); }
}
