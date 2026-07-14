package cpf.acc.reference.service;

import cpf.cmn.contract.reference.AccMemberExternalFacade;
import cpf.cmn.contract.reference.AccMemberExternalRequest;
import cpf.cmn.contract.reference.AccMemberExternalResponse;
import cpf.cmn.contract.reference.ExternalExchangeRequest;
import cpf.cmn.contract.reference.ExternalExchangeResponse;
import cpf.pfw.common.http.CpfWebClient;
import cpf.pfw.common.logging.TransactionContext;
import cpf.pfw.common.logging.segment.TransactionSegmentContext;
import cpf.pfw.common.servicecall.ServiceCallRequest;
import org.springframework.stereotype.Service;

/**
 * ACC reference domain의 회원 기반 외부연계 공개 파사드 구현입니다.
 *
 * <p>동일 JVM에서는 이 구현체가 Local Facade로 사용되고, 별도 프로세스에서는 같은 계약을 공개하는
 * REST Controller가 사용됩니다. EXS 호출은 항상 PFW Service Call Engine을 경유합니다.</p>
 */
@Service
public class AccMemberExternalFacadeService implements AccMemberExternalFacade {
    private static final String EXS_SERVICE_ID = "exs";
    private static final String EXS_OUTBOUND_PATH = "/api/exs/outbound";

    private final CpfWebClient cpfWebClient;

    public AccMemberExternalFacadeService(CpfWebClient cpfWebClient) {
        this.cpfWebClient = cpfWebClient;
    }

    @Override
    public AccMemberExternalResponse requestExternal(AccMemberExternalRequest request) {
        if (request == null || request.memberId() == null || request.memberId() <= 0) {
            throw new IllegalArgumentException("유효한 회원 ID는 필수입니다.");
        }

        String transactionGlobalId = TransactionContext.getOrCreateTransactionId();
        String externalKey = hasText(request.externalKey())
                ? request.externalKey().trim()
                : transactionGlobalId + "-MEMBER-" + request.memberId();
        String institutionCode = hasText(request.institutionCode())
                ? request.institutionCode().trim()
                : "REFERENCE";

        ServiceCallRequest callRequest = ServiceCallRequest.builder(EXS_SERVICE_ID)
                .endpointCode("EXS_REFERENCE_OUTBOUND")
                .httpMethod("POST")
                .requestPath(EXS_OUTBOUND_PATH)
                .attribute("sourceModuleCode", "ACC")
                .attribute("transactionGlobalId", transactionGlobalId)
                .attribute("externalKey", externalKey)
                .build();

        ExternalExchangeResponse exchange = cpfWebClient.post(
                callRequest,
                new ExternalExchangeRequest(
                        externalKey,
                        institutionCode,
                        "INTERNAL_API",
                        "EXS_REFERENCE_OUTBOUND",
                        "ACC reference domain 회원 외부연계"),
                ExternalExchangeResponse.class);

        return new AccMemberExternalResponse(
                exchange.transactionGlobalId(),
                firstText(exchange.transactionSegmentId(), TransactionSegmentContext.currentSegmentId()),
                request.memberId(),
                exchange.externalTransactionId(),
                exchange.status(),
                "SUCCESS".equals(exchange.status()) ? "ACC_EXS_SUCCESS" : "ACC_EXS_FAILED");
    }

    private String firstText(String first, String second) {
        return hasText(first) ? first : second;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
