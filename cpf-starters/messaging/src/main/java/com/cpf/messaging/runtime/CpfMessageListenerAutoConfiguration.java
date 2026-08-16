package com.cpf.messaging.runtime;

import com.cpf.messaging.api.CpfBrokerBridgePort;
import com.cpf.messaging.api.CpfMessageListener;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

/** @CpfMessageListener 등록을 broker provider 초기화 이후 수행합니다. */
@AutoConfiguration
@ConditionalOnClass(CpfMessageListener.class)
@ConditionalOnBean(CpfBrokerBridgePort.class)
@ConditionalOnProperty(prefix="cpf.messaging.listener", name="enabled", havingValue="true", matchIfMissing=true)
public class CpfMessageListenerAutoConfiguration {
    @Bean
    CpfMessageListenerRegistrar cpfMessageListenerRegistrar(ApplicationContext context, CpfBrokerBridgePort bridge) { return new CpfMessageListenerRegistrar(context, bridge); }
    @Bean
    SmartInitializingSingleton cpfMessageListenerRegistration(CpfMessageListenerRegistrar registrar) { return registrar::registerAll; }
}
