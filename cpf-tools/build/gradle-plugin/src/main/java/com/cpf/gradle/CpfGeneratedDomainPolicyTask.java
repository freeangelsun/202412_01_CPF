package com.cpf.gradle;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

/** Fail-closed Build/Architecture gate for CPF Generated Domains. */
public abstract class CpfGeneratedDomainPolicyTask extends DefaultTask {
    private static final Map<String, List<String>> DEPENDENCY_RULES = Map.ofEntries(
            Map.entry("DIRECT_KAFKA", List.of("org.springframework.kafka:spring-kafka")),
            Map.entry("DIRECT_RABBIT", List.of("org.springframework.amqp:spring-rabbit")),
            Map.entry("DIRECT_JMS", List.of("org.springframework:spring-jms")),
            Map.entry("DIRECT_JDBC", List.of("org.springframework.boot:spring-boot-starter-jdbc", "org.springframework:spring-jdbc")),
            Map.entry("DIRECT_MYBATIS", List.of("org.mybatis.spring.boot:mybatis-spring-boot-starter")),
            Map.entry("DIRECT_VALKEY", List.of("org.springframework.boot:spring-boot-starter-data-redis", "io.lettuce:lettuce-core", "redis.clients:jedis")),
            Map.entry("DIRECT_OTEL", List.of("io.opentelemetry:opentelemetry-sdk")),
            Map.entry("DIRECT_SFTP", List.of("org.apache.sshd:sshd-sftp")),
            Map.entry("DIRECT_RESILIENCE", List.of("io.github.resilience4j")),
            Map.entry("DIRECT_FEATUREFLAG", List.of("dev.openfeature")));
    private static final Map<String, Pattern> SOURCE_RULES = Map.ofEntries(
            Map.entry("IMPORT_KAFKA_TEMPLATE", Pattern.compile("\\bKafkaTemplate\\b")),
            Map.entry("IMPORT_RABBIT_TEMPLATE", Pattern.compile("\\bRabbitTemplate\\b")),
            Map.entry("IMPORT_JMS_TEMPLATE", Pattern.compile("\\bJmsTemplate\\b")),
            Map.entry("IMPORT_JDBC_TEMPLATE", Pattern.compile("\\b(?:JdbcTemplate|NamedParameterJdbcTemplate)\\b")),
            Map.entry("IMPORT_MYBATIS_TEMPLATE", Pattern.compile("\\b(?:SqlSessionTemplate|SqlSessionFactoryBean)\\b")),
            Map.entry("IMPORT_OTEL_SDK", Pattern.compile("\\bOpenTelemetrySdk\\b")),
            Map.entry("IMPORT_SFTP_CLIENT", Pattern.compile("\\bSftpClient\\b")),
            Map.entry("IMPORT_REDIS_CLIENT", Pattern.compile("\\b(?:RedisTemplate|StringRedisTemplate|LettuceConnectionFactory|RedissonClient)\\b")),
            Map.entry("SECURITY_FILTER_BYPASS", Pattern.compile("\\bSecurityFilterChain\\b")),
            Map.entry("DIRECT_DATASOURCE_BEAN", Pattern.compile("(?s)@Bean\\b.{0,600}?(?:DataSource|HikariDataSource)\\b")),
            Map.entry("DIRECT_SECURITY_BEAN", Pattern.compile("(?s)@Bean\\b.{0,600}?SecurityFilterChain\\b")),
            Map.entry("DIRECT_OTEL_BEAN", Pattern.compile("(?s)@Bean\\b.{0,600}?OpenTelemetrySdk\\b")));
    private static final Map<String, List<String>> RULE_ARTIFACTS = Map.ofEntries(
            Map.entry("DIRECT_KAFKA", List.of("org.springframework.kafka:spring-kafka")),
            Map.entry("IMPORT_KAFKA_TEMPLATE", List.of("org.springframework.kafka:spring-kafka")),
            Map.entry("CONFIG_KAFKA_BYPASS", List.of("org.springframework.kafka:spring-kafka")),
            Map.entry("DIRECT_RABBIT", List.of("org.springframework.amqp:spring-rabbit")),
            Map.entry("IMPORT_RABBIT_TEMPLATE", List.of("org.springframework.amqp:spring-rabbit")),
            Map.entry("CONFIG_RABBIT_BYPASS", List.of("org.springframework.amqp:spring-rabbit")),
            Map.entry("DIRECT_JMS", List.of("org.springframework:spring-jms", "jakarta.jms:jakarta.jms-api")),
            Map.entry("IMPORT_JMS_TEMPLATE", List.of("org.springframework:spring-jms")),
            Map.entry("CONFIG_JMS_BYPASS", List.of("org.springframework:spring-jms")),
            Map.entry("DIRECT_JDBC", List.of("org.springframework.boot:spring-boot-starter-jdbc", "org.springframework:spring-jdbc")),
            Map.entry(
                    "IMPORT_JDBC_TEMPLATE",
                    List.of(
                            "org.springframework:spring-jdbc",
                            "org.springframework.boot:spring-boot-starter-jdbc")),
            Map.entry("CONFIG_JDBC_BYPASS", List.of("org.springframework.boot:spring-boot-starter-jdbc", "org.springframework:spring-jdbc")),
            Map.entry("DIRECT_DATASOURCE_BEAN", List.of("org.springframework.boot:spring-boot-starter-jdbc", "com.zaxxer:HikariCP")),
            Map.entry("DIRECT_MYBATIS", List.of("org.mybatis.spring.boot:mybatis-spring-boot-starter")),
            Map.entry("IMPORT_MYBATIS_TEMPLATE", List.of("org.mybatis.spring.boot:mybatis-spring-boot-starter")),
            Map.entry("DIRECT_VALKEY", List.of("org.springframework.boot:spring-boot-starter-data-redis", "io.lettuce:lettuce-core", "redis.clients:jedis")),
            Map.entry(
                    "IMPORT_REDIS_CLIENT",
                    List.of(
                            "org.springframework.boot:spring-boot-starter-data-redis",
                            "org.springframework.data:spring-data-redis",
                            "io.lettuce:lettuce-core",
                            "redis.clients:jedis",
                            "org.redisson:redisson")),
            Map.entry("CONFIG_REDIS_BYPASS", List.of("org.springframework.boot:spring-boot-starter-data-redis", "org.springframework.data:spring-data-redis")),
            Map.entry("DIRECT_OTEL", List.of("io.opentelemetry:opentelemetry-sdk")),
            Map.entry("IMPORT_OTEL_SDK", List.of("io.opentelemetry:opentelemetry-sdk")),
            Map.entry("CONFIG_OTLP_BYPASS", List.of("io.opentelemetry:opentelemetry-sdk")),
            Map.entry("DIRECT_OTEL_BEAN", List.of("io.opentelemetry:opentelemetry-sdk")),
            Map.entry("DIRECT_SFTP", List.of("org.apache.sshd:sshd-sftp", "com.github.mwiede:jsch")),
            Map.entry("IMPORT_SFTP_CLIENT", List.of("org.apache.sshd:sshd-sftp")),
            Map.entry("CONFIG_SFTP_BYPASS", List.of("org.apache.sshd:sshd-sftp")),
            Map.entry("SECURITY_FILTER_BYPASS", List.of("org.springframework.security:spring-security-config", "org.springframework.boot:spring-boot-starter-security")),
            Map.entry("DIRECT_SECURITY_BEAN", List.of("org.springframework.security:spring-security-config", "org.springframework.boot:spring-boot-starter-security")));
    private static final Pattern PROFILE_COORDINATE = Pattern.compile("cpf-starter-profile-([a-z-]+)");
    private static final Pattern DECLARED_COORDINATE = Pattern.compile(
            "(?:implementation|api|runtimeOnly|compileOnly|annotationProcessor)\\s*\\(?\\s*[\"']([^\"']+:[^\"']+:[^\"']+)[\"']");
    private static final Pattern INTERNAL_PROVIDER_IMPORT = Pattern.compile(
            "(?m)^\\s*import\\s+com\\.cpf\\.starter\\.[A-Za-z0-9_.$]+\\s*;");
    private static final Map<String, Pattern> CONFIG_RULES = Map.ofEntries(
            Map.entry("CONFIG_KAFKA_BYPASS", springConfigPattern("kafka", "spring\\.kafka\\.")),
            Map.entry("CONFIG_RABBIT_BYPASS", springConfigPattern("rabbitmq", "spring\\.rabbitmq\\.")),
            Map.entry("CONFIG_JMS_BYPASS", springConfigPattern("jms", "spring\\.jms\\.")),
            Map.entry("CONFIG_JDBC_BYPASS", springConfigPattern("datasource", "spring\\.datasource\\.")),
            Map.entry("CONFIG_REDIS_BYPASS", springConfigPattern("data", "spring\\.data\\.redis\\.")),
            Map.entry("CONFIG_OTLP_BYPASS", Pattern.compile(
                    "(?m)^\\s*management\\.otlp\\.|(?ms)^management\\s*:\\s*\n(?:[ \t]+[^\n]*\n)*?[ \t]+otlp\\s*:")),
            Map.entry("CONFIG_SFTP_BYPASS", Pattern.compile(
                    "(?m)^\\s*spring\\.integration\\.sftp\\.|(?ms)^spring\\s*:\\s*\n(?:[ \t]+[^\n]*\n)*?[ \t]+integration\\s*:\\s*\n(?:[ \t]{2,}[^\n]*\n)*?[ \t]{4,}sftp\\s*:")));

