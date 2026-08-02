package com.cpf.gateway.scg;

import static org.springframework.web.servlet.function.RequestPredicates.path;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RequestPredicate;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerResponse;

/** Gateway 업무 Data Plane만 SCG에 위임하며 Actuator/Control Plane은 별도 보안 경계에 둡니다. */
@Configuration(proxyBeanMethods = false)
public class CpfScgPrimaryRouteConfiguration {
    @Bean
    RouterFunction<ServerResponse> cpfPrimaryGatewayRoutes(CpfScgPrimaryHandler handler) {
        RequestPredicate business = path("/**")
                .and(path("/actuator/**").negate())
                .and(path("/internal/**").negate())
                .and(path("/api/gateway/control/**").negate());
        return RouterFunctions.route(business, handler);
    }
}
