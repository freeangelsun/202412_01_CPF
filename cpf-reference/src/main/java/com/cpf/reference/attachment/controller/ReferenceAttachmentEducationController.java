package cpf.xyz.attachment.controller;

import cpf.pfw.common.attachment.CpfStoredAttachment;
import cpf.pfw.common.execution.CpfOnlineTransaction;
import cpf.xyz.attachment.XyzAttachmentEducationSample;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** PFW 첨부 저장 port를 실행해 보는 XYZ 교육 API입니다. */
@RestController
@RequestMapping({"/api/xyz/reference/attachments", "/xyz/edu/attachments"})
@Tag(name = "XYZ Reference 17. 첨부파일", description = "안전한 첨부 저장, checksum, 저장 adapter 교체 교육 샘플")
public class XyzAttachmentEducationController extends cpf.xyz.common.base.XyzBaseController {
    private final XyzAttachmentEducationSample sample;

    public XyzAttachmentEducationController(XyzAttachmentEducationSample sample) {
        this.sample = sample;
    }

    @PostMapping("/text")
    @CpfOnlineTransaction(id = "OXYZAA0065", name = "XYZ첨부파일저장")
    @Operation(operationId = "xyzAttachmentEducationStoreText", summary = "교육용 UTF-8 첨부 저장",
            description = "PFW 저장 port의 파일명·확장자·크기·경로 검증과 SHA-256 계산 결과를 확인합니다.")
    public ResponseEntity<CpfStoredAttachment> storeText(
            @RequestBody XyzAttachmentEducationSample.AttachmentTextRequest request) {
        return ResponseEntity.ok(sample.storeText(request));
    }

    @PostMapping("/verify")
    @CpfOnlineTransaction(id = "OXYZAA0066", name = "XYZ첨부파일검증")
    @Operation(operationId = "xyzAttachmentEducationVerify", summary = "교육용 첨부 checksum 검증",
            description = "저장 key로 본문을 다시 읽어 예상 SHA-256과 일치하는지 확인합니다.")
    public ResponseEntity<XyzAttachmentEducationSample.AttachmentVerification> verify(
            @RequestBody XyzAttachmentEducationSample.AttachmentVerifyRequest request) {
        return ResponseEntity.ok(sample.verify(request));
    }
}
