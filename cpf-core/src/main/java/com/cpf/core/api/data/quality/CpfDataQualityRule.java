package com.cpf.core.api.data.quality;
import java.util.Map;
public record CpfDataQualityRule(String ruleId,long version,String fieldName,String expression,Severity severity,State state,Map<String,String> parameters){
    public enum Severity{INFO,WARNING,ERROR,CRITICAL} public enum State{DRAFT,ACTIVE,DISABLED}
    public CpfDataQualityRule { if(ruleId==null||ruleId.isBlank()||version<1||fieldName==null||fieldName.isBlank()||expression==null||expression.isBlank()||severity==null||state==null)throw new IllegalArgumentException("invalid quality rule"); parameters=parameters==null?Map.of():Map.copyOf(parameters); }
}
