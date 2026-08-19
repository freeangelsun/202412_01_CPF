package com.cpf.education.online.messaging.dto;
/** 메시징 교육 예제의 DTO 역할과 CPF 표준 사용 경계를 보여줍니다. */
public record OrderEventCommand(String messageId,String memberId,String payload,String idempotencyKey) { }
