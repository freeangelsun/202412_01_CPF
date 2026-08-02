package com.cpf.starter.base;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Runtime fail-closed verifier for Generator-owned standards and approved domain overrides. */
public final class CpfGeneratedDomainPolicyRuntimeVerifier {
    static final String POLICY_RESOURCE = "META-INF/cpf/generated-domain-policy.properties";
    static final String EXCEPTION_RESOURCE = "META-INF/cpf/cpf-approved-exceptions.csv";
    private static final List<String> PROFILES = List.of(
            "minimal-domain", "web-api", "secure-api", "browser-bff", "event-service", "batch-service");
    private static final Set<String> CAPABILITIES = Set.of(
            "data", "messaging", "integration", "file", "notification", "security", "platform-operations");
    private static final List<String> REQUIRED_STANDARDS = List.of(
            "standard-error", "header-context", "transaction-id", "security-boundary", "audit",
            "masking", "observability", "config", "dependency-version", "architecture-gate");
    private static final List<String> FIELDS = List.of(
            "exception_id", "module", "capability", "artifact", "version", "owner", "reason",
            "standard_path_gap", "environments", "security_impact", "license_review",
            "supply_chain_review", "operations_responsibility", "approved_by", "approved_at",
            "expires_at", "rollback", "return_plan", "rule_ids", "config_files",
            "evidence_path", "status", "config_hash");
    private static final List<String> HASH_FIELDS = FIELDS.subList(0, FIELDS.size() - 1);

    private final ClassLoader classLoader;
    private final Clock clock;
    private final Function<String, String> propertyLookup;
    private volatile VerificationResult lastResult = VerificationResult.notGeneratedDomain();

    public CpfGeneratedDomainPolicyRuntimeVerifier(ClassLoader classLoader, Clock clock) {
        this(classLoader, clock, key -> null);
    }

    public CpfGeneratedDomainPolicyRuntimeVerifier(
            ClassLoader classLoader, Clock clock, Function<String, String> propertyLookup) {
        this.classLoader = classLoader;
        this.clock = clock;
        this.propertyLookup = propertyLookup;
    }

