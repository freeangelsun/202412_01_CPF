package com.cpf.education.web.gateway;
import com.cpf.gateway.api.CpfGatewayRateLimitPort;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EducationGatewayRateLimitEducationSampleTest {
    private static final Instant NOW = Instant.parse("2026-08-05T00:00:00Z");

    @Test
    void usesConsumerGeneratedRequestIdAndReturnsRetryGuidance() {
        AtomicReference<CpfGatewayRateLimitPort.Request> captured = new AtomicReference<>();
        java.util.function.Function<CpfGatewayRateLimitPort.Request, CpfGatewayRateLimitPort.Decision> decision = request -> {
            captured.set(request);
            return new CpfGatewayRateLimitPort.Decision(
                    false, "API:masked", CpfGatewayRateLimitPort.Scope.API, 0,
                    NOW.plusSeconds(10), Duration.ofSeconds(10), false, false, "QUOTA_EXCEEDED");
        };
        EducationGatewayRateLimitEducationSample sample = new EducationGatewayRateLimitEducationSample(
                new StubPort(decision), Clock.fixed(NOW, ZoneOffset.UTC));

        var result = sample.check("GET:/v1/orders", "orders", "client-a", "web", "tenant-a");

        assertThat(result.allowed()).isFalse();
        assertThat(result.retryAfterSeconds()).isEqualTo(10);
        assertThat(captured.get().requestId()).isNotBlank();
        assertThat(captured.get().requestedAt()).isEqualTo(NOW);
    }

    @Test
    void rejectsMissingExecutionIdBeforeCallingPort() {
        EducationGatewayRateLimitEducationSample sample = new EducationGatewayRateLimitEducationSample(
                new StubPort(request -> { throw new AssertionError("must not be called"); }),
                Clock.fixed(NOW, ZoneOffset.UTC));

        assertThatThrownBy(() -> sample.check(" ", "orders", "client", "web", "tenant"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("executionId");
    }

    private record StubPort(java.util.function.Function<CpfGatewayRateLimitPort.Request,
            CpfGatewayRateLimitPort.Decision> function) implements CpfGatewayRateLimitPort {
        @Override public Decision acquire(Request request) { return function.apply(request); }
        @Override public Health health() { return new Health(true, false, 0, "UP", NOW); }
    }
}
