package com.cpf.core.common.transaction;

import com.cpf.core.api.error.CpfValidationException;
import com.cpf.core.common.header.CpfHeaderNames;
import com.cpf.core.common.idempotency.CpfIdempotencyPort;
import com.cpf.core.common.idempotency.CpfIdempotencyRecord;
import com.cpf.core.common.logging.TransactionIdGenerator;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class CpfInboundTransactionIdPolicyTest {
    private static final String TX = "20260808120000000ADMchannel0000001";

    @Test
    void officialTrustedChannelStartPreservesItsCanonicalTransactionId() {
        CpfIdempotencyPort replay = mock(CpfIdempotencyPort.class);
        when(replay.find("CPF_TRANSACTION_START", TX)).thenReturn(Optional.empty());
        when(replay.reserve(any())).thenReturn(true);
        CpfInboundTransactionIdPolicy policy = new CpfInboundTransactionIdPolicy(replay);
        HttpServletRequest request = request(TX, "MOBILE", "ADM", "CHANNEL", "REQ-1", true);

        var resolved = policy.resolve(request, generator());

        assertThat(resolved.transactionId()).isEqualTo(TX);
        assertThat(resolved.propagated()).isTrue();
        assertThat(resolved.transactionStart()).isTrue();
        verify(replay).reserve(any(CpfIdempotencyRecord.class));
    }

    @Test
    void trustedInternalHopPreservesOriginalTransactionIdWithoutCreatingNewStartClaim() {
        CpfIdempotencyPort replay = mock(CpfIdempotencyPort.class);
        CpfInboundTransactionIdPolicy policy = new CpfInboundTransactionIdPolicy(replay);
        HttpServletRequest request = request(TX, "MOBILE", "CMN", "INTERNAL", "REQ-1", true);
        when(request.getHeader(CpfHeaderNames.CALLER_SERVICE)).thenReturn("cpf-common");

        var resolved = policy.resolve(request, generator());

        assertThat(resolved.transactionId()).isEqualTo(TX);
        assertThat(resolved.transactionStart()).isFalse();
        verifyNoInteractions(replay);
    }

    @Test
    void unauthenticatedPublicClientCannotInjectAValidInternalTransactionId() {
        CpfInboundTransactionIdPolicy policy = new CpfInboundTransactionIdPolicy(mock(CpfIdempotencyPort.class));
        HttpServletRequest request = request(TX, "MOBILE", null, "PUBLIC", "REQ-X", false);

        assertThatThrownBy(() -> policy.resolve(request, generator()))
                .isInstanceOf(CpfValidationException.class)
                .hasMessageContaining("비신뢰");
    }

    @Test
    void originSystemMismatchIsRejectedEvenWhenTransportIsTrusted() {
        CpfInboundTransactionIdPolicy policy = new CpfInboundTransactionIdPolicy(mock(CpfIdempotencyPort.class));
        HttpServletRequest request = request(TX, "MOBILE", "BZA", "CHANNEL", "REQ-2", true);

        assertThatThrownBy(() -> policy.resolve(request, generator()))
                .isInstanceOf(CpfValidationException.class)
                .hasMessageContaining("SystemCode");
    }

    @Test
    void differentStartRequestCannotReplayAnExistingTransactionId() {
        CpfIdempotencyPort replay = mock(CpfIdempotencyPort.class);
        when(replay.find("CPF_TRANSACTION_START", TX)).thenReturn(Optional.of(new CpfIdempotencyRecord(
                "CPF_TRANSACTION_START", TX, "different-request-hash", "payload", "COMPLETED", null,
                false, Instant.now(), Instant.now(), Instant.now().plusSeconds(3600))));
        CpfInboundTransactionIdPolicy policy = new CpfInboundTransactionIdPolicy(replay);
        HttpServletRequest request = request(TX, "WEB", "ADM", "CHANNEL", "REQ-NEW", true);

        assertThatThrownBy(() -> policy.resolve(request, generator()))
                .isInstanceOf(CpfValidationException.class)
                .hasMessageContaining("replay");
        verify(replay, never()).reserve(any());
    }

    @Test
    void noInboundIdCreatesNewLocalTransactionId() {
        CpfInboundTransactionIdPolicy policy = new CpfInboundTransactionIdPolicy((CpfIdempotencyPort) null);
        HttpServletRequest request = mock(HttpServletRequest.class);

        var resolved = policy.resolve(request, generator());

        assertThat(resolved.transactionId()).hasSize(34).startsWith("20260808120000000ADM");
        assertThat(resolved.propagated()).isFalse();
    }

    @Test
    void trustedChannelMetadataAloneCannotAssertSystemProvenance() {
        CpfInboundTransactionIdPolicy policy = new CpfInboundTransactionIdPolicy(mock(CpfIdempotencyPort.class));
        HttpServletRequest request = request(TX, "MOBILE", null, "CHANNEL", "REQ-META", true);

        assertThatThrownBy(() -> policy.resolve(request, generator()))
                .isInstanceOf(CpfValidationException.class)
                .hasMessageContaining("System provenance");
    }

    private static HttpServletRequest request(String transactionId, String channelCode, String authenticatedSystem,
            String ingress, String requestId, boolean trusted) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(CpfHeaderNames.TRANSACTION_ID)).thenReturn(transactionId);
        when(request.getHeader(CpfHeaderNames.ORIGINAL_CHANNEL_CODE)).thenReturn(channelCode);
        when(request.getHeader(CpfHeaderNames.CHANNEL_CODE)).thenReturn(channelCode);
        when(request.getHeader(CpfHeaderNames.INGRESS_TYPE)).thenReturn(ingress);
        when(request.getHeader(CpfHeaderNames.REQUEST_ID)).thenReturn(requestId);
        when(request.getAttribute(CpfInboundTransactionIdPolicy.TRUSTED_CONTEXT_ATTRIBUTE))
                .thenReturn(trusted ? Boolean.TRUE : null);
        when(request.getAttribute(CpfInboundTransactionIdPolicy.AUTHENTICATED_SYSTEM_CODE_ATTRIBUTE))
                .thenReturn(authenticatedSystem);
        return request;
    }

    private static TransactionIdGenerator generator() {
        return new TransactionIdGenerator("ADM", "channel", 7,
                Clock.fixed(Instant.parse("2026-08-08T12:00:00Z"), ZoneOffset.UTC));
    }
}
