package com.cpf.admin.opr.batch.runtime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Schema(name = "BatchJobDefinitionRequest", description = "Versioned Batch Job Definition 검증·Draft 저장 요청")
public record BatchJobDefinitionRequest(
        @NotBlank @Size(max = 128) String jobId,
        @Min(1) Long definitionVersion,
        @NotBlank @Size(max = 200) String jobName,
        @NotBlank @Size(max = 64) String executorType,
        @NotBlank @Size(max = 32) String state,
        @NotBlank @Size(max = 64) String ownerDomain,
        @Size(max = 2000) String description,
        Map<String,Object> trigger,
        List<Map<String,Object>> parameters,
        List<Map<String,Object>> dependencies,
        Map<String,Object> resourcePolicy,
        Map<String,Object> recoveryPolicy,
        Map<String,Object> alertPolicy,
        @Size(max = 512) String executorReference,
        @Size(max = 128) String checksum,
        @NotBlank @Size(min = 5, max = 500) String reason,
        String effectiveFrom,
        String effectiveUntil,
        @Min(0) Long expectedRowVersion) {
    public Map<String,Object> toMap() {
        Map<String,Object> out = new LinkedHashMap<>();
        put(out,"jobId",jobId); put(out,"definitionVersion",definitionVersion); put(out,"jobName",jobName);
        put(out,"executorType",executorType); put(out,"state",state); put(out,"ownerDomain",ownerDomain);
        put(out,"description",description); put(out,"trigger",trigger); put(out,"parameters",parameters);
        put(out,"dependencies",dependencies); put(out,"resourcePolicy",resourcePolicy); put(out,"recoveryPolicy",recoveryPolicy);
        put(out,"alertPolicy",alertPolicy); put(out,"executorReference",executorReference); put(out,"checksum",checksum);
        put(out,"reason",reason); put(out,"effectiveFrom",effectiveFrom); put(out,"effectiveUntil",effectiveUntil);
        put(out,"expectedRowVersion",expectedRowVersion);
        return out;
    }
    private static void put(Map<String,Object> out,String key,Object value){ if(value!=null) out.put(key,value); }
}
