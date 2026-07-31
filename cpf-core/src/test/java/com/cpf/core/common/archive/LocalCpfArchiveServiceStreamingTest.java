package com.cpf.core.common.archive;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.cpf.core.api.archive.CpfArchiveEntry;
import com.cpf.core.api.archive.CpfArchivePolicy;
import com.cpf.core.api.archive.CpfArchiveRequest;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LocalCpfArchiveServiceStreamingTest {
    @TempDir Path temp;

    @Test
    void createsZipFromBoundedStreamAndPublishesAtomically() throws Exception {
        byte[] payload = "streaming-entry".getBytes(StandardCharsets.UTF_8);
        AtomicInteger opens = new AtomicInteger();
        CpfArchiveEntry entry = CpfArchiveEntry.streaming("data/value.txt", payload.length, () -> {
            opens.incrementAndGet();
            return new ByteArrayInputStream(payload);
        });
        Path target = temp.resolve("archive.zip");

        var result = new LocalCpfArchiveService().create(CpfArchiveRequest.zip(
                target, List.of(entry), CpfArchivePolicy.local(temp)));

        assertThat(result.status()).isEqualTo("SUCCESS");
        assertThat(Files.isRegularFile(target)).isTrue();
        assertThat(opens).hasValue(1);
        try (var paths = Files.list(temp)) {
            assertThat(paths.map(path -> path.getFileName().toString()).toList())
                    .containsExactly("archive.zip");
        }
    }

    @Test
    void rejectsChangedStreamSizeAndRemovesPartialTarget() {
        CpfArchiveEntry entry = CpfArchiveEntry.streaming(
                "data/value.txt", 4, () -> new ByteArrayInputStream(new byte[] {1, 2}));
        Path target = temp.resolve("broken.zip");

        assertThatThrownBy(() -> new LocalCpfArchiveService().create(CpfArchiveRequest.zip(
                target, List.of(entry), CpfArchivePolicy.local(temp))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ARCHIVE_ENTRY_SIZE_CHANGED");
        assertThat(target).doesNotExist();
    }

    @Test
    void rejectsDuplicateLogicalPaths() {
        CpfArchiveEntry first = CpfArchiveEntry.streaming(
                "data/value.txt", 1, () -> new ByteArrayInputStream(new byte[] {1}));
        CpfArchiveEntry second = CpfArchiveEntry.streaming(
                "data\\value.txt", 1, () -> new ByteArrayInputStream(new byte[] {2}));
        Path target = temp.resolve("duplicate.zip");

        assertThatThrownBy(() -> new LocalCpfArchiveService().create(CpfArchiveRequest.zip(
                target, List.of(first, second), CpfArchivePolicy.local(temp))))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("ARCHIVE_DUPLICATE_CANONICAL_ENTRY");
        assertThat(target).doesNotExist();
    }

    @Test
    void rejectsCaseInsensitiveCollisionAndReservedNames() {
        CpfArchiveEntry first = CpfArchiveEntry.streaming(
                "data/Value.txt", 1, () -> new ByteArrayInputStream(new byte[] {1}));
        CpfArchiveEntry second = CpfArchiveEntry.streaming(
                "data/value.txt", 1, () -> new ByteArrayInputStream(new byte[] {2}));
        assertThatThrownBy(() -> new LocalCpfArchiveService().create(CpfArchiveRequest.zip(
                temp.resolve("case.zip"), List.of(first, second), CpfArchivePolicy.local(temp))))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("DUPLICATE_CANONICAL");

        CpfArchiveEntry reserved = CpfArchiveEntry.streaming(
                "data/CON.txt", 1, () -> new ByteArrayInputStream(new byte[] {1}));
        assertThatThrownBy(() -> new LocalCpfArchiveService().create(CpfArchiveRequest.zip(
                temp.resolve("reserved.zip"), List.of(reserved), CpfArchivePolicy.local(temp))))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("RESERVED_NAME");
    }

    @Test
    void rejectsNestedArchiveByDefault() {
        CpfArchiveEntry nested = CpfArchiveEntry.streaming(
                "payload/archive.zip", 1, () -> new ByteArrayInputStream(new byte[] {1}));
        assertThatThrownBy(() -> new LocalCpfArchiveService().create(CpfArchiveRequest.zip(
                temp.resolve("nested.zip"), List.of(nested), CpfArchivePolicy.local(temp))))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("NESTED_ENTRY_DENIED");
    }

    @Test
    void rollsBackPublishedEntriesAndRestoresOverwrittenFileWhenLaterEntryFails() throws Exception {
        Path archive = temp.resolve("rollback.zip");
        try (ZipOutputStream output = new ZipOutputStream(Files.newOutputStream(archive))) {
            output.putNextEntry(new ZipEntry("first.txt"));
            output.write("new".getBytes(StandardCharsets.UTF_8));
            output.closeEntry();
            output.putNextEntry(new ZipEntry("too-large.txt"));
            output.write("1234".getBytes(StandardCharsets.UTF_8));
            output.closeEntry();
        }
        Path target = temp.resolve("rollback-target");
        Files.createDirectories(target);
        Files.writeString(target.resolve("first.txt"), "old", StandardCharsets.UTF_8);
        CpfArchivePolicy policy = new CpfArchivePolicy(temp, 3, 4, true, ".tmp", ".archived");

        assertThatThrownBy(() -> new LocalCpfArchiveService().extract(
                archive, com.cpf.core.api.archive.CpfArchiveFormat.ZIP, target, policy))
                .isInstanceOf(RuntimeException.class);

        assertThat(Files.readString(target.resolve("first.txt"), StandardCharsets.UTF_8)).isEqualTo("old");
        assertThat(target.resolve("too-large.txt")).doesNotExist();
        try (var paths = Files.walk(temp)) {
            assertThat(paths.map(path -> path.getFileName().toString()).toList())
                    .noneMatch(name -> name.contains("cpf-backup"));
        }
    }

}
