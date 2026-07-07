package cpf.exs.operation.service;

import cpf.cmn.sec.crypto.CmnCryptoService;
import cpf.cmn.utils.TextUtils;
import cpf.exs.operation.repository.ExsOperationRepository;
import cpf.exs.operation.repository.ExsOperationRepository.ControlPolicyWrite;
import cpf.exs.operation.repository.ExsOperationRepository.ExchangeLogWrite;
import cpf.exs.operation.repository.ExsOperationRepository.RetryWrite;
import cpf.exs.operation.repository.ExsOperationRepository.TokenEventWrite;
import cpf.exs.operation.repository.ExsOperationRepository.TokenWrite;
import cpf.pfw.common.logging.SensitiveDataMasker;
import cpf.pfw.common.logging.ServerInstanceIdentity;
import cpf.pfw.common.logging.TransactionContext;
import cpf.pfw.common.logging.file.CpfFileLogWriter;
import cpf.pfw.common.logging.segment.TransactionSegmentContext;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * EXS 외부연계 운영 서비스입니다.
 *
 * <p>외부기관 token, 통제 정책, 재처리 요청, 송수신 추적 로그를 exsDB 원장에 저장합니다.
 * token 원문은 저장하거나 반환하지 않고 hash와 마스킹 값만 운영 화면에 제공합니다.</p>
 */
@Service
public class ExsOperationService {
    private final CmnCryptoService cryptoService;
    private final ExsOperationRepository operationRepository;
    private final ObjectProvider<CpfFileLogWriter> fileLogWriterProvider;

    @Autowired
    public ExsOperationService(
            CmnCryptoService cryptoService,
            ExsOperationRepository operationRepository,
            ObjectProvider<CpfFileLogWriter> fileLogWriterProvider) {
        this.cryptoService = cryptoService;
        this.operationRepository = operationRepository;
        this.fileLogWriterProvider = fileLogWriterProvider;
    }

    public ExsOperationService(
            CmnCryptoService cryptoService,
            ExsOperationRepository operationRepository) {
        this(cryptoService, operationRepository, null);
    }

    public List<Map<String, Object>> findInstitutions() {
        return operationRepository.findInstitutions();
    }

    public List<Map<String, Object>> findChannels() {
        return operationRepository.findChannels();
    }

    public List<Map<String, Object>> findEndpoints() {
        return operationRepository.findEndpoints();
    }

    public List<Map<String, Object>> findAuthProfiles() {
        return operationRepository.findAuthProfiles();
    }

    public List<Map<String, Object>> findRoutes() {
        return operationRepository.findRoutes();
    }

    public List<Map<String, Object>> findTransactions(int limit) {
        return operationRepository.findTransactions(boundedLimit(limit));
    }

    public List<Map<String, Object>> findMessages(int limit) {
        return operationRepository.findMessages(boundedLimit(limit));
    }

