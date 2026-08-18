package com.cpf.education.online.recovery.dto;
/** TransferCommand는 UNKNOWN 결과를 Probe/Reconcile/Recovery 경로로 수렴시키는 복구 Golden Path입니다. */
public record TransferCommand(String idempotencyKey, String payload) { }
