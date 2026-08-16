package com.cpf.education.file.attachment;
import com.cpf.file.common.attachment.LocalCpfAttachmentStorageAdapter;
import com.cpf.core.api.error.CpfValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EducationAttachmentEducationSampleTest {

    @TempDir
    Path tempDir;

    @Test
    void storesTextAndVerifiesChecksumThroughCpfPort() {
        var adapter = new LocalCpfAttachmentStorageAdapter(tempDir, 1024, Set.of("txt"));
        var sample = new EducationAttachmentEducationSample(adapter);

        var stored = sample.storeText(
                new EducationAttachmentEducationSample.AttachmentTextRequest("EDU", "guide.txt", "CPF 첨부 교육"));
        var verified = sample.verify(
                new EducationAttachmentEducationSample.AttachmentVerifyRequest(
                        stored.storageKey(), stored.checksumSha256()));

        assertThat(verified.checksumMatched()).isTrue();
        assertThat(verified.fileSize()).isPositive();
        assertThat(verified.checksumSha256()).hasSize(64);
        assertThat(EducationAttachmentEducationSample.STORE_SAMPLE_ID).isEqualTo("EDU Education-ATTACH-001");
        assertThat(EducationAttachmentEducationSample.VERIFY_SAMPLE_ID).isEqualTo("EDU Education-ATTACH-002");
    }

    @Test
    void rejectsExecutableExtensionAtFrameworkBoundary() {
        var adapter = new LocalCpfAttachmentStorageAdapter(tempDir, 1024, Set.of("txt"));
        var sample = new EducationAttachmentEducationSample(adapter);

        assertThatThrownBy(() -> sample.storeText(
                new EducationAttachmentEducationSample.AttachmentTextRequest("EDU", "script.exe", "실행 금지")))
                .isInstanceOf(CpfValidationException.class)
                .hasMessageContaining("확장자");
    }
}
