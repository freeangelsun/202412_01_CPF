package cpf.pfw.common.servicecall;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 업무 Remote Facade Proxy가 공통 서비스 호출 엔진을 사용하도록 돕는 기반 클래스입니다.
 */
public class CpfRemoteFacadeProxySupport {
    private final CpfServiceCallEngine serviceCallEngine;

    public CpfRemoteFacadeProxySupport(CpfServiceCallEngine serviceCallEngine) {
        this.serviceCallEngine = serviceCallEngine;
    }

    protected ServiceCallResolvedTarget resolve(ServiceCallRequest request) {
        return serviceCallEngine.resolve(request);
    }

    protected <T> ServiceCallResult<T> call(ServiceCallRequest request, Supplier<T> remoteCall) {
        return serviceCallEngine.invoke(request, remoteCall);
    }

    protected <T> ServiceCallResult<T> call(
            ServiceCallRequest request,
            Function<ServiceCallResolvedTarget, T> remoteCall) {
        return serviceCallEngine.invoke(request, remoteCall);
    }
}
