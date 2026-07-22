package com.cpf.common.msg.service;

import com.cpf.core.common.exception.CpfResolvedResponse;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CmnCpfResponseCodeResolverTest {

    @Test
    void resolve_ShouldUseResponseCodeAndMessageCaches() {
        ResponseCodeCacheService responseCodeCacheService = mock(ResponseCodeCacheService.class);
        MessageCacheService messageCacheService = mock(MessageCacheService.class);
        CmnCpfResponseCodeResolver resolver = new CmnCpfResponseCodeResolver(responseCodeCacheService, messageCacheService);

        when(responseCodeCacheService.getResponseCode("EREF010001")).thenReturn(Map.of(
                "response_code", "EREF010001",
                "message_code", "MREF010001",
                "result_type", "E",
                "http_status", 400));
        when(messageCacheService.getMessageByKeyAndLocale("MREF010001", "ko")).thenReturn(Map.of(
                "external_message", "{0} is invalid.",
                "internal_message", "REF validation failed. field={0}"));

        CpfResolvedResponse resolved = resolver.resolve(
                "EREF010001",
                Locale.KOREAN,
                Map.of("0", "accountId"),
                "invalid accountId");

        assertThat(resolved.httpStatus()).isEqualTo(400);
        assertThat(resolved.responseCode()).isEqualTo("EREF010001");
        assertThat(resolved.messageCode()).isEqualTo("MREF010001");
        assertThat(resolved.externalMessage()).isEqualTo("accountId is invalid.");
        assertThat(resolved.internalMessage()).isEqualTo("REF validation failed. field=accountId");
        assertThat(resolved.errorCode()).isEqualTo("EREF010001");
        assertThat(resolved.errorMessage()).isEqualTo("invalid accountId");
    }
}

