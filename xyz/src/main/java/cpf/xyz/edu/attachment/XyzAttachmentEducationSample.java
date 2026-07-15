package cpf.xyz.edu.attachment;

import cpf.pfw.common.attachment.CpfAttachmentContent;
import cpf.pfw.common.attachment.CpfAttachmentStoragePort;
import cpf.pfw.common.attachment.CpfStoredAttachment;
import cpf.pfw.common.exception.CpfValidationException;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/** PFW 첨부 저장 port를 업무 코드에서 사용하는 XYZ 교육 샘플입니다. */
@Component
public class XyzAttachmentEducationSample {
    public static final String STORE_SAMPLE_ID = "XYZ-EDU-ATTACH-001";
    public static final String VERIFY_SAMPLE_ID = "XYZ-EDU-ATTACH-002";

    private final CpfAttachmentStoragePort storagePort;

    public XyzAttachmentEducationSample(CpfAttachmentStoragePort storagePort) {
        this.storagePort = storagePort;
    }

    /** 문자열을 UTF-8 파일로 변환해 PFW 저장 정책을 거쳐 저장합니다. */
    public CpfStoredAttachment storeText(AttachmentTextRequest request) {
        if (request == null) {
            throw new CpfValidationException("첨부파일 요청은 필수입니다.");
        }
        String text = required(request.text(), "text");
        return storagePort.store(
                required(request.groupId(), "groupId"),
                required(request.fileName(), "fileName"),
                "text/plain; charset=UTF-8",
                text.getBytes(StandardCharsets.UTF_8));
    }

    /** 저장 key를 다시 읽어 checksum과 본문 크기가 보존됐는지 확인합니다. */
    public AttachmentVerification verify(AttachmentVerifyRequest request) {
        if (request == null) {
            throw new CpfValidationException("첨부파일 검증 요청은 필수입니다.");
        }
        CpfAttachmentContent content = storagePort.read(required(request.storageKey(), "storageKey"));
        String expected = required(request.expectedChecksumSha256(), "expectedChecksumSha256");
        return new AttachmentVerification(
                request.storageKey(),
                content.bytes().length,
                content.checksumSha256(),
                content.checksumSha256().equalsIgnoreCase(expected));
    }

    private String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new CpfValidationException(field + " 값은 필수입니다.");
        }
        return value.trim();
    }

    public record AttachmentTextRequest(String groupId, String fileName, String text) {
    }

    public record AttachmentVerifyRequest(String storageKey, String expectedChecksumSha256) {
    }

    public record AttachmentVerification(
            String storageKey,
            long fileSize,
            String checksumSha256,
            boolean checksumMatched) {
    }
}