    private static Pattern springConfigPattern(String yamlChild, String propertiesPrefix) {
        return Pattern.compile("(?m)^\\s*" + propertiesPrefix
                + "|(?ms)^spring\\s*:\\s*\n(?:[ \t]+[^\n]*\n)*?[ \t]+"
                + Pattern.quote(yamlChild) + "\\s*:");
    }


    @TaskAction
    public final void verifyPolicy() {
        try {
            verifyPolicyInternal();
        } catch (GradleException exception) {
            throw exception;
        } catch (Exception exception) {
            String message = exception.getMessage() == null
                    ? exception.getClass().getSimpleName() : exception.getMessage();
            throw new GradleException("CPF Generated Domain policy verification failed: " + message, exception);
        }
    }

    private void verifyPolicyInternal() throws Exception {
        Path root = getProject().getProjectDir().toPath().toAbsolutePath().normalize();
        Path manifestPath = root.resolve(CpfGeneratedDomainPolicySupport.MANIFEST_PATH);
        if (!Files.isRegularFile(manifestPath)) {
            return;
        }
        Path lockPath = required(root, CpfGeneratedDomainPolicySupport.LOCK_PATH);
        Path policyPath = required(root, CpfGeneratedDomainPolicySupport.POLICY_PATH);
        Path exceptionConfig = required(root, CpfGeneratedDomainPolicySupport.EXCEPTION_CONFIG_PATH);
        Path exceptionResource = required(root, CpfGeneratedDomainPolicySupport.EXCEPTION_RESOURCE_PATH);

        Map<String, Object> manifest = CpfGeneratedDomainPolicySupport.readJsonObject(manifestPath);
        Map<String, Object> lock = CpfGeneratedDomainPolicySupport.readJsonObject(lockPath);
        Properties policy = CpfGeneratedDomainPolicySupport.readProperties(policyPath);
        String projectName = CpfGeneratedDomainPolicySupport.stringValue(manifest.get("projectName"));
        if (!projectName.equals(getProject().getName())) {
            fail("manifest projectName mismatch: " + projectName + " != " + getProject().getName());
        }
        String profile = requiredProperty(policy, "profile");
        if (!CpfGeneratedDomainPolicySupport.PUBLIC_PROFILES.contains(profile)) {
            fail("invalid public profile: " + profile);
        }
        if (!projectName.equals(requiredProperty(policy, "module"))) {
            fail("runtime policy module mismatch");
        }
        if (!"true".equals(requiredProperty(policy, "failClosed"))) {
            fail("generated domain policy must be fail-closed");
        }
        Set<String> capabilities = csvSet(policy.getProperty("capabilities", ""));
        if (!CpfGeneratedDomainPolicySupport.PUBLIC_CAPABILITIES.containsAll(capabilities)) {
            fail("unknown capability in runtime policy: " + capabilities);
        }
        Set<String> standards = csvSet(requiredProperty(policy, "requiredStandards"));
        if (!standards.equals(new LinkedHashSet<>(CpfGeneratedDomainPolicySupport.REQUIRED_STANDARDS))) {
            fail("mandatory CPF standards are incomplete: " + standards);
        }
        if (!profile.equals(CpfGeneratedDomainPolicySupport.stringValue(lock.get("profile")))) {
            fail("profile drift between policy and resolved lock");
        }
        if (!capabilities.equals(CpfGeneratedDomainPolicySupport.stringSet(lock.get("capabilityGroups")))) {
            fail("capability drift between policy and resolved lock");
        }
        requireMap(lock, "providerBindings");
        requireMap(lock, "resolvedStarterVersions");
        verifyBuildFile(root, profile);

        if (Files.mismatch(exceptionConfig, exceptionResource) != -1L) {
            fail("approved exception config/resource drift");
        }
        String registrySha = CpfGeneratedDomainPolicySupport.fileSha256(exceptionResource);
        if (!registrySha.equals(requiredProperty(policy, "exceptionRegistrySha256"))) {
            fail("approved exception registry hash drift");
        }
        if (!registrySha.equals(CpfGeneratedDomainPolicySupport.stringValue(lock.get("exceptionRegistrySha256")))) {
            fail("approved exception registry lock drift");
        }
        List<Map<String, String>> exceptions = loadAndValidateExceptions(
                exceptionConfig, projectName, manifest, root);
        verifyExceptionLock(lock, exceptions);
        verifyBuildEnvironmentScope(exceptions);

        List<Violation> violations = new ArrayList<>();
        scanDependencies(root, violations);
        scanSources(root, violations);
        scanConfiguration(root, violations);
        List<String> unapproved = violations.stream()
                .filter(violation -> !isApproved(violation, exceptions))
                .map(value -> value.toString())
                .sorted()
                .toList();
        if (!unapproved.isEmpty()) {
            fail("unapproved CPF standard bypass:\n" + String.join("\n", unapproved));
        }
        getLogger().lifecycle(
                "[CPF][GENERATED-DOMAIN][PASS] profile={}, capabilities={}, providers={}, exceptions={}, bypasses={}",
                profile, capabilities.size(), ((Map<?, ?>) lock.get("providerBindings")).size(),
                exceptions.size(), violations.size());
    }


