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

class CpfStandardHeaderE2eTest {
    private static final String TRANSACTION_ID = "20260615120000000MBRlocal010000001";

    @AfterEach
    void tearDown() {
        System.clearProperty(CpfTrustedProxyPolicy.TRUSTED_PROXIES_PROPERTY);
        TransactionContext.clear();
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void inboundHeadersCreateContextAndPropagateOnlyAllowedMaskedHeaders() {
        System.setProperty(CpfTrustedProxyPolicy.TRUSTED_PROXIES_PROPERTY, "10.0.0.1");
        MockHttpServletRequest request = standardRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        CpfInboundHeaderValidator validator = new CpfInboundHeaderValidator(7);
        assertThat(validator.missingRequiredHeaders(request)).isEmpty();
        assertThat(validator.isValidTransactionId(request.getHeader(CpfHeaderNames.TRANSACTION_ID))).isTrue();

        TransactionHeader header = CpfHeaderExtractor.toTransactionHeader(request, "local01");
        TransactionContext.initialize(
                request.getHeader(CpfHeaderNames.TRANSACTION_ID),
                request.getHeader(CpfHeaderNames.TRACE_ID),
                request.getHeader(CpfHeaderNames.PARENT_SPAN_ID),
                TRANSACTION_ID,
                header);

        assertThat(TransactionContext.currentTransactionId()).isEqualTo(TRANSACTION_ID);
        assertThat(TransactionContext.currentHeader()).isSameAs(header);
        assertThat(TransactionContext.userId()).isEqualTo("login-user");
        assertThat(TransactionContext.operatorId()).isEqualTo("adm-operator");
        assertThat(TransactionContext.customerNo()).isEqualTo("CUST-001");
        assertThat(TransactionContext.memberNo()).isEqualTo("MBR-001");
        assertThat(TransactionContext.originalChannelCode()).isEqualTo("MOBILE");
        assertThat(TransactionContext.channelCode()).isEqualTo("MBR");
        assertThat(TransactionContext.clientIp()).isEqualTo("203.0.113.10");

        Map<String, String> inbound = CpfHeaderExtractor.extractInboundHeaders(request);
        assertThat(inbound)
                .containsEntry(CpfHeaderNames.AUTHORIZATION, "****")
                .containsEntry(CpfHeaderNames.API_KEY, "****")
                .containsEntry(CpfHeaderNames.FORWARDED_FOR, "****")
                .doesNotContainEntry(CpfHeaderNames.AUTHORIZATION, "Bearer raw-token")
                .doesNotContainEntry(CpfHeaderNames.API_KEY, "raw-api-key");

        TransactionHeader updated = CpfHeaderMutator.put(CpfHeaderNames.CHANNEL_DETAIL_CODE, "APP");
        assertThat(updated.getChannelDetailCode()).isEqualTo("APP");
        assertThat(updated.getUserId()).isEqualTo("login-user");
        assertThat(updated.getOperatorId()).isEqualTo("adm-operator");

        Map<String, String> outbound = CpfHeaderPropagator.outboundHeaders();
        assertThat(outbound)
                .containsEntry(CpfHeaderNames.TRANSACTION_ID, TRANSACTION_ID)
                .containsEntry(CpfHeaderNames.PARENT_TRANSACTION_ID, TRANSACTION_ID)
                .containsEntry(CpfHeaderNames.ORIGINAL_CHANNEL_CODE, "MOBILE")
                .containsEntry(CpfHeaderNames.CHANNEL_CODE, "MBR")
                .containsEntry(CpfHeaderNames.USER_ID, "login-user")
                .containsEntry(CpfHeaderNames.CUSTOMER_NO, "CUST-001")
                .containsEntry(CpfHeaderNames.MEMBER_NO, "MBR-001")
                .containsEntry(CpfHeaderNames.OPERATOR_ID, "adm-operator")
                .doesNotContainKey(CpfHeaderNames.AUTHORIZATION)
                .doesNotContainKey(CpfHeaderNames.API_KEY)
                .doesNotContainKey(CpfHeaderNames.NONCE)
                .doesNotContainKey(CpfHeaderNames.SPAN_ID)
                .doesNotContainKey(CpfHeaderNames.CLIENT_IP)
                .doesNotContainKey(CpfHeaderNames.FORWARDED_FOR);

        CpfHeaderSnapshot snapshot = CpfHeaderPropagator.currentSnapshot(updated);
        assertThat(snapshot.inboundHeaders())
                .containsEntry(CpfHeaderNames.USER_ID, "login-user")
                .containsEntry(CpfHeaderNames.CUSTOMER_NO, "CUST-001")
                .containsEntry(CpfHeaderNames.MEMBER_NO, "MBR-001")
                .doesNotContainEntry(CpfHeaderNames.AUTHORIZATION, "Bearer raw-token");
        assertThat(snapshot.resolvedHeaders())
                .containsEntry(CpfHeaderNames.CLIENT_IP, "203.0.113.10")
                .containsEntry(CpfHeaderNames.CHANNEL_DETAIL_CODE, "APP");
        assertThat(snapshot.outboundHeaders())
                .containsEntry(CpfHeaderNames.PARENT_TRANSACTION_ID, TRANSACTION_ID)
                .doesNotContainKey(CpfHeaderNames.AUTHORIZATION);
        assertThat(snapshot.responseHeaders())
                .containsEntry(CpfHeaderNames.TRANSACTION_ID, TRANSACTION_ID)
                .containsEntry(CpfHeaderNames.TRACE_ID, "TRACE-1");
    }

    private MockHttpServletRequest standardRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.1");
        request.addHeader(CpfHeaderNames.TRANSACTION_ID, TRANSACTION_ID);
        request.addHeader(CpfHeaderNames.TRACE_ID, "TRACE-1");
        request.addHeader(CpfHeaderNames.PARENT_SPAN_ID, "SPAN-PARENT-1");
        request.addHeader(CpfHeaderNames.REQUEST_TYPE, "ONLINE");
        request.addHeader(CpfHeaderNames.ORIGINAL_CHANNEL_CODE, "MOBILE");
        request.addHeader(CpfHeaderNames.CHANNEL_CODE, "MBR");
        request.addHeader(CpfHeaderNames.CLIENT_VERSION, "1.2.3");
        request.addHeader(CpfHeaderNames.CALLER_SERVICE, "cpf-mobile");
        request.addHeader(CpfHeaderNames.USER_ID, "login-user");
        request.addHeader(CpfHeaderNames.OPERATOR_ID, "adm-operator");
        request.addHeader(CpfHeaderNames.CUSTOMER_NO, "CUST-001");
        request.addHeader(CpfHeaderNames.MEMBER_NO, "MBR-001");
        request.addHeader(CpfHeaderNames.FORWARDED_FOR, "203.0.113.10, 10.0.0.1");
        request.addHeader(CpfHeaderNames.AUTHORIZATION, "Bearer raw-token");
        request.addHeader(CpfHeaderNames.API_KEY, "raw-api-key");
        request.addHeader(CpfHeaderNames.NONCE, "raw-nonce");
        return request;
    }
}
