package com.cpf.integration.api.servicecall;

/**
 * HTTP 이외의 전송 Adapter까지 Service Call Engine에 실제 응답 상태를 전달하기 위한 최소 계약입니다.
 *
 * <p>전송 성공과 업무 성공을 혼동하지 않도록, 응답 객체가 이 계약을 구현하면 Engine은 고정 200 대신
 * 실제 상태를 호출 이력·분산 추적에 기록합니다. 4xx는 대상 인스턴스 장애로 취급하지 않으며,
 * retry가 필요한 상태는 {@link CpfServiceCallTransportException}으로 전달합니다.</p>
 */
public interface CpfServiceCallResponseMetadata {
    /** HTTP 계열 전송이 아니거나 상태가 없는 경우 {@code null}을 반환합니다. */
    Integer httpStatus();
}