    /**
     * 인증기관 token을 갱신하고 token hash, 마스킹 값, 운영 이벤트를 DB에 저장합니다.
     */
    public Map<String, Object> refreshToken(TokenRefreshRequest request) {
        String authProfileCode = TextUtils.requireText(request.authProfileCode(), "authProfileCode");
        String tokenKey = TextUtils.defaultIfBlank(request.tokenKey(), "access-token");
        String reason = TextUtils.requireText(request.reason(), "reason");
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
                reason,
                transactionGlobalId,
                serverInstanceId,
                requestUser));
        return tokenResponse(token);
    }

    public List<Map<String, Object>> findTokens() {
        return operationRepository.findTokens();
    }

    public List<Map<String, Object>> findTokenEvents(int limit) {
        return operationRepository.findTokenEvents(boundedLimit(limit));
    }

    public Map<String, Object> requestRetry(RetryRequest request) {
        return operationRepository.insertRetry(new RetryWrite(
                TextUtils.requireText(request.transactionGlobalId(), "transactionGlobalId"),
                request.externalTransactionId(),
                TextUtils.requireText(request.reason(), "reason"),
                TextUtils.defaultIfBlank(request.requestUser(), "EXS-OPERATOR")));
    }

    public List<Map<String, Object>> findRetryRequests(int limit) {
        return operationRepository.findRetryRequests(boundedLimit(limit));
    }

    public Map<String, Object> saveControlPolicy(ControlPolicyRequest request) {
        return operationRepository.upsertControlPolicy(new ControlPolicyWrite(
                TextUtils.requireText(request.institutionCode(), "institutionCode"),
                TextUtils.defaultIfBlank(request.controlType(), "INBOUND"),
                TextUtils.defaultIfBlank(request.enabledYn(), "Y"),
                TextUtils.requireText(request.reason(), "reason"),
                TextUtils.defaultIfBlank(request.requestUser(), "EXS-OPERATOR"),
                ServerInstanceIdentity.current().serverInstanceId()));
    }

    public List<Map<String, Object>> findControlPolicies() {
        return operationRepository.findControlPolicies();
    }

    /**
     * 외부 수신 전문을 CPF 거래 ID와 외부 거래 ID 기준으로 사전 적재합니다.
     */
    public Map<String, Object> receiveInbound(String transactionGlobalId, Map<String, Object> payload) {
        return saveExchangeLog(transactionGlobalId, payload, "INBOUND", "POST", "/api/exs/inbound", "N",
                "SUCCESS", "EXS_SUCCESS", 200, null, null);
    }

    /**
     * 외부 송신 전문을 CPF 거래 ID와 외부 거래 ID 기준으로 사전 적재합니다.
     */
    public Map<String, Object> sendOutbound(String transactionGlobalId, Map<String, Object> payload) {
        return saveExchangeLog(transactionGlobalId, payload, "OUTBOUND", "POST", "/api/exs/outbound", "Y",
                "SUCCESS", "EXS_SUCCESS", 200, null, null);
    }

    /**
     * 실패 송신 로그를 원장에 남기고 호출자에게 실패 응답을 돌려주는 샘플에서 사용합니다.
     */
    public Map<String, Object> sendOutboundFailure(String transactionGlobalId, Map<String, Object> payload) {
        Map<String, Object> body = payload == null ? Map.of() : payload;
        return saveExchangeLog(transactionGlobalId, body, "OUTBOUND", "POST", "/api/exs/outbound/failure", "Y",
                "FAILED", "EXS_FAILURE", 502,
                text(body, "failureCode", "EXS_TIMEOUT"),
                text(body, "failureMessage", "외부기관 응답 지연"));
    }

    private Map<String, Object> saveExchangeLog(
            String transactionGlobalId,
            Map<String, Object> payload,
            String direction,
            String httpMethod,
            String requestUri,
            String retryableYn,
            String status,
            String resultCode,
            Integer httpStatus,
            String failureCode,
            String failureMessage) {
        Map<String, Object> body = payload == null ? Map.of() : payload;
        String resolvedTransactionId = TextUtils.defaultIfBlank(transactionGlobalId, TransactionContext.getOrCreateTransactionId());
        String externalTransactionId = text(body, "externalTransactionId", "EXT-" + direction + "-" + Instant.now().toEpochMilli());
        Map<String, Object> saved = operationRepository.saveExchangeLog(new ExchangeLogWrite(
                resolvedTransactionId,
                TransactionSegmentContext.currentSegmentId(),
                externalTransactionId,
                text(body, "institutionCode", "BANK01"),
                text(body, "channelCode", "OPENAPI"),
                text(body, "endpointCode", direction.equals("INBOUND") ? "BANK01_INBOUND" : "BANK01_BALANCE"),
                requestUri,
                "EXS",
                text(body, "wasId", "exsAP01"),
                ServerInstanceIdentity.current().serverInstanceId(),
                Instant.now(),
                direction,
                httpMethod,
                requestUri,
                "{}",
                "{}",
                SensitiveDataMasker.mask(body.toString(), 2000),
                SensitiveDataMasker.mask(Map.of("externalTransactionId", externalTransactionId, "status", status).toString(), 2000),
                TextUtils.defaultIfBlank(status, "PRE_SAVED"),
                TextUtils.defaultIfBlank(resultCode, "EXS_PRE_SAVED"),
                httpStatus,
                SensitiveDataMasker.truncate(failureCode, 100),
                SensitiveDataMasker.mask(failureMessage, 1000),
                retryableYn,
                intValue(body, "timeoutMs", 3000),
                intValue(body, "retryCount", 0),
                text(body, "messageCode", "BALANCE_REQ"),
                SensitiveDataMasker.mask(text(body, "messageSummary", direction + " 전문 사전 적재"), 1000)));
        writeExchangeFileLog(body, direction, httpMethod, requestUri, status, resultCode, httpStatus, failureCode, failureMessage, externalTransactionId);
        return saved;
    }

    private void writeExchangeFileLog(
            Map<String, Object> body,
            String direction,
            String httpMethod,
            String requestUri,
            String status,
            String resultCode,
            Integer httpStatus,
            String failureCode,
            String failureMessage,
            String externalTransactionId) {

        CpfFileLogWriter writer = fileLogWriterProvider == null ? null : fileLogWriterProvider.getIfAvailable();
        if (writer == null) {
            return;
        }
        writer.writeIntegration(
                "EXS",
                "EXS",
                direction,
                httpMethod,
                requestUri,
                httpStatus,
                status,
                null,
                failureCode,
                failureMessage,
                Map.of(
                        "institutionCode", text(body, "institutionCode", "BANK01"),
                        "endpointCode", text(body, "endpointCode", "BANK01_BALANCE"),
                        "externalTransactionId", externalTransactionId,
                        "responseCode", resultCode,
                        "retryCount", intValue(body, "retryCount", 0),
                        "timeoutMs", intValue(body, "timeoutMs", 3000),
                        "timeoutYn", failureCode != null && failureCode.toUpperCase().contains("TIMEOUT") ? "Y" : "N",
                        "requestPayloadMasked", SensitiveDataMasker.mask(body.toString(), 2000),
                        "responsePayloadMasked", SensitiveDataMasker.mask(Map.of("externalTransactionId", externalTransactionId, "status", status).toString(), 2000)));
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

    private int boundedLimit(int limit) {
        return Math.max(1, Math.min(limit, 500));
    }

    private String maskedToken(String rawToken) {
        if (rawToken.length() <= 12) {
            return "***";
        }
        return rawToken.substring(0, 6) + "****" + rawToken.substring(rawToken.length() - 6);
    }

    private String text(Map<String, Object> payload, String key, String fallback) {
        Object value = payload.get(key);
        if (value == null || String.valueOf(value).isBlank()) {
            return fallback;
        }
        return String.valueOf(value);
    }

    private Integer intValue(Map<String, Object> payload, String key, int fallback) {
        Object value = payload.get(key);
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value == null || String.valueOf(value).isBlank()) {
            return fallback;
        }
        try {
            return Integer.parseInt(String.valueOf(value).trim());
        } catch (NumberFormatException ex) {
            return fallback;
        }
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
