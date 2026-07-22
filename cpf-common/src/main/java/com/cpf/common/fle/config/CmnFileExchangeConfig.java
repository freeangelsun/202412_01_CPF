package com.cpf.common.fle.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** CMN 파일 교환 설정을 Spring 빈으로 등록합니다. */
@Configuration
@EnableConfigurationProperties(CmnFileExchangeProperties.class)
public class CmnFileExchangeConfig {
}

