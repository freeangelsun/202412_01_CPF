package com.cpf.admin.opr.batch.runtime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Schema(name = "RetentionPolicySaveRequest", description = "Retention 정책 변경 승인 요청. rowVersion은 BAT Owner CAS 기준입니다.")
public record RetentionPolicySaveRequest(
        @NotBlank @Size(max=128) String policyId,
        @NotBlank @Size(max=256) String target,
        @NotBlank @Size(max=32) String action,
        @Min(0) @Max(36500) int retentionDays,
        @Size(max=256) String scheduleExpression,
        String maintenanceStart,
        String maintenanceEnd,
        boolean enabled,
        boolean legalHold,
        @Min(1) int chunkSize,
        @Min(0) long throttleMillis,
        @Min(1) long maxRowsPerRun,
        @Min(1) long maxRuntimeSeconds,
        @Min(1) int leaseSeconds,
        @Min(0) long policyVersion,
        Instant nextRunAt,
        @Min(0) long rowVersion,
        @NotBlank @Size(min=5,max=500) String reason) {
    public Map<String,Object> toMap(){
        Map<String,Object> out=new LinkedHashMap<>();
        out.put("policyId",policyId);out.put("target",target);out.put("action",action);out.put("retentionDays",retentionDays);
        out.put("scheduleExpression",scheduleExpression);out.put("maintenanceStart",maintenanceStart);out.put("maintenanceEnd",maintenanceEnd);
        out.put("enabled",enabled);out.put("legalHold",legalHold);out.put("chunkSize",chunkSize);out.put("throttleMillis",throttleMillis);
        out.put("maxRowsPerRun",maxRowsPerRun);out.put("maxRuntimeSeconds",maxRuntimeSeconds);out.put("leaseSeconds",leaseSeconds);
        out.put("policyVersion",policyVersion);out.put("nextRunAt",nextRunAt);out.put("rowVersion",rowVersion);out.put("reason",reason);
        return out;
    }
}
