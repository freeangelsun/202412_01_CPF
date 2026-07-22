package cpf.pfw.common.logging.file;

import org.springframework.core.env.Environment;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;

/**
 * CPF 파일 로그의 절대 root와 환경·모듈·인스턴스별 경로를 계산합니다.
 *
 * <p>실행 디렉터리에 따라 로그 위치가 달라지지 않도록 상대경로를 허용하지 않습니다.
 * local에서는 저장소 root를 찾아 {@code <repository>/logs}를 사용하고, dev/stg/prod에서는
 * 외부에서 주입한 {@code CPF_LOG_ROOT}와 {@code CPF_INSTANCE_ID}를 필수로 사용합니다.</p>
 */
public final class CpfLogPathPolicy {
    private static final Set<String> STRICT_ENVIRONMENTS = Set.of("dev", "stg", "prod");
    private static final DateTimeFormatter BUSINESS_DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;
    private static final DateTimeFormatter FILE_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private final Path logRoot;
    private final String environmentCode;
    private final String runtimeModuleCode;
    private final String instanceId;

    public CpfLogPathPolicy(Environment environment) {
        this(environment, Path.of(System.getProperty("user.dir", ".")));
    }

    CpfLogPathPolicy(Environment environment, Path workingDirectory) {
        if (environment == null) {
            throw new IllegalArgumentException("Spring Environment는 필수입니다.");
        }
        this.environmentCode = resolveEnvironmentCode(environment);
        this.runtimeModuleCode = resolveRuntimeModuleCode(environment);
        this.instanceId = resolveInstanceId(environment, environmentCode, runtimeModuleCode);
        this.logRoot = resolveLogRoot(environment, workingDirectory, environmentCode);
    }

    public Path logRoot() {
        return logRoot;
    }

    public String environmentCode() {
        return environmentCode;
    }

    public String runtimeModuleCode() {
        return runtimeModuleCode;
    }

    public String instanceId() {
        return instanceId;
    }

    public Path environmentRoot() {
        return safeResolve(logRoot, Path.of(environmentCode));
    }

    public Path instanceRoot() {
        return safeResolve(environmentRoot(), Path.of(
                runtimeModuleCode.toLowerCase(Locale.ROOT),
                instanceId));
    }

    /**
     * 일반 실행 로그 유형을 실행 주체 인스턴스 아래의 표준 디렉터리로 연결합니다.
     */
    public Path generalLogPath(String ownerModuleCode, String logType, LocalDate logDate) {
        String normalizedOwner = normalizeModuleCode(ownerModuleCode, runtimeModuleCode);
        String normalizedType = sanitizeToken(logType, "application", 40).toLowerCase(Locale.ROOT);
        Path categoryDirectory = categoryDirectory(normalizedOwner, normalizedType);
        String date = FILE_DATE_FORMATTER.format(requireDate(logDate));
        String fileName = "cpf-"
                + normalizedOwner.toLowerCase(Locale.ROOT)
                + '-' + normalizedType
                + '-' + instanceId
                + '.' + date + ".log";
        return safeResolve(categoryDirectory, Path.of(fileName));
    }

    /**
     * 온라인 거래 로그를 거래 종류와 업무일자로 분리합니다.
     */
    public Path transactionLogPath(String transactionId, LocalDate businessDate) {
        String safeTransactionId = sanitizeToken(transactionId, null, 120);
        String date = BUSINESS_DATE_FORMATTER.format(requireDate(businessDate));
        return safeResolve(
                instanceRoot(),
                Path.of("transactions", date, safeTransactionId + '_' + date + ".log"));
    }

    /**
     * BAT JobInstance 로그의 논리 경로에 환경 디렉터리를 추가합니다.
     */
    public Path batchJobLogPath(Path batchRelativePath) {
        return safeResolve(environmentRoot(), requireRelativePath(batchRelativePath));
    }

