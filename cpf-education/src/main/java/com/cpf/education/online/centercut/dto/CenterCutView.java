package com.cpf.education.online.centercut.dto;
import java.util.Map;
/** Center-Cut 교육 예제의 DTO 역할과 CPF 표준 사용 경계를 보여줍니다. */
public record CenterCutView(String executionId,String transactionId,Map<String,Object> accepted,Map<String,Object> current) { }
