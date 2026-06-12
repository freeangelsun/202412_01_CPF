package fps.acc.tst.controller;

import fps.pfw.common.exception.FpsBusinessException;
import fps.pfw.common.exception.FpsExternalServiceException;
import fps.pfw.common.exception.FpsValidationException;
import fps.pfw.common.logging.FpsTransaction;
import fps.pfw.common.workflow.FpsWorkflow;
import fps.pfw.common.workflow.FpsWorkflowFailurePolicy;
import fps.pfw.common.workflow.FpsWorkflowStep;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * ACC 테스트 주제영역의 거래 로그 검증용 컨트롤러입니다.
 *
 * <p>이 컨트롤러는 실제 업무 API가 아니라 PFW 프레임워크가 제공하는 기능을 개발자가 눈으로 확인할 수 있도록
 * 만든 샘플입니다. 각 메서드는 {@link FpsTransaction}으로 업무 거래ID를 선언하고,
 * 필요 시 {@link FpsWorkflow}와 {@link FpsWorkflowStep}으로 워크플로우/보상 정책을 선언합니다.</p>
 *
 * <p>PFW의 {@code LoggingAspect}는 이 컨트롤러 실행을 가로채서 TRAN_LOG와 TRAN_LOG_DTL에
 * 거래ID, 워크플로우 상태, 보상 여부, 실제 실행 클래스/메서드 정보를 자동 저장합니다.</p>
 */
@RestController
@RequestMapping("/acc/tran")
@Tag(name = "ACC-TST 거래로그/워크플로우", description = "거래 로그, 표준 예외, 보상/워크플로우 테스트 API")
public class TransactionController {

    /**
     * 정상 종료되는 샘플 거래입니다.
     *
     * <p>이 메서드는 성공 응답을 반환하므로 로그에는 {@code LOG_TYPE=SUCCESS},
     * {@code WORKFLOW_STATUS=COMPLETED}로 적재됩니다.</p>
     *
     * @param menuId 화면 또는 메뉴에서 넘기는 샘플 메뉴 ID입니다. 로그의 메뉴 식별 보조값으로 활용할 수 있습니다.
     * @param execUser 거래를 실행한 샘플 사용자 ID입니다. 로그의 수행자와 감사 컬럼에 활용됩니다.
     * @return HTTP 200과 성공 문자열을 반환합니다.
     */
    @GetMapping("/success")
    @FpsTransaction(id = "ACC09TST0001", name = "ACC샘플성공거래")
    @FpsWorkflow(id = "ACC09TST9001", name = "ACC테스트워크플로우")
    @FpsWorkflowStep(name = "ACC샘플성공스텝")
    @Operation(summary = "성공 거래 로그 샘플", description = "정상 종료 거래가 SUCCESS/COMPLETED 상태로 로그에 남는지 확인합니다.")
    public ResponseEntity<String> handleSuccessfulTransaction(@RequestParam String menuId, @RequestParam String execUser) {
        // ResponseEntity.ok는 HTTP 200 응답을 만들며, PFW 로그에는 성공 거래로 기록됩니다.
        return ResponseEntity.ok("Transaction processed successfully.");
    }

    /**
     * 예외를 발생시키는 샘플 거래입니다.
     *
     * <p>실패 정책을 {@link FpsWorkflowFailurePolicy#COMPENSATE}로 선언했으므로,
     * 예외가 발생하면 로그에는 {@code WORKFLOW_STATUS=COMPENSATING}과
     * {@code COMPENSATION_TRANSACTION_ID=ACC09TST0005}가 함께 적재됩니다.</p>
     *
     * @param menuId 샘플 메뉴 ID입니다.
     * @param execUser 샘플 수행자 ID입니다.
     * @return 이 메서드는 항상 예외를 던지므로 정상 응답을 반환하지 않습니다.
     */
    @GetMapping("/failure")
    @FpsTransaction(id = "ACC09TST0002", name = "ACC샘플실패거래")
    @FpsWorkflow(id = "ACC09TST9002", name = "ACC보상테스트워크플로우")
    @FpsWorkflowStep(
            name = "ACC샘플실패스텝",
            failurePolicy = FpsWorkflowFailurePolicy.COMPENSATE,
            compensationTransactionId = "ACC09TST0005")
    @Operation(summary = "실패 거래와 보상 필요 샘플", description = "실패 정책 COMPENSATE가 로그에 COMPENSATING 상태로 남는지 확인합니다.")
    public ResponseEntity<String> handleFailedTransaction(@RequestParam String menuId, @RequestParam String execUser) {
        // 실패 로그와 보상 필요 상태를 검증하기 위해 의도적으로 RuntimeException을 발생시킵니다.
        throw new RuntimeException("Simulated transaction failure.");
    }

