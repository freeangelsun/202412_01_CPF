package cpf.mbr.reference.service;

import cpf.cmn.contract.reference.AccMemberExternalFacade;
import cpf.cmn.contract.reference.AccMemberExternalRequest;
import cpf.cmn.contract.reference.AccMemberExternalResponse;
import cpf.pfw.common.http.CpfWebClient;
import cpf.pfw.common.logging.TransactionContext;
import cpf.pfw.common.logging.segment.TransactionSegmentDirection;
import cpf.pfw.common.logging.segment.TransactionSegmentRole;
import cpf.pfw.common.logging.segment.TransactionSegmentService;
import cpf.pfw.common.servicecall.ServiceCallRequest;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * MBR에서 ACC 공개 계약을 Local Facade 또는 Remote Proxy 방식으로 호출하는 표준 클라이언트입니다.
 *
 * <p>AUTO는 동일 JVM에 ACC 파사드 구현이 있으면 LOCAL을 사용하고, 없으면 REMOTE를 사용합니다.
 * 업무 코드는 ACC 구현 패키지나 저장소에 의존하지 않습니다.</p>
 */
@Service
public class MbrAccReferenceClient {
    private static final String ACC_SERVICE_ID = "acc";
    private static final String ACC_REFERENCE_PATH = "/api/v1/acc/reference/member-external";

    private final ObjectProvider<AccMemberExternalFacade> localFacadeProvider;
    private final CpfWebClient cpfWebClient;
    private final TransactionSegmentService segmentService;
    private final CallMode configuredMode;

    public MbrAccReferenceClient(
            ObjectProvider<AccMemberExternalFacade> localFacadeProvider,
            CpfWebClient cpfWebClient,
            TransactionSegmentService segmentService,
            @Value("${cpf.mbr.reference.acc-call-mode:AUTO}") String configuredMode) {
        this.localFacadeProvider = localFacadeProvider;
        this.cpfWebClient = cpfWebClient;
        this.segmentService = segmentService;
        this.configuredMode = CallMode.valueOf(configuredMode.trim().toUpperCase(Locale.ROOT));
    }

    public AccMemberExternalResponse requestExternal(AccMemberExternalRequest request) {
        AccMemberExternalFacade localFacade = localFacadeProvider.getIfAvailable();
        CallMode effectiveMode = configuredMode == CallMode.AUTO
                ? (localFacade == null ? CallMode.REMOTE : CallMode.LOCAL)
                : configuredMode;
        if (effectiveMode == CallMode.LOCAL) {
            if (localFacade == null) {
                throw new IllegalStateException("LOCAL 호출 모드에 필요한 ACC 파사드 구현이 없습니다.");
            }
            return segmentService.around(
                    TransactionSegmentRole.SUB,
                    TransactionSegmentDirection.INTERNAL,
                    "MBR",
                    "MBR",
                    "ACC",
                    ACC_REFERENCE_PATH,
                    "MBR에서 ACC Local Facade 호출",
                    () -> localFacade.requestExternal(request));
        }
        return remote(request);
    }

    private AccMemberExternalResponse remote(AccMemberExternalRequest request) {
        String transactionGlobalId = TransactionContext.getOrCreateTransactionId();
        ServiceCallRequest callRequest = ServiceCallRequest.builder(ACC_SERVICE_ID)
                .endpointCode("ACC_MEMBER_EXTERNAL_REFERENCE")
                .httpMethod("POST")
                .requestPath(ACC_REFERENCE_PATH)
                .attribute("sourceModuleCode", "MBR")
                .attribute("transactionGlobalId", transactionGlobalId)
                .attribute("externalKey", request.externalKey())
                .build();
        return cpfWebClient.post(callRequest, request, AccMemberExternalResponse.class);
    }

    public enum CallMode {
        AUTO,
        LOCAL,
        REMOTE
    }
}
