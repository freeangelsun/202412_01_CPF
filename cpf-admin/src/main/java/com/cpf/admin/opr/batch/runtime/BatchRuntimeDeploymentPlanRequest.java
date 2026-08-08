package com.cpf.admin.opr.batch.runtime;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.LinkedHashMap;
import java.util.Map;

@Schema(name = "BatchRuntimeDeploymentPlanRequest", description = "BAT Deployment plan 생성 요청. requestedBy는 서버 인증 Context에서 주입됩니다.")
public final class BatchRuntimeDeploymentPlanRequest {
    public String planId;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) public Map<String,Object> manifest;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) public String reason;
    public Map<String,Object> toMap(){
        Map<String,Object> out=new LinkedHashMap<>();
        if(planId!=null&&!planId.isBlank()) out.put("planId",planId.trim());
        if(manifest!=null) out.put("manifest",manifest);
        if(reason!=null) out.put("reason",reason);
        return out;
    }
}
