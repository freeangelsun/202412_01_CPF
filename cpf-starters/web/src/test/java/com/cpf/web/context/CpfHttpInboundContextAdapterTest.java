package com.cpf.web.context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import com.cpf.foundation.id.spi.CpfExecutionIdGenerator;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CpfHttpInboundContextAdapterTest {
    private static final String CANONICAL_TX = "20260810010101001MBWlocal010000001";

    @Test
    void canonicalTransactionIdKeepsValidTrustedIdForRetryAndHopPropagation() {
        assertEquals(CANONICAL_TX, CpfHttpInboundContextAdapter.canonicalTransactionId(CANONICAL_TX));
    }

    @Test
    void nonCanonicalInboundIdIsRejectedInsteadOfSilentlyAccepted() {
        assertThrows(IllegalArgumentException.class,
                () -> CpfHttpInboundContextAdapter.canonicalTransactionId("EXT-TRX-20260810-0001"));
    }

    @Test
    void externalDirectCallRequiresCanonicalSixAndValidatesCurrentSystem() {
        var adapter = adapter();
        var result = adapter.resolve(externalHeaders("MBR"), CpfHttpIngressTrust.UNTRUSTED_EXTERNAL,
                null, null, null, "POST /members", LocalDate.of(2026, 8, 18), null,
                new CpfRuntimeIdentity("MBR", "member", "MBR01"));

        assertEquals(CANONICAL_TX, result.snapshot().transaction().transactionId());
        assertEquals("MBW", result.snapshot().context().originalSystemCode());
        assertEquals("MBR", result.snapshot().context().currentSystemCode());
        assertEquals("MBW", result.snapshot().context().callerSystemCode());
        assertEquals("MBR", result.snapshot().context().targetSystemCode());
        assertEquals("MBR_MEMBER_JOIN", result.snapshot().context().targetOperationId());
    }

    @Test
    void externalChannelContextIsOptionalAndNeverPartOfCanonicalSix() {
        var adapter = adapter();
        var headers = externalHeaders("MBR");
        headers.remove(CpfHttpHeaderNames.CALLER_CHANNEL);
        var result = adapter.resolve(headers, CpfHttpIngressTrust.UNTRUSTED_EXTERNAL,
                null, null, null, "POST /members", LocalDate.of(2026, 8, 18), null,
                new CpfRuntimeIdentity("MBR", "member", "MBR01"));
        assertEquals("MBR", result.snapshot().context().currentSystemCode());
    }

    @Test
    void transactionIssuerAndOriginalSystemAreIndependentCanonicalFacts() {
        var adapter = adapter();
        var headers = externalHeaders("MBR");
        headers.put(CpfHttpHeaderNames.ORIGINAL_SYSTEM_CODE, "EXS");
        var result = adapter.resolve(
                headers, CpfHttpIngressTrust.UNTRUSTED_EXTERNAL,
                null, null, null, "POST /members", LocalDate.of(2026, 8, 18), null,
                new CpfRuntimeIdentity("MBR", "member", "MBR01"));

        // TransactionId issuer(MBW)는 최초 trusted Channel이고, Original System(EXS)은
        // 업무 System lineage다. 값이 우연히 같을 수는 있어도 universal equality가 아니다.
        assertEquals("EXS", result.snapshot().context().originalSystemCode());
        assertEquals("MBR", result.snapshot().context().currentSystemCode());
    }

    @Test
    void externalTargetSystemMustMatchReceiverBeforeController() {
        var adapter = adapter();
        assertThrows(CpfHeaderValidationException.class, () -> adapter.resolve(
                externalHeaders("EXS"), CpfHttpIngressTrust.UNTRUSTED_EXTERNAL,
                null, null, null, "POST /members", LocalDate.of(2026, 8, 18), null,
                new CpfRuntimeIdentity("MBR", "member", "MBR01")));
    }

    @Test
    void externalSystemCodeMustMatchReceiverIdentity() {
        var adapter = adapter();
        var headers = externalHeaders("MBR");
        headers.put(CpfHttpHeaderNames.SYSTEM_CODE, "EXS");
        assertThrows(CpfHeaderValidationException.class, () -> adapter.resolve(
                headers, CpfHttpIngressTrust.UNTRUSTED_EXTERNAL,
                null, null, null, "POST /members", LocalDate.of(2026, 8, 18), null,
                new CpfRuntimeIdentity("MBR", "member", "MBR01")));
    }

    @Test
    void externalCanonicalSixHeadersAreAllRequired() {
        var adapter = adapter();
        for (String required : new String[] {
                CpfHttpHeaderNames.TRANSACTION_ID, CpfHttpHeaderNames.ORIGINAL_SYSTEM_CODE,
                CpfHttpHeaderNames.SYSTEM_CODE, CpfHttpHeaderNames.CALLER_SYSTEM_CODE, CpfHttpHeaderNames.TARGET_SYSTEM_CODE,
                CpfHttpHeaderNames.TARGET_OPERATION_ID}) {
            var headers = externalHeaders("MBR");
            headers.remove(required);
            assertThrows(CpfHeaderValidationException.class, () -> adapter.resolve(
                    headers, CpfHttpIngressTrust.UNTRUSTED_EXTERNAL,
                    null, null, null, "POST /members", LocalDate.of(2026, 8, 18), null,
                    new CpfRuntimeIdentity("MBR", "member", "MBR01")), required);
        }
    }

    @Test
    void systemlessTopologyAcceptsChannelOnlyControlPlaneIngress() {
        var headers = new LinkedHashMap<String,String>();
        headers.put(CpfHttpHeaderNames.TRANSACTION_ID, CANONICAL_TX);
        headers.put(CpfHttpHeaderNames.TARGET_OPERATION_ID, "getAdmReadiness");
        headers.put(CpfHttpHeaderNames.ORIGINAL_CHANNEL, "ADM");
        headers.put(CpfHttpHeaderNames.CURRENT_CHANNEL, "ADM");
        headers.put(CpfHttpHeaderNames.CALLER_CHANNEL, "ADM");
        headers.put(CpfHttpHeaderNames.TARGET_CHANNEL, "ADM");

        var result = adapter().resolve(headers, CpfHttpIngressTrust.UNTRUSTED_EXTERNAL,
                null, null, null, "GET /adm/api/health", LocalDate.of(2026, 8, 18), null,
                new CpfRuntimeIdentity(null, "ADM", "cpf-local-runtime", "local-1"));

        assertEquals(CANONICAL_TX, result.snapshot().transaction().transactionId());
        assertEquals(null, result.snapshot().context().currentSystemCode());
        assertEquals("ADM", result.snapshot().context().currentChannel());
        assertEquals("getAdmReadiness", result.snapshot().context().targetOperationId());
    }

    @Test
    void systemlessTopologyRejectsPartialBusinessSystemMetadata() {
        var headers = new LinkedHashMap<String,String>();
        headers.put(CpfHttpHeaderNames.TRANSACTION_ID, CANONICAL_TX);
        headers.put(CpfHttpHeaderNames.TARGET_OPERATION_ID, "getAdmReadiness");
        headers.put(CpfHttpHeaderNames.SYSTEM_CODE, "MBW");
        assertThrows(CpfHeaderValidationException.class, () -> adapter().resolve(
                headers, CpfHttpIngressTrust.UNTRUSTED_EXTERNAL,
                null, null, null, "GET /adm/api/health", LocalDate.of(2026, 8, 18), null,
                new CpfRuntimeIdentity(null, "ADM", "cpf-local-runtime", "local-1")));
    }

    private static CpfHttpInboundContextAdapter adapter() {
        var txIds = (com.cpf.foundation.id.spi.CpfTransactionIdGenerator) () ->
                "20260818082400001CPFedge0010000001";
        var executionIds = new CpfExecutionIdGenerator() {
            @Override public String newExecutionId() { return "EX-1"; }
            @Override public String newSegmentId() { return "SG-1"; }
        };
        return new CpfHttpInboundContextAdapter(txIds, executionIds);
    }

    private static Map<String,String> externalHeaders(String targetSystemCode) {
        var headers = new LinkedHashMap<String,String>();
        headers.put(CpfHttpHeaderNames.TRANSACTION_ID, CANONICAL_TX);
        headers.put(CpfHttpHeaderNames.ORIGINAL_SYSTEM_CODE, "MBW");
        headers.put(CpfHttpHeaderNames.SYSTEM_CODE, targetSystemCode);
        headers.put(CpfHttpHeaderNames.CALLER_SYSTEM_CODE, "MBW");
        headers.put(CpfHttpHeaderNames.TARGET_SYSTEM_CODE, targetSystemCode);
        // Channel is optional policy/context and is deliberately not one of the canonical six.
        headers.put(CpfHttpHeaderNames.CALLER_CHANNEL, "MBW");
        headers.put(CpfHttpHeaderNames.TARGET_OPERATION_ID, "MBR_MEMBER_JOIN");
        return headers;
    }

    @Test
    void invalidControlCharacterIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> CpfHttpInboundContextAdapter.canonicalTransactionId("contains secret\nheader"));
    }
}
