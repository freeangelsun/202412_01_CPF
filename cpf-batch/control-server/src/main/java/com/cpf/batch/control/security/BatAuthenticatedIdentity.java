package com.cpf.batch.control.security;

/**
 * 인증 경계가 검증한 BAT 호출 신원과 승인 위임 문맥입니다.
 *
 * <p>Controller는 요청 Body의 행위자 필드를 사용하지 않고 이 문맥만 사용합니다.</p>
 */
public record BatAuthenticatedIdentity(
        String operatorId,
        String callerService,
        String callerInstanceId,
        String clientCertificateSubject,
        String approvalRequestId,
        String approvalRequesterId) {
}