    /**
     * DB 로그 장애 복구 journal 경로를 실행 인스턴스에 귀속합니다.
     */
    public Path recoveryPath(Path recoveryRelativePath) {
        return safeResolve(instanceRoot(), Path.of("recovery").resolve(requireRelativePath(recoveryRelativePath)));
    }

    public Path relativeToLogRoot(Path absolutePath) {
        Path normalized = absolutePath.toAbsolutePath().normalize();
        if (!normalized.startsWith(logRoot)) {
            throw new IllegalArgumentException("로그 경로가 CPF_LOG_ROOT를 벗어났습니다.");
        }
        return logRoot.relativize(normalized);
    }

    public static Path findRepositoryRoot(Path startDirectory) {
        if (startDirectory == null) {
            return null;
        }
        Path current = startDirectory.toAbsolutePath().normalize();
        while (current != null) {
            if (Files.isRegularFile(current.resolve("settings.gradle"), LinkOption.NOFOLLOW_LINKS)
                    && (Files.isRegularFile(current.resolve("gradlew.bat"), LinkOption.NOFOLLOW_LINKS)
                    || Files.isRegularFile(current.resolve("gradlew"), LinkOption.NOFOLLOW_LINKS))) {
                return current;
            }
            current = current.getParent();
        }
        return null;
    }

    private Path categoryDirectory(String ownerModuleCode, String logType) {
        Path root = instanceRoot();
        if ("PFW".equals(ownerModuleCode)) {
            return safeResolve(root, Path.of("framework", "pfw"));
        }
        if ("CMN".equals(ownerModuleCode)) {
            return safeResolve(root, Path.of("common", "cmn"));
        }
        String category = switch (logType) {
            case "error", "integration", "audit", "security", "worker", "recovery" -> logType;
            default -> "application";
        };
        return safeResolve(root, Path.of(category));
    }

    private static Path resolveLogRoot(
            Environment environment,
            Path workingDirectory,
            String environmentCode) {
        String configured = firstText(
                environment.getProperty("cpf.logging.file.base-path"),
                environment.getProperty("CPF_LOG_ROOT"));
        Path resolved;
        if (hasText(configured)) {
            Path configuredPath = Path.of(configured.trim());
            if (!configuredPath.isAbsolute() || containsParentTraversal(configuredPath)) {
                throw new IllegalStateException("CPF_LOG_ROOT는 '..'를 포함하지 않는 절대경로여야 합니다.");
            }
            resolved = configuredPath.normalize();
        } else {
            if (STRICT_ENVIRONMENTS.contains(environmentCode)) {
                throw new IllegalStateException(environmentCode + " 환경에서는 CPF_LOG_ROOT가 필수입니다.");
            }
            String repositoryRoot = firstText(
                    environment.getProperty("cpf.repository-root"),
                    environment.getProperty("CPF_REPOSITORY_ROOT"));
            Path repository = hasText(repositoryRoot)
                    ? Path.of(repositoryRoot.trim()).toAbsolutePath().normalize()
                    : findRepositoryRoot(workingDirectory);
            if (repository == null) {
                throw new IllegalStateException("local 로그 root를 계산할 저장소 root를 찾을 수 없습니다. CPF_LOG_ROOT를 지정하세요.");
            }
            resolved = repository.resolve("logs").toAbsolutePath().normalize();
        }
        rejectExistingSymbolicLinks(resolved);
        return resolved;
    }

    private static String resolveEnvironmentCode(Environment environment) {
        String configured = firstText(
                environment.getProperty("cpf.environment"),
                environment.getProperty("CPF_ENV"));
        if (!hasText(configured)) {
            configured = Arrays.stream(environment.getActiveProfiles())
                    .map(value -> value.toLowerCase(Locale.ROOT))
                    .filter(value -> Set.of("local", "dev", "test", "stg", "prod").contains(value))
                    .findFirst()
                    .orElse("local");
        }
        return sanitizeToken(configured, "local", 20).toLowerCase(Locale.ROOT);
    }

