package com.cpf.admin.opr.batch.runtime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.LinkedHashMap;
import java.util.Map;

@Schema(name = "BatchJobDefinitionTransitionRequest", description = "Batch Job Definition 상태 전이 요청")
public record BatchJobDefinitionTransitionRequest(
        @Min(0) long expectedRowVersion,
        @NotBlank @Size(max = 32) String targetState,
        @Size(max = 128) String approvalRequestId,
        @NotBlank @Size(min = 5, max = 500) String reason) {
    public Map<String,Object> toMap(){
        Map<String,Object> out=new LinkedHashMap<>();
        out.put("expectedRowVersion",expectedRowVersion); out.put("targetState",targetState); out.put("reason",reason);
        if(approvalRequestId!=null&&!approvalRequestId.isBlank()) out.put("approvalRequestId",approvalRequestId.trim());
        return out;
    }
}
