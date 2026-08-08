package com.cpf.core.common.filter;

import com.cpf.core.common.header.CpfHeaderNames;
import com.cpf.core.common.idempotency.CpfIdempotencyPort;
import com.cpf.core.common.idempotency.CpfIdempotencyRecord;
import com.cpf.core.common.logging.TransactionContext;
import com.cpf.core.common.logging.TransactionIdGenerator;
import com.cpf.core.common.transaction.CpfInboundTransactionIdPolicy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.security.Principal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/** 인증된 ingress identity가 transactionId 정책보다 먼저 소비되는 실제 필터 체인 계약을 검증합니다. */
class CpfAuthenticatedSystemIdentityFilterIntegrationTest {
    private final MemoryIdempotencyPort replay = new MemoryIdempotencyPort();
    private final TransactionIdGenerator generator = new TransactionIdGenerator(
            "ADM", "local01", 7, Clock.fixed(Instant.parse("2026-08-08T00:00:00Z"), ZoneOffset.UTC));
    private final CpfInboundTransactionIdPolicy policy = new CpfInboundTransactionIdPolicy(replay);

    @AfterEach void clearContext() { TransactionContext.clear(); }

    @Test
    void authenticatedChannelStartPreservesOfficialTransactionIdThroughConsumer() throws Exception {
        String tx = generator.generate("ADM", "local01");
        MockEnvironment env = new MockEnvironment().withProperty("cpf.security.authenticated-system-map.gateway-principal", "ADM");
        CpfAuthenticatedSystemIdentityFilter identity = new CpfAuthenticatedSystemIdentityFilter(env);
        TransactionContextFilter context = new TransactionContextFilter(generator, policy);
        MockHttpServletRequest request = channelRequest(tx, "request-001");
        request.setUserPrincipal(named("gateway-principal")); request.setRemoteAddr("203.0.113.10");
        MockHttpServletResponse response = new MockHttpServletResponse(); AtomicReference<String> seen = new AtomicReference<>();
        identity.doFilter(request, response, (r, s) -> context.doFilter(r, s, (rr, ss) -> seen.set(TransactionContext.getOrCreateTransactionId())));
        assertThat(response.getStatus()).isEqualTo(200); assertThat(seen.get()).isEqualTo(tx);
        assertThat(response.getHeader(CpfHeaderNames.TRANSACTION_ID)).isEqualTo(tx);
        assertThat(request.getAttribute(CpfInboundTransactionIdPolicy.AUTHENTICATED_SYSTEM_CODE_ATTRIBUTE)).isEqualTo("ADM");
    }

    @Test
    void sameOfficialRequestRetryKeepsTransactionIdAndDoesNotCreateSecondClaim() throws Exception {
        String tx = generator.generate("ADM", "local01");
        MockEnvironment env = new MockEnvironment().withProperty("cpf.security.authenticated-system-map.gateway-principal", "ADM");
        CpfAuthenticatedSystemIdentityFilter identity = new CpfAuthenticatedSystemIdentityFilter(env);
        TransactionContextFilter context = new TransactionContextFilter(generator, policy);
        for (int attempt = 1; attempt <= 2; attempt++) {
            MockHttpServletRequest request = channelRequest(tx, "retry-key-001"); request.setUserPrincipal(named("gateway-principal")); request.setRemoteAddr("203.0.113.10");
            MockHttpServletResponse response = new MockHttpServletResponse(); AtomicReference<String> seen = new AtomicReference<>();
            identity.doFilter(request, response, (r, s) -> context.doFilter(r, s, (rr, ss) -> seen.set(TransactionContext.getOrCreateTransactionId())));
            assertThat(response.getStatus()).isEqualTo(200); assertThat(seen.get()).isEqualTo(tx);
        }
        assertThat(replay.reserveCount).isEqualTo(1);
    }

