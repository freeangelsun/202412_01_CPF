package cpf.xyz.centercut.controller;

import cpf.pfw.common.execution.CpfOnlineTransaction;
import cpf.xyz.centercut.dto.XyzCenterCutExecutionResponse;
import cpf.xyz.centercut.application.XyzCenterCutEducationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 업무 DB 기반 center-cut adapter 교육 API입니다.
 */
@RestController
@RequestMapping({"/api/xyz/reference/center-cut", "/xyz/edu/center-cut"})
@Tag(name = "XYZ Reference 14. Center-Cut", description = "업무 DB 기반 center-cut target/provider/handler/result adapter 샘플")
public class XyzCenterCutEducationController extends cpf.xyz.common.base.XyzBaseController {
    private final XyzCenterCutEducationService educationService;

    public XyzCenterCutEducationController(XyzCenterCutEducationService educationService) {
        this.educationService = educationService;
    }

    @PostMapping("/run")
    @CpfOnlineTransaction(id = "OXYZAA0056", name = "XYZCenterCut업무DBAdapter실행")
    @Operation(operationId = "xyzCenterCutEducationRun",
            summary = "업무 DB 기반 center-cut 샘플 실행",
            description = "xyz_center_cut_sample_target을 조회하고 item별 성공/실패 결과를 xyz_center_cut_sample_result에 기록합니다.")
    public ResponseEntity<XyzCenterCutExecutionResponse> run(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "false") boolean resetBeforeRun) {
        return ResponseEntity.ok(educationService.runSample(limit, resetBeforeRun));
    }
}
