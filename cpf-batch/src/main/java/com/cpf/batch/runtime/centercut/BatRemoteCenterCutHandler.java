package com.cpf.batch.runtime.centercut;

import com.cpf.core.api.centercut.CpfCenterCutResult;
import com.cpf.core.api.centercut.CpfCenterCutStatus;
import com.cpf.core.api.centercut.CpfCenterCutTarget;
import com.cpf.core.api.servicecall.CpfServiceCaller;
import com.cpf.core.api.servicecall.CpfServiceRequest;
import com.cpf.core.api.servicecall.CpfServiceResult;
import com.cpf.core.spi.centercut.CenterCutHandler;

import java.util.Objects;

/**
 * Generated Domain/분리 WAS Center-Cut을 CPF 공개 ServiceCall API로 호출하는 BAT 표준 adapter입니다.
 * 내부 호출은 Gateway를 재경유하지 않으며 registry/health/retry/failover/UNKNOWN 정책을 그대로 사용합니다.
 */
public final class BatRemoteCenterCutHandler implements CenterCutHandler {
    private final String serviceId;
    private final String endpointCode;
    private final String requestPath;
    private final CpfServiceCaller serviceCaller;
    private final BatCenterCutRemoteTransport transport;

    public BatRemoteCenterCutHandler(
            String serviceId,
            String endpointCode,
            String requestPath,
            CpfServiceCaller serviceCaller,
            BatCenterCutRemoteTransport transport) {
        this.serviceId = requireText(serviceId, "serviceId");
        this.endpointCode = requireText(endpointCode, "endpointCode");
        this.requestPath = requireText(requestPath, "requestPath");
        this.serviceCaller = Objects.requireNonNull(serviceCaller, "serviceCaller");
        this.transport = Objects.requireNonNull(transport, "transport");
    }

    @Override
    public CpfCenterCutResult handle(CpfCenterCutTarget target) {
        Objects.requireNonNull(target, "target");
        BatCenterCutRemoteTransport.BatCenterCutRemoteRequest remoteRequest =
                new BatCenterCutRemoteTransport.BatCenterCutRemoteRequest(
                        requestPath,
                        target.targetId(),
                        target.centerCutJobId(),
                        target.businessKey(),
                        target.businessDate() == null ? null : target.businessDate().toString(),
                        target.payload(),
                        target.transactionId(),
                        target.parentSegmentId(),
                        target.transactionSegmentId(),
                        target.retryCount());

        CpfServiceRequest.Builder builder = CpfServiceRequest.builder(serviceId)
                .endpointCode(endpointCode)
                .httpMethod("POST")
                .requestPath(requestPath)
                .attribute("centerCutJobId", target.centerCutJobId())
                .attribute("centerCutTargetId", target.targetId())
                .attribute("businessKey", target.businessKey());
        addHeader(builder, "X-Cpf-Transaction-Id", target.transactionId());
        addHeader(builder, "X-Cpf-Parent-Segment-Id", target.parentSegmentId());
        addHeader(builder, "X-Cpf-Transaction-Segment-Id", target.transactionSegmentId());

        CpfServiceResult<String> call = serviceCaller.invoke(
                builder.build(),
                resolvedTarget -> transport.exchange(resolvedTarget, remoteRequest));

        if (call.success()) {
            return CpfCenterCutResult.success(target, "remote handler success", call.responseBody());
        }
        if (call.unknown()) {
            return new CpfCenterCutResult(
                    target.targetId(),
                    CpfCenterCutStatus.UNKNOWN_RESULT,
                    "UNKNOWN_RESULT: " + safe(call.failureMessage()),
                    null,
                    target.transactionSegmentId());
        }
        return CpfCenterCutResult.failed(
                target,
                "remote handler failed: " + safe(call.failureCode()) + " / " + safe(call.failureMessage()),
                null);
    }

    private static void addHeader(CpfServiceRequest.Builder builder, String name, String value) {
        if (value != null && !value.isBlank()) builder.header(name, value);
    }
    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " is required");
        return value;
    }
    private static String safe(String value) { return value == null || value.isBlank() ? "-" : value; }
}
