package cpf.exs.sample.service;

import cpf.cmn.sec.crypto.CmnCryptoService;
import cpf.cmn.utils.TextUtils;
import cpf.pfw.common.logging.ServerInstanceIdentity;
import cpf.pfw.common.logging.TransactionContext;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * EXS 대외 연계 운영 기본 구현체입니다.
 *
 * <p>외부기관 access token, 통제 정책, 재처리 요청은 사람이 로그인할 때 쓰는 ADM/BIZADM/MBR token과
 * 저장소와 의미가 다릅니다. 이 서비스는 대외 token 원문을 저장하지 않고 hash와 마스킹 표시만 운영 화면에 제공합니다.</p>
 */
@Service
public class ExsOperationService {
    private final CmnCryptoService cryptoService;
    private final Map<String, ExternalTokenState> tokensByProfile = new ConcurrentHashMap<>();
    private final Map<String, ControlPolicyState> policiesByKey = new ConcurrentHashMap<>();
    private final List<Map<String, Object>> retryRequests = new CopyOnWriteArrayList<>();
    private final List<Map<String, Object>> tokenEvents = new CopyOnWriteArrayList<>();

    public ExsOperationService(CmnCryptoService cryptoService) {
        this.cryptoService = cryptoService;
        seedDefaults();
    }

    /**
     * 외부기관 token을 갱신합니다. 원문 token은 응답/저장소에 남기지 않고 hash와 마스킹 값만 사용합니다.
     */
    public Map<String, Object> refreshToken(TokenRefreshRequest request) {
        String authProfileCode = TextUtils.requireText(request.authProfileCode(), "authProfileCode");
        String rawToken = cryptoService.secureRandomToken(48);
        String tokenHash = cryptoService.sha256Base64Url(rawToken);
        Instant issuedAt = Instant.now();
        Instant expireAt = issuedAt.plusSeconds(Math.max(60, request.ttlSeconds() <= 0 ? 3600 : request.ttlSeconds()));
        ExternalTokenState state = new ExternalTokenState(
                authProfileCode,
                TextUtils.defaultIfBlank(request.tokenKey(), "access-token"),
                tokenHash,
                maskedToken(rawToken),
                "VALID",
                issuedAt,
                expireAt,
                request.requestUser(),
                TransactionContext.getOrCreateTransactionId(),
                ServerInstanceIdentity.current().serverInstanceId());
        tokensByProfile.put(authProfileCode, state);
        tokenEvents.add(state.toEvent("TOKEN_REFRESH", request.reason()));
        return state.toResponse();
    }

    /**
     * 외부기관 token 상태를 조회합니다.
     */
    public List<Map<String, Object>> findTokens() {
        return tokensByProfile.values().stream()
                .sorted(Comparator.comparing(ExternalTokenState::authProfileCode))
                .map(ExternalTokenState::toResponse)
                .toList();
    }

    /**
     * token 이벤트 이력을 조회합니다.
     */
    public List<Map<String, Object>> findTokenEvents(int limit) {
        int resolvedLimit = Math.max(1, Math.min(limit, 500));
        return tokenEvents.stream()
                .skip(Math.max(0, tokenEvents.size() - resolvedLimit))
                .toList();
    }

    /**
     * 대외 재처리 요청을 등록합니다.
     */
    public Map<String, Object> requestRetry(RetryRequest request) {
        String transactionGlobalId = TextUtils.requireText(request.transactionGlobalId(), "transactionGlobalId");
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("retryRequestId", "EXS-RETRY-" + (retryRequests.size() + 1));
        row.put("transactionGlobalId", transactionGlobalId);
        row.put("externalTransactionId", request.externalTransactionId());
        row.put("retryStatus", "REQUESTED");
        row.put("retryCount", 0);
        row.put("reason", TextUtils.requireText(request.reason(), "reason"));
        row.put("requestUser", TextUtils.defaultIfBlank(request.requestUser(), "EXS-OPERATOR"));
        row.put("requestedAt", Instant.now());
        row.put("serverInstanceId", ServerInstanceIdentity.current().serverInstanceId());
        retryRequests.add(row);
        return row;
    }

