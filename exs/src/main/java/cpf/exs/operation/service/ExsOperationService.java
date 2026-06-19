package cpf.exs.operation.service;

import cpf.cmn.sec.crypto.CmnCryptoService;
import cpf.cmn.utils.TextUtils;
import cpf.exs.operation.repository.ExsOperationRepository;
import cpf.exs.operation.repository.ExsOperationRepository.ControlPolicyWrite;
import cpf.exs.operation.repository.ExsOperationRepository.RetryWrite;
import cpf.exs.operation.repository.ExsOperationRepository.TokenEventWrite;
import cpf.exs.operation.repository.ExsOperationRepository.TokenWrite;
import cpf.pfw.common.logging.ServerInstanceIdentity;
import cpf.pfw.common.logging.TransactionContext;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * EXS 대외 연계 운영 서비스입니다.
 *
 * <p>대외 token, 통제 정책, 재처리 요청은 사람이 로그인할 때 쓰는 ADM/BIZADM/MBR token과 분리합니다.
 * 모든 운영 데이터는 exsDB 저장소를 기준으로 남기며 원문 token은 저장하지 않습니다.</p>
 */
@Service
public class ExsOperationService {
    private final CmnCryptoService cryptoService;
    private final ExsOperationRepository operationRepository;

    public ExsOperationService(CmnCryptoService cryptoService, ExsOperationRepository operationRepository) {
        this.cryptoService = cryptoService;
        this.operationRepository = operationRepository;
    }

    /**
     * 외부기관 token을 갱신하고 hash/마스킹 값과 이벤트 이력을 DB에 저장합니다.
     */
    public Map<String, Object> refreshToken(TokenRefreshRequest request) {
        String authProfileCode = TextUtils.requireText(request.authProfileCode(), "authProfileCode");
        String tokenKey = TextUtils.defaultIfBlank(request.tokenKey(), "access-token");
        String rawToken = cryptoService.secureRandomToken(48);
        String tokenHash = cryptoService.sha256Base64Url(rawToken);
        Instant issuedAt = Instant.now();
        Instant expireAt = issuedAt.plusSeconds(Math.max(60, request.ttlSeconds() <= 0 ? 3600 : request.ttlSeconds()));
        String transactionGlobalId = TransactionContext.getOrCreateTransactionId();
        String serverInstanceId = ServerInstanceIdentity.current().serverInstanceId();
        String requestUser = TextUtils.defaultIfBlank(request.requestUser(), "EXS-OPERATOR");

        TokenWrite token = new TokenWrite(
                authProfileCode,
                tokenKey,
                tokenHash,
                maskedToken(rawToken),
                "VALID",
                issuedAt,
                expireAt,
                transactionGlobalId,
                serverInstanceId,
                requestUser);
        operationRepository.upsertToken(token);
        operationRepository.insertTokenEvent(new TokenEventWrite(
                authProfileCode,
                tokenKey,
                "TOKEN_REFRESH",
                TextUtils.requireText(request.reason(), "reason"),
                transactionGlobalId,
                serverInstanceId,
                requestUser));
        return tokenResponse(token);
    }

    /**
     * 외부기관 token 상태를 DB에서 조회합니다.
     */
    public List<Map<String, Object>> findTokens() {
        return operationRepository.findTokens();
    }

    /**
     * token 이벤트 이력을 DB에서 조회합니다.
     */
    public List<Map<String, Object>> findTokenEvents(int limit) {
        return operationRepository.findTokenEvents(Math.max(1, Math.min(limit, 500)));
    }

    /**
     * 대외 재처리 요청을 DB에 저장합니다.
     */
    public Map<String, Object> requestRetry(RetryRequest request) {
        return operationRepository.insertRetry(new RetryWrite(
                TextUtils.requireText(request.transactionGlobalId(), "transactionGlobalId"),
                request.externalTransactionId(),
                TextUtils.requireText(request.reason(), "reason"),
                TextUtils.defaultIfBlank(request.requestUser(), "EXS-OPERATOR")));
    }

    /**
     * 대외 재처리 요청 이력을 DB에서 조회합니다.
     */
    public List<Map<String, Object>> findRetryRequests(int limit) {
        return operationRepository.findRetryRequests(Math.max(1, Math.min(limit, 500)));
    }

    /**
     * 대외기관 통제 정책을 DB에 저장합니다.
     */
    public Map<String, Object> saveControlPolicy(ControlPolicyRequest request) {
        return operationRepository.upsertControlPolicy(new ControlPolicyWrite(
                TextUtils.requireText(request.institutionCode(), "institutionCode"),
                TextUtils.defaultIfBlank(request.controlType(), "INBOUND"),
                TextUtils.defaultIfBlank(request.enabledYn(), "Y"),
                TextUtils.requireText(request.reason(), "reason"),
                TextUtils.defaultIfBlank(request.requestUser(), "EXS-OPERATOR"),
                ServerInstanceIdentity.current().serverInstanceId()));
    }

    /**
     * 대외기관 통제 정책을 DB에서 조회합니다.
     */
    public List<Map<String, Object>> findControlPolicies() {
        return operationRepository.findControlPolicies();
    }

    private Map<String, Object> tokenResponse(TokenWrite token) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("authProfileCode", token.authProfileCode());
        response.put("tokenKey", token.tokenKey());
        response.put("tokenHashPreview", token.tokenHash().substring(0, Math.min(12, token.tokenHash().length())) + "...");
        response.put("maskedToken", token.maskedToken());
        response.put("tokenStatus", token.tokenStatus());
        response.put("issuedAt", token.issuedAt());
        response.put("expireAt", token.expireAt());
        response.put("transactionGlobalId", token.transactionGlobalId());
        response.put("serverInstanceId", token.serverInstanceId());
        response.put("requestUser", token.requestUser());
        return response;
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
}
