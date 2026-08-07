package com.cpf.admin.approval.security;

import com.cpf.core.spi.data.quality.CpfDataQualityCorrectionPort;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AdmDataQualityApprovalProofServiceR6Test {
    @Test
    void proofExpiresAfterConfiguredTtl() {
        String key=Base64.getEncoder().encodeToString(new byte[32]);
        Instant issued=Instant.parse("2026-08-07T00:00:00Z");
        AdmDataQualityApprovalProofService signer=new AdmDataQualityApprovalProofService(
                key,null,Duration.ofMinutes(5),Clock.fixed(issued,ZoneOffset.UTC));
        String proof=signer.sign("q1",1,"ADM-APPROVAL:1:c1","a".repeat(64),"n1",issued);
        CpfDataQualityCorrectionPort.ApprovedCorrection command=new CpfDataQualityCorrectionPort.ApprovedCorrection(
                "q1",1,Map.of("x","y"),"actor","reason-123","ADM-APPROVAL:1:c1",
                "a".repeat(64),"n1",proof,issued);
        assertThat(signer.verify(command)).isTrue();
        AdmDataQualityApprovalProofService expired=new AdmDataQualityApprovalProofService(
                key,null,Duration.ofMinutes(5),Clock.fixed(issued.plus(Duration.ofMinutes(6)),ZoneOffset.UTC));
        assertThat(expired.verify(command)).isFalse();
    }
}
