package com.cpf.platform.operations.featureflag.openfeature;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.cpf.platform.operations.api.featureflag.CpfFeatureFlagContext;
import com.cpf.platform.operations.api.featureflag.CpfFeatureFlagResult;
import com.cpf.platform.operations.api.featureflag.CpfFeatureFlagValue;
import com.cpf.platform.operations.spi.featureflag.CpfFeatureFlagAuditSink;
import com.cpf.platform.operations.spi.featureflag.CpfFeatureFlagProvider;
import com.cpf.platform.operations.spi.featureflag.CpfFeatureFlagStateStore;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class CpfFeatureFlagRuntimeTest {
    private static final Instant NOW = Instant.parse("2026-08-03T00:00:00Z");
    private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);
    private static final CpfFeatureFlagValue.BooleanValue DEFAULT =
            new CpfFeatureFlagValue.BooleanValue(false);

    @Test
    void secureOverrideWinsAndSensitiveContextIsRemoved() {
        CpfFeatureFlagResult<CpfFeatureFlagValue> override = result(
                "payments.kill", new CpfFeatureFlagValue.BooleanValue(true),
                CpfFeatureFlagResult.Source.SECURE_OVERRIDE, 2);
        StubStore store = new StubStore(override);
        RecordingAudit audit = new RecordingAudit();
        CpfFeatureFlagRuntime runtime = runtime(provider((key, fallback, context) -> result(
                key, fallback, CpfFeatureFlagResult.Source.PROVIDER, 1), 1), store, audit);

        CpfFeatureFlagContext context = new CpfFeatureFlagContext(
                "user-1", Map.of("token", "secret", "region", "KR"));
        CpfFeatureFlagResult<CpfFeatureFlagValue> evaluated =
                runtime.evaluate("payments.kill", DEFAULT, context);

        assertThat(context.attributes()).containsOnlyKeys("region");
        assertThat(evaluated.source()).isEqualTo(CpfFeatureFlagResult.Source.SECURE_OVERRIDE);
        assertThat(audit.events).singleElement().asString().doesNotContain("secret");
    }

    @Test
    void providerFailureReturnsTypedFallbackAndAuditsIt() {
        StubStore store = new StubStore(null);
        RecordingAudit audit = new RecordingAudit();
        CpfFeatureFlagProvider failing = provider((key, fallback, context) -> {
            throw new IllegalStateException("provider unavailable");
        }, 1);
        CpfFeatureFlagRuntime runtime = runtime(failing, store, audit);

        CpfFeatureFlagResult<CpfFeatureFlagValue> result = runtime.evaluate(
                "checkout.new-flow", DEFAULT, new CpfFeatureFlagContext("user-1", Map.of()));

        assertThat(result.source()).isEqualTo(CpfFeatureFlagResult.Source.FALLBACK);
        assertThat(result.value()).isEqualTo(DEFAULT);
        assertThat(result.reasonCode()).isEqualTo("PROVIDER_ERROR");
        assertThat(audit.events).hasSize(1);
    }

    @Test
    void cacheIsSharedUntilProviderOrStateRevisionChanges() {
        StubStore store = new StubStore(null);
        AtomicInteger calls = new AtomicInteger();
        CpfFeatureFlagProvider provider = new CpfFeatureFlagProvider() {
            @Override public CpfFeatureFlagResult<CpfFeatureFlagValue> evaluate(
                    String key, CpfFeatureFlagValue fallback, CpfFeatureFlagContext context) {
                calls.incrementAndGet();
                return result(key, new CpfFeatureFlagValue.BooleanValue(true),
                        CpfFeatureFlagResult.Source.PROVIDER, revision());
            }
            @Override public long revision() { return 5; }
        };
        CpfFeatureFlagRuntime runtime = runtime(provider, store, new RecordingAudit());
        CpfFeatureFlagContext context = new CpfFeatureFlagContext("user-1", Map.of("region", "KR"));

        CpfFeatureFlagResult<CpfFeatureFlagValue> first = runtime.evaluate("feature.a", DEFAULT, context);
        CpfFeatureFlagResult<CpfFeatureFlagValue> second = runtime.evaluate("feature.a", DEFAULT, context);
        store.revision++;
        CpfFeatureFlagResult<CpfFeatureFlagValue> third = runtime.evaluate("feature.a", DEFAULT, context);

        assertThat(first.source()).isEqualTo(CpfFeatureFlagResult.Source.PROVIDER);
        assertThat(second.source()).isEqualTo(CpfFeatureFlagResult.Source.CACHE);
        assertThat(third.source()).isEqualTo(CpfFeatureFlagResult.Source.PROVIDER);
        assertThat(calls).hasValue(2);
    }

    @Test
    void expiredOverrideRequestIsRejectedBeforePersistence() {
        StubStore store = new StubStore(null);
        CpfFeatureFlagRuntime runtime = runtime(provider((key, fallback, context) -> result(
                key, fallback, CpfFeatureFlagResult.Source.PROVIDER, 1), 1), store, new RecordingAudit());

        assertThatThrownBy(() -> runtime.requestOverride(
                "feature.a", DEFAULT, NOW, "operator-a", "temporary test"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("future");
        assertThat(store.requestCount).isZero();
    }

    @Test
    void auditFailureDoesNotReturnAnUnauditedEvaluation() {
        StubStore store = new StubStore(null);
        CpfFeatureFlagAuditSink failingAudit = (eventType, flagKey, actorId, reason, attributes, at) -> {
            throw new IllegalStateException("audit unavailable");
        };
        CpfFeatureFlagRuntime runtime = runtime(provider((key, fallback, context) -> result(
                key, fallback, CpfFeatureFlagResult.Source.PROVIDER, 1), 1), store, failingAudit);

        assertThatThrownBy(() -> runtime.evaluate(
                "feature.a", DEFAULT, new CpfFeatureFlagContext("user-1", Map.of())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("audit unavailable");
    }


    private static CpfFeatureFlagProvider provider(Evaluator evaluator, long revision) {
        return new CpfFeatureFlagProvider() {
            @Override public CpfFeatureFlagResult<CpfFeatureFlagValue> evaluate(
                    String key, CpfFeatureFlagValue fallback, CpfFeatureFlagContext context) {
                return evaluator.evaluate(key, fallback, context);
            }
            @Override public long revision() { return revision; }
        };
    }

    @FunctionalInterface
    private interface Evaluator {
        CpfFeatureFlagResult<CpfFeatureFlagValue> evaluate(
                String key, CpfFeatureFlagValue fallback, CpfFeatureFlagContext context);
    }

    private static CpfFeatureFlagRuntime runtime(CpfFeatureFlagProvider provider,
                                                  CpfFeatureFlagStateStore store,
                                                  CpfFeatureFlagAuditSink audit) {
        return new CpfFeatureFlagRuntime(provider, store, audit, CLOCK, Duration.ofSeconds(10));
    }

    private static CpfFeatureFlagResult<CpfFeatureFlagValue> result(
            String key, CpfFeatureFlagValue value, CpfFeatureFlagResult.Source source, long revision) {
        return new CpfFeatureFlagResult<>(key, value, null, source.name(), source, revision, NOW);
    }

    private static final class StubStore implements CpfFeatureFlagStateStore {
        private final CpfFeatureFlagResult<CpfFeatureFlagValue> controlled;
        private long revision = 1;
        private int requestCount;

        private StubStore(CpfFeatureFlagResult<CpfFeatureFlagValue> controlled) {
            this.controlled = controlled;
        }
        @Override public Optional<CpfFeatureFlagResult<CpfFeatureFlagValue>> findEffective(
                String key, Instant now) {
            return Optional.ofNullable(controlled);
        }
        @Override public List<CpfFeatureFlagResult<CpfFeatureFlagValue>> search(
                String filter, int offset, int limit, Instant now) {
            return controlled == null ? List.of() : List.of(controlled);
        }
        @Override public String requestOverride(String key, CpfFeatureFlagValue value,
                                                Instant expiresAt, String requester, String reason) {
            requestCount++;
            return "request-1";
        }
        @Override public CpfFeatureFlagResult<CpfFeatureFlagValue> approveOverride(
                String requestId, String approver, String reason, Instant now) {
            return controlled;
        }
        @Override public void revokeOverride(String requestId, String operator, String reason, Instant now) {
        }
        @Override public void setKillSwitch(String key, boolean enabled,
                                            String operator, String reason, Instant now) {
        }
        @Override public long revision() { return revision; }
    }

    private static final class RecordingAudit implements CpfFeatureFlagAuditSink {
        private final List<String> events = new ArrayList<>();
        @Override public void record(String eventType, String flagKey, String actorId,
                                     String reason, Map<String, String> attributes, Instant at) {
            events.add(eventType + ":" + flagKey + ":" + reason + ":" + attributes);
        }
    }
}
