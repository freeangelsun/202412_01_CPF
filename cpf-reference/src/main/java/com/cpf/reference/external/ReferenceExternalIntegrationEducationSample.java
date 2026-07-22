package cpf.xyz.external;

import cpf.pfw.common.http.CpfWebClient;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 외부 장애 시뮬레이터를 PFW Service Call Engine 경계로 실행하는 고급 샘플입니다.
 *
 * <p>업무 입력은 시뮬레이션 조건만 전달하며 timeout과 retry는 중앙 endpoint 정책을 사용합니다.</p>
 */
@Component
public class XyzExternalIntegrationEducationSample {
    private final CpfWebClient webClient;

    public XyzExternalIntegrationEducationSample(CpfWebClient webClient) {
        this.webClient = webClient;
    }

    public Map<String, Object> call(String externalKey, int status, long delayMillis) {
        @SuppressWarnings("unchecked")
        Map<String, Object> result = webClient.get(
                "XYZ-EXTERNAL-SIMULATOR",
                uri -> uri.path("/api/xyz/reference/external-simulator/response")
                        .queryParam("status", status)
                        .queryParam("delayMillis", delayMillis)
                        .queryParam("externalKey", externalKey)
                        .build(),
                Map.class);
        return result;
    }
}
