package com.cpf.starter.foundation.base;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

class CpfGeneratedDomainPolicyRuntimeVerifierTest {
    private static final List<String> FIELDS = List.of(
            "exception_id", "module", "capability", "artifact", "version", "owner", "reason",
            "standard_path_gap", "environments", "security_impact", "license_review",
            "supply_chain_review", "operations_responsibility", "approved_by", "approved_at",
            "expires_at", "rollback", "return_plan", "rule_ids", "config_files",
            "evidence_path", "status", "config_hash");
    private static final String HEADER = String.join(",", FIELDS) + "\n";
    private static final Instant NOW = Instant.parse("2026-08-02T00:00:00Z");

    @Test
    void acceptsMinimalPolicyWithoutApprovedExceptions() throws Exception {
        String policy = policy(sha256(HEADER.getBytes(StandardCharsets.UTF_8)), "");
        var verifier = verifier(Map.of(
                CpfGeneratedDomainPolicyRuntimeVerifier.POLICY_RESOURCE, policy,
                CpfGeneratedDomainPolicyRuntimeVerifier.EXCEPTION_RESOURCE, HEADER), key -> null);

        var result = verifier.verify();
        assertThat(result.generatedDomain()).isTrue();
        assertThat(result.profile()).isEqualTo("minimal-domain");
        assertThat(result.approvedExceptionCount()).isZero();
    }

    @Test
    void acceptsApprovedDomainSideExceptionWithRuntimeAttestation() throws Exception {
        ExceptionFixture fixture = approvedFixture("2027-08-02T00:00:00Z", "client.timeout=3s\n");
        var verifier = verifier(fixture.resources(), key -> fixture.attestation().get(key));

        var result = verifier.verify();
        assertThat(result.approvedExceptionCount()).isEqualTo(1);
        assertThat(result.status()).isEqualTo("UP");
    }

    @Test
    void rejectsMissingRuntimeAttestation() throws Exception {
        ExceptionFixture fixture = approvedFixture("2027-08-02T00:00:00Z", "client.timeout=3s\n");
        assertThatThrownBy(() -> verifier(fixture.resources(), key -> null).verify())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Runtime attestation property is missing");
    }

    @Test
    void rejectsExpiredException() throws Exception {
        ExceptionFixture fixture = approvedFixture("2026-08-01T23:59:59Z", "client.timeout=3s\n");
        assertThatThrownBy(() -> verifier(fixture.resources(), fixture.attestation()::get).verify())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("expired");
    }

    @Test
    void rejectsConfigHashTampering() throws Exception {
        ExceptionFixture fixture = approvedFixture("2027-08-02T00:00:00Z", "client.timeout=3s\n");
        Map<String, String> tampered = new HashMap<>(fixture.resources());
        tampered.put("config/customer-client.properties", "client.timeout=99s\n");
        assertThatThrownBy(() -> verifier(tampered, fixture.attestation()::get).verify())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("config_hash");
    }


    @Test
    void rejectsEnvironmentScopeMismatch() throws Exception {
        ExceptionFixture fixture = approvedFixture("2027-08-02T00:00:00Z", "client.timeout=3s\n");
        Map<String, String> attestation = new HashMap<>(fixture.attestation());
        attestation.put("cpf.generated-domain.environment", "dev");
        assertThatThrownBy(() -> verifier(fixture.resources(), attestation::get).verify())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("environment scope mismatch");
    }

    @Test
    void rejectsActiveArtifactVersionDrift() throws Exception {
        ExceptionFixture fixture = approvedFixture("2027-08-02T00:00:00Z", "client.timeout=3s\n");
        Map<String, String> attestation = new HashMap<>(fixture.attestation());
        String prefix = "cpf.generated-domain.approved-exceptions.CPF-EX-CUSTOM_HTTP";
        attestation.put(prefix + ".artifact-version", "1.2.4");
        assertThatThrownBy(() -> verifier(fixture.resources(), attestation::get).verify())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("active_artifact_version");
    }

    @Test
    void reportsApprovedExceptionIdsAndActiveEnvironment() throws Exception {
        ExceptionFixture fixture = approvedFixture("2027-08-02T00:00:00Z", "client.timeout=3s\n");
        var result = verifier(fixture.resources(), fixture.attestation()::get).verify();
        assertThat(result.activeEnvironment()).isEqualTo("prod");
        assertThat(result.approvedExceptionIds()).containsExactly("CPF-EX-CUSTOM_HTTP");
    }

