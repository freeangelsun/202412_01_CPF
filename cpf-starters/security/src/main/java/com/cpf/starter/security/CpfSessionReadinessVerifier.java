package com.cpf.starter.security;

import java.sql.Connection;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.core.env.Environment;
import javax.sql.DataSource;

/** Product profile에서 JDBC Session 저장소와 Secure Cookie 정책을 fail-closed로 확인합니다. */
final class CpfSessionReadinessVerifier implements SmartInitializingSingleton {
    private final DataSource dataSource;
    private final Environment environment;
    private final CpfServerSessionProperties properties;

    CpfSessionReadinessVerifier(DataSource dataSource, Environment environment, CpfServerSessionProperties properties) {
        this.dataSource = dataSource;
        this.environment = environment;
        this.properties = properties;
    }

    @Override
    public void afterSingletonsInstantiated() {
        boolean product = environment.matchesProfiles("prod", "stg", "qa", "dr");
        if (product && !properties.secure()) {
            throw new IllegalStateException("CPF privileged console session requires Secure cookie in product profiles.");
        }
        try (Connection connection = dataSource.getConnection()) {
            if (!connection.isValid(5)) throw new IllegalStateException("JDBC session datasource is not ready.");
        } catch (Exception e) {
            throw new IllegalStateException("CPF JDBC session store readiness verification failed.", e);
        }
    }
}
