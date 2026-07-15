package cpf.pfw.common.http;

import cpf.pfw.common.header.CpfHeaderNames;
import cpf.pfw.common.logging.TransactionContext;
import cpf.pfw.common.logging.TransactionHeader;
import cpf.pfw.common.logging.file.CpfFileLogWriter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CpfRestClientInterceptorTest {
    @AfterEach
    void tearDown() {
        TransactionContext.clear();
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void restClientHeadersAreAppliedWithoutOverwritingExplicitHeaders() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        TransactionContext.initialize(
                "20260615120000000MBRlocal010000001",
                "TRACE-1",
                null,
                "20260615120000000MBRlocal010000001",
                TransactionHeader.builder()
                        .requestType("INQUIRY")
                        .originalChannelCode("APP")
                        .channelCode("MBR")
                        .clientAppId("cpf-mobile")
                        .clientVersion("1.0.0")
                        .extensionHeaders(java.util.Map.of("X-Cpf-Ext-Campaign-Id", "CMP-2026"))
                        .build());
        HttpHeaders headers = new HttpHeaders();
        headers.add(CpfHeaderNames.CHANNEL_CODE, "CUSTOM");

        CpfRestClientInterceptor.applyHeaders(headers);

        assertThat(headers.getFirst(CpfHeaderNames.TRANSACTION_ID)).isEqualTo("20260615120000000MBRlocal010000001");
        assertThat(headers.getFirst(CpfHeaderNames.TRACE_ID)).isEqualTo("TRACE-1");
        assertThat(headers.getFirst(CpfHeaderNames.PARENT_TRANSACTION_ID)).isEqualTo("20260615120000000MBRlocal010000001");
        assertThat(headers.getFirst(CpfHeaderNames.CHANNEL_CODE)).isEqualTo("CUSTOM");
        assertThat(headers.getFirst("X-Cpf-Ext-Campaign-Id")).isEqualTo("CMP-2026");
        assertThat(headers).doesNotContainKey(CpfHeaderNames.AUTHORIZATION);
    }

    @Test
    @SuppressWarnings("unchecked")
    void restClientInterceptorWritesOutboundIntegrationEvents() throws Exception {
        CpfFileLogWriter writer = mock(CpfFileLogWriter.class);
        CpfRestClientInterceptor interceptor = new CpfRestClientInterceptor(writer);
        HttpHeaders headers = new HttpHeaders();
        HttpRequest request = new HttpRequest() {
            @Override
            public HttpMethod getMethod() {
                return HttpMethod.GET;
            }

            @Override
            public URI getURI() {
                return URI.create("http://localhost:8091/api/bza/approvals");
            }

            @Override
            public HttpHeaders getHeaders() {
                return headers;
            }

            @Override
            public Map<String, Object> getAttributes() {
                return Map.of();
            }
        };
        ClientHttpResponse response = mock(ClientHttpResponse.class);
        when(response.getStatusCode()).thenReturn(HttpStatus.OK);

        interceptor.intercept(request, new byte[0], (outboundRequest, body) -> response);

        var attributes = org.mockito.ArgumentCaptor.forClass(Map.class);
        verify(writer, times(2)).writeIntegration(
                isNull(),
                eq("BZA"),
                eq("OUTBOUND"),
                eq("GET"),
                eq("/api/bza/approvals"),
                any(),
                any(),
                any(),
                any(),
                any(),
                attributes.capture());
        List<Map<String, Object>> captured = (List<Map<String, Object>>) (List<?>) attributes.getAllValues();
        assertThat(captured).extracting(event -> event.get("eventType"))
                .containsExactly("OUTBOUND_REQUEST", "OUTBOUND_RESPONSE");
    }
}
