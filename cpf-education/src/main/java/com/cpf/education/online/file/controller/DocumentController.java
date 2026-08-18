package com.cpf.education.online.file.controller;
import com.cpf.education.online.file.dto.*; import com.cpf.education.online.file.service.DocumentService; import com.cpf.foundation.execution.api.CpfOnlineTransaction; import com.cpf.web.api.CpfRestController; import io.swagger.v3.oas.annotations.Operation; import java.io.IOException; import org.springframework.web.bind.annotation.*;
@CpfRestController @RequestMapping("/edu/online/documents")
/** DocumentController는 CPF File/Object Storage Public API로 업로드·다운로드 책임을 분리하는 File Golden Path입니다. */
public class DocumentController { private final DocumentService service; public DocumentController(DocumentService service){this.service=service;}
 @PostMapping @Operation(operationId="EDU_DOCUMENT_TRANSFER",summary="Document upload/download") @CpfOnlineTransaction(operationId="EDU_DOCUMENT_TRANSFER",name="문서 전송",description="Controller는 stream/checksum/storage를 직접 관리하지 않고 CPF File/Object Storage Public API를 Service에서 사용한다.") public DocumentUploadResponse upload(@RequestBody DocumentUploadRequest r) throws IOException{return service.upload(r);} }
