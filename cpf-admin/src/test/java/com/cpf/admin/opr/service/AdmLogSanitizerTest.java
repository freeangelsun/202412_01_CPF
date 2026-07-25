package com.cpf.admin.opr.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AdmLogSanitizerTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void masksSecretsHeadersAndIdentitiesInJson() {
        String safe = AdmLogSanitizer.sanitizeJson(objectMapper, """
                {"Authorization":"Bearer abc.def.ghi","cookie":"SID=raw",
                 "password":"plain","memberNo":"1234567890","email":"person@example.com"}
                """);
        assertThat(safe).doesNotContain("abc.def.ghi", "SID=raw", "plain", "1234567890", "person@example.com");
        assertThat(safe).contains("****");
    }

    @Test
    void sanitizesSummaryByKeyNotOnlyByValuePattern() {
        Map<String, Object> safe = AdmLogSanitizer.sanitizeMap(Map.of(
                "MEMBER_NO", "M1234567890",
                "AUTHORIZATION", "opaque-value",
                "URI", "/api/v1/member"));
        assertThat(String.valueOf(safe.get("MEMBER_NO"))).doesNotContain("M1234567890");
        assertThat(safe.get("AUTHORIZATION")).isEqualTo("****");
        assertThat(safe.get("URI")).isEqualTo("/api/v1/member");
    }
}
