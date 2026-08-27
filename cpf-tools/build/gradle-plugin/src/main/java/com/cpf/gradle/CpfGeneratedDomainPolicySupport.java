package com.cpf.gradle;

import groovy.json.JsonSlurper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

final class CpfGeneratedDomainPolicySupport {
    static final String POLICY_PATH = "src/main/resources/META-INF/cpf/generated-domain-policy.properties";
    static final String EXCEPTION_CONFIG_PATH = "config/cpf-approved-exceptions.csv";
    static final String EXCEPTION_RESOURCE_PATH = "src/main/resources/META-INF/cpf/cpf-approved-exceptions.csv";
    static final String LOCK_PATH = "manifest/resolved-starter-lock.json";
    static final String MANIFEST_PATH = "manifest/domain-manifest.json";
    static final List<String> PUBLIC_PROFILES = List.of(
            "minimal-domain", "web-api", "secure-api", "browser-bff", "event-service", "batch-service");
    static final Set<String> PUBLIC_CAPABILITIES = Set.of(
            "data", "messaging", "integration", "file", "notification", "security", "platform-operations");
    static final List<String> REQUIRED_STANDARDS = List.of(
            "standard-error", "header-context", "transaction-id", "security-boundary", "audit",
            "masking", "observability", "config", "dependency-version", "architecture-gate");
    static final List<String> EXCEPTION_FIELDS = List.of(
            "exception_id", "module", "capability", "artifact", "version", "owner", "reason",
            "standard_path_gap", "environments", "security_impact", "license_review",
            "supply_chain_review", "operations_responsibility", "approved_by", "approved_at",
            "expires_at", "rollback", "return_plan", "rule_ids", "config_files",
            "evidence_path", "status", "config_hash");
    static final List<String> HASH_FIELDS = EXCEPTION_FIELDS.subList(0, EXCEPTION_FIELDS.size() - 1);

    private CpfGeneratedDomainPolicySupport() {}

    static Properties readProperties(Path path) throws IOException {
        Properties properties = new Properties();
        try (var input = Files.newInputStream(path)) {
            properties.load(input);
        }
        return properties;
    }

    @SuppressWarnings("unchecked")
    static Map<String, Object> readJsonObject(Path path) throws IOException {
        Object parsed = new JsonSlurper().parse(path.toFile(), StandardCharsets.UTF_8.name());
        if (!(parsed instanceof Map<?, ?> map)) {
            throw new IllegalArgumentException("JSON object required: " + path);
        }
        return (Map<String, Object>) map;
    }

