package com.cpf.core.common.servicecall;

import com.cpf.core.api.servicecall.CpfServiceCallCommand;
import com.cpf.core.api.servicecall.CpfServiceCallExecutor;
import com.cpf.core.api.servicecall.CpfServiceCallOutcome;
import com.cpf.core.api.servicecall.CpfServiceCallTarget;
import java.util.function.Function;

/** Core internal ServiceCallEngine을 공개 API 모델로 변환하는 유일한 adapter입니다. */
public final class CpfServiceCallExecutorAdapter implements CpfServiceCallExecutor {
    private final CpfServiceCallEngine engine;
    public CpfServiceCallExecutorAdapter(CpfServiceCallEngine engine){this.engine=engine;}
    @Override
    public <T> CpfServiceCallOutcome<T> invoke(CpfServiceCallCommand command, Function<CpfServiceCallTarget,T> remoteCall){
        ServiceCallRequest request=ServiceCallRequest.builder(command.serviceId())
                .httpMethod(command.httpMethod()).requestPath(command.requestPath())
                .timeoutMillis(command.timeoutMillis()).retryCount(command.retryCount()).build();
        request=new ServiceCallRequest(request.serviceId(),request.endpointCode(),request.instanceId(),request.httpMethod(),
                request.requestPath(),request.timeoutMillis(),request.retryCount(),command.headers(),command.attributes());
        ServiceCallResult<T> result=engine.invoke(request,target->remoteCall.apply(toPublic(target)));
        return new CpfServiceCallOutcome<>(result.status(),toPublic(result.target()),result.responseBody(),result.httpStatus(),
                result.durationMillis(),result.attemptCount(),result.failureCode(),result.failureMessage());
    }
    private CpfServiceCallTarget toPublic(ServiceCallResolvedTarget target){
        if(target==null)return null;
        return new CpfServiceCallTarget(target.serviceId(),target.endpointCode(),target.instanceId(),target.baseUrl(),target.failoverEnabled());
    }
}
