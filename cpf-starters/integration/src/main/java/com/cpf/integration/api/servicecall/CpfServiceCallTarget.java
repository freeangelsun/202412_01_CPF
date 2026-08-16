package com.cpf.integration.api.servicecall;

/** 표준 호출 엔진이 선택한 실제 대상 instance 공개 모델입니다. */
public record CpfServiceCallTarget(
        String serviceId, String endpointCode, String instanceId, String baseUrl, boolean failoverEnabled) {
}
