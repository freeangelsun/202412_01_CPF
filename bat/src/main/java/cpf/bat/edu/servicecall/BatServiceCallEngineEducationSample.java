package cpf.bat.edu.servicecall;

import cpf.pfw.common.servicecall.CpfServiceCallEngine;
import cpf.pfw.common.servicecall.ServiceCallRequest;
import cpf.pfw.common.servicecall.ServiceCallResolvedTarget;
import cpf.pfw.common.servicecall.ServiceCallResult;

import java.util.function.Function;

/**
 * BAT가 배치 처리 중 PFW Service Call Engine을 사용하는 교육 샘플입니다.
 */
public class BatServiceCallEngineEducationSample {
    private final CpfServiceCallEngine serviceCallEngine;

    public BatServiceCallEngineEducationSample(CpfServiceCallEngine serviceCallEngine) {
        this.serviceCallEngine = serviceCallEngine;
    }

    public ServiceCallResult<String> callMemberGrade(
            String memberNo,
            Function<ServiceCallResolvedTarget, String> remoteAdapter) {
        ServiceCallRequest request = ServiceCallRequest.builder("MBR")
                .endpointCode("MBR_MEMBER_GRADE")
                .httpMethod("GET")
                .requestPath("/mbr/members/" + memberNo + "/grade")
                .timeoutMillis(5000)
                .retryCount(1)
                .attribute("sourceModuleCode", "BAT")
                .attribute("externalKey", memberNo)
                .build();
        return serviceCallEngine.invoke(request, remoteAdapter);
    }
}
