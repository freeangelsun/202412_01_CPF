package cpf.mbr.integration.account;

import cpf.cmn.api.account.AccountSummary;
import cpf.cmn.api.account.AccountSummaryFacade;
import cpf.pfw.common.execution.CpfStandardExecutionId;
import cpf.pfw.common.http.CpfWebClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * MBR가 ACC 저장소를 직접 참조하지 않고 PFW Service Call Engine을 경유하도록 하는 Remote Facade Proxy입니다.
 */
@Component
@ConditionalOnProperty(prefix = "cpf.mbr.account", name = "mode", havingValue = "remote", matchIfMissing = true)
public class MbrAccountSummaryRemoteProxy implements AccountSummaryFacade {
    private static final CpfStandardExecutionId STANDARD_EXECUTION_ID =
            CpfStandardExecutionId.parse("SACCAC0001");
    private final CpfWebClient webClient;

    public MbrAccountSummaryRemoteProxy(CpfWebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public AccountSummary findSummary(long accountId) {
        return webClient.get(
                STANDARD_EXECUTION_ID,
                "ACC",
                uri -> uri.path("/internal/api/v1/accounts/{accountId}/summary").build(accountId),
                AccountSummary.class);
    }
}
