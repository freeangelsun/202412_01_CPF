package com.cpf.education.online.externalsideeffect.dto;
/** PaymentCommand는 외부 Side Effect와 Local DB Commit 결과를 분리하고 UNKNOWN을 복구로 연결하는 거래 Golden Path입니다. */
public record PaymentCommand(String idempotencyKey,long amount) { }
