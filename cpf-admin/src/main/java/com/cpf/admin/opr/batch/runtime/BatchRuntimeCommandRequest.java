package com.cpf.admin.opr.batch.runtime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "BatchRuntimeCommandRequest", description = "이미 승인된 BAT Runtime 명령 실행 요청. 실제 target/command/actor는 승인 Snapshot과 서버 인증 Context에서 복원됩니다.")
public final class BatchRuntimeCommandRequest {
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) public String approvalRequestId;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) public String reason;
}