    private static List<Map<String, String>> loadAndValidateExceptions(
            Path exceptionConfig,
            String projectName,
            Map<String, Object> manifest,
            Path root) throws Exception {
        try {
            List<Map<String, String>> exceptions =
                    CpfGeneratedDomainPolicySupport.readExceptionCsv(exceptionConfig);
            CpfGeneratedDomainPolicySupport.validateExceptionRows(
                    exceptions, projectName, manifest, Instant.now(), root);
            return exceptions;
        } catch (IllegalArgumentException exception) {
            fail(exception.getMessage());
            return List.of();
        }
    }

    private static Path required(Path root, String relative) {
        Path path = root.resolve(relative);
        if (!Files.isRegularFile(path)) {
            fail(relative + " is missing");
        }
        return path;
    }

    private static void verifyBuildFile(Path root, String profile) throws Exception {
        String build = Files.readString(root.resolve("build.gradle"), StandardCharsets.UTF_8);
        if (!build.contains("com.cpf.platform-conventions")) {
            fail("CPF convention plugin is missing");
        }
        Matcher matcher = PROFILE_COORDINATE.matcher(build);
        List<String> profiles = new ArrayList<>();
        while (matcher.find()) {
            profiles.add(matcher.group(1));
        }
        profiles = profiles.stream().distinct().toList();
        if (profiles.size() != 1 || !profiles.getFirst().equals(profile)) {
            fail("exactly one resolved public profile is required. build=" + profiles + ", lock=" + profile);
        }
    }

