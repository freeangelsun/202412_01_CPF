package com.cpf.core.service.reliability;

import com.cpf.core.api.feature.CpfFeatureFlagContext;
import com.cpf.core.api.feature.CpfFeatureFlags;
import com.cpf.core.api.reliability.CpfFaultInjector;

import java.time.Duration;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * test/verification/chaos Profile 전용 Fault Injector.
 * Target allowlist 밖에서는 절대 장애를 주입하지 않으며 최대 지연시간을 제한합니다.
 */
final class CpfControlledFaultInjector implements CpfFaultInjector {
    private final CpfFeatureFlags flags;
    private final Set<String> targets;
    private final long delayMillis;
    private final boolean throwFailure;

    CpfControlledFaultInjector(
            CpfFeatureFlags flags,
            String targetText,
            long delayMillis,
            boolean throwFailure) {
        this.flags = flags;
        this.targets = Arrays.stream(targetText.split(",")).map(String::trim).filter(v -> !v.isBlank()).collect(Collectors.toUnmodifiableSet());
        this.delayMillis = Math.max(0, Math.min(delayMillis, Duration.ofSeconds(30).toMillis()));
        this.throwFailure = throwFailure;
    }

    @Override
    public void before(String targetId) {
        if (targetId == null || !targets.contains(targetId)) return;
        boolean enabled = flags.bool("fault-injection", new CpfFeatureFlagContext(
                "verification", null, null, null, targetId, java.util.Map.of("targetId", targetId)), false).value();
        if (!enabled) return;
        if (delayMillis > 0) {
            try {
                Thread.sleep(delayMillis);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Controlled fault injection interrupted", ex);
            }
        }
        if (throwFailure) throw new CpfInjectedFaultException("Controlled fault injected: " + targetId);
    }
}
