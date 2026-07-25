package com.cpf.core.common.saga;

public record CpfSagaStepSnapshot(int stepNo,String stepId,CpfSagaStepStatus status,String resultCode,String resultSnapshot,String errorMessage,int executeAttempts,int compensationAttempts) {}