    private static void verifyExceptionLock(
            Map<String, Object> lock, List<Map<String, String>> rows) throws Exception {
        Map<String, String> expected = new LinkedHashMap<>();
        for (Map<String, String> row : rows) {
            expected.put(row.get("exception_id"), row.get("config_hash").toLowerCase(Locale.ROOT));
        }
        Map<String, String> actual = new LinkedHashMap<>();
        for (Map<String, String> row : CpfGeneratedDomainPolicySupport.lockExceptions(lock)) {
            String id = row.getOrDefault("exceptionId", "");
            String hash = row.getOrDefault("configHash", "").toLowerCase(Locale.ROOT);
            if (actual.putIfAbsent(id, hash) != null) {
                fail("duplicate approved exception in resolved lock: " + id);
            }
        }
        if (!expected.equals(actual)) {
            fail("approved exception lock mismatch. expected=" + expected + ", actual=" + actual);
        }
    }

    private void verifyBuildEnvironmentScope(List<Map<String, String>> exceptions) {
        if (exceptions.isEmpty()) {
            return;
        }
        Object configured = getProject().findProperty("cpfTargetEnvironment");
        if (configured == null || configured.toString().isBlank()) {
            fail("cpfTargetEnvironment is required when approved exceptions exist");
        }
        String targetEnvironment = java.util.Objects.requireNonNull(configured, "cpfTargetEnvironment").toString().trim();
        if (!targetEnvironment.matches("^[A-Za-z0-9][A-Za-z0-9_.-]{0,63}$")) {
            fail("cpfTargetEnvironment is invalid: " + targetEnvironment);
        }
        for (Map<String, String> exception : exceptions) {
            if (!CpfGeneratedDomainPolicySupport.semicolonSet(exception.get("environments"))
                    .contains(targetEnvironment)) {
                fail("approved exception environment scope mismatch: "
                        + exception.get("exception_id") + ", target=" + targetEnvironment);
            }
        }
    }

