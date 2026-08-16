package com.cpf.data.api.quality;
import java.util.Map;
/** CpfDataQualityRule 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public record CpfDataQualityRule(String ruleId,long version,String fieldName,String expression,Severity severity,State state,Map<String,String> parameters){
    public enum Severity{INFO,WARNING,ERROR,CRITICAL} public enum State{DRAFT,ACTIVE,DISABLED}
    public CpfDataQualityRule { if(ruleId==null||ruleId.isBlank()||version<1||fieldName==null||fieldName.isBlank()||expression==null||expression.isBlank()||severity==null||state==null)throw new IllegalArgumentException("invalid quality rule"); parameters=parameters==null?Map.of():Map.copyOf(parameters); }
}
