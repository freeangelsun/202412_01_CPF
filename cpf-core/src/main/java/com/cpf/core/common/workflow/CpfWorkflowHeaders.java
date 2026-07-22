package com.cpf.core.common.workflow;

/** 워크플로와 보상 거래 메타데이터를 전파하는 표준 HTTP 헤더 모음입니다. */
public final class CpfWorkflowHeaders {
    public static final String HEADER_WORKFLOW_ID = "X-Workflow-Id";
    public static final String HEADER_WORKFLOW_NAME = "X-Workflow-Name";
    public static final String HEADER_WORKFLOW_INSTANCE_ID = "X-Workflow-Instance-Id";
    public static final String HEADER_WORKFLOW_STEP_ID = "X-Workflow-Step-Id";
    public static final String HEADER_WORKFLOW_STEP_NAME = "X-Workflow-Step-Name";
    public static final String HEADER_WORKFLOW_FAILURE_POLICY = "X-Workflow-Failure-Policy";
    public static final String HEADER_COMPENSATION_YN = "X-Compensation-Yn";
    public static final String HEADER_COMPENSATION_TRANSACTION_ID = "X-Compensation-Transaction-Id";
    public static final String HEADER_COMPENSATION_TARGET_TRANSACTION_ID = "X-Compensation-Target-Transaction-Id";

    private CpfWorkflowHeaders() {
    }
}

