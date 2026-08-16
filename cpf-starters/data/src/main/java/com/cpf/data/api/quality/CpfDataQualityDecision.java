package com.cpf.data.api.quality;
import java.time.Instant; import java.util.List;
/** CpfDataQualityDecision 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public record CpfDataQualityDecision(String recordId,boolean accepted,List<Violation> violations,String quarantineId,Instant decidedAt){
 public record Violation(String ruleId,CpfDataQualityRule.Severity severity,String fieldName,String message){}
 public CpfDataQualityDecision{violations=List.copyOf(violations);}
}
