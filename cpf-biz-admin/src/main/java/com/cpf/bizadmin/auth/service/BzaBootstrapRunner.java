package com.cpf.bizadmin.auth.service;

import com.cpf.bizadmin.auth.repository.BzaAuthRepository;
import com.cpf.core.api.security.password.CpfPasswordService;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/** 사전 승인된 1회용 Token과 제한된 Secret 파일이 있을 때만 최초 BZA 운영자를 생성합니다. */
@Component
public final class BzaBootstrapRunner implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(BzaBootstrapRunner.class);
    private static final Set<AclEntryPermission> SENSITIVE_ACL_PERMISSIONS = Set.of(
            AclEntryPermission.READ_DATA,
            AclEntryPermission.WRITE_DATA,
            AclEntryPermission.APPEND_DATA,
            AclEntryPermission.READ_ACL,
            AclEntryPermission.WRITE_ACL,
            AclEntryPermission.WRITE_OWNER,
            AclEntryPermission.DELETE);

    private final Environment environment;
    private final CpfPasswordService passwordService;
    private final BzaAuthRepository authRepository;
    private final BzaBootstrapApprovalRepository approvals;

    public BzaBootstrapRunner(
            Environment environment,
            CpfPasswordService passwordService,
            BzaAuthRepository authRepository,
            BzaBootstrapApprovalRepository approvals) {
        this.environment = environment;
        this.passwordService = passwordService;
        this.authRepository = authRepository;
        this.approvals = approvals;
    }

    @Override
    public void run(ApplicationArguments arguments) throws Exception {
        String tokenFile = environment.getProperty("cpf.bza.bootstrap.approval-token-file");
        if (tokenFile == null || tokenFile.isBlank()) return;

        Path tokenPath = null;
        Path passwordPath = null;
        char[] token = null;
        char[] password = null;
        String tokenHash = null;
        Throwable primaryFailure = null;
        try {
            rejectLegacyBootstrapProperties();
            tokenPath = secureSecretFile(tokenFile, "APPROVAL_TOKEN");
            passwordPath = secureSecretFile(require("cpf.bza.bootstrap.password-file"), "PASSWORD");
            token = readSecret(tokenPath, 512);
            password = readSecret(passwordPath, 1024);
            tokenHash = sha256(token);

            String environmentFingerprint = environmentFingerprint();
            String loginId = require("cpf.bza.bootstrap.login-id");
            String operatorName = require("cpf.bza.bootstrap.operator-name");
            String roleCode = environment.getProperty("cpf.bza.bootstrap.role-code", "BZA_MANAGER").trim();
            String operationId = operationId(environmentFingerprint, loginId);
            String claimOwnerId = claimOwnerId();
            long leaseSeconds = leaseSeconds();

            requireStrongPassword(loginId, password);
            BzaAuthRepository.BootstrapResult existing = authRepository.findBootstrapOperation(operationId).orElse(null);
            if (existing != null) {
                requireSameLogin(existing, loginId);
                approvals.reconcileComplete(tokenHash, operationId, existing.adminUserId(), Instant.now());
                log.info("BZA bootstrap reconciled. operationId={}, adminUserId={}", operationId, existing.adminUserId());
                return;
            }

            Instant now = Instant.now();
            boolean claimed = approvals.claim(tokenHash, environmentFingerprint, operationId, claimOwnerId,
                    now, now.plusSeconds(leaseSeconds));
            if (!claimed) {
                BzaBootstrapApprovalRepository.ApprovalState state = approvals.find(tokenHash)
                        .orElseThrow(() -> new SecurityException("BZA_BOOTSTRAP_APPROVAL_NOT_FOUND"));
                existing = authRepository.findBootstrapOperation(operationId).orElse(null);
                if (existing != null) {
                    requireSameLogin(existing, loginId);
                    approvals.reconcileComplete(tokenHash, operationId, existing.adminUserId(), Instant.now());
                    log.info("BZA bootstrap reconciled after claim conflict. operationId={}, adminUserId={}",
                            operationId, existing.adminUserId());
                    return;
                }
                throw new SecurityException("BZA_BOOTSTRAP_APPROVAL_NOT_CLAIMABLE:" + state.status());
            }

            try {
                BzaAuthRepository.BootstrapResult result = authRepository.bootstrapOperator(
                        loginId, operatorName, passwordService.hash(password), roleCode, operationId,
                        now.plusSeconds(90L * 24L * 60L * 60L));
                approvals.complete(tokenHash, operationId, claimOwnerId, result.adminUserId(), Instant.now());
                log.info("BZA bootstrap completed. operationId={}, adminUserId={}", operationId, result.adminUserId());
            } catch (RuntimeException failure) {
                BzaAuthRepository.BootstrapResult reconciled = authRepository.findBootstrapOperation(operationId).orElse(null);
                if (reconciled != null) {
                    requireSameLogin(reconciled, loginId);
                    approvals.reconcileComplete(tokenHash, operationId, reconciled.adminUserId(), Instant.now());
                    log.warn("BZA bootstrap result was reconciled after terminal update uncertainty. operationId={}", operationId);
                } else {
                    approvals.fail(tokenHash, operationId, claimOwnerId, failure.getClass().getSimpleName(), Instant.now());
                    throw failure;
                }
            }
        } catch (Exception | Error failure) {
            primaryFailure = failure;
            throw failure;
        } finally {
            if (token != null) Arrays.fill(token, '\0');
            if (password != null) Arrays.fill(password, '\0');
            String cleanupFailure = destroySecrets(tokenPath, passwordPath);
            if (tokenHash != null) {
                try {
                    approvals.cleanup(tokenHash, cleanupFailure == null ? "COMPLETED" : "FAILED",
                            cleanupFailure, Instant.now());
                } catch (RuntimeException cleanupLedgerFailure) {
                    if (primaryFailure != null) primaryFailure.addSuppressed(cleanupLedgerFailure);
                    else throw cleanupLedgerFailure;
                }
            }
            if (cleanupFailure != null) {
                IllegalStateException cleanupException =
                        new IllegalStateException("BZA_BOOTSTRAP_SECRET_DESTROY_FAILED:" + cleanupFailure);
                if (primaryFailure != null) primaryFailure.addSuppressed(cleanupException);
                else throw cleanupException;
            }
        }
    }

    private void rejectLegacyBootstrapProperties() {
        if (environment.getProperty("cpf.bza.bootstrap.enabled") != null
                || environment.getProperty("cpf.bza.bootstrap.password") != null) {
            throw new IllegalStateException("BZA_BOOTSTRAP_LEGACY_ENABLE_OR_PLAINTEXT_PASSWORD_FORBIDDEN");
        }
    }

    private String environmentFingerprint() {
        String environmentCode = require("cpf.environment.code");
        String approvalScope = require("cpf.bza.bootstrap.approval-scope");
        String profiles = String.join(",", environment.getActiveProfiles());
        return sha256((environmentCode + "|" + approvalScope + "|" + profiles).toCharArray());
    }

    private String operationId(String fingerprint, String loginId) {
        String configured = environment.getProperty("cpf.bza.bootstrap.operation-id");
        String value = configured == null || configured.isBlank()
                ? "BZA-BOOTSTRAP-" + fingerprint.substring(0, 16) + "-" + loginId.toUpperCase(Locale.ROOT)
                : configured.trim();
        if (value.length() > 100 || !value.matches("[A-Za-z0-9._:-]+")) {
            throw new IllegalStateException("BZA_BOOTSTRAP_OPERATION_ID_INVALID");
        }
        return value;
    }

    private String claimOwnerId() {
        String value = firstNonBlank(
                environment.getProperty("cpf.instance.id"),
                System.getenv("CPF_INSTANCE_ID"),
                System.getenv("HOSTNAME"),
                System.getenv("COMPUTERNAME"));
        if (value == null || value.length() > 100 || !value.matches("[A-Za-z0-9._:-]+")) {
            throw new IllegalStateException("BZA_BOOTSTRAP_INSTANCE_ID_REQUIRED");
        }
        return value;
    }

    private long leaseSeconds() {
        String configured = environment.getProperty("cpf.bza.bootstrap.claim-lease-seconds", "300");
        try {
            long value = Long.parseLong(configured.trim());
            if (value < 30 || value > 1800) throw new NumberFormatException("out of range");
            return value;
        } catch (NumberFormatException failure) {
            throw new IllegalStateException("BZA_BOOTSTRAP_CLAIM_LEASE_SECONDS_INVALID", failure);
        }
    }

    private String require(String key) {
        String value = environment.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("BZA_BOOTSTRAP_PROPERTY_REQUIRED:" + key);
        }
        return value.trim();
    }

    static Path secureSecretFile(String value, String label) throws IOException {
        Path path = Path.of(value).toAbsolutePath().normalize();
        if (!Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS) || Files.isSymbolicLink(path)) {
            throw new SecurityException("BZA_BOOTSTRAP_" + label + "_FILE_UNSAFE");
        }
        if (Files.size(path) > 4096) throw new SecurityException("BZA_BOOTSTRAP_SECRET_FILE_TOO_LARGE");
        verifySecretAcl(path, label);
        return path;
    }

    private static void verifySecretAcl(Path path, String label) throws IOException {
        if (FileSystems.getDefault().supportedFileAttributeViews().contains("posix")) {
            Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(path, LinkOption.NOFOLLOW_LINKS);
            if (!permissions.contains(PosixFilePermission.OWNER_READ)
                    || permissions.contains(PosixFilePermission.GROUP_READ)
                    || permissions.contains(PosixFilePermission.GROUP_WRITE)
                    || permissions.contains(PosixFilePermission.GROUP_EXECUTE)
                    || permissions.contains(PosixFilePermission.OTHERS_READ)
                    || permissions.contains(PosixFilePermission.OTHERS_WRITE)
                    || permissions.contains(PosixFilePermission.OTHERS_EXECUTE)) {
                throw new SecurityException("BZA_BOOTSTRAP_" + label + "_POSIX_PERMISSION_UNSAFE");
            }
            return;
        }

        AclFileAttributeView aclView = Files.getFileAttributeView(
                path, AclFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
        if (aclView == null) {
            throw new SecurityException("BZA_BOOTSTRAP_" + label + "_ACL_UNVERIFIABLE");
        }
        String owner = aclView.getOwner().getName();
        for (AclEntry entry : aclView.getAcl()) {
            String principal = entry.principal().getName();
            if (samePrincipal(principal, owner) || trustedOperatingSystemPrincipal(principal)) continue;
            if (entry.permissions().stream().anyMatch(SENSITIVE_ACL_PERMISSIONS::contains)) {
                throw new SecurityException("BZA_BOOTSTRAP_" + label + "_WINDOWS_ACL_UNSAFE");
            }
        }
    }

    private static boolean samePrincipal(String left, String right) {
        return left != null && right != null && left.equalsIgnoreCase(right);
    }

    private static boolean trustedOperatingSystemPrincipal(String principal) {
        if (principal == null) return false;
        String normalized = principal.replace('/', '\\').toUpperCase(Locale.ROOT);
        return normalized.endsWith("\\SYSTEM")
                || normalized.endsWith("\\ADMINISTRATORS")
                || normalized.equals("SYSTEM")
                || normalized.equals("ADMINISTRATORS");
    }

    private static char[] readSecret(Path path, int maximum) throws IOException {
        long declaredSize = Files.size(path);
        if (declaredSize < 1 || declaredSize > 4096) {
            throw new SecurityException("BZA_BOOTSTRAP_SECRET_FILE_SIZE_INVALID");
        }
        byte[] encoded = new byte[(int) declaredSize];
        CharBuffer decoded = null;
        try (InputStream input = Files.newInputStream(path, LinkOption.NOFOLLOW_LINKS)) {
            int offset = 0;
            while (offset < encoded.length) {
                int read = input.read(encoded, offset, encoded.length - offset);
                if (read < 0) {
                    throw new SecurityException("BZA_BOOTSTRAP_SECRET_FILE_TRUNCATED");
                }
                offset += read;
            }
            if (input.read() != -1) {
                throw new SecurityException("BZA_BOOTSTRAP_SECRET_FILE_CHANGED");
            }
            decoded = StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(encoded));
            int start = 0;
            int end = decoded.length();
            while (start < end && Character.isWhitespace(decoded.charAt(start))) start++;
            while (end > start && Character.isWhitespace(decoded.charAt(end - 1))) end--;
            int length = end - start;
            if (length == 0 || length > maximum) throw new SecurityException("BZA_BOOTSTRAP_SECRET_INVALID");
            char[] value = new char[length];
            for (int index = 0; index < length; index++) value[index] = decoded.charAt(start + index);
            return value;
        } finally {
            Arrays.fill(encoded, (byte) 0);
            if (decoded != null && decoded.hasArray()) Arrays.fill(decoded.array(), '\0');
        }
    }

    static String destroySecrets(Path... paths) {
        StringBuilder failures = new StringBuilder();
        for (Path path : paths) {
            if (path == null) continue;
            try {
                destroySecretFile(path);
            } catch (IOException failure) {
                if (!failures.isEmpty()) failures.append(',');
                failures.append(path.getFileName()).append(':').append(failure.getClass().getSimpleName());
            }
        }
        return failures.isEmpty() ? null : failures.toString();
    }

    static void destroySecretFile(Path path) throws IOException {
        if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS)) return;
        if (!Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS) || Files.isSymbolicLink(path)) {
            throw new IOException("secret file type changed");
        }
        long size = Files.size(path);
        if (size > 0 && size <= 4096) {
            byte[] zeros = new byte[(int) size];
            Files.write(path, zeros);
            Arrays.fill(zeros, (byte) 0);
        }
        Files.delete(path);
    }

    private static void requireSameLogin(BzaAuthRepository.BootstrapResult result, String loginId) {
        if (!result.loginId().equals(loginId)) {
            throw new SecurityException("BZA_BOOTSTRAP_OPERATION_LOGIN_MISMATCH");
        }
    }

    private static String sha256(char[] value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            ByteBuffer encoded = StandardCharsets.UTF_8.encode(CharBuffer.wrap(value));
            byte[] bytes = new byte[encoded.remaining()];
            encoded.get(bytes);
            try {
                return HexFormat.of().formatHex(digest.digest(bytes));
            } finally {
                Arrays.fill(bytes, (byte) 0);
            }
        } catch (Exception failure) {
            throw new IllegalStateException("SHA256_UNAVAILABLE", failure);
        }
    }

    static void requireStrongPassword(String loginId, char[] password) {
        if (password.length < 14) throw new SecurityException("BZA_BOOTSTRAP_PASSWORD_POLICY_REJECTED");
        boolean upper = false;
        boolean lower = false;
        boolean digit = false;
        boolean special = false;
        for (char value : password) {
            upper |= Character.isUpperCase(value);
            lower |= Character.isLowerCase(value);
            digit |= Character.isDigit(value);
            special |= !Character.isLetterOrDigit(value);
        }
        int categories = (upper ? 1 : 0) + (lower ? 1 : 0) + (digit ? 1 : 0) + (special ? 1 : 0);
        if (categories < 3 || containsIgnoreCase(password, loginId)) {
            throw new SecurityException("BZA_BOOTSTRAP_PASSWORD_POLICY_REJECTED");
        }
    }

    private static boolean containsIgnoreCase(char[] source, String candidate) {
        if (candidate == null || candidate.isBlank() || candidate.length() > source.length) return false;
        for (int start = 0; start <= source.length - candidate.length(); start++) {
            boolean match = true;
            for (int index = 0; index < candidate.length(); index++) {
                if (Character.toLowerCase(source[start + index])
                        != Character.toLowerCase(candidate.charAt(index))) {
                    match = false;
                    break;
                }
            }
            if (match) return true;
        }
        return false;
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) return value.trim();
        }
        return null;
    }
}
