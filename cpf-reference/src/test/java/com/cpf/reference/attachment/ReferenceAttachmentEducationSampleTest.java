package cpf.xyz.attachment;

import cpf.pfw.common.attachment.LocalCpfAttachmentStorageAdapter;
import cpf.pfw.common.exception.CpfValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class XyzAttachmentEducationSampleTest {

    @TempDir
    Path tempDir;

    @Test
    void storesTextAndVerifiesChecksumThroughPfwPort() {
        var adapter = new LocalCpfAttachmentStorageAdapter(tempDir, 1024, Set.of("txt"));
        var sample = new XyzAttachmentEducationSample(adapter);

        var stored = sample.storeText(
                new XyzAttachmentEducationSample.AttachmentTextRequest("EDU", "guide.txt", "CPF 첨부 교육"));
        var verified = sample.verify(
                new XyzAttachmentEducationSample.AttachmentVerifyRequest(
                        stored.storageKey(), stored.checksumSha256()));

        assertThat(verified.checksumMatched()).isTrue();
        assertThat(verified.fileSize()).isPositive();
        assertThat(verified.checksumSha256()).hasSize(64);
        assertThat(XyzAttachmentEducationSample.STORE_SAMPLE_ID).isEqualTo("XYZ Reference-ATTACH-001");
        assertThat(XyzAttachmentEducationSample.VERIFY_SAMPLE_ID).isEqualTo("XYZ Reference-ATTACH-002");
    }

    @Test
    void rejectsExecutableExtensionAtFrameworkBoundary() {
        var adapter = new LocalCpfAttachmentStorageAdapter(tempDir, 1024, Set.of("txt"));
        var sample = new XyzAttachmentEducationSample(adapter);

        assertThatThrownBy(() -> sample.storeText(
                new XyzAttachmentEducationSample.AttachmentTextRequest("EDU", "script.exe", "실행 금지")))
                .isInstanceOf(CpfValidationException.class)
                .hasMessageContaining("확장자");
    }
}
