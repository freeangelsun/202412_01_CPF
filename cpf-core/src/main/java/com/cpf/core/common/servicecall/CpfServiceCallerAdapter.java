package com.cpf.core.common.servicecall;

import com.cpf.core.api.servicecall.CpfServiceCaller;
import com.cpf.core.api.servicecall.CpfServiceRequest;
import com.cpf.core.api.servicecall.CpfServiceResult;
import com.cpf.core.api.servicecall.CpfServiceTarget;
import com.cpf.core.api.servicecall.CpfServiceTransport;

import java.util.Objects;

/** Public ServiceCall API를 기존 Core 엔진에 연결하는 내부 adapter입니다. */
public final class CpfServiceCallerAdapter implements CpfServiceCaller {
    private final CpfServiceCallEngine engine;
    public CpfServiceCallerAdapter(CpfServiceCallEngine engine){ this.engine = Objects.requireNonNull(engine,"engine"); }
    @Override
    public <T> CpfServiceResult<T> invoke(CpfServiceRequest request, CpfServiceTransport<T> transport) {
        Objects.requireNonNull(request,"request"); Objects.requireNonNull(transport,"transport");
        ServiceCallRequest internal = new ServiceCallRequest(
                request.serviceId(), request.endpointCode(), request.instanceId(), request.httpMethod(), request.requestPath(),
                request.timeoutMillis(), request.retryCount(), request.headers(), request.attributes());
        ServiceCallResult<T> result = engine.invoke(internal, target -> transport.exchange(toPublic(target)));
        return new CpfServiceResult<>(result.status(), toPublic(result.target()), result.responseBody(), result.httpStatus(),
                result.durationMillis(), result.attemptCount(), result.failureCode(), result.failureMessage());
    }
    private static CpfServiceTarget toPublic(ServiceCallResolvedTarget target) {
        if (target == null) return null;
        return new CpfServiceTarget(target.service(), target.endpoint(), target.instance(), target.routingPolicy(), target.baseUrl(), target.routingMode());
    }
}
