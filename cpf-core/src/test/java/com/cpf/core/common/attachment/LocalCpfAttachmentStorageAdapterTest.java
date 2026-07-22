package com.cpf.core.common.attachment;

import com.cpf.core.common.exception.CpfValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalCpfAttachmentStorageAdapterTest {

    @TempDir
    Path tempDir;

    @Test
    void storesAndReadsAttachmentWithChecksum() {
        var adapter = new LocalCpfAttachmentStorageAdapter(tempDir, 1024, Set.of("txt", "pdf"));
        byte[] content = "CPF 첨부파일".getBytes(StandardCharsets.UTF_8);

        CpfStoredAttachment stored = adapter.store("APPROVAL_1", "evidence.txt", "text/plain", content);
        CpfAttachmentContent loaded = adapter.read(stored.storageKey());

        assertThat(stored.storageKey()).startsWith("APPROVAL_1/").doesNotContain("..");
        assertThat(stored.checksumSha256()).hasSize(64).isEqualTo(loaded.checksumSha256());
        assertThat(loaded.bytes()).isEqualTo(content);
    }

    @Test
    void rejectsTraversalUnknownExtensionAndOversizedContent() {
        var adapter = new LocalCpfAttachmentStorageAdapter(tempDir, 4, Set.of("txt"));

        assertThatThrownBy(() -> adapter.store("GROUP", "../secret.txt", "text/plain", new byte[]{1}))
                .isInstanceOf(CpfValidationException.class);
        assertThatThrownBy(() -> adapter.store("GROUP", "script.exe", "application/octet-stream", new byte[]{1}))
                .isInstanceOf(CpfValidationException.class);
        assertThatThrownBy(() -> adapter.store("GROUP", "large.txt", "text/plain", new byte[]{1, 2, 3, 4, 5}))
                .isInstanceOf(CpfValidationException.class);
        assertThatThrownBy(() -> adapter.read("../outside.txt"))
                .isInstanceOf(CpfValidationException.class);
    }
}
