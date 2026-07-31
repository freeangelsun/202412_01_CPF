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
                .hasMessageContaining("ARCHIVE_DUPLICATE_ENTRY");
        assertThat(target).doesNotExist();
    }
}
