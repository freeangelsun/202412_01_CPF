package com.cpf.file.common.archive;

import com.cpf.file.archive.api.CpfArchiveEntry;
import com.cpf.file.archive.api.CpfArchiveFormat;
import com.cpf.file.archive.api.CpfArchivePolicy;
import com.cpf.file.archive.api.CpfArchiveRequest;
import com.cpf.file.archive.api.CpfArchiveResult;
import com.cpf.file.archive.api.CpfExtractedArchiveEntry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CpfArchiveServiceTest {
    @TempDir
    Path tempDir;

    @Test
    void zipCreateAndExtractUsesSafeBaseDirectory() throws Exception {
        LocalCpfArchiveService service = new LocalCpfArchiveService();
        CpfArchivePolicy policy = CpfArchivePolicy.local(tempDir);
        Path zipPath = tempDir.resolve("result.zip");

        Path firstSource = tempDir.resolve("a.txt");
        Path secondSource = tempDir.resolve("b.txt");
        Files.writeString(firstSource, "A", StandardCharsets.UTF_8);
        Files.writeString(secondSource, "B", StandardCharsets.UTF_8);

        CpfArchiveResult result = service.create(CpfArchiveRequest.zip(zipPath, List.of(
                CpfArchiveEntry.fromPath("out/a.txt", firstSource),
                CpfArchiveEntry.fromPath("out/b.txt", secondSource)
        ), policy));

        assertThat(result.status()).isEqualTo("SUCCESS");
        assertThat(result.entryCount()).isEqualTo(2);
        assertThat(result.checksum()).hasSize(64);

        Path extractDir = tempDir.resolve("extract");
        List<CpfExtractedArchiveEntry> entries = service.extract(
                zipPath, CpfArchiveFormat.ZIP, extractDir, policy);

        assertThat(entries).extracting(value -> value.name())
                .containsExactly("out/a.txt", "out/b.txt");
        assertThat(entries).allSatisfy(entry -> {
            assertThat(entry.path()).startsWith(extractDir);
            assertThat(entry.checksumSha256()).hasSize(64);
        });
        assertThat(Files.readString(extractDir.resolve("out/a.txt"))).isEqualTo("A");
    }

    @Test
    void gzipCreateAndExtractRoundTripsSingleFile() throws Exception {
        LocalCpfArchiveService service = new LocalCpfArchiveService();
        Path source = tempDir.resolve("source.txt");
        Path gzip = tempDir.resolve("source.txt.gz");
        Files.writeString(source, "gzip-body", StandardCharsets.UTF_8);

        CpfArchivePolicy policy = CpfArchivePolicy.local(tempDir);
        CpfArchiveResult result = service.create(CpfArchiveRequest.gzip(source, gzip, policy));
        Path extractDir = tempDir.resolve("gzip-extract");
        List<CpfExtractedArchiveEntry> entries = service.extract(
                gzip, CpfArchiveFormat.GZIP, extractDir, policy);

        assertThat(result.status()).isEqualTo("SUCCESS");
        assertThat(entries).singleElement()
                .satisfies(entry -> {
                    assertThat(entry.name()).isEqualTo("source.txt");
                    assertThat(entry.path()).isEqualTo(extractDir.resolve("source.txt").toAbsolutePath().normalize());
                    assertThat(entry.size()).isEqualTo("gzip-body".getBytes(StandardCharsets.UTF_8).length);
                    assertThat(entry.checksumSha256()).hasSize(64);
                });
        assertThat(Files.readString(extractDir.resolve("source.txt"), StandardCharsets.UTF_8))
                .isEqualTo("gzip-body");
    }
}
