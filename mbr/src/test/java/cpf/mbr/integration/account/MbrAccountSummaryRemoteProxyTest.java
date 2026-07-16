package cpf.mbr.integration.account;

import cpf.cmn.api.account.AccountSummary;
import cpf.pfw.common.http.CpfWebClient;
import cpf.pfw.common.servicecall.ServiceCallRequest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MbrAccountSummaryRemoteProxyTest {
    @Test
    void MBR에서ACC공유실행ID와서비스호출메타를전파한다() {
        CpfWebClient webClient = mock(CpfWebClient.class);
        AccountSummary expected = new AccountSummary(7L, "ACC-7", "계정", "ACTIVE", 1L);
        ArgumentCaptor<ServiceCallRequest> request = ArgumentCaptor.forClass(ServiceCallRequest.class);
        when(webClient.get(request.capture(), eq(AccountSummary.class))).thenReturn(expected);

        AccountSummary actual = new MbrAccountSummaryRemoteProxy(webClient).findSummary(7L);

        assertThat(actual).isEqualTo(expected);
        assertThat(request.getValue().serviceId()).isEqualTo("ACC");
        assertThat(request.getValue().endpointCode()).isEqualTo("ACC_ACCOUNT_SUMMARY");
        assertThat(request.getValue().headers()).containsEntry("X-Cpf-Standard-Execution-Id", "SACCAC0001");
        verify(webClient).get(request.getValue(), AccountSummary.class);
    }
}