    private void scanDependencies(Path root, List<Violation> out) throws Exception {
        Set<String> exactCoordinates = new LinkedHashSet<>();
        getProject().getConfigurations().forEach(configuration -> {
            for (Dependency dependency : configuration.getDependencies()) {
                if (dependency.getGroup() == null || dependency.getName() == null) {
                    continue;
                }
                String version = dependency.getVersion() == null ? "<UNVERSIONED>" : dependency.getVersion();
                exactCoordinates.add(dependency.getGroup() + ":" + dependency.getName() + ":" + version);
            }
        });

        // Keep a source-level fallback for unusual Gradle declaration forms, but the Gradle model above
        // is the source of truth and catches variables, maps, and convention-defined dependencies.
        String build = Files.readString(root.resolve("build.gradle"), StandardCharsets.UTF_8);
        Matcher declared = DECLARED_COORDINATE.matcher(build);
        while (declared.find()) {
            exactCoordinates.add(declared.group(1));
        }
        DEPENDENCY_RULES.forEach((rule, patterns) -> patterns.forEach(pattern -> {
            List<String> matches = exactCoordinates.stream()
                    .filter(coordinate -> coordinate.startsWith(pattern + ":") || coordinate.equals(pattern))
                    .toList();
            matches.forEach(coordinate -> out.add(
                    new Violation(rule, "build.gradle", coordinate, coordinate)));
        }));
    }

