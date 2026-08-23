package com.cpf.admin.approval.security;

import com.cpf.data.spi.quality.CpfDataQualityCorrectionPort;
import org.junit.jupiter.api.Test;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class AdmDataQualityApprovalProofServiceTest {
    private static AdmDataQualityApprovalProofService service(Instant now) {
        return new AdmDataQualityApprovalProofService(
                Base64.getEncoder().encodeToString(new byte[32]),
                null,
                Duration.ofMinutes(15),
                Clock.fixed(now, ZoneOffset.UTC));
    }

    @Test void rejectsCallerForgedOrTamperedCapability() {
        Instant at = Instant.parse("2026-08-07T00:00:00Z");
        AdmDataQualityApprovalProofService service = service(at);
        String hash = "a".repeat(64); String nonce = "nonce-0123456789abcdef"; String ref = "ADM-APP-1-CMD-1";
        String proof = service.sign("DQ-1",1,ref,hash,nonce,at);
        var valid = new CpfDataQualityCorrectionPort.ApprovedCorrection("DQ-1",1,Map.of("name","ok"),
                "approver","approved correction",ref,hash,nonce,proof,at);
        assertTrue(service.verify(valid));
        var tampered = new CpfDataQualityCorrectionPort.ApprovedCorrection("DQ-1",2,Map.of("name","ok"),
                "approver","approved correction",ref,hash,nonce,proof,at);
        assertFalse(service.verify(tampered));
        var forged = new CpfDataQualityCorrectionPort.ApprovedCorrection("DQ-1",1,Map.of("name","ok"),
                "approver","approved correction",ref,hash,nonce,"f".repeat(64),at);
        assertFalse(service.verify(forged));
    }

    @Test void rejectsInvalidSecretMaterial() {
        assertThrows(IllegalStateException.class, () -> new AdmDataQualityApprovalProofService("not-base64"));
        assertThrows(IllegalStateException.class, () -> new AdmDataQualityApprovalProofService(Base64.getEncoder().encodeToString(new byte[16])));
    }
}
