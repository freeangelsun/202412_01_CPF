package cpf.mbr.reference.service;

import cpf.cmn.contract.reference.AccMemberExternalFacade;
import cpf.cmn.contract.reference.AccMemberExternalRequest;
import cpf.cmn.contract.reference.AccMemberExternalResponse;
import cpf.pfw.common.http.CpfWebClient;
import cpf.pfw.common.logging.TransactionContext;
import cpf.pfw.common.logging.segment.TransactionSegmentService;
import cpf.pfw.common.servicecall.ServiceCallRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
class MbrAccReferenceClientTest {

    private final ObjectProvider<AccMemberExternalFacade> provider = mock(ObjectProvider.class);
    private final CpfWebClient cpfWebClient = mock(CpfWebClient.class);
    private final TransactionSegmentService segmentService = mock(TransactionSegmentService.class);

    @AfterEach
    void clearTransactionContext() {
        TransactionContext.clear();
    }

    @Test
    void autoModeUsesLocalFacadeWhenImplementationExists() {
        AccMemberExternalRequest request = new AccMemberExternalRequest(1, "BANK01", "LOCAL-1");
        AccMemberExternalResponse expected = response("LOCAL-SEG");
        AccMemberExternalFacade localFacade = mock(AccMemberExternalFacade.class);
        when(provider.getIfAvailable()).thenReturn(localFacade);
        when(localFacade.requestExternal(request)).thenReturn(expected);
        when(segmentService.around(any(), any(), anyString(), anyString(), anyString(), anyString(), anyString(), any()))
                .thenAnswer(invocation -> ((Supplier<?>) invocation.getArgument(7)).get());
        MbrAccReferenceClient client = new MbrAccReferenceClient(
                provider, cpfWebClient, segmentService, "AUTO");

        AccMemberExternalResponse actual = client.requestExternal(request);

        assertThat(actual).isEqualTo(expected);
        verify(localFacade).requestExternal(request);
        verify(cpfWebClient, never()).post(any(ServiceCallRequest.class), any(), eq(AccMemberExternalResponse.class));
    }

    @Test
    void autoModeUsesRemoteProxyWhenLocalFacadeDoesNotExist() {
        TransactionContext.initialize("20260714120000000MBRLOCAL010000001", "trace-mbr-reference", null);
        AccMemberExternalRequest request = new AccMemberExternalRequest(2, "BANK02", "REMOTE-2");
        AccMemberExternalResponse expected = response("REMOTE-SEG");
        when(provider.getIfAvailable()).thenReturn(null);
        when(cpfWebClient.post(any(ServiceCallRequest.class), eq(request), eq(AccMemberExternalResponse.class)))
                .thenReturn(expected);
        MbrAccReferenceClient client = new MbrAccReferenceClient(
                provider, cpfWebClient, segmentService, "AUTO");

        AccMemberExternalResponse actual = client.requestExternal(request);

        ArgumentCaptor<ServiceCallRequest> callCaptor = ArgumentCaptor.forClass(ServiceCallRequest.class);
        verify(cpfWebClient).post(callCaptor.capture(), eq(request), eq(AccMemberExternalResponse.class));
        assertThat(actual).isEqualTo(expected);
        assertThat(callCaptor.getValue().serviceId()).isEqualTo("acc");
        assertThat(callCaptor.getValue().requestPath()).isEqualTo("/api/v1/acc/reference/member-external");
        assertThat(callCaptor.getValue().attributes())
                .containsEntry("sourceModuleCode", "MBR")
                .containsEntry("externalKey", "REMOTE-2");
    }

    @Test
    void localModeFailsFastWhenFacadeIsMissing() {
        when(provider.getIfAvailable()).thenReturn(null);
        MbrAccReferenceClient client = new MbrAccReferenceClient(
                provider, cpfWebClient, segmentService, "LOCAL");

        assertThatThrownBy(() -> client.requestExternal(new AccMemberExternalRequest(3, null, null)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ACC 파사드");
    }

    private AccMemberExternalResponse response(String segmentId) {
        return new AccMemberExternalResponse(
                "20260714120000000MBRLOCAL010000001",
                segmentId,
                1,
                "EXTERNAL-1",
                "SUCCESS",
                "ACC_EXS_SUCCESS");
    }
}
