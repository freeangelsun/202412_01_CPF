package com.cpf.admin.opr.batch.runtime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.Map;

@Schema(name = "RetentionPreviewRequest", description = "Retention 실제 handler dry-run 요청")
public record RetentionPreviewRequest(
        @NotBlank @Size(max=256) String target,
        @NotBlank @Size(max=32) String action,
        @NotNull Instant cutoff,
        boolean legalHold,
        @NotBlank @Size(min=5,max=500) String reason,
        @Min(1) @Max(100000) int limit) {
    public Map<String,Object> toMap(){return Map.of("target",target,"action",action,"cutoff",cutoff.toString(),"legalHold",legalHold,"reason",reason,"limit",limit);}
}
