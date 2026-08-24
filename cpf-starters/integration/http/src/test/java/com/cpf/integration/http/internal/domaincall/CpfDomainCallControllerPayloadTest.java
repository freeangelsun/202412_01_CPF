package com.cpf.integration.http.internal.domaincall;

import com.cpf.core.api.base.CpfRequest;
import com.cpf.core.api.base.CpfResponse;
import com.cpf.core.api.result.CpfResult;
import com.cpf.integration.api.domaincall.CpfDomainOperation;
import com.cpf.integration.api.domaincall.CpfDomainOperationRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CpfDomainCallControllerPayloadTest {
    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void boot4MvcWireBodyIsParsedByTheCpfMapperAndInvokedAsTheTypedRequest() throws Exception {
        CpfDefaultDomainOperationRegistry registry = mock(CpfDefaultDomainOperationRegistry.class);
        CpfDomainInvocationGuard guard = mock(CpfDomainInvocationGuard.class);
        CpfDomainOperation operation = mock(CpfDomainOperation.class);
        var metadata = CpfDomainOperationRegistry.InvocationMetadata.trustedInternal("BAT");
        when(registry.requireOperation("MBR", "MBR_SAMPLE_TX_CREATE")).thenReturn(operation);
        when(operation.requestType()).thenReturn(SampleRequest.class);
        when(operation.responseType()).thenReturn(SampleResponse.class);
        when(guard.verify(any(), eq(operation))).thenReturn(metadata);
        when(registry.invoke(eq(metadata), eq("MBR"), eq("MBR_SAMPLE_TX_CREATE"),
                any(SampleRequest.class), eq(SampleResponse.class)))
                .thenReturn(CpfResult.success(new SampleResponse(true)));
        MockMvc mvc = MockMvcBuilders.standaloneSetup(
                new CpfDomainCallController(registry, new ObjectMapper(), guard)).build();

        mvc.perform(post("/_cpf/domain/MBR/MBR_SAMPLE_TX_CREATE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sampleKey\":\"member-1\"}"))
                .andExpect(status().isOk());

        ArgumentCaptor<SampleRequest> request = ArgumentCaptor.forClass(SampleRequest.class);
        verify(registry).invoke(eq(metadata), eq("MBR"), eq("MBR_SAMPLE_TX_CREATE"),
                request.capture(), eq(SampleResponse.class));
        assertThat(request.getValue().sampleKey()).isEqualTo("member-1");
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void malformedOrNonObjectJsonIsRejectedAsBadRequestBeforeInvocation() throws Exception {
        CpfDefaultDomainOperationRegistry registry = mock(CpfDefaultDomainOperationRegistry.class);
        CpfDomainInvocationGuard guard = mock(CpfDomainInvocationGuard.class);
        CpfDomainOperation operation = mock(CpfDomainOperation.class);
        when(registry.requireOperation("MBR", "OP")).thenReturn(operation);
        when(guard.verify(any(), eq(operation))).thenReturn(
                CpfDomainOperationRegistry.InvocationMetadata.trustedInternal("BAT"));
        MockMvc mvc = MockMvcBuilders.standaloneSetup(
                new CpfDomainCallController(registry, new ObjectMapper(), guard)).build();

        mvc.perform(post("/_cpf/domain/MBR/OP")
                        .contentType(MediaType.APPLICATION_JSON).content("{broken"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/_cpf/domain/MBR/OP")
                        .contentType(MediaType.APPLICATION_JSON).content("[]"))
                .andExpect(status().isBadRequest());
    }

    record SampleRequest(String sampleKey) implements CpfRequest {}
    record SampleResponse(boolean persisted) implements CpfResponse {}
}
