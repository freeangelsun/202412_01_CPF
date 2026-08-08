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
    /** 문자열/byte 배열의 SHA-256 checksum을 계산합니다.
     * @param value 입력 값. 각 메서드의 nullable 규칙을 따릅니다.
     * @return 정규화/변환된 문자열 또는 계약상 null
     */
    public static String sha256(String value) {
        return sha256(value == null ? new byte[0] : value.getBytes(StandardCharsets.UTF_8));
    }
    /** 문자열/byte 배열의 SHA-256 checksum을 계산합니다.
     * @param value 입력 값. 각 메서드의 nullable 규칙을 따릅니다.
     * @return 정규화/변환된 문자열 또는 계약상 null
     */
    public static String sha256(byte[] value) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value)); }
        catch (NoSuchAlgorithmException ex) { throw new IllegalStateException("SHA-256을 사용할 수 없습니다.", ex); }
    }
    /** 파일을 streaming 하여 SHA-256 checksum을 계산합니다.
     * @param path 읽을 파일 경로
     * @return 정규화/변환된 문자열 또는 계약상 null
     * @throws java.io.IOException 파일을 읽을 수 없는 경우
     * @throws IllegalStateException SHA-256 provider를 사용할 수 없는 경우
     */
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
