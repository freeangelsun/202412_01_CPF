package cpf.xyz.edu.servicecall;

import cpf.pfw.common.servicecall.CpfServiceCallEngine;
import cpf.pfw.common.servicecall.ServiceCallRequest;
import cpf.pfw.common.servicecall.ServiceCallResolvedTarget;
import cpf.pfw.common.servicecall.ServiceCallResult;

import java.util.function.Function;

/**
 * XYZ가 PFW Service Call Engine으로 타 주제영역을 호출하는 교육 샘플입니다.
 *
 * <p>샘플 내부에서 후보 인스턴스를 만들지 않습니다. 레지스트리 조회, retry, failover, circuit 및
 * 시도별 segment 기록은 실제 엔진에 위임하고, 전송 어댑터만 호출 함수로 전달합니다.</p>
 */
public class XyzServiceCallEngineEducationSample {
    private final CpfServiceCallEngine serviceCallEngine;

    public XyzServiceCallEngineEducationSample(CpfServiceCallEngine serviceCallEngine) {
        this.serviceCallEngine = serviceCallEngine;
    }

    public ServiceCallResult<String> callAccountSummary(
            String accountNo,
            Function<ServiceCallResolvedTarget, String> remoteAdapter) {
        ServiceCallRequest request = ServiceCallRequest.builder("ACC")
                .endpointCode("ACC_ACCOUNT_SUMMARY")
                .httpMethod("GET")
                .requestPath("/accounts/" + accountNo)
                .timeoutMillis(3000)
                .retryCount(2)
                .attribute("sourceModuleCode", "XYZ")
                .attribute("externalKey", accountNo)
                .build();
        return serviceCallEngine.invoke(request, remoteAdapter);
    }
}
