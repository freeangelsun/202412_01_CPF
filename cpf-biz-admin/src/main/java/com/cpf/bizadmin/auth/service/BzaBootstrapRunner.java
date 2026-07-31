package com.cpf.bizadmin.auth.service;

import com.cpf.bizadmin.auth.repository.BzaAuthRepository;
import com.cpf.core.api.security.password.CpfPasswordService;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Locale;
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
    private final Environment environment;
    private final CpfPasswordService passwordService;
    private final BzaAuthRepository authRepository;
    private final BzaBootstrapApprovalRepository approvals;

    public BzaBootstrapRunner(
            Environment environment, CpfPasswordService passwordService,
            BzaAuthRepository authRepository, BzaBootstrapApprovalRepository approvals) {
        this.environment = environment;
        this.passwordService = passwordService;
        this.authRepository = authRepository;
        this.approvals = approvals;
    }

    @Override
    public void run(ApplicationArguments arguments) throws Exception {
        String tokenFile = environment.getProperty("cpf.bza.bootstrap.approval-token-file");
        if (tokenFile == null || tokenFile.isBlank()) return;
        if (environment.getProperty("cpf.bza.bootstrap.enabled") != null
                || environment.getProperty("cpf.bza.bootstrap.password") != null) {
            throw new IllegalStateException("BZA_BOOTSTRAP_LEGACY_ENABLE_OR_PLAINTEXT_PASSWORD_FORBIDDEN");
        }

        Path tokenPath = secureSecretFile(tokenFile, "APPROVAL_TOKEN");
        Path passwordPath = secureSecretFile(require("cpf.bza.bootstrap.password-file"), "PASSWORD");
        char[] token = readSecret(tokenPath, 512);
        char[] password = readSecret(passwordPath, 1024);
        String tokenHash = sha256(token);
        String environmentFingerprint = environmentFingerprint();
        String loginId = require("cpf.bza.bootstrap.login-id");
        String operatorName = require("cpf.bza.bootstrap.operator-name");
        String roleCode = environment.getProperty("cpf.bza.bootstrap.role-code", "BZA_MANAGER").trim();
        String operationId = operationId(environmentFingerprint, loginId);
        Instant now = Instant.now();

        try {
            requireStrongPassword(loginId, password);
            if (!approvals.claim(tokenHash, environmentFingerprint, operationId, now)) {
                BzaBootstrapApprovalRepository.ApprovalState state = approvals.find(tokenHash)
                        .orElseThrow(() -> new SecurityException("BZA_BOOTSTRAP_APPROVAL_NOT_FOUND"));
                throw new SecurityException("BZA_BOOTSTRAP_APPROVAL_NOT_CLAIMABLE:" + state.status());
            }
            try {
                BzaAuthRepository.BootstrapResult result = authRepository.bootstrapOperator(
                        loginId, operatorName, passwordService.hash(password), roleCode, operationId,
                        now.plusSeconds(90L * 24L * 60L * 60L));
                approvals.complete(tokenHash, result.adminUserId(), Instant.now());
                log.info("BZA bootstrap completed. operationId={}, adminUserId={}", operationId, result.adminUserId());
            } catch (RuntimeException failure) {
                approvals.fail(tokenHash, failure.getClass().getSimpleName(), Instant.now());
                throw failure;
            }
        } finally {
            Arrays.fill(token, '\0');
            Arrays.fill(password, '\0');
            destroySecretFile(tokenPath);
            destroySecretFile(passwordPath);
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

    private String require(String key) {
        String value = environment.getProperty(key);
        if (value == null || value.isBlank()) throw new IllegalStateException("BZA_BOOTSTRAP_PROPERTY_REQUIRED:" + key);
        return value.trim();
    }

    private static Path secureSecretFile(String value, String label) throws IOException {
        Path path = Path.of(value).toAbsolutePath().normalize();
        if (!Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS) || Files.isSymbolicLink(path)) {
            throw new SecurityException("BZA_BOOTSTRAP_" + label + "_FILE_UNSAFE");
        }
        if (Files.size(path) > 4096) throw new SecurityException("BZA_BOOTSTRAP_SECRET_FILE_TOO_LARGE");
        return path;
    }

    private static char[] readSecret(Path path, int maximum) throws IOException {
        char[] value = Files.readString(path, StandardCharsets.UTF_8).strip().toCharArray();
        if (value.length == 0 || value.length > maximum) {
            Arrays.fill(value, '\0');
            throw new SecurityException("BZA_BOOTSTRAP_SECRET_INVALID");
        }
        return value;
    }

    private static void destroySecretFile(Path path) {
        try {
            long size = Files.size(path);
            if (size > 0 && size <= 4096) Files.write(path, new byte[(int) size]);
            Files.deleteIfExists(path);
        } catch (IOException failure) {
            throw new IllegalStateException("BZA_BOOTSTRAP_SECRET_DESTROY_FAILED", failure);
        }
    }

    private static String sha256(char[] value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            ByteBuffer encoded = StandardCharsets.UTF_8.encode(CharBuffer.wrap(value));
            byte[] bytes = new byte[encoded.remaining()];
            encoded.get(bytes);
            try { return HexFormat.of().formatHex(digest.digest(bytes)); }
            finally { Arrays.fill(bytes, (byte) 0); }
        } catch (Exception failure) {
            throw new IllegalStateException("SHA256_UNAVAILABLE", failure);
        }
    }

    private static void requireStrongPassword(String loginId, char[] password) {
        if (password.length < 14) throw new SecurityException("BZA_BOOTSTRAP_PASSWORD_POLICY_REJECTED");
        boolean upper = false, lower = false, digit = false, special = false;
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
                if (Character.toLowerCase(source[start + index]) != Character.toLowerCase(candidate.charAt(index))) {
                    match = false;
                    break;
                }
            }
            if (match) return true;
        }
        return false;
    }
}
