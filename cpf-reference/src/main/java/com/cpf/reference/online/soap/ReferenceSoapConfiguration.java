package com.cpf.reference.online.soap;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
@Configuration(proxyBeanMethods=false)
@EnableConfigurationProperties(ReferenceSoapProperties.class)
public class ReferenceSoapConfiguration {}
