package com.cpf.web.context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import com.cpf.core.api.context.CpfContext;
import com.cpf.foundation.id.spi.CpfExecutionIdGenerator;
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
    void untrustedExternalTransactionIdIsNotAcceptedAsInternalTransactionId() {
        String generated = "20260810010101001CPFedge0010000001";
        var txIds = (com.cpf.foundation.id.spi.CpfTransactionIdGenerator) () -> generated;
        var executionIds = new CpfExecutionIdGenerator() {
            @Override public String newExecutionId() { return "EX-1"; }
            @Override public String newSegmentId() { return "SG-1"; }
        };
        var adapter = new CpfHttpInboundContextAdapter(txIds, executionIds);
        var result = adapter.resolve(
                Map.of(CpfHttpHeaderNames.TRANSACTION_ID, "EXT-TRX-20260810-0001"),
                CpfHttpIngressTrust.UNTRUSTED_EXTERNAL,
                null, null, null, "STD-1", null, null);
        assertEquals(generated, result.snapshot().transaction().transactionId());
        assertNotEquals("EXT-TRX-20260810-0001", result.snapshot().transaction().transactionId());
        assertEquals("EXT-TRX-20260810-0001", result.snapshot().transaction().correlationId());
    }

    @Test
    void invalidControlCharacterIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> CpfHttpInboundContextAdapter.canonicalTransactionId("contains secret\nheader"));
    }
}
