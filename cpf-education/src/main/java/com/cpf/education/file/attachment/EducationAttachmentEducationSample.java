package com.cpf.education.file.attachment;
import com.cpf.file.attachment.api.CpfAttachmentStream;
import com.cpf.file.attachment.api.CpfAttachmentStoragePort;
import com.cpf.file.attachment.api.CpfStoredAttachment;
import com.cpf.core.api.error.CpfValidationException;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/** CPF 첨부 저장 port를 업무 코드에서 사용하는 EDU 교육 샘플입니다. */
@Component
public class EducationAttachmentEducationSample {
    public static final String STORE_SAMPLE_ID = "EDU Education-ATTACH-001";
    public static final String VERIFY_SAMPLE_ID = "EDU Education-ATTACH-002";

    private final CpfAttachmentStoragePort storagePort;

    /** EducationAttachmentEducationSample 작업을 CPF 표준 계약에 따라 수행한다. */
    public EducationAttachmentEducationSample(CpfAttachmentStoragePort storagePort) {
        this.storagePort = storagePort;
    }

    /** 문자열을 UTF-8 파일로 변환해 CPF 저장 정책을 거쳐 저장합니다. */
    public CpfStoredAttachment storeText(AttachmentTextRequest request) {
        if (request == null) {
            throw new CpfValidationException("첨부파일 요청은 필수입니다.");
        }
        String text = required(request.text(), "text");
        byte[] content = text.getBytes(StandardCharsets.UTF_8);
        return storagePort.store(
                required(request.groupId(), "groupId"),
                required(request.fileName(), "fileName"),
                "text/plain; charset=UTF-8",
                new ByteArrayInputStream(content),
                content.length);
    }

    /** 저장 key를 다시 읽어 checksum과 본문 크기가 보존됐는지 확인합니다. */
    public AttachmentVerification verify(AttachmentVerifyRequest request) {
        if (request == null) {
            throw new CpfValidationException("첨부파일 검증 요청은 필수입니다.");
        }
        String expected = required(request.expectedChecksumSha256(), "expectedChecksumSha256");
        try (CpfAttachmentStream content = storagePort.open(required(request.storageKey(), "storageKey"))) {
            return new AttachmentVerification(
                    request.storageKey(),
                    content.size(),
                    content.checksumSha256(),
                    content.checksumSha256().equalsIgnoreCase(expected));
        // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
        } catch (IOException e) {
            throw new IllegalStateException("첨부파일 스트림 종료에 실패했습니다.", e);
        }
    }

    private String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new CpfValidationException(field + " 값은 필수입니다.");
        }
        return value.trim();
    }

    /** AttachmentTextRequest 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
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
