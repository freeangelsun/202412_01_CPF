package com.cpf.integration.api.domaincall;

import com.cpf.core.api.base.CpfRequest;
import com.cpf.core.api.base.CpfResponse;
import com.cpf.core.api.result.CpfResult;

/** 동일 JVM/Remote transport 모두에서 같은 Domain Operation 실행 경계를 제공하는 Registry 계약입니다. */
public interface CpfDomainOperationRegistry {
    boolean has(String systemCode, String operationId);

    /**
     * Framework가 검증한 호출 주체 metadata와 함께 Operation을 실행합니다.
     * Channel 값과 trusted System metadata를 혼동하지 않고, Local/Remote 모두 같은 정책 경계를 사용합니다.
     */
    <I extends CpfRequest, O extends CpfResponse> CpfResult<O> invoke(
            InvocationMetadata metadata,
            String systemCode,
            String operationId,
            I request,
            Class<O> responseType);

    /** Metadata 없이 정책 경계를 우회하는 직접 실행은 fail-close합니다. */
    default <I extends CpfRequest, O extends CpfResponse> CpfResult<O> invoke(
            String systemCode, String operationId, I request, Class<O> responseType) {
        throw new IllegalStateException("CPF Domain invocation metadata is required");
    }

    record InvocationMetadata(
            String callerSystemCode,
            boolean authenticated,
            boolean signed,
            boolean trustedInternal) {
        public InvocationMetadata {
            callerSystemCode = callerSystemCode == null || callerSystemCode.isBlank()
                    ? null : callerSystemCode.trim();
        }

        /** Same-JVM 내부 호출이더라도 명시적 trusted boundary를 통과했음을 표시하는 Framework 전용 metadata를 생성합니다. */
        public static InvocationMetadata trustedInternal(String callerSystemCode) {
            return new InvocationMetadata(callerSystemCode, false, false, true);
        }
    }
}
