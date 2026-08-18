package com.cpf.web.context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import com.cpf.core.api.context.CpfContext;
import com.cpf.foundation.id.spi.CpfExecutionIdGenerator;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CpfHttpInboundContextAdapterTest {
    private static final String CANONICAL_TX = "20260810010101001MBRlocal010000001";

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
    void externalDirectCallAcceptsFiveHeadersAndReceiverSetsCurrentChannel() {
        var adapter = adapter();
        var result = adapter.resolve(externalHeaders("MBR"), CpfHttpIngressTrust.UNTRUSTED_EXTERNAL,
                null, null, null, "POST /members", LocalDate.of(2026, 8, 18), null,
                new CpfRuntimeIdentity("MBR", "member", "MBR01"));

        assertEquals(CANONICAL_TX, result.snapshot().transaction().transactionId());
        assertEquals("WEB2", result.snapshot().context().originalChannel());
        assertEquals("MBR", result.snapshot().context().currentChannel());
        assertEquals("WEB2", result.snapshot().context().callerChannel());
        assertEquals("MBR", result.snapshot().context().targetChannel());
        assertEquals("MBR_MEMBER_JOIN", result.snapshot().context().targetOperationId());
    }

    @Test
    void externalCurrentChannelIsOptionalAndNeverRequired() {
        var adapter = adapter();
        var headers = externalHeaders("MBR");
        headers.remove(CpfHttpHeaderNames.CURRENT_CHANNEL);
        var result = adapter.resolve(headers, CpfHttpIngressTrust.UNTRUSTED_EXTERNAL,
                null, null, null, "POST /members", LocalDate.of(2026, 8, 18), null,
                new CpfRuntimeIdentity("MBR", "member", "MBR01"));
        assertEquals("MBR", result.snapshot().context().currentChannel());
    }

    @Test
    void externalTargetMustMatchReceiverChannelBeforeController() {
        var adapter = adapter();
        assertThrows(CpfHeaderValidationException.class, () -> adapter.resolve(
                externalHeaders("EXS"), CpfHttpIngressTrust.UNTRUSTED_EXTERNAL,
                null, null, null, "POST /members", LocalDate.of(2026, 8, 18), null,
                new CpfRuntimeIdentity("MBR", "member", "MBR01")));
    }

    @Test
    void externalCurrentAssertionCannotOverrideReceiverIdentity() {
        var adapter = adapter();
        var headers = externalHeaders("MBR");
        headers.put(CpfHttpHeaderNames.CURRENT_CHANNEL, "EXS");
        assertThrows(CpfHeaderValidationException.class, () -> adapter.resolve(
                headers, CpfHttpIngressTrust.UNTRUSTED_EXTERNAL,
                null, null, null, "POST /members", LocalDate.of(2026, 8, 18), null,
                new CpfRuntimeIdentity("MBR", "member", "MBR01")));
    }

    @Test
    void externalFiveHeadersAreAllRequired() {
        var adapter = adapter();
        for (String required : new String[] {
                CpfHttpHeaderNames.TRANSACTION_ID, CpfHttpHeaderNames.ORIGINAL_CHANNEL,
                CpfHttpHeaderNames.CALLER_CHANNEL, CpfHttpHeaderNames.TARGET_CHANNEL,
                CpfHttpHeaderNames.TARGET_OPERATION_ID}) {
            var headers = externalHeaders("MBR");
            headers.remove(required);
            assertThrows(CpfHeaderValidationException.class, () -> adapter.resolve(
                    headers, CpfHttpIngressTrust.UNTRUSTED_EXTERNAL,
                    null, null, null, "POST /members", LocalDate.of(2026, 8, 18), null,
                    new CpfRuntimeIdentity("MBR", "member", "MBR01")), required);
        }
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

    private static Map<String,String> externalHeaders(String targetChannel) {
        var headers = new LinkedHashMap<String,String>();
        headers.put(CpfHttpHeaderNames.TRANSACTION_ID, CANONICAL_TX);
        headers.put(CpfHttpHeaderNames.ORIGINAL_CHANNEL, "WEB2");
        headers.put(CpfHttpHeaderNames.CALLER_CHANNEL, "WEB2");
        headers.put(CpfHttpHeaderNames.TARGET_CHANNEL, targetChannel);
        headers.put(CpfHttpHeaderNames.TARGET_OPERATION_ID, "MBR_MEMBER_JOIN");
        return headers;
    }

    @Test
    void invalidControlCharacterIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> CpfHttpInboundContextAdapter.canonicalTransactionId("contains secret\nheader"));
    }
}
