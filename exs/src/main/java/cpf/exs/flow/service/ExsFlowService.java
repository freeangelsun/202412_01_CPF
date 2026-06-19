package cpf.exs.flow.service;

import cpf.cmn.utils.TextUtils;
import cpf.exs.operation.repository.ExsOperationRepository;
import cpf.exs.operation.repository.ExsOperationRepository.ExchangeLogWrite;
import cpf.pfw.common.logging.ServerInstanceIdentity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

/**
 * EXS 대외 수신/송신 표준 흐름 서비스입니다.
 *
 * <p>대외 표준 API는 들어온 <code>X-Transaction-Id</code>를 조용히 생성하지 않습니다. 검증된 거래 ID를
 * 기준으로 거래 로그와 전문 요약을 같은 DB transaction으로 먼저 저장한 뒤 내부 처리를 이어갈 수 있게 합니다.</p>
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
     * 대외 수신 요청을 선저장합니다.
     */
    public Map<String, Object> receiveInbound(String transactionGlobalId, ExchangeRequest request, String requestUri) {
        return saveExchange(transactionGlobalId, request, "INBOUND", "POST", requestUri);
    }

    /**
     * 대외 송신 요청을 선저장합니다.
     */
    public Map<String, Object> sendOutbound(String transactionGlobalId, ExchangeRequest request, String requestUri) {
        return saveExchange(transactionGlobalId, request, "OUTBOUND", "POST", requestUri);
    }

    private Map<String, Object> saveExchange(
            String transactionGlobalId,
            ExchangeRequest request,
            String direction,
            String httpMethod,
            String requestUri) {
        ExchangeRequest resolved = request == null ? new ExchangeRequest(null, null, null, null, null) : request;
        return operationRepository.saveExchangeLog(new ExchangeLogWrite(
                transactionGlobalId,
                TextUtils.defaultIfBlank(resolved.externalTransactionId(), "EXT-" + Instant.now().toEpochMilli()),
                TextUtils.defaultIfBlank(resolved.institutionCode(), "UNKNOWN"),
                TextUtils.defaultIfBlank(resolved.channelCode(), "API"),
                resolved.endpointCode(),
                moduleId,
                wasId,
                ServerInstanceIdentity.current().serverInstanceId(),
                Instant.now(),
                direction,
                httpMethod,
                requestUri,
                "Y",
                summarize(resolved.messageSummary())));
    }

    private String summarize(String messageSummary) {
        String value = TextUtils.defaultIfBlank(messageSummary, "대외 요청 선저장");
        return value.length() <= 1000 ? value : value.substring(0, 1000);
    }

    public record ExchangeRequest(
            String externalTransactionId,
            String institutionCode,
            String channelCode,
            String endpointCode,
            String messageSummary) {
    }
}
