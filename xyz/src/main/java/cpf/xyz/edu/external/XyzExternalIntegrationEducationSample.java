package cpf.xyz.edu.external;

import cpf.pfw.common.http.CpfWebClient;
import cpf.pfw.common.servicecall.ServiceCallRequest;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 외부 호출을 PFW Service Call Engine 경계로 실행하는 EDU 샘플입니다.
 *
 * <p>timeout과 retry 횟수, 외부 업무키를 표준 요청에 넣어 시도별 segment와 unknown 결과를
 * ADM에서 같은 transactionGlobalId로 추적할 수 있게 합니다.</p>
 */
@Component
public class XyzExternalIntegrationEducationSample {
    private final CpfWebClient webClient;

    public XyzExternalIntegrationEducationSample(CpfWebClient webClient) {
        this.webClient = webClient;
    }

    public Map<String, Object> call(String externalKey, int status, long delayMillis) {
        String path = "/xyz/edu/external-simulator/response?status=" + status
                + "&delayMillis=" + delayMillis + "&externalKey=" + externalKey;
        ServiceCallRequest request = ServiceCallRequest.builder("XYZ-EXTERNAL-SIMULATOR")
                .endpointCode("NEUTRAL_EXTERNAL_RESPONSE")
                .httpMethod("GET")
                .requestPath(path)
                .timeoutMillis(1000)
                .retryCount(1)
                .attribute("sourceModuleCode", "XYZ")
                .attribute("externalKey", externalKey)
                .build();
        @SuppressWarnings("unchecked")
        Map<String, Object> result = webClient.get(request, Map.class);
        return result;
    }
}
