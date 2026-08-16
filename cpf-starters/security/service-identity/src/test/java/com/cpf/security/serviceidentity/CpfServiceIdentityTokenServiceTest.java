package com.cpf.security.serviceidentity;
import java.time.*;import org.junit.jupiter.api.Test;import static org.assertj.core.api.Assertions.*;
class CpfServiceIdentityTokenServiceTest {
 private static CpfServiceIdentityProperties props(){var p=new CpfServiceIdentityProperties();p.setEnabled(true);p.setServiceId("ADM");p.setActiveKeyId("k2");p.setActiveSecret("01234567890123456789012345678901");p.setPreviousKeyId("k1");p.setPreviousSecret("abcdefghijklmnopqrstuvwxyzABCDEF");p.setTtl(Duration.ofSeconds(60));p.setClockSkew(Duration.ZERO);return p;}
 @Test void issuesAndVerifiesAudienceAndNonce(){var clock=Clock.fixed(Instant.parse("2026-08-02T00:00:00Z"),ZoneOffset.UTC);var service=new CpfServiceIdentityTokenService(props(),clock);var verified=service.verify(service.issue("BATCH","n-1"),"BATCH");assertThat(verified.serviceId()).isEqualTo("ADM");assertThat(verified.nonce()).isEqualTo("n-1");}
 @Test void rejectsWrongAudience(){var service=new CpfServiceIdentityTokenService(props(),Clock.fixed(Instant.EPOCH,ZoneOffset.UTC));assertThatThrownBy(()->service.verify(service.issue("BATCH","n"),"GATEWAY")).isInstanceOf(SecurityException.class);}
}
