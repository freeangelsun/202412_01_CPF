package com.cpf.gradle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CpfGeneratedDomainPolicySupportTest {
    @TempDir Path tempDir;

    @Test
    void validatesApprovedExceptionAndDetectsConfigDrift() throws Exception {
        Path config = tempDir.resolve("src/main/resources/cpf-override.yml");
        Path evidence = tempDir.resolve("evidence/approval.txt");
        Files.createDirectories(config.getParent());
        Files.createDirectories(evidence.getParent());
        Files.writeString(config, "cpf:\n  sample: true\n", StandardCharsets.UTF_8);
        Files.writeString(evidence, "approved", StandardCharsets.UTF_8);
        Map<String, String> row = validRow();
        row.put("config_hash", CpfGeneratedDomainPolicySupport.exceptionConfigHash(row, tempDir));
        Map<String, Object> manifest = Map.of("domainName", "account", "projectName", "cpf-account");
        CpfGeneratedDomainPolicySupport.validateExceptionRows(
                List.of(row), "cpf-account", manifest, Instant.parse("2026-08-02T00:00:00Z"), tempDir);

        Files.writeString(config, "cpf:\n  sample: false\n", StandardCharsets.UTF_8);
        assertThrows(IllegalArgumentException.class, () ->
                CpfGeneratedDomainPolicySupport.validateExceptionRows(
                        List.of(row), "cpf-account", manifest,
                        Instant.parse("2026-08-02T00:00:00Z"), tempDir));
    }


    @Test
    void acceptsDomainNameEqualToProjectNameWithoutDuplicateSetFailure() throws Exception {
        Path config = tempDir.resolve("src/main/resources/cpf-override.yml");
        Path evidence = tempDir.resolve("evidence/approval.txt");
        Files.createDirectories(config.getParent());
        Files.createDirectories(evidence.getParent());
        Files.writeString(config, "cpf:\n  sample: true\n", StandardCharsets.UTF_8);
        Files.writeString(evidence, "approved", StandardCharsets.UTF_8);
        Map<String, String> row = validRow();
        row.put("module", "cpf-account");
        row.put("config_hash", CpfGeneratedDomainPolicySupport.exceptionConfigHash(row, tempDir));
        Map<String, Object> manifest = Map.of("domainName", "cpf-account", "projectName", "cpf-account");
        CpfGeneratedDomainPolicySupport.validateExceptionRows(
                List.of(row), "cpf-account", manifest,
                Instant.parse("2026-08-02T00:00:00Z"), tempDir);
    }

    @Test
    void rejectsInvalidEnvironmentScope() throws Exception {
        Path config = tempDir.resolve("src/main/resources/cpf-override.yml");
        Path evidence = tempDir.resolve("evidence/approval.txt");
        Files.createDirectories(config.getParent());
        Files.createDirectories(evidence.getParent());
        Files.writeString(config, "cpf:\n  sample: true\n", StandardCharsets.UTF_8);
        Files.writeString(evidence, "approved", StandardCharsets.UTF_8);
        Map<String, String> row = validRow();
        row.put("environments", "prod;../escape");
        row.put("config_hash", CpfGeneratedDomainPolicySupport.exceptionConfigHash(row, tempDir));
        Map<String, Object> manifest = Map.of("domainName", "account", "projectName", "cpf-account");
        assertThrows(IllegalArgumentException.class, () ->
                CpfGeneratedDomainPolicySupport.validateExceptionRows(
                        List.of(row), "cpf-account", manifest,
                        Instant.parse("2026-08-02T00:00:00Z"), tempDir));
    }

    @Test
    void parsesQuotedCsv() {
        assertEquals(List.of("a", "b,c", "d\"e"),
                CpfGeneratedDomainPolicySupport.parseCsvLine("a,\"b,c\",\"d\"\"e\""));
    }

    private Map<String, String> validRow() {
        Map<String, String> row = new LinkedHashMap<>();
        row.put("exception_id", "CPF-EX-ACCOUNT_001");
        row.put("module", "cpf-account");
        row.put("capability", "messaging");
        row.put("artifact", "org.springframework.kafka:spring-kafka");
        row.put("version", "4.0.0");
        row.put("owner", "account-team");
        row.put("reason", "customer extension");
        row.put("standard_path_gap", "provider not yet supported");
        row.put("environments", "prod");
        row.put("security_impact", "reviewed");
        row.put("license_review", "approved");
        row.put("supply_chain_review", "approved");
        row.put("operations_responsibility", "account-team");
        row.put("approved_by", "platform-owner");
        row.put("approved_at", "2026-08-01T00:00:00Z");
        row.put("expires_at", "2027-08-01T00:00:00Z");
        row.put("rollback", "remove dependency");
        row.put("return_plan", "formal provider");
        row.put("rule_ids", "DIRECT_KAFKA;IMPORT_KAFKA_TEMPLATE");
        row.put("config_files", "src/main/resources/cpf-override.yml");
        row.put("evidence_path", "evidence/approval.txt");
        row.put("status", "APPROVED");
        row.put("config_hash", "pending");
        return row;
    }
}