    @Test
    void authenticatedInternalPropagationPreservesOriginTransactionIdEvenWhenCallerDiffers() throws Exception {
        String tx = generator.generate("ADM", "local01");
        MockEnvironment env = new MockEnvironment().withProperty("cpf.security.authenticated-system-map.biz-service", "BIZ");
        CpfAuthenticatedSystemIdentityFilter identity = new CpfAuthenticatedSystemIdentityFilter(env);
        TransactionContextFilter context = new TransactionContextFilter(generator, policy);
        MockHttpServletRequest request = new MockHttpServletRequest(); request.addHeader(CpfHeaderNames.TRANSACTION_ID, tx); request.addHeader(CpfHeaderNames.INGRESS_TYPE, "INTERNAL"); request.addHeader(CpfHeaderNames.CALLER_SERVICE, "BIZ"); request.setUserPrincipal(named("biz-service")); request.setRemoteAddr("203.0.113.11");
        MockHttpServletResponse response = new MockHttpServletResponse(); AtomicReference<String> seen = new AtomicReference<>();
        identity.doFilter(request, response, (r, s) -> context.doFilter(r, s, (rr, ss) -> seen.set(TransactionContext.getOrCreateTransactionId())));
        assertThat(response.getStatus()).isEqualTo(200); assertThat(seen.get()).isEqualTo(tx);
    }

    @Test
    void rawClientIdentityHeadersCannotSpoofOfficialStarter() throws Exception {
        String tx = generator.generate("ADM", "local01");
        CpfAuthenticatedSystemIdentityFilter identity = new CpfAuthenticatedSystemIdentityFilter(new MockEnvironment());
        TransactionContextFilter context = new TransactionContextFilter(generator, policy);
        MockHttpServletRequest request = channelRequest(tx, "request-spoof"); request.addHeader(CpfHeaderNames.CALLER_SERVICE, "ADM"); request.addHeader(CpfHeaderNames.GATEWAY_INSTANCE_ID, "gw-spoof"); request.setRemoteAddr("203.0.113.99");
        MockHttpServletResponse response = new MockHttpServletResponse(); AtomicReference<String> seen = new AtomicReference<>();
        identity.doFilter(request, response, (r, s) -> context.doFilter(r, s, (rr, ss) -> seen.set(TransactionContext.getOrCreateTransactionId())));
        assertThat(response.getStatus()).isEqualTo(400); assertThat(seen.get()).isNull();
        assertThat(request.getAttribute(CpfInboundTransactionIdPolicy.AUTHENTICATED_SYSTEM_CODE_ATTRIBUTE)).isNull();
    }

    @Test
    void alteredOriginSystemIsRejectedAfterServerAuthentication() throws Exception {
        String tx = generator.generate("ADM", "local01");
        MockEnvironment env = new MockEnvironment().withProperty("cpf.security.authenticated-system-map.gateway-principal", "BIZ");
        CpfAuthenticatedSystemIdentityFilter identity = new CpfAuthenticatedSystemIdentityFilter(env);
        TransactionContextFilter context = new TransactionContextFilter(generator, policy);
        MockHttpServletRequest request = channelRequest(tx, "request-altered"); request.setUserPrincipal(named("gateway-principal")); request.setRemoteAddr("203.0.113.10");
        MockHttpServletResponse response = new MockHttpServletResponse();
        identity.doFilter(request, response, (r, s) -> context.doFilter(r, s, (rr, ss) -> {}));
        assertThat(response.getStatus()).isEqualTo(400);
    }

    private static MockHttpServletRequest channelRequest(String tx, String requestId) {
        MockHttpServletRequest request = new MockHttpServletRequest(); request.addHeader(CpfHeaderNames.TRANSACTION_ID, tx); request.addHeader(CpfHeaderNames.INGRESS_TYPE, "CHANNEL"); request.addHeader(CpfHeaderNames.CHANNEL_CODE, "WEB"); request.addHeader(CpfHeaderNames.REQUEST_ID, requestId); return request;
    }
    private static Principal named(String name) { return () -> name; }

    private static final class MemoryIdempotencyPort implements CpfIdempotencyPort {
        private final Map<String, CpfIdempotencyRecord> rows = new ConcurrentHashMap<>(); private int reserveCount;
        @Override public boolean reserve(CpfIdempotencyRecord record) { boolean inserted = rows.putIfAbsent(record.scope() + '|' + record.idempotencyKey(), record) == null; if (inserted) reserveCount++; return inserted; }
        @Override public Optional<CpfIdempotencyRecord> find(String scope, String idempotencyKey) { return Optional.ofNullable(rows.get(scope + '|' + idempotencyKey)); }
        @Override public void complete(String scope, String idempotencyKey, String status, String storedResponse, boolean retryAllowed) { }
        @Override public boolean restart(String scope, String idempotencyKey, String requestHash, String payloadHash, Instant now, Instant expiresAt) { return false; }
        @Override public void expire(String scope, String idempotencyKey) { }
        @Override public int expireBefore(Instant now, int limit) { return 0; }
    }
}
