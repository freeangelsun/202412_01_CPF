package cpf.acc.bse.service;

import cpf.pfw.common.http.CpfWebClient;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * ACC 샘플에서 EXS 대외연계 교육 API를 호출하는 client입니다.
 */
@Service
@RequiredArgsConstructor
public class ExsExchangeClientService {
    private static final String EXS_SERVICE_ID = "exs";

    private final CpfWebClient cpfWebClient;

    public Map<String, Object> requestExternalTransfer(Map<String, Object> request) {
        return cpfWebClient.post(
                EXS_SERVICE_ID,
                "/api/exs/edu/external-transfer",
                request,
                new ParameterizedTypeReference<Map<String, Object>>() {
                });
    }

    public Map<String, Object> requestExternalTransferFailure(Map<String, Object> request) {
        return cpfWebClient.post(
                EXS_SERVICE_ID,
                "/api/exs/edu/external-transfer/failure",
                request,
                new ParameterizedTypeReference<Map<String, Object>>() {
                });
    }
}