    private static void scanSources(Path root, List<Violation> out) throws Exception {
        for (String sourceDirectory : List.of("src/main/java", "src/main/kotlin")) {
            Path sourceRoot = root.resolve(sourceDirectory);
            if (!Files.isDirectory(sourceRoot)) {
                continue;
            }
            try (var stream = Files.walk(sourceRoot)) {
                for (Path path : stream.filter(item -> item.toString().endsWith(".java")
                        || item.toString().endsWith(".kt")).sorted().toList()) {
                    String source = Files.readString(path, StandardCharsets.UTF_8);
                    String relative = root.relativize(path).toString().replace('\\', '/');
                    SOURCE_RULES.forEach((rule, pattern) -> {
                        if (pattern.matcher(source).find()) {
                            out.add(new Violation(rule, relative, pattern.pattern(), ""));
                        }
                    });
                    Matcher internal = INTERNAL_PROVIDER_IMPORT.matcher(source);
                    while (internal.find()) {
                        out.add(new Violation("INTERNAL_PROVIDER_IMPORT", relative, internal.group().trim(), ""));
                    }
                }
            }
        }
    }

    private static void scanConfiguration(Path root, List<Violation> out) throws Exception {
        Path resourceRoot = root.resolve("src/main/resources");
        if (!Files.isDirectory(resourceRoot)) {
            return;
        }
        try (var stream = Files.walk(resourceRoot)) {
            for (Path path : stream.filter(Files::isRegularFile).sorted().toList()) {
                String fileName = path.getFileName().toString().toLowerCase(Locale.ROOT);
                if (!(fileName.endsWith(".yml") || fileName.endsWith(".yaml")
                        || fileName.endsWith(".properties"))) {
                    continue;
                }
                String relative = root.relativize(path).toString().replace('\\', '/');
                String content = Files.readString(path, StandardCharsets.UTF_8);
                CONFIG_RULES.forEach((rule, pattern) -> {
                    if (pattern.matcher(content).find()) {
                        out.add(new Violation(rule, relative, pattern.pattern(), ""));
                    }
                });
            }
        }
    }

    private static boolean isApproved(Violation violation, Collection<Map<String, String>> exceptions) {
        // Internal CPF implementation packages are never an extension point.
        if ("INTERNAL_PROVIDER_IMPORT".equals(violation.ruleId())) {
            return false;
        }
        for (Map<String, String> exception : exceptions) {
            if (!CpfGeneratedDomainPolicySupport.semicolonSet(exception.get("rule_ids"))
                    .contains(violation.ruleId())) {
                continue;
            }
            List<String> allowedArtifacts = RULE_ARTIFACTS.getOrDefault(
                    violation.ruleId(), List.of());
            if (!allowedArtifacts.isEmpty()
                    && !allowedArtifacts.contains(exception.get("artifact"))) {
                continue;
            }
            if (!violation.coordinate().isBlank()) {
                String expected = exception.get("artifact") + ":" + exception.get("version");
                if (!expected.equals(violation.coordinate())) {
                    continue;
                }
            }
            if (violation.ruleId().startsWith("CONFIG_")
                    && !CpfGeneratedDomainPolicySupport.semicolonSet(exception.get("config_files"))
                            .contains(violation.path())) {
                continue;
            }
            return true;
        }
        return false;
    }

    private static Set<String> csvSet(String value) {
        Set<String> result = new LinkedHashSet<>();
        for (String item : value.split(",")) {
            if (!item.isBlank()) {
                result.add(item.trim());
            }
        }
        return Set.copyOf(result);
    }

    private static String requiredProperty(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            fail("generated domain policy property is missing: " + key);
        }
        return java.util.Objects.requireNonNull(value, key).trim();
    }

    private static Map<?, ?> requireMap(Map<String, Object> map, String key) {
        if (!(map.get(key) instanceof Map<?, ?> _)) {
            fail(key + " must be an object");
        }
        return (Map<?, ?>) map.get(key);
    }

    private static void fail(String message) {
        throw new GradleException("[CPF][GENERATED-DOMAIN][FAIL] " + message);
    }

    private record Violation(String ruleId, String path, String detail, String coordinate) {
        @Override
        public String toString() {
            return ruleId + " " + path + " " + detail;
        }
    }
}
