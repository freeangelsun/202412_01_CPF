package com.cpf.member.integration.account;

import com.cpf.common.api.account.AccountSummary;
import com.cpf.common.api.account.AccountSummaryFacade;
import com.cpf.core.common.execution.CpfStandardExecutionId;
import com.cpf.core.common.http.CpfWebClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * MBR가 ACC 저장소를 직접 참조하지 않고 CPF Service Call Engine을 경유하도록 하는 Remote Facade Proxy입니다.
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
