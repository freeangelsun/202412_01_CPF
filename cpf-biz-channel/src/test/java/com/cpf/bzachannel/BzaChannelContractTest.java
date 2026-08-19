package com.cpf.bzachannel;

import static org.junit.jupiter.api.Assertions.*;
import com.cpf.bzachannel.shared.routing.BzaOperationRouteCatalog;
import com.cpf.bzachannel.shared.protocol.CanonicalTransactionHeaders;
import com.cpf.bzachannel.shared.protocol.ChannelTransactionIdGenerator;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class BzaChannelContractTest {
    @Test void routeCatalogUsesCanonicalBackendOperationId() {
        var catalog = new BzaOperationRouteCatalog();
        assertEquals(96, catalog.size());
        assertEquals("bzaApprovalSubmissionDetail", catalog.require("GET", "/api/bza/approvals/submissions/ABC").operationId());
    }
    @Test void transactionIdUsesPublicWireFormatWithoutCpfJavaDependency() {
        var generator = new ChannelTransactionIdGenerator("BCH", "BZA01", Clock.fixed(Instant.parse("2026-08-19T00:00:00Z"), ZoneOffset.UTC));
        String id = generator.next();
        assertTrue(id.matches("\\d{17}BCH[A-Z0-9]{7}\\d{7}"));
        assertEquals(34, id.length());
    }
    @Test void receiverOwnedSystemHeaderIsProtectedFromBrowser() {
        assertTrue(CanonicalTransactionHeaders.BROWSER_FORBIDDEN.contains("X-System-Code"));
    }
}
