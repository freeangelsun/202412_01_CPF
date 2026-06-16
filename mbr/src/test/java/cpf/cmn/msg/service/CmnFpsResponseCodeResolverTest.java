package cpf.cmn.msg.service;

import cpf.pfw.common.exception.FpsResolvedResponse;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CmnFpsResponseCodeResolverTest {

    @Test
    void resolve_ShouldUseResponseCodeAndMessageCaches() {
        ResponseCodeCacheService responseCodeCacheService = mock(ResponseCodeCacheService.class);
        MessageCacheService messageCacheService = mock(MessageCacheService.class);
        CmnFpsResponseCodeResolver resolver = new CmnFpsResponseCodeResolver(responseCodeCacheService, messageCacheService);

        when(responseCodeCacheService.getResponseCode("EACC010001")).thenReturn(Map.of(
                "response_code", "EACC010001",
                "message_code", "MACC010001",
                "result_type", "E",
                "http_status", 400));
        when(messageCacheService.getMessageByKeyAndLocale("MACC010001", "ko")).thenReturn(Map.of(
                "external_message", "{0} is invalid.",
                "internal_message", "ACC validation failed. field={0}"));

        FpsResolvedResponse resolved = resolver.resolve(
                "EACC010001",
                Locale.KOREAN,
                Map.of("0", "accountId"),
                "invalid accountId");

        assertThat(resolved.httpStatus()).isEqualTo(400);
        assertThat(resolved.responseCode()).isEqualTo("EACC010001");
        assertThat(resolved.messageCode()).isEqualTo("MACC010001");
        assertThat(resolved.externalMessage()).isEqualTo("accountId is invalid.");
        assertThat(resolved.internalMessage()).isEqualTo("ACC validation failed. field=accountId");
        assertThat(resolved.errorCode()).isEqualTo("EACC010001");
        assertThat(resolved.errorMessage()).isEqualTo("invalid accountId");
    }
}

