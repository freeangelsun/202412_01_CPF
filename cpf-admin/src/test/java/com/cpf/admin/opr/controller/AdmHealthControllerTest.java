package com.cpf.admin.opr.controller;

import com.cpf.admin.config.AdmPersistencePolicy;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.env.MockEnvironment;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdmHealthControllerTest {

    @Test
    void readinessReturnsUpWhenOwnedDatabasesRespond() {
        JdbcTemplate adm = respondingTemplate();
        JdbcTemplate cpf = respondingTemplate();

        var environment = new MockEnvironment();
        var response = new AdmHealthController(
                adm, cpf, environment, new AdmPersistencePolicy(environment)).readiness();
        Map<String, Object> result = response.getBody();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result).isNotNull();
        assertThat(result)
                .containsEntry("status", "UP")
                .containsEntry("service", "ADM");
        assertThat(result.get("checks")).isEqualTo(Map.of(
                "admDB", "UP",
                "cpfDB", "UP",
                "sessionStore", "UP"));
    }

    @Test
    void readinessFailsWhenOwnedDatabaseIsDown() {
        JdbcTemplate adm = respondingTemplate();
        JdbcTemplate cpf = failingTemplate();

        var environment = new MockEnvironment();
        var response = new AdmHealthController(
                adm, cpf, environment, new AdmPersistencePolicy(environment)).readiness();
        Map<String, Object> result = response.getBody();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(result).isNotNull().containsEntry("status", "DOWN");
        assertThat(result.get("checks")).isEqualTo(Map.of(
                "admDB", "UP",
                "cpfDB", "DOWN",
                "sessionStore", "UP"));
    }

    private JdbcTemplate respondingTemplate() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForObject("SELECT 1", Integer.class)).thenReturn(1);
        return jdbcTemplate;
    }

    private JdbcTemplate failingTemplate() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForObject("SELECT 1", Integer.class)).thenThrow(new IllegalStateException("down"));
        return jdbcTemplate;
    }
}
