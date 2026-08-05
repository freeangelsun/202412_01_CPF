package com.cpf.gateway.control;

import org.apache.catalina.connector.Connector;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Gateway Control Plane을 Data Plane과 다른 Tomcat Connector에 바인딩합니다. */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(TomcatServletWebServerFactory.class)
@ConditionalOnProperty(prefix = "cpf.gateway.control", name = "enabled", havingValue = "true")
public class CpfGatewayControlListenerConfiguration {

    @Bean
    WebServerFactoryCustomizer<TomcatServletWebServerFactory> cpfGatewayControlPlaneConnector(
            CpfGatewayControlSecurityProperties properties) {
        properties.validate();
        return factory -> factory.addAdditionalTomcatConnectors(connector(properties));
    }

    static Connector connector(CpfGatewayControlSecurityProperties properties) {
        Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
        connector.setPort(properties.getListenerPort());
        connector.setProperty("address", properties.getBindAddress());
        connector.setProperty("bindOnInit", "false");
        if (properties.isTlsEnabled()) {
            connector.setScheme("https");
            connector.setSecure(true);
            connector.setProperty("SSLEnabled", "true");
            connector.setProperty("sslProtocol", "TLS");
            connector.setProperty("sslEnabledProtocols", properties.getEnabledProtocols());
            connector.setProperty("keystoreFile", properties.getKeyStore());
            connector.setProperty("keystorePass", properties.getKeyStorePassword());
            connector.setProperty("keystoreType", properties.getKeyStoreType());
        } else {
            connector.setScheme("http");
            connector.setSecure(false);
        }
        return connector;
    }
}
