package com.cpf.education.file.attachment.controller;
import com.cpf.file.attachment.api.CpfStoredAttachment;
import com.cpf.foundation.annotation.CpfOnlineTransaction;
import com.cpf.education.file.attachment.EducationAttachmentEducationSample;
import com.cpf.file.attachment.CpfMultipartAttachmentAdapter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** CPF 첨부 저장 port를 실행해 보는 EDU 교육 API입니다. */
@RestController
@RequestMapping({"/api/education/attachments", "/education/edu/attachments"})
@Tag(name = "EDU Education 17. 첨부파일", description = "안전한 첨부 저장, checksum, 저장 adapter 교체 교육 샘플")
public class EducationAttachmentEducationController extends com.cpf.education.base.EducationBaseController {
    private final EducationAttachmentEducationSample sample;
    private final CpfMultipartAttachmentAdapter multipart;

    /** EducationAttachmentEducationController 작업을 CPF 표준 계약에 따라 수행한다. */
    public EducationAttachmentEducationController(EducationAttachmentEducationSample sample, CpfMultipartAttachmentAdapter multipart) {
        this.sample = sample;
        this.multipart = multipart;
    }

    @PostMapping("/text")
    @CpfOnlineTransaction(id = "OEDUAA0065", name = "EDU첨부파일저장", ownerDomain="EDU")
    @Operation(operationId = "refAttachmentEducationStoreText", summary = "교육용 UTF-8 첨부 저장",
            description = "CPF 저장 port의 파일명·확장자·크기·경로 검증과 SHA-256 계산 결과를 확인합니다.")
    /** storeText 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<CpfStoredAttachment> storeText(
            @RequestBody EducationAttachmentEducationSample.AttachmentTextRequest request) {
        return ResponseEntity.ok(sample.storeText(request));
    }

    @PostMapping(path = "/multipart", consumes = "multipart/form-data")
    @CpfOnlineTransaction(id = "OEDUAA0067", name = "EDUMultipart첨부저장", ownerDomain="EDU")
    @Operation(operationId = "refAttachmentEducationStoreMultipart", summary = "교육용 Multipart 첨부 저장",
            description = "Spring MultipartFile을 CPF streaming storage port로 연결하면서 크기, 파일명, content-type 정책을 적용합니다.")
    /** storeMultipart 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<CpfStoredAttachment> storeMultipart(
            @RequestParam("groupId") String groupId, @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(multipart.store(groupId, file));
    }

    @PostMapping("/verify")
    @CpfOnlineTransaction(id = "OEDUAA0066", name = "EDU첨부파일검증", ownerDomain="EDU")
    @Operation(operationId = "refAttachmentEducationVerify", summary = "교육용 첨부 checksum 검증",
            description = "저장 key로 본문을 다시 읽어 예상 SHA-256과 일치하는지 확인합니다.")
    /** verify 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<EducationAttachmentEducationSample.AttachmentVerification> verify(
            @RequestBody EducationAttachmentEducationSample.AttachmentVerifyRequest request) {
        return ResponseEntity.ok(sample.verify(request));
    }
}