    private static String resolveRuntimeModuleCode(Environment environment) {
        return normalizeModuleCode(firstText(
                environment.getProperty("cpf.framework.module-id"),
                environment.getProperty("CPF_MODULE_CODE"),
                environment.getProperty("CPF_MODULE_ID"),
                environment.getProperty("spring.application.name")), "APP");
    }

    private static String resolveInstanceId(
            Environment environment,
            String environmentCode,
            String runtimeModuleCode) {
        String externallyInjected = firstText(
                environment.getProperty("CPF_INSTANCE_ID"),
                environment.getProperty(runtimeModuleCode + "_INSTANCE_ID"),
                environment.getProperty("SERVER_INSTANCE_ID"));
        if (!hasText(externallyInjected) && STRICT_ENVIRONMENTS.contains(environmentCode)) {
            throw new IllegalStateException(environmentCode + " 환경에서는 CPF_INSTANCE_ID가 필수입니다.");
        }
        String explicit = firstText(
                externallyInjected,
                environment.getProperty("cpf.framework.instance-id"));
        String fallback = firstText(
                environment.getProperty("cpf.framework.was-id"),
                runtimeModuleCode.toLowerCase(Locale.ROOT) + '-' + environmentCode + "-01");
        return sanitizeToken(firstText(explicit, fallback), null, 128);
    }

    private static String normalizeModuleCode(String value, String fallback) {
        String normalized = firstText(value, fallback);
        if (normalized != null && normalized.toLowerCase(Locale.ROOT).startsWith("cpf-")) {
            normalized = normalized.substring(4);
        }
        return sanitizeToken(normalized, fallback, 20).toUpperCase(Locale.ROOT);
    }

    static String sanitizeToken(String value, String fallback, int maxLength) {
        String resolved = firstText(value, fallback);
        if (!hasText(resolved)) {
            throw new IllegalArgumentException("로그 경로 식별자는 필수입니다.");
        }
        String sanitized = resolved.trim().replaceAll("[^A-Za-z0-9._-]", "_");
        if (sanitized.isBlank()
                || ".".equals(sanitized)
                || "..".equals(sanitized)
                || sanitized.length() > maxLength
                || !sanitized.matches("[A-Za-z0-9][A-Za-z0-9._-]*")) {
            throw new IllegalArgumentException("로그 경로 식별자는 영문 또는 숫자로 시작하는 " + maxLength + "자 이하 토큰이어야 합니다.");
        }
        return sanitized;
    }

    private static Path safeResolve(Path root, Path relativePath) {
        Path normalizedRoot = root.toAbsolutePath().normalize();
        Path relative = requireRelativePath(relativePath);
        Path resolved = normalizedRoot.resolve(relative).normalize();
        if (!resolved.startsWith(normalizedRoot)) {
            throw new IllegalArgumentException("로그 경로가 허용된 root를 벗어났습니다.");
        }
        return resolved;
    }

    private static Path requireRelativePath(Path path) {
        if (path == null || path.isAbsolute() || path.normalize().startsWith("..")) {
            throw new IllegalArgumentException("로그 하위 경로는 root 내부 상대경로여야 합니다.");
        }
        return path.normalize();
    }

    private static LocalDate requireDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("로그 업무일자는 필수입니다.");
        }
        return date;
    }

    private static boolean containsParentTraversal(Path path) {
        for (Path part : path) {
            if ("..".equals(part.toString()) || ".".equals(part.toString())) {
                return true;
            }
        }
        return false;
    }

    private static void rejectExistingSymbolicLinks(Path path) {
        Path current = path.getRoot();
        for (Path part : path) {
            current = current == null ? part : current.resolve(part);
            if (Files.exists(current, LinkOption.NOFOLLOW_LINKS) && Files.isSymbolicLink(current)) {
                throw new IllegalStateException("CPF_LOG_ROOT 경로에 symbolic link를 사용할 수 없습니다: " + current);
            }
        }
    }

    private static String firstText(String... values) {
        for (String value : values) {
            if (hasText(value)) {
                return value.trim();
            }
        }
        return null;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
