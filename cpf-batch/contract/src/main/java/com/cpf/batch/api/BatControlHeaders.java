package com.cpf.batch.api;

/**
 * 인증된 ADM Approval Engine이 BAT Owner Command API에 전달하는 승인 위임 Header 계약입니다.
 *
 * <p>이 값은 mTLS로 인증된 ADM caller 경계 안에서만 신뢰하며 요청 Body의 행위자 필드를
 * 대신 검증하는 데 사용합니다.</p>
 */
public final class BatControlHeaders {
    public static final String APPROVAL_REQUEST_ID = "X-Cpf-Ext-Approval-Request-Id";
    public static final String APPROVAL_REQUESTER_ID = "X-Cpf-Ext-Approval-Requester-Id";

    private BatControlHeaders() {
    }
}
