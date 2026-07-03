package cpf.mbr.bse.controller;

import cpf.mbr.bse.service.MbrCompositeTransactionService;
import cpf.pfw.common.logging.CpfTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * MBR 복합 거래 trace 교육 API입니다.
 */
@RestController
@RequestMapping("/mbr/edu/composite")
@RequiredArgsConstructor
@Validated
@Tag(name = "MBR-EDU Composite Transaction", description = "MBR 구간 및 MBR→EXS 중첩 호출 trace 교육 API")
public class MbrCompositeEducationController {
    private final MbrCompositeTransactionService compositeTransactionService;

    @GetMapping("/member-profile")
    @CpfTransaction(id = "MBR09EDU0001", name = "MBRCompositeMemberProfile")
    @Operation(summary = "MBR 회원 조회 구간", description = "ACC에서 전달된 transactionGlobalId와 parentSegmentId를 이어받아 MBR 구간을 기록합니다.")
    public ResponseEntity<Map<String, Object>> memberProfile(
            @RequestParam(defaultValue = "1") @Positive Integer memberId) {
        return ResponseEntity.ok(compositeTransactionService.memberProfile(memberId));
    }

    @GetMapping("/member-calls-external")
    @CpfTransaction(id = "MBR09EDU0002", name = "MBRCompositeMemberCallsExternal")
    @Operation(summary = "MBR 중첩 외부연계 구간", description = "MBR이 EXS를 호출하는 중첩 복합 거래 구간을 기록합니다.")
    public ResponseEntity<Map<String, Object>> memberCallsExternal(
            @RequestParam(defaultValue = "1") @Positive Integer memberId) {
        return ResponseEntity.ok(compositeTransactionService.memberCallsExternal(memberId));
    }
}
