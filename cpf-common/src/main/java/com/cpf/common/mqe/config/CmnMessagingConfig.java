package com.cpf.common.mqe.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** CMN 메시징 설정 속성을 Spring 구성에 등록합니다. */
@Configuration
@EnableConfigurationProperties(CmnMessagingProperties.class)
public class CmnMessagingConfig {
}

