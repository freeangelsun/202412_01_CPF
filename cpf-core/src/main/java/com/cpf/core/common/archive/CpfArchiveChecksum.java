package com.cpf.core.common.archive;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * 압축 전후 파일 무결성을 확인하기 위한 checksum 유틸리티입니다.
 */
public final class CpfArchiveChecksum {
    private CpfArchiveChecksum() {
    }

    public static String sha256(byte[] content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(content == null ? new byte[0] : content));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 알고리즘을 사용할 수 없습니다.", ex);
        }
    }

    public static String sha256(Path path) {
        try (InputStream inputStream = Files.newInputStream(path)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int read;
            while ((read = inputStream.read(buffer)) >= 0) {
                digest.update(buffer, 0, read);
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (IOException ex) {
            throw new IllegalStateException("checksum 계산 중 파일을 읽지 못했습니다. path=" + path, ex);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 알고리즘을 사용할 수 없습니다.", ex);
        }
    }
}