    static List<Map<String, String>> readExceptionCsv(Path path) throws IOException {
        if (!Files.isRegularFile(path)) {
            throw new IllegalArgumentException("CPF approved exception registry is missing: " + path);
        }
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("CPF approved exception registry is empty: " + path);
        }
        List<String> header = parseCsvLine(stripBom(lines.getFirst()));
        if (!header.equals(EXCEPTION_FIELDS)) {
            throw new IllegalArgumentException("CPF approved exception header mismatch. expected="
                    + EXCEPTION_FIELDS + ", actual=" + header);
        }
        List<Map<String, String>> result = new ArrayList<>();
        for (int lineNumber = 2; lineNumber <= lines.size(); lineNumber++) {
            String line = lines.get(lineNumber - 1);
            if (line.isBlank() || line.stripLeading().startsWith("#")) {
                continue;
            }
            List<String> values = parseCsvLine(line);
            if (values.size() != header.size()) {
                throw new IllegalArgumentException("CPF approved exception column mismatch at line " + lineNumber);
            }
            Map<String, String> row = new LinkedHashMap<>();
            for (int i = 0; i < header.size(); i++) {
                row.put(header.get(i), values.get(i).trim());
            }
            result.add(Collections.unmodifiableMap(row));
        }
        return Collections.unmodifiableList(result);
    }

    static void validateExceptionRows(
            List<Map<String, String>> rows,
            String projectName,
            Map<String, Object> manifest,
            Instant now,
            Path projectDir) throws IOException {
        Set<String> ids = new LinkedHashSet<>();
        String domainName = stringValue(manifest.get("domainName"));
        String manifestProjectName = stringValue(manifest.get("projectName"));
        for (Map<String, String> row : rows) {
            for (String field : EXCEPTION_FIELDS) {
                if (row.getOrDefault(field, "").isBlank()) {
                    throw new IllegalArgumentException("CPF approved exception field is blank: " + field
                            + ", exception=" + row.get("exception_id"));
                }
            }
            String id = row.get("exception_id");
            if (!id.matches("^CPF-EX-[A-Z0-9_-]{3,64}$")) {
                throw new IllegalArgumentException("Invalid CPF approved exception id: " + id);
            }
            if (!ids.add(id)) {
                throw new IllegalArgumentException("Duplicate CPF approved exception id: " + id);
            }
            String module = row.get("module");
            Set<String> allowedModules = new LinkedHashSet<>();
            for (String candidate : List.of(projectName, domainName, manifestProjectName)) {
                if (candidate != null && !candidate.isBlank()) {
                    allowedModules.add(candidate);
                }
            }
            if (!allowedModules.contains(module)) {
                throw new IllegalArgumentException("CPF exception module does not match generated domain: " + id
                        + ", module=" + module + ", allowed=" + allowedModules);
            }
            String capability = row.get("capability").toLowerCase(Locale.ROOT);
            if (!PUBLIC_CAPABILITIES.contains(capability)) {
                throw new IllegalArgumentException("Unsupported CPF exception capability: " + id + ", " + capability);
            }
            Set<String> environments = semicolonSet(row.get("environments"));
            if (environments.isEmpty() || environments.stream().anyMatch(
                    environment -> !environment.matches("^[A-Za-z0-9][A-Za-z0-9_.-]{0,63}$"))) {
                throw new IllegalArgumentException("CPF exception environments are invalid: " + id
                        + ", environments=" + environments);
            }
            if (!row.get("artifact").matches("^[A-Za-z0-9_.-]+:[A-Za-z0-9_.-]+$")) {
                throw new IllegalArgumentException("CPF exception artifact must be exact group:name: " + id);
            }
            if (!row.get("version").matches("^[0-9A-Za-z][0-9A-Za-z._+\\-]{0,127}$")) {
                throw new IllegalArgumentException("CPF exception version is invalid: " + id);
            }
            if (!"APPROVED".equals(row.get("status"))) {
                throw new IllegalArgumentException("CPF exception is not APPROVED: " + id);
            }
            Instant approvedAt = parseInstant(row.get("approved_at"));
            Instant expiresAt = parseInstant(row.get("expires_at"));
            if (approvedAt.isAfter(now)) {
                throw new IllegalArgumentException("CPF exception approval time is in the future: " + id);
            }
            if (!expiresAt.isAfter(now)) {
                throw new IllegalArgumentException("CPF exception is expired: " + id + ", expiresAt=" + expiresAt);
            }
            if (semicolonSet(row.get("rule_ids")).isEmpty()) {
                throw new IllegalArgumentException("CPF exception rule_ids is empty: " + id);
            }
            List<String> configFiles = semicolonList(row.get("config_files"));
            if (configFiles.isEmpty()) {
                throw new IllegalArgumentException("CPF exception config_files is empty: " + id);
            }
            for (String configFile : configFiles) {
                if (!configFile.replace('\\', '/').startsWith("src/main/resources/")) {
                    throw new IllegalArgumentException(
                            "CPF exception config must be a Generated Domain runtime resource: " + id
                                    + ", path=" + configFile);
                }
            }
            String expectedHash = exceptionConfigHash(row, projectDir);
            if (!MessageDigest.isEqual(expectedHash.getBytes(StandardCharsets.US_ASCII),
                    row.get("config_hash").toLowerCase(Locale.ROOT).getBytes(StandardCharsets.US_ASCII))) {
                throw new IllegalArgumentException("CPF exception config hash mismatch: " + id);
            }
            Path evidence = safeModulePath(projectDir, row.get("evidence_path"), "evidence", id);
            if (!Files.isRegularFile(evidence)) {
                throw new IllegalArgumentException("CPF exception evidence does not exist: " + id
                        + ", path=" + row.get("evidence_path"));
            }
        }
    }

    static String exceptionConfigHash(Map<String, String> row, Path projectDir) throws IOException {
        MessageDigest digest = sha256Digest();
        String canonical = HASH_FIELDS.stream()
                .map(field -> normalizeHashValue(row.get(field)))
                .collect(Collectors.joining("\u001f"));
        digest.update(canonical.getBytes(StandardCharsets.UTF_8));
        digest.update((byte) 0);
        for (String relative : semicolonList(row.get("config_files"))) {
            Path file = safeModulePath(projectDir, relative, "config", row.get("exception_id"));
            if (!Files.isRegularFile(file)) {
                throw new IllegalArgumentException("CPF exception config does not exist: " + relative);
            }
            digest.update(relative.replace('\\', '/').getBytes(StandardCharsets.UTF_8));
            digest.update((byte) 0);
            digest.update(Files.readAllBytes(file));
            digest.update((byte) 0);
        }
        return hex(digest.digest());
    }

    static String fileSha256(Path path) throws IOException {
        return sha256(Files.readAllBytes(path));
    }

    static String sha256(byte[] bytes) {
        MessageDigest digest = sha256Digest();
        return hex(digest.digest(bytes));
    }

    static List<String> parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (quoted) {
                if (ch == '"' && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else if (ch == '"') {
                    quoted = false;
                } else {
                    current.append(ch);
                }
            } else if (ch == '"') {
                quoted = true;
            } else if (ch == ',') {
                values.add(current.toString());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }
        if (quoted) {
            throw new IllegalArgumentException("Unclosed CSV quote");
        }
        values.add(current.toString());
        return values;
    }

    static Instant parseInstant(String value) {
        Objects.requireNonNull(value, "value");
        try {
            return OffsetDateTime.parse(value).toInstant();
        } catch (RuntimeException ignored) {
            return LocalDate.parse(value).atStartOfDay().toInstant(ZoneOffset.UTC);
        }
    }

    static String stringValue(Object value) {
        return value == null ? "" : value.toString().trim();
    }

    static Set<String> stringSet(Object value) {
        if (!(value instanceof Iterable<?> iterable)) {
            return Set.of();
        }
        Set<String> values = new LinkedHashSet<>();
        iterable.forEach(item -> values.add(stringValue(item)));
        return Collections.unmodifiableSet(values);
    }

    static Set<String> semicolonSet(String value) {
        return Collections.unmodifiableSet(new LinkedHashSet<>(semicolonList(value)));
    }

    static List<String> semicolonList(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return value.lines()
                .flatMap(line -> java.util.Arrays.stream(line.split(";")))
                .map(token -> token.trim())
                .filter(item -> !item.isBlank())
                .distinct()
                .sorted()
                .toList();
    }

    static List<Map<String, String>> lockExceptions(Map<String, Object> lock) {
        Object value = lock.get("approvedExceptions");
        if (!(value instanceof Iterable<?> iterable)) {
            return List.of();
        }
        List<Map<String, String>> result = new ArrayList<>();
        for (Object item : iterable) {
            if (!(item instanceof Map<?, ?> map)) {
                throw new IllegalArgumentException("approvedExceptions must contain objects");
            }
            Map<String, String> normalized = new LinkedHashMap<>();
            map.forEach((key, entryValue) -> normalized.put(String.valueOf(key), stringValue(entryValue)));
            result.add(normalized);
        }
        return result;
    }

    static String stripBom(String value) {
        return value.startsWith("\ufeff") ? value.substring(1) : value;
    }

    private static Path safeModulePath(Path projectDir, String relative, String kind, String id) {
        Path base = projectDir.toAbsolutePath().normalize();
        Path resolved = base.resolve(relative).normalize();
        if (!resolved.startsWith(base)) {
            throw new IllegalArgumentException("CPF exception " + kind + " path escapes module: " + id);
        }
        return resolved;
    }

    private static String normalizeHashValue(String value) {
        return value == null ? "" : value.trim().replace("\r\n", "\n").replace('\r', '\n');
    }

    private static MessageDigest sha256Digest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (Exception exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private static String hex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            builder.append(Character.forDigit((value >>> 4) & 0x0f, 16));
            builder.append(Character.forDigit(value & 0x0f, 16));
        }
        return builder.toString();
    }
}
