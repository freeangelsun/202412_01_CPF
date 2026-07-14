package cpf.acc.bse.controller;

import cpf.acc.bse.service.AccCompositeTransactionService;
import cpf.pfw.common.logging.CpfTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 복합 거래 trace 교육 API입니다.
 */
@RestController
@RequestMapping("/acc/edu/composite")
@RequiredArgsConstructor
@Validated
@Tag(name = "ACC-EDU Composite Transaction", description = "ACC/MBR/EXS 복합 거래 trace 교육 API")
public class AccCompositeEducationController {
    private final AccCompositeTransactionService compositeTransactionService;

    @PostMapping("/member-then-external")
    @CpfTransaction(id = "ACC09EDU0001", name = "ACCCompositeMemberThenExternal")
    @Operation(operationId = "accCompositeEducationMemberThenExternal", summary = "ACC 오케스트레이션 복합 거래", description = "ACC가 MBR 조회 후 EXS 외부연계를 호출하고 transactionGlobalId와 segment 흐름을 반환합니다.")
    public ResponseEntity<Map<String, Object>> memberThenExternal(
            @RequestParam(defaultValue = "1") @Positive Integer memberId) {
        return ResponseEntity.ok(compositeTransactionService.memberThenExternal(memberId));
    }

    @PostMapping("/member-calls-external")
    @CpfTransaction(id = "ACC09EDU0002", name = "ACCCompositeMemberCallsExternal")
    @Operation(operationId = "accCompositeEducationMemberCallsExternal", summary = "MBR 중첩 외부연계 복합 거래", description = "ACC가 MBR을 호출하고 MBR이 EXS를 호출하는 중첩 흐름을 반환합니다.")
    public ResponseEntity<Map<String, Object>> memberCallsExternal(
            @RequestParam(defaultValue = "1") @Positive Integer memberId) {
        return ResponseEntity.ok(compositeTransactionService.memberCallsExternal(memberId));
    }

    @PostMapping("/member-then-external-failure")
    @CpfTransaction(id = "ACC09EDU0003", name = "ACCCompositeMemberThenExternalFailure")
    @Operation(operationId = "accCompositeEducationMemberThenExternalFailure", summary = "ACC 복합 거래 실패 trace", description = "ACC가 MBR 성공 후 EXS 실패를 만나고 실패 구간, 실패 코드, 마스킹된 메시지를 반환합니다.")
    public ResponseEntity<Map<String, Object>> memberThenExternalFailure(
            @RequestParam(defaultValue = "1") @Positive Integer memberId) {
        return ResponseEntity.ok(compositeTransactionService.memberThenExternalFailure(memberId));
    }
}
