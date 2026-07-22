package com.cpf.admin.opr.controller;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdmHealthControllerTest {

    @Test
    void healthReturnsUpWhenAllDatabasesRespond() {
        // smoke 자동화는 ADM, CPF, MBR datasource가 모두 응답할 때만 실제 준비 완료로 판단합니다.
        JdbcTemplate adm = respondingTemplate();
        JdbcTemplate cpf = respondingTemplate();
        JdbcTemplate mbr = respondingTemplate();

        Map<String, Object> result = new AdmHealthController(adm, cpf, mbr).health();

        assertThat(result)
                .containsEntry("status", "UP")
                .containsEntry("service", "ADM");
        assertThat(result.get("checks")).isEqualTo(Map.of(
                "admDB", "UP",
                "cpfDB", "UP",
                "mbrDB", "UP"));
    }

    @Test
    void healthReturnsDegradedWhenAnyDatabaseFails() {
        // 앱 프로세스가 떠 있어도 운영 datasource 중 하나가 실패하면 OpenAPI/API smoke 전 단계에서 구분합니다.
        JdbcTemplate adm = respondingTemplate();
        JdbcTemplate cpf = failingTemplate();
        JdbcTemplate mbr = respondingTemplate();

        Map<String, Object> result = new AdmHealthController(adm, cpf, mbr).health();

        assertThat(result).containsEntry("status", "DEGRADED");
        assertThat(result.get("checks")).isEqualTo(Map.of(
                "admDB", "UP",
                "cpfDB", "DOWN",
                "mbrDB", "UP"));
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
