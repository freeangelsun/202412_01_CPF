package com.cpf.gateway.config;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class CpfGatewayConcurrencyFilterTest {
    @Test
    void rejectsDataPlaneWhenSafetyCapIsExceeded() throws Exception {
        CpfGatewaySafetyProperties safety = new CpfGatewaySafetyProperties();
        safety.setMaxConcurrentRequestsCap(1);
        CpfGatewayConcurrencyFilter filter = new CpfGatewayConcurrencyFilter(safety);
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        AtomicInteger invoked = new AtomicInteger();
        FilterChain blocking = (request, response) -> {
            invoked.incrementAndGet();
            entered.countDown();
            try { release.await(2, TimeUnit.SECONDS); }
            catch (InterruptedException interrupted) { Thread.currentThread().interrupt(); }
        };
        Thread first = Thread.ofVirtual().start(() -> {
            try { filter.doFilter(new MockHttpServletRequest("GET", "/cpf/execute/A"), new MockHttpServletResponse(), blocking); }
            catch (Exception error) { throw new RuntimeException(error); }
        });
        assertThat(entered.await(1, TimeUnit.SECONDS)).isTrue();
        MockHttpServletResponse rejected = new MockHttpServletResponse();
        filter.doFilter(new MockHttpServletRequest("GET", "/cpf/execute/B"), rejected, blocking);
        release.countDown(); first.join();
        assertThat(rejected.getStatus()).isEqualTo(503);
        assertThat(rejected.getHeader("X-Cpf-Gateway-Rejection")).isEqualTo("CONCURRENCY_LIMIT");
        assertThat(invoked).hasValue(1);
    }
}
