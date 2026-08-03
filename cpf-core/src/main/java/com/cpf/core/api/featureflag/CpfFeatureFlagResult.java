package com.cpf.core.api.featureflag;

import java.time.Instant;

/** Typed evaluation result including source, revision and fallback state. */
public record CpfFeatureFlagResult<T>(
        String flagKey,
        T value,
        String variant,
        String reasonCode,
        Source source,
        long revision,
        Instant evaluatedAt) {
    public enum Source { PROVIDER, SECURE_OVERRIDE, KILL_SWITCH, CACHE, FALLBACK }

    public CpfFeatureFlagResult {
        if (flagKey == null || flagKey.isBlank()) throw new IllegalArgumentException("flagKey is required");
        flagKey = flagKey.trim();
        if (value == null || source == null) throw new IllegalArgumentException("value/source is required");
        if (revision < 0) throw new IllegalArgumentException("revision must be non-negative");
        variant = variant == null || variant.isBlank() ? null : variant.trim();
        reasonCode = reasonCode == null || reasonCode.isBlank() ? "UNSPECIFIED" : reasonCode.trim();
        evaluatedAt = evaluatedAt == null ? Instant.now() : evaluatedAt;
    }

    public boolean defaulted() {
        return source == Source.FALLBACK;
    }

    public String providerVersion() {
        return Long.toString(revision);
    }
}
