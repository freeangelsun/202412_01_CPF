package com.cpf.common.config;

import com.cpf.common.calendar.CmnCalendarService;
import com.cpf.common.calendar.runtime.CmnBusinessCalendarRuntimeApplier;
import com.cpf.common.cde.service.CodeCacheService;
import com.cpf.common.cfg.service.ConfigCacheService;
import com.cpf.common.msg.service.MessageCacheService;
import com.cpf.common.msg.service.ResponseCodeCacheService;
import com.cpf.common.ref.service.CmnCacheRuntimeApplier;
import com.cpf.core.api.runtimecontrol.CpfRuntimeChangeApplier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/** cpf-common의 실제 Runtime Consumer를 공통 Control Plane SPI에 연결합니다. */
@AutoConfiguration
public class CmnRuntimeControlAutoConfiguration {
    @Bean(name = "cmnBusinessCalendarRuntimeApplier")
    @ConditionalOnBean(CmnCalendarService.class)
    @ConditionalOnMissingBean(name = "cmnBusinessCalendarRuntimeApplier")
    public CpfRuntimeChangeApplier cmnBusinessCalendarRuntimeApplier(CmnCalendarService calendarService) {
        return new CmnBusinessCalendarRuntimeApplier(calendarService);
    }

    @Bean(name = "cmnCommonCodeRuntimeApplier")
    @ConditionalOnBean(CodeCacheService.class)
    @ConditionalOnMissingBean(name = "cmnCommonCodeRuntimeApplier")
    public CpfRuntimeChangeApplier cmnCommonCodeRuntimeApplier(CodeCacheService service) {
        return new CmnCacheRuntimeApplier("COMMON_CODE", service::refreshCodes, service::cacheStatus);
    }

    @Bean(name = "cmnMessageRuntimeApplier")
    @ConditionalOnBean(MessageCacheService.class)
    @ConditionalOnMissingBean(name = "cmnMessageRuntimeApplier")
    public CpfRuntimeChangeApplier cmnMessageRuntimeApplier(MessageCacheService service) {
        return new CmnCacheRuntimeApplier("MESSAGE_CATALOG", service::refreshMessages, service::cacheStatus);
    }

    @Bean(name = "cmnResponseCodeRuntimeApplier")
    @ConditionalOnBean(ResponseCodeCacheService.class)
    @ConditionalOnMissingBean(name = "cmnResponseCodeRuntimeApplier")
    public CpfRuntimeChangeApplier cmnResponseCodeRuntimeApplier(ResponseCodeCacheService service) {
        return new CmnCacheRuntimeApplier("RESPONSE_CODE", service::refreshResponseCodes, service::cacheStatus);
    }

    @Bean(name = "cmnRuntimeConfigApplier")
    @ConditionalOnBean(ConfigCacheService.class)
    @ConditionalOnMissingBean(name = "cmnRuntimeConfigApplier")
    public CpfRuntimeChangeApplier cmnRuntimeConfigApplier(ConfigCacheService service) {
        return new CmnCacheRuntimeApplier("RUNTIME_CONFIG", service::refreshConfigs, service::cacheStatus);
    }
}
