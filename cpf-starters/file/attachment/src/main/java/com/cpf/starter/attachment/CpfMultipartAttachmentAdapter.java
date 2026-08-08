package com.cpf.starter.attachment;

import com.cpf.core.api.attachment.CpfAttachmentStoragePort;
import com.cpf.core.api.attachment.CpfStoredAttachment;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;
import org.springframework.web.multipart.MultipartFile;

/** Spring MultipartFile을 CPF streaming storage port로 안전하게 연결하는 convenience adapter입니다. */
public final class CpfMultipartAttachmentAdapter {
    private final CpfAttachmentStoragePort storage;
    private final CpfAttachmentUploadPolicy policy;

    public CpfMultipartAttachmentAdapter(CpfAttachmentStoragePort storage, CpfAttachmentUploadPolicy policy) {
        this.storage = Objects.requireNonNull(storage, "storage");
        this.policy = Objects.requireNonNull(policy, "policy");
    }

    public CpfStoredAttachment store(String groupId, MultipartFile file) {
        Objects.requireNonNull(file, "file");
        var metadata = policy.validate(file.getOriginalFilename(), file.getContentType(), file.getSize());
        try (var input = file.getInputStream()) {
            return storage.store(groupId, metadata.filename(), metadata.contentType(), input, metadata.size());
        } catch (IOException ex) {
            throw new UncheckedIOException("첨부 업로드 stream 처리에 실패했습니다.", ex);
        }
    }

    public CpfAttachmentStoragePort nativeStorage() { return storage; }
}
