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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CpfExtensionHeaderPolicyTest {
    private static final String TRANSACTION_ID = "20260615120000000MBRlocal010000001";

    @AfterEach
    void tearDown() {
        TransactionContext.clear();
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void extensionHeaderNamingRuleSupportsReservedAndKeyBasedHeaders() {
        assertThat(CpfHeaderSpecs.isAllowedExtensionHeader(CpfHeaderNames.EXTENSION_1)).isTrue();
        assertThat(CpfHeaderSpecs.isAllowedExtensionHeader(CpfHeaderNames.EXTENSION_5)).isTrue();
        assertThat(CpfHeaderSpecs.isAllowedExtensionHeader("X-Cpf-Ext-Campaign-Id")).isTrue();
        assertThat(CpfHeaderSpecs.isAllowedExtensionHeader("X-Cpf-Ext-Partner-Code")).isTrue();

        assertThat(CpfHeaderSpecs.isAllowedExtensionHeader("X-Random-Test")).isFalse();
        assertThat(CpfHeaderSpecs.isRequired("X-Cpf-Ext-Campaign-Id")).isFalse();
        assertThat(CpfHeaderSpecs.shouldPropagate("X-Cpf-Ext-Campaign-Id")).isTrue();
        assertThat(CpfHeaderSpecs.shouldMask("X-Cpf-Ext-Campaign-Id")).isFalse();
        assertThat(CpfHeaderSpecs.find("X-Cpf-Ext-Campaign-Id"))
                .get()
                .extracting(CpfHeaderSpec::category)
                .isEqualTo(CpfHeaderCategory.OPTIONAL);
    }

    @Test
    void securityAliasCannotBeUsedAsExtensionHeaderBypass() {
        assertThat(CpfHeaderSpecs.isAllowedExtensionHeader("X-Cpf-Ext-Authorization")).isFalse();
        assertThat(CpfHeaderSpecs.isAllowedExtensionHeader("X-Cpf-Ext-Token")).isFalse();
        assertThat(CpfHeaderSpecs.isAllowedExtensionHeader("X-Cpf-Ext-Api-Key")).isFalse();
        assertThat(CpfHeaderSpecs.canLogRaw("X-Cpf-Ext-Token")).isFalse();
        assertThat(CpfHeaderSpecs.shouldPropagate("X-Cpf-Ext-Token")).isFalse();

        assertThatThrownBy(() -> CpfHeaderMutator.withAllowedHeader(
                TransactionHeader.builder().build(),
                "X-Cpf-Ext-Token",
                "raw-token"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("우회 저장하거나 전파할 수 없습니다");
    }

    @Test
    void extractorContextSnapshotAndOutboundPropagationIncludeAllowedExtensionHeaders() {
        MockHttpServletRequest request = standardRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        CpfInboundHeaderValidator validator = new CpfInboundHeaderValidator(7);
        assertThat(validator.missingRequiredHeaders(request)).isEmpty();
        assertThat(validator.invalidExtensionHeaders(request))
                .containsExactlyInAnyOrder("X-Cpf-Ext-Token", "X-Cpf-Ext-Api-Key");

        Map<String, String> extracted = CpfHeaderExtractor.extractExtensionHeaders(request);
        assertThat(extracted)
                .containsEntry(CpfHeaderNames.EXTENSION_1, "reserved-one")
                .containsEntry("X-Cpf-Ext-Campaign-Id", "CMP-2026")
                .containsEntry("X-Cpf-Ext-Partner-Code", "PARTNER-A")
                .doesNotContainKey("X-Random-Test")
                .doesNotContainKey("X-Cpf-Ext-Token")
                .doesNotContainKey("X-Cpf-Ext-Api-Key");

        TransactionHeader header = CpfHeaderExtractor.toTransactionHeader(request, "local01");
        TransactionContext.initialize(
                request.getHeader(CpfHeaderNames.TRANSACTION_ID),
                request.getHeader(CpfHeaderNames.TRACE_ID),
                request.getHeader(CpfHeaderNames.PARENT_SPAN_ID),
                TRANSACTION_ID,
                header);

        assertThat(TransactionContext.currentHeader().getExtensionHeaders())
                .containsEntry(CpfHeaderNames.EXTENSION_1, "reserved-one")
                .containsEntry("X-Cpf-Ext-Campaign-Id", "CMP-2026")
                .containsEntry("X-Cpf-Ext-Partner-Code", "PARTNER-A");

        Map<String, String> inbound = CpfHeaderExtractor.extractInboundHeaders(request);
        assertThat(inbound)
                .containsEntry("X-Cpf-Ext-Campaign-Id", "CMP-2026")
                .doesNotContainKey("X-Cpf-Ext-Token");

        Map<String, String> outbound = CpfHeaderPropagator.outboundHeaders();
        assertThat(outbound)
                .containsEntry("X-Cpf-Ext-Campaign-Id", "CMP-2026")
                .containsEntry("X-Cpf-Ext-Partner-Code", "PARTNER-A")
                .doesNotContainKey("X-Cpf-Ext-Token");

        CpfHeaderSnapshot snapshot = CpfHeaderPropagator.currentSnapshot(header);
        assertThat(snapshot.inboundHeaders()).containsEntry("X-Cpf-Ext-Campaign-Id", "CMP-2026");
        assertThat(snapshot.resolvedHeaders()).containsEntry("X-Cpf-Ext-Partner-Code", "PARTNER-A");
        assertThat(snapshot.outboundHeaders()).containsEntry(CpfHeaderNames.EXTENSION_1, "reserved-one");
    }

    @Test
    void mutatorCanAddUpdateAndRemoveAllowedExtensionHeader() {
        TransactionHeader source = TransactionHeader.builder()
                .extensionHeaders(Map.of("X-Cpf-Ext-Campaign-Id", "OLD"))
                .build();

        TransactionHeader updated = CpfHeaderMutator.withAllowedHeader(
                source,
                "X-Cpf-Ext-Campaign-Id",
                "NEW");
        assertThat(updated.getExtensionHeaders()).containsEntry("X-Cpf-Ext-Campaign-Id", "NEW");

        TransactionHeader removed = CpfHeaderMutator.withAllowedHeader(
                updated,
                "x-cpf-ext-campaign-id",
                null);
        assertThat(removed.getExtensionHeaders()).doesNotContainKey("X-Cpf-Ext-Campaign-Id");
    }

    private MockHttpServletRequest standardRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(CpfHeaderNames.TRANSACTION_ID, TRANSACTION_ID);
        request.addHeader(CpfHeaderNames.TRACE_ID, "TRACE-1");
        request.addHeader(CpfHeaderNames.PARENT_SPAN_ID, "SPAN-PARENT-1");
        request.addHeader(CpfHeaderNames.REQUEST_TYPE, "ONLINE");
        request.addHeader(CpfHeaderNames.ORIGINAL_CHANNEL_CODE, "MOBILE");
        request.addHeader(CpfHeaderNames.CHANNEL_CODE, "MBR");
        request.addHeader(CpfHeaderNames.EXTENSION_1, "reserved-one");
        request.addHeader("X-Cpf-Ext-Campaign-Id", "CMP-2026");
        request.addHeader("X-Cpf-Ext-Partner-Code", "PARTNER-A");
        request.addHeader("X-Random-Test", "ignored");
        request.addHeader("X-Cpf-Ext-Token", "raw-token");
        request.addHeader("X-Cpf-Ext-Api-Key", "raw-api-key");
        return request;
    }
}
