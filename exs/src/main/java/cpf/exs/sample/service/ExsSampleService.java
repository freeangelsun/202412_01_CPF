package cpf.exs.sample.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * 대외 연계 샘플 데이터를 제공합니다.
 *
 * <p>실제 프로젝트에서는 이 계층에 기관 enabled 판단, 인증 프로파일 해석, 선저장 로그, 재처리 정책,
 * 외부 호출 어댑터를 연결합니다. 샘플은 CPF 표준 필드와 주제영역 경계를 보여주는 데 집중합니다.</p>
 */
@Service
public class ExsSampleService {

    public List<Map<String, Object>> findInstitutions() {
        return List.of(Map.of("institutionCode", "BANK001", "institutionName", "샘플 은행", "enabledYn", "Y"));
    }

    public List<Map<String, Object>> findChannels() {
        return List.of(Map.of("channelCode", "BANK_API", "institutionCode", "BANK001", "direction", "BOTH"));
    }

    public List<Map<String, Object>> findEndpoints() {
        return List.of(Map.of("endpointId", "BANK001_BALANCE", "method", "POST", "timeoutMs", 3000));
    }

    public List<Map<String, Object>> findAuthProfiles() {
        return List.of(Map.of("authProfileId", "BANK001_MOCK_OAUTH", "authType", "MOCK_OAUTH", "secretRef", "ENV:EXS_BANK001_SECRET"));
    }

    public List<Map<String, Object>> findTokens() {
        return List.of(Map.of("tokenId", "BANK001_ACCESS", "status", "MOCK", "expireAt", "local-only"));
    }

    public List<Map<String, Object>> findRoutes() {
        return List.of(Map.of("routeId", "BALANCE_ROUTE", "institutionCode", "BANK001", "endpointId", "BANK001_BALANCE"));
    }

    public List<Map<String, Object>> findTransactions() {
        return List.of(Map.of(
                "transactionGlobalId", "20260615120000000EXSexsAP010000001",
                "externalTransactionId", "EXT-20260615-0001",
                "status", "SUCCESS"));
    }

    public List<Map<String, Object>> findMessages() {
        return List.of(Map.of("messageId", "MSG-0001", "direction", "INBOUND", "maskedPayload", "{\"accountNo\":\"12****90\"}"));
    }

    public List<Map<String, Object>> findControlPolicies() {
        return List.of(Map.of("policyId", "BANK001_ENABLED", "enabledYn", "Y", "reason", "정상 운영"));
    }

    public List<Map<String, Object>> findRetries() {
        return List.of(Map.of("retryId", "RETRY-0001", "retryableYn", "Y", "lastError", "timeout"));
    }

    public Map<String, Object> receiveInbound(String transactionGlobalId, Map<String, Object> payload) {
        return Map.of(
                "transactionGlobalId", transactionGlobalId,
                "externalTransactionId", value(payload, "externalTransactionId", "EXT-IN-" + Instant.now().toEpochMilli()),
                "preSavedYn", "Y",
                "status", "RECEIVED");
    }

    public Map<String, Object> sendOutbound(String transactionGlobalId, Map<String, Object> payload) {
        return Map.of(
                "transactionGlobalId", transactionGlobalId,
                "externalTransactionId", value(payload, "externalTransactionId", "EXT-OUT-" + Instant.now().toEpochMilli()),
                "propagatedHeader", "X-Transaction-Id",
                "status", "MOCK_SENT");
    }

    private String value(Map<String, Object> payload, String key, String fallback) {
        if (payload == null || payload.get(key) == null || String.valueOf(payload.get(key)).isBlank()) {
            return fallback;
        }
        return String.valueOf(payload.get(key));
    }
}
