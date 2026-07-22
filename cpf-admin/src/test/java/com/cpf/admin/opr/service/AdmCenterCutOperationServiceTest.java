package com.cpf.admin.opr.service;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class AdmCenterCutOperationServiceTest {

    private final JdbcTemplate cpfJdbcTemplate = mock(JdbcTemplate.class);
    private final JdbcTemplate refJdbcTemplate = mock(JdbcTemplate.class);
    private final AdmCenterCutOperationService service =
            new AdmCenterCutOperationService(cpfJdbcTemplate, refJdbcTemplate);

    @Test
    void maskPayloadForDisplayKeepsOnlyLengthAndMaskedText() {
        Map<String, Object> result = service.maskPayloadForDisplay("resultPayload", "{\"accountNo\":\"1234567890\"}");

        assertThat(result)
                .containsEntry("resultPayloadMasked", "[MASKED resultPayload length=26]")
                .containsEntry("resultPayloadLength", 26);
        assertThat(result.values().toString()).doesNotContain("1234567890");
    }

    @Test
    void maskPayloadForDisplayHandlesNullPayload() {
        Map<String, Object> result = service.maskPayloadForDisplay("targetPayload", null);

        assertThat(result)
                .containsEntry("targetPayloadMasked", null)
                .containsEntry("targetPayloadLength", null);
    }
}
