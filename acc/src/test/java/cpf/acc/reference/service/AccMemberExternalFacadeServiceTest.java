package cpf.acc.reference.service;

import cpf.cmn.contract.reference.AccMemberExternalRequest;
import cpf.cmn.contract.reference.AccMemberExternalResponse;
import cpf.cmn.contract.reference.ExternalExchangeRequest;
import cpf.cmn.contract.reference.ExternalExchangeResponse;
import cpf.pfw.common.http.CpfWebClient;
import cpf.pfw.common.logging.TransactionContext;
import cpf.pfw.common.servicecall.ServiceCallRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AccMemberExternalFacadeServiceTest {

    private final CpfWebClient cpfWebClient = mock(CpfWebClient.class);
    private final AccMemberExternalFacadeService service = new AccMemberExternalFacadeService(cpfWebClient);

    @AfterEach
    void clearTransactionContext() {
        TransactionContext.clear();
    }

    @Test
    void requestExternalCallsExsThroughStandardServiceCallRequest() {
        TransactionContext.initialize("20260714120000000ACCLOCAL010000001", "trace-acc-reference", null);
        AccMemberExternalRequest request = new AccMemberExternalRequest(7, "BANK01", "ORDER-0007");
        ExternalExchangeResponse exchangeResponse = new ExternalExchangeResponse(
                TransactionContext.currentTransactionId(),
                "SEG-EXS-001",
                "ORDER-0007",
                "OUTBOUND",
                "Y",
                "SUCCESS");
        when(cpfWebClient.post(any(ServiceCallRequest.class), any(ExternalExchangeRequest.class),
                eq(ExternalExchangeResponse.class))).thenReturn(exchangeResponse);

        AccMemberExternalResponse result = service.requestExternal(request);

        ArgumentCaptor<ServiceCallRequest> callCaptor = ArgumentCaptor.forClass(ServiceCallRequest.class);
        ArgumentCaptor<ExternalExchangeRequest> bodyCaptor = ArgumentCaptor.forClass(ExternalExchangeRequest.class);
        verify(cpfWebClient).post(callCaptor.capture(), bodyCaptor.capture(), eq(ExternalExchangeResponse.class));
        assertThat(callCaptor.getValue().serviceId()).isEqualTo("exs");
        assertThat(callCaptor.getValue().requestPath()).isEqualTo("/api/exs/outbound");
        assertThat(callCaptor.getValue().attributes())
                .containsEntry("sourceModuleCode", "ACC")
                .containsEntry("externalKey", "ORDER-0007");
        assertThat(bodyCaptor.getValue().externalTransactionId()).isEqualTo("ORDER-0007");
        assertThat(result.status()).isEqualTo("SUCCESS");
        assertThat(result.transactionSegmentId()).isEqualTo("SEG-EXS-001");
    }

    @Test
    void requestExternalRejectsInvalidMemberIdBeforeRemoteCall() {
        assertThatThrownBy(() -> service.requestExternal(new AccMemberExternalRequest(0, "BANK01", "KEY")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("회원 ID");
    }
}
