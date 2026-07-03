package cpf.acc.bse.service;

import cpf.pfw.common.http.CpfWebClient;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * ACC 샘플에서 EXS 외부연계 mock API를 호출하는 client입니다.
 */
@Service
@RequiredArgsConstructor
public class ExsExchangeClientService {
    private static final String EXS_SERVICE_ID = "exs";

    private final CpfWebClient cpfWebClient;

    public Map<String, Object> requestExternalTransfer(Map<String, Object> request) {
        return cpfWebClient.service(EXS_SERVICE_ID)
                .post()
                .uri("/api/exs/edu/external-transfer")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .block();
    }
}
