package com.cpf.core.common.archive;

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

        CpfArchiveResult result = service.create(CpfArchiveRequest.zip(zipPath, List.of(
                new CpfArchiveEntry("out/a.txt", "A".getBytes(StandardCharsets.UTF_8)),
                new CpfArchiveEntry("out/b.txt", "B".getBytes(StandardCharsets.UTF_8))
        ), policy));

        assertThat(result.status()).isEqualTo("SUCCESS");
        assertThat(result.entryCount()).isEqualTo(2);
        assertThat(result.checksum()).hasSize(64);

        Path extractDir = tempDir.resolve("extract");
        List<CpfArchiveEntry> entries = service.extract(zipPath, CpfArchiveFormat.ZIP, extractDir, policy);

        assertThat(entries).extracting(CpfArchiveEntry::name)
                .containsExactly("out/a.txt", "out/b.txt");
        assertThat(Files.readString(extractDir.resolve("out/a.txt"))).isEqualTo("A");
    }

    @Test
    void gzipCreateAndExtractRoundTripsSingleFile() throws Exception {
        LocalCpfArchiveService service = new LocalCpfArchiveService();
        Path source = tempDir.resolve("source.txt");
        Path gzip = tempDir.resolve("source.txt.gz");
        Files.writeString(source, "gzip-body", StandardCharsets.UTF_8);

        CpfArchiveResult result = service.create(CpfArchiveRequest.gzip(source, gzip, CpfArchivePolicy.local(tempDir)));
        List<CpfArchiveEntry> entries = service.extract(gzip, CpfArchiveFormat.GZIP, tempDir.resolve("source.txt"), CpfArchivePolicy.local(tempDir));

        assertThat(result.status()).isEqualTo("SUCCESS");
        assertThat(entries).singleElement()
                .satisfies(entry -> assertThat(new String(entry.content(), StandardCharsets.UTF_8)).isEqualTo("gzip-body"));
    }
}