    /**
     * 실패 거래를 보정하는 보상 거래 샘플입니다.
     *
     * <p>{@code compensation=true}로 선언했기 때문에 로그에는 {@code COMPENSATION_YN=Y}가 적재됩니다.
     * {@code compensationTargetTransactionId}는 어떤 원거래를 보상하는지 추적하기 위한 값입니다.</p>
     *
     * @param menuId 샘플 메뉴 ID입니다.
     * @param execUser 샘플 수행자 ID입니다.
     * @return HTTP 200과 보상 처리 성공 문자열을 반환합니다.
     */
    @GetMapping("/compensate")
    @FpsTransaction(id = "ACC09TST0005", name = "ACC샘플보상거래")
    @FpsWorkflow(id = "ACC09TST9002", name = "ACC보상테스트워크플로우")
    @FpsWorkflowStep(
            name = "ACC샘플보상스텝",
            compensation = true,
            compensationTargetTransactionId = "ACC09TST0002")
    @Operation(summary = "보상 거래 샘플", description = "보상 거래가 COMPENSATION_YN=Y로 로그에 남는지 확인합니다.")
    public ResponseEntity<String> handleCompensationTransaction(@RequestParam String menuId, @RequestParam String execUser) {
        // 실제 프로젝트에서는 여기에서 원거래 취소, 정정, 접수 상태 변경 같은 보상 로직을 수행합니다.
        return ResponseEntity.ok("Compensation transaction processed successfully.");
    }

    /**
     * PFW 표준 예외와 CMN 메시지 자동 매핑을 확인하는 샘플 거래입니다.
     *
     * <p>type 값에 따라 개발자가 업무 코드에서 어떤 표준 예외를 던지면 되는지 보여줍니다.
     * 예외가 발생하면 {@code FpsGlobalExceptionHandler}가 고객용 메시지, 오류코드,
     * 응답 헤더를 만들고 {@code LoggingAspect}가 내부 메시지와 상세 사유를 거래 로그에 남깁니다.</p>
     *
     * @param type 발생시킬 예외 유형입니다. validation, business, external 중 하나를 사용합니다.
     * @return 이 메서드는 표준 예외를 던지는 샘플이므로 정상 응답을 반환하지 않습니다.
     */
    @GetMapping("/standard-exception")
    @FpsTransaction(id = "ACC09TST0006", name = "PFW표준예외샘플")
    @Operation(summary = "표준 예외 샘플", description = "검증/업무/외부연계 예외가 공통 오류 응답으로 변환되는지 확인합니다.")
    public ResponseEntity<String> throwStandardException(@RequestParam(defaultValue = "validation") String type) {
        if ("business".equalsIgnoreCase(type)) {
            // 업무 규칙 위반 예시입니다. 예: 이미 처리된 신청, 한도 초과, 상태 불일치 등
            throw new FpsBusinessException("샘플 업무 규칙 위반입니다. type=" + type);
        }
        if ("external".equalsIgnoreCase(type)) {
            // 타 주제영역 또는 외부 시스템 호출 실패 예시입니다. 원인 예외를 함께 전달해 내부 로그 추적성을 높입니다.
            throw new FpsExternalServiceException("샘플 외부 연계 실패입니다. target=MBR", new IllegalStateException("MBR timeout sample"));
        }

        // 입력값 검증 실패 예시입니다. 고객에게는 표준 안내 메시지만 내려가고, 상세 값은 내부 로그에 남습니다.
        throw new FpsValidationException("샘플 입력값 검증 실패입니다. type=" + type);
    }
}
