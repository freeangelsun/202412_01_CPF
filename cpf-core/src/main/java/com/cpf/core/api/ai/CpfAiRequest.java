package com.cpf.core.api.ai;
import java.time.Duration; import java.util.*;
/** Provider-neutral AI 요청입니다. */
public record CpfAiRequest(String transactionId,String model,String prompt,CpfAiRisk risk,Duration timeout,boolean humanApproved,Map<String,String> attributes) {
 public CpfAiRequest { if(transactionId==null||transactionId.isBlank()) throw new IllegalArgumentException("transactionId required"); if(model==null||model.isBlank()) throw new IllegalArgumentException("model required"); if(prompt==null||prompt.isBlank()) throw new IllegalArgumentException("prompt required"); risk=Objects.requireNonNullElse(risk,CpfAiRisk.LOW); timeout=Objects.requireNonNullElse(timeout,Duration.ofSeconds(10)); if(timeout.isZero()||timeout.isNegative()) throw new IllegalArgumentException("timeout must be positive"); attributes=attributes==null?Map.of():Map.copyOf(attributes); }
}
