package com.cpf.core.api.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/** SHA-256 기준의 CPF Checksum API입니다. */
public final class CpfHashes {
    private CpfHashes() {}
    public static String sha256(String value) {
        return sha256(value == null ? new byte[0] : value.getBytes(StandardCharsets.UTF_8));
    }
    public static String sha256(byte[] value) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value)); }
        catch (NoSuchAlgorithmException ex) { throw new IllegalStateException("SHA-256을 사용할 수 없습니다.", ex); }
    }
    public static String sha256(Path path) throws IOException {
        try (InputStream in = Files.newInputStream(path)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            for (int read; (read = in.read(buffer)) >= 0;) if (read > 0) digest.update(buffer, 0, read);
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256을 사용할 수 없습니다.", ex);
        }
    }
}
