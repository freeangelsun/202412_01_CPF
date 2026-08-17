package com.cpf.education.online;

import com.cpf.batch.api.CenterCutExecutionRequest;
import com.cpf.batch.api.CpfCenterCutOperations;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.foundation.execution.api.CpfOnlineTransaction;
import com.cpf.security.api.annotation.CpfPreAuthorize;
import com.cpf.web.api.CpfRestController;
import io.swagger.v3.oas.annotations.Operation;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/** 온라인-13 Center-Cut Consumer: 거래 Context와 executionId를 연계해 실행 접수와 상태를 한 흐름으로 확인합니다. */
@CpfRestController
@RequestMapping("/edu/online/13-center-cut")
public class Online13CenterCutExample {
    private final CpfCenterCutOperations centerCut;

    public Online13CenterCutExample(CpfCenterCutOperations centerCut) {
        this.centerCut = centerCut;
    }

    @PostMapping
    @CpfPreAuthorize("hasAuthority('CENTER_CUT_EXECUTE')")
    @Operation(operationId = "EDU-ONLINE-13", summary = "Center-Cut 대응 온라인 거래")
    @CpfOnlineTransaction(
            operationId = "EDU-ONLINE-13",
            name = "Center-Cut 대응 온라인 거래",
            description = "Center-Cut 실행 접수와 상태 조회를 Public API로 수행하고 transactionId와 executionId를 연계한다.")
    /** launch 단계는 CPF Public 계약을 사용해 이 EDU 시나리오의 업무 흐름을 수행합니다. */
    public CenterCutView launch(@RequestBody Command command) throws Exception {
        Map<String, Object> accepted = centerCut.launch(new CenterCutExecutionRequest(
                "EDU-BATCH-05", command.idempotencyKey(), command.parameters(), "1",
                command.tpsLimit(), command.concurrencyLimit(), command.requestedBy(),
                "교육 Center-Cut 실행", CpfContexts.transactionId(), CpfContexts.currentSegmentId()));
        String executionId = String.valueOf(accepted.getOrDefault("executionId", CpfContexts.currentSegmentId()));
        Map<String, Object> current = centerCut.status(executionId);
        return new CenterCutView(executionId, CpfContexts.transactionId(), accepted, current);
    }

    /** 이 타입은 해당 EDU 시나리오의 입력·출력 또는 업무 경계를 명확히 표현합니다. */
    public record Command(String idempotencyKey, Map<String, Object> parameters, int tpsLimit, int concurrencyLimit, String requestedBy) { }
    /** 이 타입은 해당 EDU 시나리오의 입력·출력 또는 업무 경계를 명확히 표현합니다. */
    public record CenterCutView(String executionId, String transactionId, Map<String, Object> accepted, Map<String, Object> current) { }
}
