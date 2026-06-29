package cpf.pfw.common.http;

import cpf.pfw.common.header.CpfHeaderNames;
import cpf.pfw.common.logging.TransactionContext;
import cpf.pfw.common.logging.TransactionHeader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.assertj.core.api.Assertions.assertThat;

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
                        .build());
        HttpHeaders headers = new HttpHeaders();
        headers.add(CpfHeaderNames.CHANNEL_CODE, "CUSTOM");

        CpfRestClientInterceptor.applyHeaders(headers);

        assertThat(headers.getFirst(CpfHeaderNames.TRANSACTION_ID)).isEqualTo("20260615120000000MBRlocal010000001");
        assertThat(headers.getFirst(CpfHeaderNames.TRACE_ID)).isEqualTo("TRACE-1");
        assertThat(headers.getFirst(CpfHeaderNames.PARENT_TRANSACTION_ID)).isEqualTo("20260615120000000MBRlocal010000001");
        assertThat(headers.getFirst(CpfHeaderNames.CHANNEL_CODE)).isEqualTo("CUSTOM");
        assertThat(headers).doesNotContainKey(CpfHeaderNames.AUTHORIZATION);
    }
}
