package com.cpf.batch.runtime.centercut;

import com.cpf.core.api.centercut.CpfCenterCutResult;
import com.cpf.core.api.centercut.CpfCenterCutStatus;
import com.cpf.core.api.centercut.CpfCenterCutTarget;
import com.cpf.core.common.servicecall.CpfServiceCallEngine;
import com.cpf.core.common.servicecall.ServiceCallRequest;
import com.cpf.core.common.servicecall.ServiceCallResult;
import com.cpf.core.spi.centercut.CenterCutHandler;

import java.util.Objects;

/**
 * Generated Domain/분리 WAS Center-Cut을 CPF ServiceCallEngine으로 호출하는 표준 BAT adapter입니다.
 * 내부 호출은 Gateway를 재경유하지 않으며 registry/health/retry/failover/UNKNOWN 처리를 그대로 사용합니다.
 */
public final class BatRemoteCenterCutHandler implements CenterCutHandler {
    private final String serviceId;
    private final String endpointCode;
    private final String requestPath;
    private final CpfServiceCallEngine serviceCallEngine;
    private final BatCenterCutRemoteTransport transport;

    public BatRemoteCenterCutHandler(
            String serviceId,
            String endpointCode,
            String requestPath,
            CpfServiceCallEngine serviceCallEngine,
            BatCenterCutRemoteTransport transport) {
        this.serviceId = requireText(serviceId, "serviceId");
        this.endpointCode = requireText(endpointCode, "endpointCode");
        this.requestPath = requireText(requestPath, "requestPath");
        this.serviceCallEngine = Objects.requireNonNull(serviceCallEngine, "serviceCallEngine");
        this.transport = Objects.requireNonNull(transport, "transport");
    }

    @Override
    public CpfCenterCutResult handle(CpfCenterCutTarget target) {
        Objects.requireNonNull(target, "target");
        BatCenterCutRemoteTransport.BatCenterCutRemoteRequest remoteRequest =
                new BatCenterCutRemoteTransport.BatCenterCutRemoteRequest(
                        target.targetId(),
                        target.centerCutJobId(),
                        target.businessKey(),
                        target.businessDate() == null ? null : target.businessDate().toString(),
                        target.payload(),
                        target.transactionId(),
                        target.parentSegmentId(),
                        target.transactionSegmentId(),
                        target.retryCount());

        ServiceCallRequest.Builder builder = ServiceCallRequest.builder(serviceId)
                .endpointCode(endpointCode)
                .httpMethod("POST")
                .requestPath(requestPath)
                .attribute("centerCutJobId", target.centerCutJobId())
                .attribute("centerCutTargetId", target.targetId())
                .attribute("businessKey", target.businessKey());
        addHeader(builder, "X-Cpf-Transaction-Id", target.transactionId());
        addHeader(builder, "X-Cpf-Parent-Segment-Id", target.parentSegmentId());
        addHeader(builder, "X-Cpf-Transaction-Segment-Id", target.transactionSegmentId());

        ServiceCallResult<String> call = serviceCallEngine.invoke(
                builder.build(),
                resolvedTarget -> transport.exchange(resolvedTarget, remoteRequest));

        if ("SUCCESS".equals(call.status())) {
            return CpfCenterCutResult.success(target, "remote handler success", call.responseBody());
        }
        if ("UNKNOWN".equals(call.status())) {
            // 결과불명은 자동 성공/실패로 확정하지 않는다. 운영 재확인/재조정 대상으로 남긴다.
            return new CpfCenterCutResult(
                    target.targetId(),
                    CpfCenterCutStatus.RETRY_REQUESTED,
                    "UNKNOWN_RESULT: " + safe(call.failureMessage()),
                    null,
                    target.transactionSegmentId());
        }
        return CpfCenterCutResult.failed(
                target,
                "remote handler failed: " + safe(call.failureCode()) + " / " + safe(call.failureMessage()),
                null);
    }

    private static void addHeader(ServiceCallRequest.Builder builder, String name, String value) {
        if (value != null && !value.isBlank()) {
            builder.header(name, value);
        }
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " is required");
        }
        return value;
    }

    private static String safe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
