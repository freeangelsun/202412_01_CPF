package cpf.exs.flow.service;

import cpf.cmn.contract.reference.ExternalExchangeRequest;
import cpf.cmn.contract.reference.ExternalExchangeResponse;
import cpf.cmn.utils.TextUtils;
import cpf.exs.operation.repository.ExsOperationRepository;
import cpf.exs.operation.repository.ExsOperationRepository.ExchangeLogWrite;
import cpf.pfw.common.logging.SensitiveDataMasker;
import cpf.pfw.common.logging.ServerInstanceIdentity;
import cpf.pfw.common.logging.segment.TransactionSegmentContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

/**
 * EXS 외부 수신/송신 표준 흐름 서비스입니다.
 *
 * <p>외부 표준 API로 들어온 거래 ID를 조작하지 않고, 검증된 transactionGlobalId와
 * 현재 transactionSegmentId 기준으로 EXS 송수신 원장을 먼저 적재합니다.</p>
 */
@Service
public class ExsFlowService {
    private final ExsOperationRepository operationRepository;
    private final String moduleId;
    private final String wasId;

    public ExsFlowService(
            ExsOperationRepository operationRepository,
            @Value("${cpf.framework.module-id:EXS}") String moduleId,
            @Value("${cpf.framework.was-id:exsAP01}") String wasId) {
        this.operationRepository = operationRepository;
        this.moduleId = moduleId;
        this.wasId = wasId;
    }

    /**
     * 외부 수신 요청을 원장에 저장합니다.
     */
    public ExternalExchangeResponse receiveInbound(
            String transactionGlobalId,
            ExternalExchangeRequest request,
            String requestUri) {
        return saveExchange(transactionGlobalId, request, "INBOUND", "POST", requestUri);
    }

    /**
     * 외부 송신 요청을 원장에 저장합니다.
     */
    public ExternalExchangeResponse sendOutbound(
            String transactionGlobalId,
            ExternalExchangeRequest request,
            String requestUri) {
        return saveExchange(transactionGlobalId, request, "OUTBOUND", "POST", requestUri);
    }

    private ExternalExchangeResponse saveExchange(
            String transactionGlobalId,
            ExternalExchangeRequest request,
            String direction,
            String httpMethod,
            String requestUri) {
        ExternalExchangeRequest resolved = request == null
                ? new ExternalExchangeRequest(null, null, null, null, null)
                : request;
        String externalTransactionId = TextUtils.defaultIfBlank(resolved.externalTransactionId(), "EXT-" + Instant.now().toEpochMilli());
        String messageSummary = summarize(resolved.messageSummary());
        Map<String, Object> saved = operationRepository.saveExchangeLog(new ExchangeLogWrite(
                transactionGlobalId,
                TransactionSegmentContext.currentSegmentId(),
                externalTransactionId,
                TextUtils.defaultIfBlank(resolved.institutionCode(), "UNKNOWN"),
                TextUtils.defaultIfBlank(resolved.channelCode(), "API"),
                resolved.endpointCode(),
                requestUri,
                moduleId,
                wasId,
                ServerInstanceIdentity.current().serverInstanceId(),
                Instant.now(),
                direction,
                httpMethod,
                requestUri,
                "{}",
                "{}",
                SensitiveDataMasker.mask(messageSummary, 2000),
                SensitiveDataMasker.mask(Map.of("externalTransactionId", externalTransactionId, "status", "SUCCESS").toString(), 2000),
                "SUCCESS",
                "EXS_SUCCESS",
                200,
                null,
                null,
                "Y",
                3000,
                0,
                TextUtils.defaultIfBlank(resolved.endpointCode(), "EXS_MESSAGE"),
                SensitiveDataMasker.mask(messageSummary, 1000)));
        return new ExternalExchangeResponse(
                String.valueOf(saved.get("transactionGlobalId")),
                value(saved.get("transactionSegmentId")),
                String.valueOf(saved.get("externalTransactionId")),
                String.valueOf(saved.get("direction")),
                String.valueOf(saved.get("preSavedYn")),
                String.valueOf(saved.get("status")));
    }

    private String summarize(String messageSummary) {
        String value = TextUtils.defaultIfBlank(messageSummary, "외부 요청 원장 저장");
        return value.length() <= 1000 ? value : value.substring(0, 1000);
    }

    private String value(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
