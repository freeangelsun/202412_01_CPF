package com.cpf.batch.api;

import java.util.Map;

/** 업무 Application이 Center-Cut Runtime 내부 구현 없이 실행 접수와 상태를 다루는 공개 API입니다. */
public interface CpfCenterCutOperations {
    Map<String,Object> launch(CenterCutExecutionRequest request) throws Exception;
    Map<String,Object> status(String executionId);
}
