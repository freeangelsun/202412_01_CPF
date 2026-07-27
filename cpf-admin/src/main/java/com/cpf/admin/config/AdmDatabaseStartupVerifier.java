package com.cpf.admin.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 제품 DATABASE 모드에서 ADM DB 연결을 기동 시점에 검증합니다.
 */
@Component
public class AdmDatabaseStartupVerifier implements ApplicationRunner {
    private final AdmPersistencePolicy persistencePolicy;
    private final JdbcTemplate jdbcTemplate;

    public AdmDatabaseStartupVerifier(
            AdmPersistencePolicy persistencePolicy,
            @Qualifier("admJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.persistencePolicy = persistencePolicy;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (persistencePolicy.databaseRequired()) {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        }
    }
}
