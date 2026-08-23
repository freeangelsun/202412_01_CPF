package com.cpf.integration.api.message;

/**
 * 전송 Adapter가 특정 전문 layout의 등록 여부를 확인하는 provider-neutral 계약입니다.
 *
 * <p>HTTP/TCP 같은 전송 Provider는 fixed-length 구현 타입을 직접 참조하지 않고 이 최소 계약만
 * 사용합니다. 실제 전문 Provider가 선택되지 않은 경우에는 Bean 자체가 존재하지 않습니다.</p>
 */
@FunctionalInterface
public interface CpfMessageLayoutValidator {
    /** 요청한 layout ID와 version이 사용 가능하지 않으면 예외를 발생시킵니다. */
    void requireAvailable(String layoutId, String version);
}
