package cpf.mbr.bse.service;

import cpf.pfw.common.http.CpfWebClient;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * MBR 복합 거래 샘플에서 EXS 외부연계 mock API를 호출하는 client입니다.
 */
@Service
@RequiredArgsConstructor
public class MbrExsClientService {
    private static final String EXS_SERVICE_ID = "exs";

    private final CpfWebClient cpfWebClient;

    public Map<String, Object> requestExternalVerification(Integer memberId) {
        return cpfWebClient.post(
                EXS_SERVICE_ID,
                "/api/exs/edu/external-transfer",
                Map.of(
                        "memberId", memberId,
                        "institutionCode", "BANK01",
                        "externalTransactionId", "EXT-MBR-" + System.currentTimeMillis()),
                new ParameterizedTypeReference<Map<String, Object>>() {
                });
    }
}
