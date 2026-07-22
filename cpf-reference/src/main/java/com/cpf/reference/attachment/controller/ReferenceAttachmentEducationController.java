package com.cpf.reference.attachment.controller;

import com.cpf.core.common.attachment.CpfStoredAttachment;
import com.cpf.core.common.execution.CpfOnlineTransaction;
import com.cpf.reference.attachment.ReferenceAttachmentEducationSample;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** CPF 첨부 저장 port를 실행해 보는 REF 교육 API입니다. */
@RestController
@RequestMapping({"/api/reference/attachments", "/reference/edu/attachments"})
@Tag(name = "REF Reference 17. 첨부파일", description = "안전한 첨부 저장, checksum, 저장 adapter 교체 교육 샘플")
public class ReferenceAttachmentEducationController extends com.cpf.reference.common.base.ReferenceBaseController {
    private final ReferenceAttachmentEducationSample sample;

    public ReferenceAttachmentEducationController(ReferenceAttachmentEducationSample sample) {
        this.sample = sample;
    }

    @PostMapping("/text")
    @CpfOnlineTransaction(id = "OREFAA0065", name = "REF첨부파일저장")
    @Operation(operationId = "refAttachmentEducationStoreText", summary = "교육용 UTF-8 첨부 저장",
            description = "CPF 저장 port의 파일명·확장자·크기·경로 검증과 SHA-256 계산 결과를 확인합니다.")
    public ResponseEntity<CpfStoredAttachment> storeText(
            @RequestBody ReferenceAttachmentEducationSample.AttachmentTextRequest request) {
        return ResponseEntity.ok(sample.storeText(request));
    }

    @PostMapping("/verify")
    @CpfOnlineTransaction(id = "OREFAA0066", name = "REF첨부파일검증")
    @Operation(operationId = "refAttachmentEducationVerify", summary = "교육용 첨부 checksum 검증",
            description = "저장 key로 본문을 다시 읽어 예상 SHA-256과 일치하는지 확인합니다.")
    public ResponseEntity<ReferenceAttachmentEducationSample.AttachmentVerification> verify(
            @RequestBody ReferenceAttachmentEducationSample.AttachmentVerifyRequest request) {
        return ResponseEntity.ok(sample.verify(request));
    }
}