    @Test
    void rejectsRegistryHashDrift() {
        String policy = policy("0".repeat(64), "");
        var verifier = verifier(Map.of(
                CpfGeneratedDomainPolicyRuntimeVerifier.POLICY_RESOURCE, policy,
                CpfGeneratedDomainPolicyRuntimeVerifier.EXCEPTION_RESOURCE, HEADER), key -> null);
        assertThatThrownBy(verifier::verify).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("exceptionRegistrySha256");
    }

    private static CpfGeneratedDomainPolicyRuntimeVerifier verifier(
            Map<String, String> resources, Function<String, String> lookup) {
        return new CpfGeneratedDomainPolicyRuntimeVerifier(
                new MapClassLoader(resources), Clock.fixed(NOW, ZoneOffset.UTC), lookup);
    }

    private static ExceptionFixture approvedFixture(String expiresAt, String config) throws Exception {
        List<String> values = new ArrayList<>(List.of(
                "CPF-EX-CUSTOM_HTTP", "cpf-sample", "integration", "com.customer:custom-http", "1.2.3",
                "integration-team", "customer protocol", "CPF HTTP starter cannot support proprietary handshake",
                "prod", "reviewed", "approved", "approved", "integration-team", "security-owner",
                "2026-08-01T00:00:00Z", expiresAt, "disable custom-http", "promote as CPF provider",
                "DIRECT_CUSTOM_HTTP", "src/main/resources/config/customer-client.properties",
                "cpf-docs/evidence/customer-http.md", "APPROVED", ""));
        values.set(values.size() - 1, exceptionHash(values, config));
        String registry = HEADER + csv(values) + "\n";
        String id = values.getFirst();
        String hash = values.getLast();
        Map<String, String> resources = Map.of(
                CpfGeneratedDomainPolicyRuntimeVerifier.POLICY_RESOURCE,
                policy(sha256(registry.getBytes(StandardCharsets.UTF_8)), id),
                CpfGeneratedDomainPolicyRuntimeVerifier.EXCEPTION_RESOURCE, registry,
                "config/customer-client.properties", config);
        String prefix = "cpf.generated-domain.approved-exceptions." + id;
        return new ExceptionFixture(resources, Map.of(
                "cpf.generated-domain.environment", "prod",
                prefix + ".config-hash", hash,
                prefix + ".artifact-version", values.get(4)));
    }

    private static String exceptionHash(List<String> values, String config) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(String.join("\u001f", values.subList(0, values.size() - 1))
                .getBytes(StandardCharsets.UTF_8));
        digest.update((byte) 0);
        String path = values.get(19);
        digest.update(path.getBytes(StandardCharsets.UTF_8));
        digest.update((byte) 0);
        digest.update(config.getBytes(StandardCharsets.UTF_8));
        digest.update((byte) 0);
        return hex(digest.digest());
    }

    private static String csv(List<String> values) {
        return values.stream().map(value -> value.contains(",") || value.contains("\"")
                        ? "\"" + value.replace("\"", "\"\"") + "\"" : value)
                .reduce((left, right) -> left + "," + right).orElse("");
    }

    private static String policy(String registryHash, String ids) {
        return "policyVersion=1.0\n"
                + "module=cpf-sample\n"
                + "profile=minimal-domain\n"
                + "platformVersion=1.0.0-SNAPSHOT\n"
                + "capabilities=\n"
                + "requiredStandards=standard-error,header-context,transaction-id,security-boundary,audit,masking,observability,config,dependency-version,architecture-gate\n"
                + "approvedExceptionIds=" + ids + "\n"
                + "exceptionRegistrySha256=" + registryHash + "\n"
                + "failClosed=true\n";
    }

    private static String sha256(byte[] bytes) throws Exception {
        return hex(MessageDigest.getInstance("SHA-256").digest(bytes));
    }

    private static String hex(byte[] bytes) {
        StringBuilder value = new StringBuilder();
        for (byte item : bytes) {
            value.append(String.format("%02x", item));
        }
        return value.toString();
    }

    private record ExceptionFixture(Map<String, String> resources, Map<String, String> attestation) {}

    private static final class MapClassLoader extends ClassLoader {
        private final Map<String, byte[]> resources = new HashMap<>();

        private MapClassLoader(Map<String, String> source) {
            source.forEach((key, value) -> resources.put(key, value.getBytes(StandardCharsets.UTF_8)));
        }

        @Override
        public InputStream getResourceAsStream(String name) {
            byte[] bytes = resources.get(name);
            return bytes == null ? null : new ByteArrayInputStream(bytes);
        }
    }
}
