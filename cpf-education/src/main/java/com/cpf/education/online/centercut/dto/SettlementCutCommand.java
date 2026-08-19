package com.cpf.education.online.centercut.dto;
import java.util.Map;
/** Center-Cut 교육 예제의 DTO 역할과 CPF 표준 사용 경계를 보여줍니다. */
public record SettlementCutCommand(String idempotencyKey,Map<String,Object> parameters,int tpsLimit,int concurrencyLimit,String requestedBy){public SettlementCutCommand{parameters=parameters==null?Map.of():Map.copyOf(parameters);}}
