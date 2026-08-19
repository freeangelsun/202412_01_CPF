package com.cpf.education.batch.centercut.service;

import com.cpf.foundation.annotation.CpfService;

import com.cpf.batch.api.CenterCutExecutionRequest;
import com.cpf.batch.api.CpfCenterCutOperations;
import com.cpf.batch.spi.BatchStepHandler.BatchStepCommand;
import com.cpf.batch.spi.BatchStepHandler.BatchStepResult;
import java.util.LinkedHashMap;
import java.util.Map;

/** 배치-05 Center-Cut: 실행계획→Worker 실행→상태조회까지 Public API로 연결하고 partial/unknown을 보존합니다. */
@CpfService
public class SettlementCenterCutJobService {
    private final CpfCenterCutOperations centerCut;

    public SettlementCenterCutJobService(CpfCenterCutOperations centerCut) {
        this.centerCut = centerCut;
    }

    /** run 단계는 CPF Public 계약을 사용해 이 EDU 시나리오의 업무 흐름을 수행합니다. */
    public BatchStepResult run(BatchStepCommand command) throws Exception {
        String idempotency = String.valueOf(
                command.jobParameters().getOrDefault("idempotencyKey", command.cpfExecutionId()));
        Map<String, Object> launch = centerCut.launch(new CenterCutExecutionRequest(
                "EDU_SETTLEMENT_CENTER_CUT_JOB",
                idempotency,
                command.jobParameters(),
                "1",
                100,
                4,
                "EDU",
                "교육 Center-Cut 실행",
                String.valueOf(command.jobParameters().getOrDefault("transactionId", "BATCH")),
                command.cpfExecutionId()));

        String executionId = String.valueOf(launch.getOrDefault("executionId", command.cpfExecutionId()));
        Map<String, Object> status = centerCut.status(executionId);
        Map<String, Object> checkpoint = new LinkedHashMap<>();
        checkpoint.put("executionId", executionId);
        checkpoint.put("launch", launch);
        checkpoint.put("status", status);
        return BatchStepResult.completed("center-cut accepted", 0, 0, checkpoint);
    }
}
