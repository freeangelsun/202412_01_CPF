package cpf.pfw.common.security;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * PFW 보안 capability의 key/cert, signature, encryption port를 학습하기 위한 샘플입니다.
 *
 * <p>Vault/KMS 실연동은 외부 런타임 영역으로 분리하고, 이 샘플은 JDK 기본 crypto로
 * 포트 계약과 원문 secret 미노출 원칙을 검증합니다.</p>
 */
public class PfwSecurityCryptoEducationSample {
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String AES_ALGORITHM = "AES";
    private static final String AES_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_TAG_BITS = 128;
    private static final int GCM_IV_BYTES = 12;

    private final SecureRandom secureRandom;

    public PfwSecurityCryptoEducationSample() {
        this(new SecureRandom());
    }

    PfwSecurityCryptoEducationSample(SecureRandom secureRandom) {
        this.secureRandom = secureRandom;
    }

    /**
     * 인증정보 원문 대신 scope, credentialId, version만 전달하는 참조값을 만듭니다.
     */
    public CpfCredentialRef credentialRef(String credentialId) {
        return new CpfCredentialRef("pfw-local", credentialId, "v1", "credential-" + credentialId);
    }

    /**
     * 운영자/감사 로그에는 secret 원문 대신 마스킹된 참조정보와 상태만 남깁니다.
     */
    public CredentialStatusSnapshot credentialStatus(CpfCredentialRef ref) {
        return new CredentialStatusSnapshot(ref.scope(), ref.credentialId(), ref.version(), ref.masked(), "AVAILABLE");
    }

    public KeyCertificateSnapshot keyCertificateSnapshot(CpfCredentialRef ref) {
        return new KeyCertificateSnapshot(
                ref.credentialId(),
                "RSA",
                "SHA256withRSA",
                "인증서 원문은 provider 내부에서만 보관합니다.",
                Instant.parse("2026-07-09T03:00:00Z"));
    }

    public CpfSignaturePort signaturePort() {
        return new LocalSignaturePort();
    }

    public CpfEncryptionPort encryptionPort() {
        return new LocalEncryptionPort();
    }

    private byte[] material(CpfCredentialRef ref, String purpose) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(ref.scope().getBytes(StandardCharsets.UTF_8));
            digest.update((byte) ':');
            digest.update(ref.credentialId().getBytes(StandardCharsets.UTF_8));
            digest.update((byte) ':');
            digest.update(ref.version().getBytes(StandardCharsets.UTF_8));
            digest.update((byte) ':');
            digest.update(purpose.getBytes(StandardCharsets.UTF_8));
            return digest.digest();
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("키 재료 생성에 실패했습니다.", ex);
        }
    }

    private final class LocalSignaturePort implements CpfSignaturePort {
        @Override
        public byte[] sign(CpfCredentialRef credentialRef, byte[] payload) {
            try {
                Mac mac = Mac.getInstance(HMAC_ALGORITHM);
                mac.init(new SecretKeySpec(material(credentialRef, "signature"), HMAC_ALGORITHM));
                return mac.doFinal(payload == null ? new byte[0] : payload);
            } catch (GeneralSecurityException ex) {
                throw new IllegalStateException("전자서명 생성에 실패했습니다.", ex);
            }
        }

        @Override
        public boolean verify(CpfCredentialRef credentialRef, byte[] payload, byte[] signature) {
            return MessageDigest.isEqual(sign(credentialRef, payload), signature == null ? new byte[0] : signature);
        }
    }

    private final class LocalEncryptionPort implements CpfEncryptionPort {
        @Override
        public byte[] encrypt(CpfCredentialRef credentialRef, byte[] plain) {
            try {
                byte[] iv = new byte[GCM_IV_BYTES];
                secureRandom.nextBytes(iv);
                Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
                cipher.init(
                        Cipher.ENCRYPT_MODE,
                        new SecretKeySpec(Arrays.copyOf(material(credentialRef, "encryption"), 16), AES_ALGORITHM),
                        new GCMParameterSpec(GCM_TAG_BITS, iv));
                byte[] encrypted = cipher.doFinal(plain == null ? new byte[0] : plain);
                byte[] result = Arrays.copyOf(iv, iv.length + encrypted.length);
                System.arraycopy(encrypted, 0, result, iv.length, encrypted.length);
                return result;
            } catch (GeneralSecurityException ex) {
                throw new IllegalStateException("payload 암호화에 실패했습니다.", ex);
            }
        }

        @Override
        public byte[] decrypt(CpfCredentialRef credentialRef, byte[] encrypted) {
            if (encrypted == null || encrypted.length <= GCM_IV_BYTES) {
                throw new IllegalArgumentException("복호화할 payload가 올바르지 않습니다.");
            }
            try {
                byte[] iv = Arrays.copyOf(encrypted, GCM_IV_BYTES);
                byte[] body = Arrays.copyOfRange(encrypted, GCM_IV_BYTES, encrypted.length);
                Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
                cipher.init(
                        Cipher.DECRYPT_MODE,
                        new SecretKeySpec(Arrays.copyOf(material(credentialRef, "encryption"), 16), AES_ALGORITHM),
                        new GCMParameterSpec(GCM_TAG_BITS, iv));
                return cipher.doFinal(body);
            } catch (GeneralSecurityException ex) {
                throw new IllegalStateException("payload 복호화에 실패했습니다.", ex);
            }
        }
    }

    public record CredentialStatusSnapshot(
            String scope,
            String credentialId,
            String version,
            String maskedDisplayName,
            String status) {

        public Map<String, String> auditSafeMap() {
            return Map.of(
                    "scope", scope,
                    "credentialId", credentialId,
                    "version", version,
                    "maskedDisplayName", maskedDisplayName,
                    "status", status);
        }
    }

    public record KeyCertificateSnapshot(
            String keyAlias,
            String keyAlgorithm,
            String signatureAlgorithm,
            String certificateStoragePolicy,
            Instant loadedAt) {
    }

    public String hex(byte[] bytes) {
        return HexFormat.of().formatHex(bytes);
    }
}
