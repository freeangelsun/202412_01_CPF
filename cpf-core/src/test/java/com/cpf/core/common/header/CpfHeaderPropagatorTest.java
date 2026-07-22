package cpf.pfw.common.header;

import cpf.pfw.common.logging.TransactionContext;
import cpf.pfw.common.logging.TransactionHeader;
import cpf.pfw.common.logging.segment.TransactionSegmentContext;
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
    void contextSurvivesBeforeRequestContextHolderIsBound() {
        RequestContextHolder.resetRequestAttributes();
        TransactionHeader header = TransactionHeader.builder()
                .requestType("ONLINE")
                .originalChannelCode("MOBILE")
                .channelCode("XYZ")
                .clientAppId("cpf-smoke-client")
                .extensionHeaders(Map.of(
                        CpfHeaderNames.EXTENSION_1, "reserved-one",
                        "X-Cpf-Ext-Campaign-Id", "CMP-SMOKE"))
                .build();

        TransactionContext.initialize(
                "20260702103000000XYZlocal010000001",
                "TRACE-STANDARD-HEADER-E2E",
                "SPAN-PARENT-E2E",
                "20260702103000000XYZlocal010000001",
                header);

        Map<String, String> outbound = CpfHeaderPropagator.outboundHeaders();

        assertThat(TransactionContext.currentHeader()).isSameAs(header);
        assertThat(outbound)
                .containsEntry(CpfHeaderNames.ORIGINAL_CHANNEL_CODE, "MOBILE")
                .containsEntry(CpfHeaderNames.CHANNEL_CODE, "XYZ")
                .containsEntry(CpfHeaderNames.EXTENSION_1, "reserved-one")
                .containsEntry("X-Cpf-Ext-Campaign-Id", "CMP-SMOKE");
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

    @Test
    void outboundHeadersContainCurrentTransactionSegment() {
        TransactionContext.initialize(
                "20260703120000000XYZtrace010000001",
                "TRACE-SEGMENT",
                "PARENT-SPAN",
                "20260703120000000XYZtrace010000001",
                TransactionHeader.builder()
                        .requestType("ONLINE")
                        .originalChannelCode("APP")
                        .channelCode("XYZ")
                        .build());
        TransactionSegmentContext.push(new TransactionSegmentContext.TransactionSegmentFrame(
                "20260703120000000XYZtrace010000001-SEG-0001-ABCDEF12",
                "20260703120000000XYZtrace010000001",
                0));

        Map<String, String> outbound = CpfHeaderPropagator.outboundHeaders();

        assertThat(outbound)
                .containsEntry(CpfHeaderNames.ROOT_TRANSACTION_ID, "20260703120000000XYZtrace010000001")
                .containsEntry(CpfHeaderNames.TRANSACTION_SEGMENT_ID, "20260703120000000XYZtrace010000001-SEG-0001-ABCDEF12")
                .containsEntry(CpfHeaderNames.PARENT_TRANSACTION_SEGMENT_ID, "20260703120000000XYZtrace010000001-SEG-0001-ABCDEF12")
                .containsEntry(CpfHeaderNames.TRANSACTION_CALL_DEPTH, "0");
    }
}
