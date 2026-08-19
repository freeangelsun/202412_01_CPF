package com.cpf.education.online.ondemandbatch.dto;

import java.util.Map;

/** 회원 Export 비동기 실행 입력입니다. Framework Context/실행 ID는 포함하지 않습니다. */
public record MemberExportCommand(
        String businessDate,
        String idempotencyKey,
        Map<String, Object> jobParameters,
        String requestUser) {
    public MemberExportCommand {
        jobParameters = jobParameters == null ? Map.of() : Map.copyOf(jobParameters);
    }
}
