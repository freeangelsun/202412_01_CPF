package com.cpf.member.integration.account;

import com.cpf.common.api.account.AccountSummary;
import com.cpf.core.api.http.CpfHttpClient;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MbrAccountSummaryRemoteProxyTest {
    @Test
    void MBR에서ACC공유실행ID와서비스호출메타를전파한다() {
        CpfHttpClient webClient = mock(CpfHttpClient.class);
        AccountSummary expected = new AccountSummary(7L, "ACC-7", "계정", "ACTIVE", 1L);
        String executionId = "SACCAC0001";
        when(webClient.get(eq(executionId), eq("ACC"), any(), eq(AccountSummary.class)))
                .thenReturn(expected);

        AccountSummary actual = new MbrAccountSummaryRemoteProxy(webClient).findSummary(7L);

        assertThat(actual).isEqualTo(expected);
        verify(webClient).get(eq(executionId), eq("ACC"), any(), eq(AccountSummary.class));
    }
}
