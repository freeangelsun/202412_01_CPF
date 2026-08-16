package com.cpf.file.common.attachment;

import com.cpf.file.attachment.api.CpfAttachmentStream;
import com.cpf.file.attachment.api.CpfStoredAttachment;
import com.cpf.core.api.error.CpfValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalCpfAttachmentStorageAdapterTest {

    @TempDir
    Path tempDir;

    @Test
    void storesAndReadsAttachmentWithChecksum() throws IOException {
        var adapter = new LocalCpfAttachmentStorageAdapter(tempDir, 1024, Set.of("txt", "pdf"));
        byte[] content = "CPF 첨부파일".getBytes(StandardCharsets.UTF_8);

        CpfStoredAttachment stored = adapter.store(
                "APPROVAL_1",
                "evidence.txt",
                "text/plain",
                new ByteArrayInputStream(content),
                content.length);

        assertThat(stored.storageKey()).startsWith("APPROVAL_1/").doesNotContain("..");
        assertThat(stored.fileSize()).isEqualTo(content.length);
        assertThat(stored.checksumSha256()).hasSize(64);
        try (CpfAttachmentStream loaded = adapter.open(stored.storageKey())) {
            assertThat(loaded.size()).isEqualTo(content.length);
            assertThat(loaded.checksumSha256()).isEqualTo(stored.checksumSha256());
            byte[] loadedContent = loaded.inputStream().readNBytes(content.length + 1);
            assertThat(loadedContent).hasSize(content.length).isEqualTo(content);
        }
    }

    @Test
    void rejectsTraversalUnknownExtensionAndOversizedContent() {
        var adapter = new LocalCpfAttachmentStorageAdapter(tempDir, 4, Set.of("txt"));

        assertThatThrownBy(() -> adapter.store(
                "GROUP", "../secret.txt", "text/plain", new ByteArrayInputStream(new byte[]{1}), 1))
                .isInstanceOf(CpfValidationException.class);
        assertThatThrownBy(() -> adapter.store(
                "GROUP", "script.exe", "application/octet-stream", new ByteArrayInputStream(new byte[]{1}), 1))
                .isInstanceOf(CpfValidationException.class);
        assertThatThrownBy(() -> adapter.store(
                "GROUP",
                "large.txt",
                "text/plain",
                new ByteArrayInputStream(new byte[]{1, 2, 3, 4, 5}),
                5))
                .isInstanceOf(CpfValidationException.class);
        assertThatThrownBy(() -> adapter.open("../outside.txt"))
                .isInstanceOf(CpfValidationException.class);
    }
}
