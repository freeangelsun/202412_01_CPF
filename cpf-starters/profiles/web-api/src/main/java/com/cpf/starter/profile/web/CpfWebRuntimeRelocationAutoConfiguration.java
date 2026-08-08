package com.cpf.starter.profile.web;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/** Owns servlet/web runtime components relocated from cpf-core. */
@AutoConfiguration
@ComponentScan(basePackages = {"com.cpf.core.common.filter", "com.cpf.core.common.web"})
public class CpfWebRuntimeRelocationAutoConfiguration {}
