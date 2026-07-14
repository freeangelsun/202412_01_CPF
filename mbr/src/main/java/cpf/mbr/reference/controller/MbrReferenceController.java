package cpf.mbr.reference.controller;

import cpf.cmn.contract.reference.AccMemberExternalRequest;
import cpf.cmn.contract.reference.AccMemberExternalResponse;
import cpf.mbr.reference.service.MbrAccReferenceClient;
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
 * MBR에서 ACC와 EXS로 이어지는 기준 MSA 흐름의 진입 API입니다.
 */
@RestController
@RequestMapping("/api/v1/mbr/reference")
@Tag(name = "MBR Reference", description = "MBR에서 시작하는 기준 MSA 호출 API")
public class MbrReferenceController {
    private final MbrAccReferenceClient accReferenceClient;

    public MbrReferenceController(MbrAccReferenceClient accReferenceClient) {
        this.accReferenceClient = accReferenceClient;
    }

    @PostMapping("/member-acc-exs")
    @CpfTransaction(id = "MBR01REF0001", name = "MbrAccExsReference")
    @Operation(
            operationId = "requestMbrAccExsReference",
            summary = "MBR-ACC-EXS 기준 거래 실행",
            description = "Local Facade 또는 Remote Proxy로 ACC를 호출하고 ACC가 EXS를 호출하는 기준 거래입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "기준 거래 완료"),
            @ApiResponse(responseCode = "400", description = "요청 형식 오류"),
            @ApiResponse(responseCode = "503", description = "하위 서비스 호출 불가")
    })
    public ResponseEntity<AccMemberExternalResponse> requestMemberAccExs(
            @Valid @RequestBody AccMemberExternalRequest request) {
        return ResponseEntity.ok(accReferenceClient.requestExternal(request));
    }
}
