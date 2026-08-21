package com.cpf.admin.opr.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.LinkedHashMap;
import java.util.Map;

@Schema(name = "AdmIncidentCreateRequest", description = "운영 Incident 생성 요청")
public record AdmIncidentCreateRequest(
        @NotBlank @Size(max = 32) String severity,
        @NotBlank @Size(max = 200) String title,
        @Size(max = 2000) String summary,
        @Size(max = 64) String sourceType,
        @Size(max = 256) String sourceId,
        @NotBlank @Size(min = 8, max = 500) String reason) {
    public Map<String,Object> toMap() {
        Map<String,Object> out = new LinkedHashMap<>();
        out.put("severity", severity); out.put("title", title); out.put("summary", summary);
        out.put("sourceType", sourceType); out.put("sourceId", sourceId); out.put("reason", reason);
        return out;
    }
}
