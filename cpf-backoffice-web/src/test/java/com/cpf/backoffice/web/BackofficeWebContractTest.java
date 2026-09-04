package com.cpf.backoffice.web;

import static org.junit.jupiter.api.Assertions.*;
import com.cpf.backoffice.web.shared.routing.BackofficeOperationRouteCatalog;
import com.cpf.backoffice.web.shared.protocol.CanonicalTransactionHeaders;
import com.cpf.backoffice.web.shared.protocol.ChannelTransactionIdGenerator;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class BackofficeWebContractTest {
    @Test void routeCatalogUsesCanonicalBackendOperationId() {
        var catalog = new BackofficeOperationRouteCatalog();
        assertEquals(96, catalog.size());
        assertEquals("MBW_APPROVAL_SUBMISSION_DETAIL", catalog.require("GET", "/api/v1/backoffice/approvals/submissions/ABC").operationId());
    }
    @Test void transactionIdUsesPublicWireFormatWithoutCpfJavaDependency() {
        var generator = new ChannelTransactionIdGenerator("MBW", "Backoffice01", Clock.fixed(Instant.parse("2026-08-19T00:00:00Z"), ZoneOffset.UTC));
        String id = generator.next();
        assertTrue(id.matches("\\d{17}MBW[A-Z0-9]{7}\\d{7}"));
        assertEquals(34, id.length());
    }
    @Test void channelFrontUsesChannelCodeAndASeparateBusinessTarget() {
        var properties = new com.cpf.backoffice.web.shared.config.BackofficeWebProperties(
                null, java.net.URI.create("http://127.0.0.1:8080"),
                java.net.URI.create("http://127.0.0.1:8082"), "MBW", "WEB",
                java.time.Duration.ofSeconds(3), java.time.Duration.ofSeconds(10),
                "CPF_MBW_ACCESS", "CPF_MBW_REFRESH", false, "Strict");
        assertEquals("WEB", properties.channelCode());
        assertEquals("MBW", properties.targetSystemCode());
    }
    @Test void receiverOwnedSystemHeaderIsProtectedFromBrowser() {
        assertTrue(CanonicalTransactionHeaders.BROWSER_FORBIDDEN.contains("X-System-Code"));
    }

}
