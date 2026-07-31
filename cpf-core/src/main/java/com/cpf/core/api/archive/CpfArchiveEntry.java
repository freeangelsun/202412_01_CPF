package com.cpf.core.api.archive;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;

/**
 * 압축 파일 안에 들어갈 논리 entry입니다.
 *
 * <p>제품 경로에서는 {@link #fromPath(String, Path)} 또는
 * {@link #streaming(String, long, InputStreamSupplier)}를 사용해 entry 전체를 heap에 적재하지 않습니다.</p>
 */
public final class CpfArchiveEntry {
    @FunctionalInterface
    public interface InputStreamSupplier {
        InputStream open() throws IOException;
    }

    private final String name;
    private final long size;
    private final InputStreamSupplier source;

    /**
     * 기존 소규모 호출 호환용입니다. 대용량 제품 Consumer는 Path/streaming factory로 이관해야 합니다.
     */
    @Deprecated(forRemoval = true)
    public CpfArchiveEntry(String name, byte[] content) {
        this(name, content == null ? 0 : content.length, immutableBytes(content));
    }

    private CpfArchiveEntry(String name, long size, InputStreamSupplier source) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("압축 entry 이름은 필수입니다.");
        }
        if (size < 0) {
            throw new IllegalArgumentException("압축 entry 크기는 0 이상이어야 합니다.");
        }
        this.name = name;
        this.size = size;
        this.source = Objects.requireNonNull(source, "압축 entry stream source는 필수입니다.");
    }

    public static CpfArchiveEntry fromPath(String name, Path path) {
        Objects.requireNonNull(path, "압축 entry source path는 필수입니다.");
        Path source = path.toAbsolutePath().normalize();
        if (!Files.isRegularFile(source, LinkOption.NOFOLLOW_LINKS) || Files.isSymbolicLink(source)) {
            throw new IllegalArgumentException("압축 entry source는 symlink가 아닌 일반 파일이어야 합니다: " + source);
        }
        try {
            long size = Files.size(source);
            return new CpfArchiveEntry(name, size, () -> Files.newInputStream(source, LinkOption.NOFOLLOW_LINKS));
        } catch (IOException failure) {
            throw new IllegalArgumentException("압축 entry source 크기를 확인할 수 없습니다: " + source, failure);
        }
    }

    public static CpfArchiveEntry streaming(String name, long size, InputStreamSupplier source) {
        return new CpfArchiveEntry(name, size, source);
    }

    public String name() {
        return name;
    }

    public long size() {
        return size;
    }

    public InputStream openStream() throws IOException {
        InputStream input = source.open();
        if (input == null) {
            throw new IOException("압축 entry stream source가 null을 반환했습니다: " + name);
        }
        return input;
    }

    /** 기존 API 호환용이며 대용량 entry에는 사용하지 않습니다. */
    @Deprecated(forRemoval = true)
    public byte[] content() {
        if (size > Integer.MAX_VALUE) {
            throw new IllegalStateException("byte[]로 반환할 수 없는 entry 크기입니다: " + size);
        }
        try (InputStream input = openStream(); ByteArrayOutputStream output = new ByteArrayOutputStream((int) size)) {
            input.transferTo(output);
            byte[] bytes = output.toByteArray();
            if (bytes.length != size) {
                throw new IllegalStateException("entry 선언 크기와 실제 크기가 다릅니다: " + name);
            }
            return bytes;
        } catch (IOException failure) {
            throw new IllegalStateException("압축 entry를 읽을 수 없습니다: " + name, failure);
        }
    }

    private static InputStreamSupplier immutableBytes(byte[] content) {
        byte[] copy = content == null ? new byte[0] : Arrays.copyOf(content, content.length);
        return () -> new ByteArrayInputStream(copy);
    }
}
