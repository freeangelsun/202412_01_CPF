package com.cpf.education.integration.soap;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
@Configuration(proxyBeanMethods=false)
@EnableConfigurationProperties(EducationSoapProperties.class)
/** EducationSoapConfiguration 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public class EducationSoapConfiguration {}