    public VerificationResult verify() {
        byte[] policyBytes = readOptional(POLICY_RESOURCE);
        if (policyBytes == null) {
            lastResult = VerificationResult.notGeneratedDomain();
            return lastResult;
        }
        try {
            Properties policy = new Properties();
            policy.load(new ByteArrayInputStream(policyBytes));
            requireEquals("policyVersion", "1.0", required(policy, "policyVersion"));
            if (!"true".equals(required(policy, "failClosed"))) {
                throw new IllegalStateException("Generated Domain runtime policy must be fail-closed");
            }
            String module = required(policy, "module");
            String profile = required(policy, "profile");
            if (!PROFILES.contains(profile)) {
                throw new IllegalStateException("Unsupported Generated Domain profile: " + profile);
            }
            Set<String> capabilities = csvSet(policy.getProperty("capabilities", ""));
            if (!CAPABILITIES.containsAll(capabilities)) {
                throw new IllegalStateException("Unknown Generated Domain capability: " + capabilities);
            }
            Set<String> standards = csvSet(required(policy, "requiredStandards"));
            if (!standards.equals(new LinkedHashSet<>(REQUIRED_STANDARDS))) {
                throw new IllegalStateException("Mandatory CPF standard inheritance is incomplete: " + standards);
            }

            byte[] exceptionBytes = readRequired(EXCEPTION_RESOURCE);
            requireEquals("exceptionRegistrySha256", required(policy, "exceptionRegistrySha256"),
                    sha256(exceptionBytes));
            List<Map<String, String>> rows = parseCsv(exceptionBytes);
            Set<String> ids = new LinkedHashSet<>();
            Instant now = clock.instant();
            String activeEnvironment = rows.isEmpty()
                    ? "NOT_REQUIRED"
                    : requiredRuntimeProperty("cpf.generated-domain.environment", "registry");
            if (!"NOT_REQUIRED".equals(activeEnvironment)
                    && !activeEnvironment.matches("^[A-Za-z0-9][A-Za-z0-9_.-]{0,63}$")) {
                throw new IllegalStateException(
                        "Generated Domain runtime environment is invalid: " + activeEnvironment);
            }
            for (Map<String, String> row : rows) {
                String id = row.get("exception_id");
                if (!id.matches("^CPF-EX-[A-Z0-9_-]{3,64}$")) {
                    throw new IllegalStateException("Invalid approved exception id: " + id);
                }
                if (!ids.add(id)) {
                    throw new IllegalStateException("Duplicate approved exception id: " + id);
                }
                if (!module.equals(row.get("module"))) {
                    throw new IllegalStateException("Approved exception module mismatch: " + id);
                }
                if (!CAPABILITIES.contains(row.get("capability"))) {
                    throw new IllegalStateException("Unsupported approved exception capability: " + id);
                }
                Set<String> allowedEnvironments = new LinkedHashSet<>(semicolonList(row.get("environments")));
                if (allowedEnvironments.isEmpty() || !allowedEnvironments.contains(activeEnvironment)) {
                    throw new IllegalStateException("External exception environment scope mismatch: " + id
                            + ", active=" + activeEnvironment + ", allowed=" + allowedEnvironments);
                }
                if (!row.get("artifact").matches("^[A-Za-z0-9_.-]+:[A-Za-z0-9_.-]+$")) {
                    throw new IllegalStateException("Approved exception artifact is invalid: " + id);
                }
                if (!row.get("version").matches("^[0-9A-Za-z][0-9A-Za-z._+\\-]{0,127}$")) {
                    throw new IllegalStateException("Approved exception version is invalid: " + id);
                }
                if (!"APPROVED".equals(row.get("status"))) {
                    throw new IllegalStateException("External exception is not approved: " + id);
                }
                Instant approvedAt = parseInstant(row.get("approved_at"));
                Instant expiresAt = parseInstant(row.get("expires_at"));
                if (approvedAt.isAfter(now)) {
                    throw new IllegalStateException("External exception approval is in the future: " + id);
                }
                if (!expiresAt.isAfter(now)) {
                    throw new IllegalStateException("External exception is expired: " + id);
                }
                if (semicolonList(row.get("rule_ids")).isEmpty()) {
                    throw new IllegalStateException("External exception rule_ids is empty: " + id);
                }
                for (String configFile : semicolonList(row.get("config_files"))) {
                    if (!configFile.replace('\\', '/').startsWith("src/main/resources/")) {
                        throw new IllegalStateException(
                                "External exception config is not a packaged domain resource: " + id);
                    }
                }
                requireEquals("config_hash(" + id + ")", row.get("config_hash"), runtimeExceptionHash(row));
                String propertyPrefix = "cpf.generated-domain.approved-exceptions." + id;
                String activeHash = requiredRuntimeProperty(propertyPrefix + ".config-hash", id);
                String activeVersion = requiredRuntimeProperty(propertyPrefix + ".artifact-version", id);
                requireEquals("active_config_hash(" + id + ")", row.get("config_hash"), activeHash);
                requireEquals("active_artifact_version(" + id + ")", row.get("version"), activeVersion);
            }
            Set<String> lockedIds = csvSet(policy.getProperty("approvedExceptionIds", ""));
            if (!ids.equals(lockedIds)) {
                throw new IllegalStateException(
                        "Approved exception id drift. registry=" + ids + ", policy=" + lockedIds);
            }
            lastResult = new VerificationResult(
                    true, profile, capabilities, ids.size(), activeEnvironment,
                    Collections.unmodifiableSet(new LinkedHashSet<>(ids)), "UP");
            return lastResult;
        } catch (IOException exception) {
            throw new IllegalStateException("Generated Domain runtime policy cannot be read", exception);
        }
    }

    public VerificationResult lastResult() {
        return lastResult;
    }

    private String runtimeExceptionHash(Map<String, String> row) throws IOException {
        MessageDigest digest = sha256Digest();
        String canonical = HASH_FIELDS.stream()
                .map(field -> normalize(row.get(field)))
                .collect(Collectors.joining("\u001f"));
        digest.update(canonical.getBytes(StandardCharsets.UTF_8));
        digest.update((byte) 0);
        for (String path : semicolonList(row.get("config_files"))) {
            String resource = path.startsWith("src/main/resources/")
                    ? path.substring("src/main/resources/".length()) : path;
            byte[] bytes = readRequired(resource);
            digest.update(path.replace('\\', '/').getBytes(StandardCharsets.UTF_8));
            digest.update((byte) 0);
            digest.update(bytes);
            digest.update((byte) 0);
        }
        return hex(digest.digest());
    }

    private byte[] readOptional(String resource) {
        try (InputStream input = classLoader.getResourceAsStream(resource)) {
            return input == null ? null : input.readAllBytes();
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot read classpath resource: " + resource, exception);
        }
    }

    private byte[] readRequired(String resource) {
        byte[] bytes = readOptional(resource);
        if (bytes == null) {
            throw new IllegalStateException("Required Generated Domain resource is missing: " + resource);
        }
        return bytes;
    }

