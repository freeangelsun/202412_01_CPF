package com.cpf.common.message.service;

import com.cpf.common.message.api.CpfMessageRecord;
import com.cpf.common.message.api.CpfResponseCodeRecord;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CmnCpfResponseCodeResolverTest {
    @Test
    void canonicalCatalogHttpStatusDrivesTheResolvedRuntimeResponse() {
        Instant now = Instant.now();
        CpfResponseCodeRecord response = new CpfResponseCodeRecord(
                "EORDER010001", "MORDER010001", "E", "ORD", "01", "0001", 422,
                "BUSINESS", "NEVER", "SAFE_MESSAGE_ONLY", null, null, 1, "validation", "Y", now);
        CpfMessageRecord message = new CpfMessageRecord(
                1, "MORDER010001", "ko", "FIXED", "주문을 처리할 수 없습니다.", "validation failed",
                0, null, null, "Y", "Y", null, null, 1, "validation", "Y", now);
        CmnErrorCatalogStore store = new CmnErrorCatalogStore() {
            @Override public CpfResponseCodeRecord response(String code) { return response; }
            @Override public CpfMessageRecord message(String code, Locale locale) { return message; }
        };

        var resolved = new CmnCpfResponseCodeResolver(store).resolve(
                response.responseCode(), Locale.KOREAN, Map.of(), null);

        assertThat(resolved.httpStatus()).isEqualTo(422);
        assertThat(resolved.responseCode()).isEqualTo("EORDER010001");
        assertThat(resolved.messageCode()).isEqualTo("MORDER010001");
    }
}
