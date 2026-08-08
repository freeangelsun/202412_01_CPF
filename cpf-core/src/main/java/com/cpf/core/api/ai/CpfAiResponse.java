package com.cpf.core.api.ai;
import java.util.*;
/** AI Provider 실행 결과와 사용량/비용 Metadata를 전달합니다. */
public record CpfAiResponse(String provider,String model,String content,CpfAiUsage usage,Map<String,String> metadata) { public CpfAiResponse { if(provider==null||provider.isBlank()) throw new IllegalArgumentException("provider required"); if(model==null||model.isBlank()) throw new IllegalArgumentException("model required"); content=Objects.requireNonNullElse(content,""); usage=Objects.requireNonNullElse(usage,new CpfAiUsage(0,0,0)); metadata=metadata==null?Map.of():Map.copyOf(metadata); } }
