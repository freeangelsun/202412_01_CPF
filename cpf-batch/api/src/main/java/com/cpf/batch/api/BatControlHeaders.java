package com.cpf.batch.api;

/**
 * BAT control-plane wire headers. These headers are owned by the authenticated ADM/BAT control
 * protocol and are not part of the canonical online CPF domain-call header set.
 */
public final class BatControlHeaders {
    public static final String CALLER_SERVICE = "X-Cpf-Bat-Caller-Service";
    public static final String CALLER_INSTANCE_ID = "X-Cpf-Bat-Caller-Instance-Id";
    public static final String OPERATOR_ID = "X-Cpf-Bat-Operator-Id";
    public static final String APPROVAL_REQUEST_ID = "X-Cpf-Bat-Approval-Request-Id";
    public static final String APPROVAL_REQUESTER_ID = "X-Cpf-Bat-Approval-Requester-Id";

    private BatControlHeaders() { }
}
