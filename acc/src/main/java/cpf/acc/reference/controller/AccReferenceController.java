package cpf.acc.reference.controller;

import cpf.cmn.contract.reference.AccMemberExternalFacade;
import cpf.cmn.contract.reference.AccMemberExternalRequest;
import cpf.cmn.contract.reference.AccMemberExternalResponse;
import cpf.pfw.common.logging.CpfTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 별도 프로세스 MBR이 ACC reference domain을 호출할 때 사용하는 공개 API입니다.
 */
@RestController
@RequestMapping("/api/v1/acc/reference")
@Tag(name = "ACC Reference", description = "ACC 최소 reference domain 공개 API")
public class AccReferenceController {
    private final AccMemberExternalFacade memberExternalFacade;

    public AccReferenceController(AccMemberExternalFacade memberExternalFacade) {
        this.memberExternalFacade = memberExternalFacade;
    }

    @PostMapping("/member-external")
    @CpfTransaction(id = "ACC01REF0001", name = "AccMemberExternalReference")
    @Operation(
            operationId = "requestAccMemberExternalReference",
            summary = "회원 기반 외부연계 실행",
            description = "ACC가 공개 계약을 검증한 후 PFW Service Call Engine을 통해 EXS 외부 송신 원장을 기록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "외부연계 처리 완료"),
            @ApiResponse(responseCode = "400", description = "요청 형식 오류"),
            @ApiResponse(responseCode = "503", description = "하위 서비스 호출 불가")
    })
    public ResponseEntity<AccMemberExternalResponse> requestExternal(
            @Valid @RequestBody AccMemberExternalRequest request) {
        return ResponseEntity.ok(memberExternalFacade.requestExternal(request));
    }
}
