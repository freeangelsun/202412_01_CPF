package com.cpf.member.integration.account;

import com.cpf.common.api.account.AccountSummary;
import com.cpf.common.api.account.AccountSummaryFacade;
import com.cpf.core.api.http.CpfHttpClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * MBR가 ACC 저장소를 직접 참조하지 않고 CPF Service Call Engine을 경유하도록 하는 Remote Facade Proxy입니다.
 */
@Component
@ConditionalOnProperty(prefix = "cpf.mbr.account", name = "mode", havingValue = "remote", matchIfMissing = true)
public class MbrAccountSummaryRemoteProxy implements AccountSummaryFacade {
    private static final String STANDARD_EXECUTION_ID = "SACCAC0001";
    private final CpfHttpClient webClient;

    public MbrAccountSummaryRemoteProxy(CpfHttpClient webClient) {
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
