package com.cpf.core.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Enables CPF AOP support such as transaction logging.
 */
@Configuration
@EnableAspectJAutoProxy
public class CpfAopConfig {
}