    private static List<Map<String, String>> parseCsv(byte[] bytes) throws IOException {
        List<String> lines;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new ByteArrayInputStream(bytes), StandardCharsets.UTF_8))) {
            lines = reader.lines().toList();
        }
        if (lines.isEmpty()) {
            throw new IllegalStateException("Approved exception registry is empty");
        }
        List<String> header = parseCsvLine(stripBom(lines.getFirst()));
        if (!header.equals(FIELDS)) {
            throw new IllegalStateException("Approved exception registry header mismatch: " + header);
        }
        List<Map<String, String>> rows = new ArrayList<>();
        for (int index = 1; index < lines.size(); index++) {
            String line = lines.get(index);
            if (line.isBlank() || line.stripLeading().startsWith("#")) {
                continue;
            }
            List<String> values = parseCsvLine(line);
            if (values.size() != FIELDS.size()) {
                throw new IllegalStateException("Approved exception column mismatch at line " + (index + 1));
            }
            Map<String, String> row = new LinkedHashMap<>();
            for (int fieldIndex = 0; fieldIndex < FIELDS.size(); fieldIndex++) {
                String value = values.get(fieldIndex).trim();
                if (value.isBlank()) {
                    throw new IllegalStateException("Approved exception field is blank: " + FIELDS.get(fieldIndex));
                }
                row.put(FIELDS.get(fieldIndex), value);
            }
            rows.add(Collections.unmodifiableMap(row));
        }
        return Collections.unmodifiableList(rows);
    }

    private static List<String> parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (int index = 0; index < line.length(); index++) {
            char character = line.charAt(index);
            if (quoted) {
                if (character == '"' && index + 1 < line.length() && line.charAt(index + 1) == '"') {
                    current.append('"');
                    index++;
                } else if (character == '"') {
                    quoted = false;
                } else {
                    current.append(character);
                }
            } else if (character == '"') {
                quoted = true;
            } else if (character == ',') {
                values.add(current.toString());
                current.setLength(0);
            } else {
                current.append(character);
            }
        }
        if (quoted) {
            throw new IllegalStateException("Unclosed CSV quote");
        }
        values.add(current.toString());
        return values;
    }

    private static List<String> semicolonList(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        List<String> values = new ArrayList<>();
        for (String item : value.split(";")) {
            if (!item.isBlank()) {
                values.add(item.trim());
            }
        }
        return values.stream().distinct().sorted().toList();
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().replace("\r\n", "\n").replace('\r', '\n');
    }

    private static String sha256(byte[] bytes) {
        return hex(sha256Digest().digest(bytes));
    }

    private static MessageDigest sha256Digest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (Exception exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private static String hex(byte[] bytes) {
        StringBuilder value = new StringBuilder(bytes.length * 2);
        for (byte item : bytes) {
            value.append(Character.forDigit((item >>> 4) & 0x0f, 16));
            value.append(Character.forDigit(item & 0x0f, 16));
        }
        return value.toString();
    }

    private static Instant parseInstant(String value) {
        try {
            return OffsetDateTime.parse(value).toInstant();
        } catch (RuntimeException ignored) {
            return LocalDate.parse(value).atStartOfDay().toInstant(ZoneOffset.UTC);
        }
    }

    private static Set<String> csvSet(String value) {
        Set<String> result = new LinkedHashSet<>();
        for (String item : value.split(",")) {
            if (!item.isBlank()) {
                result.add(item.trim());
            }
        }
        return Collections.unmodifiableSet(result);
    }

    private String requiredRuntimeProperty(String key, String exceptionId) {
        String value = propertyLookup.apply(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Runtime attestation property is missing for approved exception "
                            + exceptionId + ": " + key);
        }
        return value.trim();
    }

    private static String required(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Generated Domain policy property is missing: " + key);
        }
        return value.trim();
    }

    private static void requireEquals(String label, String expected, String actual) {
        if (!String.valueOf(expected).equalsIgnoreCase(String.valueOf(actual))) {
            throw new IllegalStateException(
                    label + " mismatch. expected=" + expected + ", actual=" + actual);
        }
    }

    private static String stripBom(String value) {
        return value.startsWith("\ufeff") ? value.substring(1) : value;
    }

    public record VerificationResult(
            boolean generatedDomain,
            String profile,
            Set<String> capabilities,
            int approvedExceptionCount,
            String activeEnvironment,
            Set<String> approvedExceptionIds,
            String status) {
        static VerificationResult notGeneratedDomain() {
            return new VerificationResult(
                    false, "NOT_GENERATED_DOMAIN", Set.of(), 0, "NOT_REQUIRED", Set.of(), "NOT_APPLICABLE");
        }
    }
}
