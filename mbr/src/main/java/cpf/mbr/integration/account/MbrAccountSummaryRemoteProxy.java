package cpf.mbr.integration.account;

import cpf.cmn.api.account.AccountSummary;
import cpf.cmn.api.account.AccountSummaryFacade;
import cpf.pfw.common.header.CpfHeaderNames;
import cpf.pfw.common.http.CpfWebClient;
import cpf.pfw.common.servicecall.ServiceCallRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * MBR가 ACC 저장소를 직접 참조하지 않고 PFW Service Call Engine을 경유하도록 하는 Remote Facade Proxy입니다.
 */
@Component
@ConditionalOnProperty(prefix = "cpf.mbr.account", name = "mode", havingValue = "remote", matchIfMissing = true)
public class MbrAccountSummaryRemoteProxy implements AccountSummaryFacade {
    private static final String STANDARD_EXECUTION_ID = "SACCAC0001";
    private final CpfWebClient webClient;

    public MbrAccountSummaryRemoteProxy(CpfWebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public AccountSummary findSummary(long accountId) {
        ServiceCallRequest request = ServiceCallRequest.builder("ACC")
                .endpointCode("ACC_ACCOUNT_SUMMARY")
                .httpMethod("GET")
                .requestPath("/internal/api/v1/accounts/" + accountId + "/summary")
                .timeoutMillis(3000)
                .retryCount(1)
                .header(CpfHeaderNames.STANDARD_EXECUTION_ID, STANDARD_EXECUTION_ID)
                .attribute("sourceModuleCode", "MBR")
                .attribute("standardExecutionId", STANDARD_EXECUTION_ID)
                .attribute("externalKey", String.valueOf(accountId))
                .build();
        return webClient.get(request, AccountSummary.class);
    }
}
