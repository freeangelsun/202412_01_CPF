package com.cpf.core.api.data.quality;
import java.time.Instant; import java.util.List;
public record CpfDataQualityDecision(String recordId,boolean accepted,List<Violation> violations,String quarantineId,Instant decidedAt){
 public record Violation(String ruleId,CpfDataQualityRule.Severity severity,String fieldName,String message){}
 public CpfDataQualityDecision{violations=List.copyOf(violations);}
}
