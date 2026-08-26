package com.cpf.integration.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.cpf.integration.ai.api.*;
import com.cpf.core.api.context.CpfContext;
import com.cpf.core.api.context.CpfContextSnapshot;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.foundation.execution.CpfContextExecutionFactory;
import com.cpf.foundation.id.spi.CpfExecutionIdGenerator;
import com.cpf.foundation.id.spi.CpfTransactionIdGenerator;
import com.cpf.foundation.time.spi.CpfBusinessDateProvider;
import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class CpfAiRouterTest {
    private static final String TX = "20260810010101001AI0local010000001";
    @Test void routesAuditsAndPreservesTransactionLineage() throws Exception {
        var audit = new AtomicInteger();
        CpfAiPolicy policy = new CpfAiPolicy() {
            public CpfAiRequest authorizeAndMask(CpfAiRequest r) { return r; }
            public void audit(CpfAiRequest r, CpfAiResponse s, Throwable f) { audit.incrementAndGet(); }
        };
        withContext(TX, factory -> {
            try (var router = new CpfAiRouter(List.of(provider("p1", true)), policy, new CpfAiProperties(), factory)) {
                var out = router.execute(req(false));
                assertThat(out.provider()).isEqualTo("p1");
                assertThat(audit).hasValue(1);
                assertThat(CpfContexts.transactionId()).isEqualTo(TX);
            }
        });
    }

    @Test void timeoutWithUnsafeFallbackIsUnknown() throws Exception {
        CpfAiProvider p = new CpfAiProvider() {
            public String providerId() { return "slow"; }
            public boolean supports(String m) { return true; }
            public boolean safeToFallbackAfterTimeout() { return false; }
            public CpfAiResponse execute(CpfAiRequest r) throws Exception { Thread.sleep(100); return response("slow"); }
        };
        var props = new CpfAiProperties(); props.setTimeout(Duration.ofMillis(10));
        withContext(TX, factory -> {
            try (var router = new CpfAiRouter(List.of(p), noopPolicy(), props, factory)) {
                assertThatThrownBy(() -> router.execute(req(false))).isInstanceOf(com.cpf.integration.ai.api.CpfAiUnknownResultException.class);
            }
        });
    }

    @Test void highRiskRequiresApproval() throws Exception {
        withContext(TX, factory -> {
            try (var router = new CpfAiRouter(List.of(provider("p", true)), noopPolicy(), new CpfAiProperties(), factory)) {
                assertThatThrownBy(() -> router.execute(req(true))).isInstanceOf(SecurityException.class);
            }
        });
    }

    private static void withContext(String transactionId, CheckedConsumer<CpfContextExecutionFactory> body) throws Exception {
        AtomicInteger ids = new AtomicInteger();
        CpfTransactionIdGenerator txIds = () -> transactionId;
        CpfExecutionIdGenerator execIds = new CpfExecutionIdGenerator() {
            public String newExecutionId() { return "EX-" + ids.incrementAndGet(); }
            public String newSegmentId() { return "SG-" + ids.incrementAndGet(); }
        };
        CpfBusinessDateProvider dates = () -> LocalDate.of(2026, 8, 10);
        Clock clock = Clock.fixed(Instant.parse("2026-08-10T00:00:00Z"), ZoneOffset.UTC);
        CpfContextExecutionFactory factory = new CpfContextExecutionFactory(txIds, execIds, dates, clock);
        CpfContext root = factory.newRoot(null, "ai.test", null, null, clock.instant().plusSeconds(5));
        CpfContextSnapshot snapshot = CpfContextSnapshot.capture(root, clock.instant());
        try (AutoCloseable _ = CpfContexts.bind(snapshot)) { body.accept(factory); }
        assertThat(CpfContexts.current()).isNull();
    }

    private static CpfAiRequest req(boolean high) { return new CpfAiRequest("m", "masked", high ? CpfAiRisk.HIGH : CpfAiRisk.LOW, Duration.ofMillis(50), false, Map.of()); }
    private static CpfAiResponse response(String provider) { return new CpfAiResponse(provider, "m", "ok", new CpfAiUsage(1, 1, 2), Map.of()); }
    private static CpfAiProvider provider(String id, boolean safe) { return new CpfAiProvider() { public String providerId(){return id;} public boolean supports(String m){return true;} public boolean safeToFallbackAfterTimeout(){return safe;} public CpfAiResponse execute(CpfAiRequest r){return response(id);} }; }
    private static CpfAiPolicy noopPolicy() { return new CpfAiPolicy() { public CpfAiRequest authorizeAndMask(CpfAiRequest r){return r;} public void audit(CpfAiRequest r,CpfAiResponse s,Throwable f){} }; }
    @FunctionalInterface private interface CheckedConsumer<T> { void accept(T value) throws Exception; }
}
