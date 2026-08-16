package com.cpf.foundation.workflow.api;

import java.util.Map;

/** 현재 요청의 CPF Workflow 전파 정보를 조회하는 공개 facade입니다. */
public final class CpfWorkflow {
    private CpfWorkflow() { }

    public static Map<String, String> propagationHeaders() {
        return com.cpf.foundation.workflow.CpfWorkflowContext.propagationHeaders();
    }
}
