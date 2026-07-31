package com.cpf.starter.security;

import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;

/** Credential 원문을 Session Attribute에 두지 않는 암호화 JDBC Vault 기본 구현입니다. */
public final class JdbcCpfBffCredentialVault implements CpfBffCredentialVault {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String INSERT = """
            INSERT INTO CPF_BFF_CREDENTIAL_VAULT
            (HANDLE_ID, KEY_ID, ACCESS_IV, ACCESS_CIPHER_TEXT, REFRESH_IV, REFRESH_CIPHER_TEXT,
             ACCESS_EXPIRES_AT, REFRESH_EXPIRES_AT, CREATED_AT, UPDATED_AT, VERSION_NO)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1)
            """;
    private static final String SELECT = """
            SELECT KEY_ID, ACCESS_IV, ACCESS_CIPHER_TEXT, REFRESH_IV, REFRESH_CIPHER_TEXT,
                   ACCESS_EXPIRES_AT, REFRESH_EXPIRES_AT, VERSION_NO
              FROM CPF_BFF_CREDENTIAL_VAULT WHERE HANDLE_ID = ?
            """;
    private static final String UPDATE = """
            UPDATE CPF_BFF_CREDENTIAL_VAULT
               SET KEY_ID=?, ACCESS_IV=?, ACCESS_CIPHER_TEXT=?, REFRESH_IV=?, REFRESH_CIPHER_TEXT=?,
                   ACCESS_EXPIRES_AT=?, REFRESH_EXPIRES_AT=?, UPDATED_AT=?, VERSION_NO=VERSION_NO+1
             WHERE HANDLE_ID=? AND VERSION_NO=?
            """;

    private final JdbcTemplate jdbc;
    private final CpfCredentialCipher cipher;
    private final String keyId;
    private final Clock clock;

    public JdbcCpfBffCredentialVault(JdbcTemplate jdbc, byte[] key, String keyId) {
        this(jdbc, key, keyId, Clock.systemUTC());
    }

    JdbcCpfBffCredentialVault(JdbcTemplate jdbc, byte[] key, String keyId, Clock clock) {
        this.jdbc = jdbc;
        this.cipher = new CpfCredentialCipher(key);
        this.keyId = keyId == null || keyId.isBlank() ? "cpf-bff-v1" : keyId.trim();
        this.clock = clock;
    }

    @Override
    public String create(String accessToken, String refreshToken, Instant accessExpiresAt, Instant refreshExpiresAt) {
        validateExpiry(accessExpiresAt, refreshExpiresAt);
        for (int attempt = 0; attempt < 3; attempt++) {
            String handle = newHandle();
            CpfCredentialCipher.Encrypted access = cipher.encrypt(handle, required(accessToken, "accessToken"));
            CpfCredentialCipher.Encrypted refresh = cipher.encrypt(handle, optional(refreshToken));
            Instant now = clock.instant();
            try {
                jdbc.update(INSERT, handle, keyId, access.iv(), access.ciphertext(),
                        bytes(refresh, true), bytes(refresh, false), Timestamp.from(accessExpiresAt),
                        Timestamp.from(refreshExpiresAt), Timestamp.from(now), Timestamp.from(now));
                return handle;
            } catch (DuplicateKeyException duplicateHandle) {
                if (attempt == 2) {
                    throw new IllegalStateException("CPF_BFF_CREDENTIAL_HANDLE_EXHAUSTED", duplicateHandle);
                }
            } catch (DataAccessException storageFailure) {
                throw new IllegalStateException("CPF_BFF_CREDENTIAL_CREATE_FAILED", storageFailure);
            }
        }
        throw new IllegalStateException("CPF_BFF_CREDENTIAL_HANDLE_EXHAUSTED");
    }

    @Override
    public CpfBffCredential rotate(String handle, String accessToken, String refreshToken,
            Instant accessExpiresAt, Instant refreshExpiresAt, long expectedVersion) {
        String normalized = required(handle, "handle");
        validateExpiry(accessExpiresAt, refreshExpiresAt);
        CpfCredentialCipher.Encrypted access = cipher.encrypt(normalized, required(accessToken, "accessToken"));
        CpfCredentialCipher.Encrypted refresh = cipher.encrypt(normalized, optional(refreshToken));
        int updated = jdbc.update(UPDATE, keyId, access.iv(), access.ciphertext(), bytes(refresh, true),
                bytes(refresh, false), Timestamp.from(accessExpiresAt), Timestamp.from(refreshExpiresAt),
                Timestamp.from(clock.instant()), normalized, expectedVersion);
        if (updated != 1) throw new IllegalStateException("CPF_BFF_CREDENTIAL_ROTATION_CONFLICT");
        return find(normalized).orElseThrow(() -> new IllegalStateException("CPF_BFF_CREDENTIAL_ROTATION_LOST"));
    }

    @Override
    public Optional<CpfBffCredential> find(String handle) {
        if (handle == null || handle.isBlank()) return Optional.empty();
        Optional<CpfBffCredential> found = jdbc.query(SELECT, rs -> {
            if (!rs.next()) return Optional.empty();
            if (!keyId.equals(rs.getString("KEY_ID"))) {
                throw new SecurityException("CPF_BFF_CREDENTIAL_KEY_ID_DENIED");
            }
            Instant accessExpiresAt = rs.getTimestamp("ACCESS_EXPIRES_AT").toInstant();
            Instant refreshExpiresAt = rs.getTimestamp("REFRESH_EXPIRES_AT").toInstant();
            CpfBffCredential credential = new CpfBffCredential(handle,
                    cipher.decrypt(handle, rs.getBytes("ACCESS_IV"), rs.getBytes("ACCESS_CIPHER_TEXT")),
                    decryptNullable(handle, rs.getBytes("REFRESH_IV"), rs.getBytes("REFRESH_CIPHER_TEXT")),
                    accessExpiresAt, refreshExpiresAt, rs.getLong("VERSION_NO"));
            return Optional.of(credential);
        }, handle);
        if (found.isPresent() && found.get().refreshExpired(clock.instant())) {
            revoke(handle);
            return Optional.empty();
        }
        return found;
    }

    @Override public void revoke(String handle) {
        if (handle != null && !handle.isBlank()) jdbc.update("DELETE FROM CPF_BFF_CREDENTIAL_VAULT WHERE HANDLE_ID=?", handle);
    }

    @Override public int purgeExpired(Instant now) {
        return jdbc.update("DELETE FROM CPF_BFF_CREDENTIAL_VAULT WHERE REFRESH_EXPIRES_AT <= ?", Timestamp.from(now));
    }

    private String decryptNullable(String handle, byte[] iv, byte[] ciphertext) {
        return ciphertext == null ? null : cipher.decrypt(handle, iv, ciphertext);
    }
    private static byte[] bytes(CpfCredentialCipher.Encrypted value, boolean iv) {
        return value == null ? null : (iv ? value.iv() : value.ciphertext());
    }
    private static String newHandle() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
    private void validateExpiry(Instant access, Instant refresh) {
        Instant now = clock.instant();
        if (access == null || !access.isAfter(now)) throw new IllegalArgumentException("access expiry must be future");
        if (refresh == null || refresh.isBefore(access)) throw new IllegalArgumentException("refresh expiry must not precede access expiry");
    }
    private static String required(String value, String name) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " is required");
        return value;
    }
    private static String optional(String value) { return value == null || value.isBlank() ? null : value; }
}
