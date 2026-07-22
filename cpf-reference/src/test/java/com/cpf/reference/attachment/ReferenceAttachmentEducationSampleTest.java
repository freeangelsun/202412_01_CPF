package com.cpf.reference.attachment;

import com.cpf.core.common.attachment.LocalCpfAttachmentStorageAdapter;
import com.cpf.core.common.exception.CpfValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReferenceAttachmentEducationSampleTest {

    @TempDir
    Path tempDir;

    @Test
    void storesTextAndVerifiesChecksumThroughCpfPort() {
        var adapter = new LocalCpfAttachmentStorageAdapter(tempDir, 1024, Set.of("txt"));
        var sample = new ReferenceAttachmentEducationSample(adapter);

        var stored = sample.storeText(
                new ReferenceAttachmentEducationSample.AttachmentTextRequest("EDU", "guide.txt", "CPF 첨부 교육"));
        var verified = sample.verify(
                new ReferenceAttachmentEducationSample.AttachmentVerifyRequest(
                        stored.storageKey(), stored.checksumSha256()));

        assertThat(verified.checksumMatched()).isTrue();
        assertThat(verified.fileSize()).isPositive();
        assertThat(verified.checksumSha256()).hasSize(64);
        assertThat(ReferenceAttachmentEducationSample.STORE_SAMPLE_ID).isEqualTo("REF Reference-ATTACH-001");
        assertThat(ReferenceAttachmentEducationSample.VERIFY_SAMPLE_ID).isEqualTo("REF Reference-ATTACH-002");
    }

    @Test
    void rejectsExecutableExtensionAtFrameworkBoundary() {
        var adapter = new LocalCpfAttachmentStorageAdapter(tempDir, 1024, Set.of("txt"));
        var sample = new ReferenceAttachmentEducationSample(adapter);

        assertThatThrownBy(() -> sample.storeText(
                new ReferenceAttachmentEducationSample.AttachmentTextRequest("EDU", "script.exe", "실행 금지")))
                .isInstanceOf(CpfValidationException.class)
                .hasMessageContaining("확장자");
    }
}