    /**
     * 대외 재처리 요청 이력을 조회합니다.
     */
    public List<Map<String, Object>> findRetryRequests(int limit) {
        int resolvedLimit = Math.max(1, Math.min(limit, 500));
        return retryRequests.stream()
                .skip(Math.max(0, retryRequests.size() - resolvedLimit))
                .toList();
    }

    /**
     * 대외기관 통제 정책을 저장합니다.
     */
    public Map<String, Object> saveControlPolicy(ControlPolicyRequest request) {
        String institutionCode = TextUtils.requireText(request.institutionCode(), "institutionCode");
        String controlType = TextUtils.defaultIfBlank(request.controlType(), "INBOUND");
        ControlPolicyState state = new ControlPolicyState(
                institutionCode,
                controlType,
                TextUtils.defaultIfBlank(request.enabledYn(), "Y"),
                TextUtils.requireText(request.reason(), "reason"),
                TextUtils.defaultIfBlank(request.requestUser(), "EXS-OPERATOR"),
                Instant.now(),
                ServerInstanceIdentity.current().serverInstanceId());
        policiesByKey.put(institutionCode + ":" + controlType, state);
        return state.toResponse();
    }

    public List<Map<String, Object>> findControlPolicies() {
        return policiesByKey.values().stream()
                .sorted(Comparator.comparing(ControlPolicyState::institutionCode).thenComparing(ControlPolicyState::controlType))
                .map(ControlPolicyState::toResponse)
                .toList();
    }

    private void seedDefaults() {
        ControlPolicyState policy = new ControlPolicyState(
                "BANK01",
                "BOTH",
                "Y",
                "기본 대외 연계 허용",
                "SYSTEM",
                Instant.now(),
                ServerInstanceIdentity.current().serverInstanceId());
        policiesByKey.put(policy.institutionCode() + ":" + policy.controlType(), policy);
    }

    private String maskedToken(String rawToken) {
        if (rawToken.length() <= 12) {
            return "***";
        }
        return rawToken.substring(0, 6) + "****" + rawToken.substring(rawToken.length() - 6);
    }

    public record TokenRefreshRequest(
            String authProfileCode,
            String tokenKey,
            long ttlSeconds,
            String reason,
            String requestUser) {
    }

    public record RetryRequest(
            String transactionGlobalId,
            String externalTransactionId,
            String reason,
            String requestUser) {
    }

    public record ControlPolicyRequest(
            String institutionCode,
            String controlType,
            String enabledYn,
            String reason,
            String requestUser) {
    }

    private record ExternalTokenState(
            String authProfileCode,
            String tokenKey,
            String tokenHash,
            String maskedToken,
            String tokenStatus,
            Instant issuedAt,
            Instant expireAt,
            String requestUser,
            String transactionGlobalId,
            String serverInstanceId) {
        private Map<String, Object> toResponse() {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("authProfileCode", authProfileCode);
            response.put("tokenKey", tokenKey);
            response.put("tokenHashPreview", tokenHash.substring(0, Math.min(12, tokenHash.length())) + "...");
            response.put("maskedToken", maskedToken);
            response.put("tokenStatus", tokenStatus);
            response.put("issuedAt", issuedAt);
            response.put("expireAt", expireAt);
            response.put("requestUser", requestUser);
            response.put("transactionGlobalId", transactionGlobalId);
            response.put("serverInstanceId", serverInstanceId);
            return response;
        }

        private Map<String, Object> toEvent(String eventType, String reason) {
            Map<String, Object> event = toResponse();
            event.put("eventType", eventType);
            event.put("reason", reason);
            event.put("eventAt", Instant.now());
            return event;
        }
    }

    private record ControlPolicyState(
            String institutionCode,
            String controlType,
            String enabledYn,
            String reason,
            String requestUser,
            Instant updatedAt,
            String serverInstanceId) {
        private Map<String, Object> toResponse() {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("institutionCode", institutionCode);
            response.put("controlType", controlType);
            response.put("enabledYn", enabledYn);
            response.put("reason", reason);
            response.put("requestUser", requestUser);
            response.put("updatedAt", updatedAt);
            response.put("serverInstanceId", serverInstanceId);
            return response;
        }
    }
}
