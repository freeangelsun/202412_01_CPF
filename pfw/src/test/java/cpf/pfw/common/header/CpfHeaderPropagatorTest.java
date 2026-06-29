package cpf.pfw.common.header;

import cpf.pfw.common.logging.TransactionContext;
import cpf.pfw.common.logging.TransactionHeader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CpfHeaderPropagatorTest {
    @AfterEach
    void tearDown() {
        TransactionContext.clear();
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void contextGettersAndOutboundHeadersUseResolvedTransactionContext() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        TransactionHeader header = TransactionHeader.builder()
                .originalTransactionId("20260615115900000MBRlocal010000009")
                .requestId("REQ-1")
                .correlationId("CORR-1")
                .requestType("INQUIRY")
                .originalChannelCode("APP")
                .channelCode("MBR")
                .channelDetailCode("MOBILE")
                .userId("user-1")
                .operatorId("op-1")
                .customerNo("CUST-1")
                .memberNo("MBR-1")
                .tenantId("TENANT-1")
                .organizationCode("ORG-1")
                .branchCode("BR-1")
                .clientIp("10.10.10.10")
                .clientCountryCode("KR")
                .clientTimezone("Asia/Seoul")
                .build();

        TransactionContext.initialize(
                "20260615120000000MBRlocal010000001",
                "TRACE-1",
                "PARENT-SPAN-1",
                "20260615120000000MBRlocal010000001",
                header);

        Map<String, String> outbound = CpfHeaderPropagator.outboundHeaders();

        assertThat(TransactionContext.originalTransactionId()).isEqualTo("20260615115900000MBRlocal010000009");
        assertThat(TransactionContext.channelDetailCode()).isEqualTo("MOBILE");
        assertThat(TransactionContext.operatorId()).isEqualTo("op-1");
        assertThat(TransactionContext.clientTimezone()).isEqualTo("Asia/Seoul");
        assertThat(outbound).containsEntry(CpfHeaderNames.PARENT_TRANSACTION_ID, "20260615120000000MBRlocal010000001");
        assertThat(outbound).containsEntry(CpfHeaderNames.ORIGINAL_TRANSACTION_ID, "20260615115900000MBRlocal010000009");
        assertThat(outbound).containsEntry(CpfHeaderNames.TRACE_ID, "TRACE-1");
        assertThat(outbound).containsKey(CpfHeaderNames.PARENT_SPAN_ID);
        assertThat(outbound).doesNotContainKey(CpfHeaderNames.SPAN_ID);
        assertThat(outbound).doesNotContainKey(CpfHeaderNames.AUTHORIZATION);
    }

    @Test
    void sensitiveHeadersAreMaskedForLogSnapshot() {
        Map<String, String> masked = CpfHeaderMasker.maskHeaders(Map.of(
                CpfHeaderNames.AUTHORIZATION, "Bearer abc.def",
                CpfHeaderNames.API_KEY, "secret-api-key",
                CpfHeaderNames.USER_ID, "user-1"));

        assertThat(masked).containsEntry(CpfHeaderNames.AUTHORIZATION, "****");
        assertThat(masked).containsEntry(CpfHeaderNames.API_KEY, "****");
        assertThat(masked).containsEntry(CpfHeaderNames.USER_ID, "user-1");
    }
}
