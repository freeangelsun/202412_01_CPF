package com.cpf.platform.operations.api.featureflag;

/** Convenience facade that preserves the previous typed caller contract while using the canonical featureflag API. */
/** CpfFeatureFlags 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public final class CpfFeatureFlags {
    private final CpfFeatureFlagOperations operations;

    public CpfFeatureFlags(CpfFeatureFlagOperations operations) {
        this.operations = java.util.Objects.requireNonNull(operations, "operations");
    }

    /** bool 작업을 CPF 표준 계약에 따라 수행한다. */
    public CpfFeatureFlagResult<Boolean> bool(String flagKey, CpfFeatureFlagContext context, boolean safeDefault) {
        return convert(operations.evaluate(flagKey, new CpfFeatureFlagValue.BooleanValue(safeDefault), context), Boolean.class);
    }

    public CpfFeatureFlagResult<String> string(String flagKey, CpfFeatureFlagContext context, String safeDefault) {
        return convert(operations.evaluate(flagKey, new CpfFeatureFlagValue.StringValue(safeDefault), context), String.class);
    }

    /** number 작업을 CPF 표준 계약에 따라 수행한다. */
    public CpfFeatureFlagResult<Double> number(String flagKey, CpfFeatureFlagContext context, double safeDefault) {
        return convert(operations.evaluate(flagKey, new CpfFeatureFlagValue.DecimalValue(safeDefault), context), Double.class);
    }

    public <T> CpfFeatureFlagResult<T> object(
            String flagKey, CpfFeatureFlagContext context, Class<T> valueType, T safeDefault) {
        if (safeDefault instanceof Boolean value) return cast(bool(flagKey, context, value));
        if (safeDefault instanceof String value) return cast(string(flagKey, context, value));
        if (safeDefault instanceof Number value) return cast(number(flagKey, context, value.doubleValue()));
        throw new IllegalArgumentException("object flags support Boolean, String and Number values only");
    }

    private static <T> CpfFeatureFlagResult<T> convert(
            CpfFeatureFlagResult<CpfFeatureFlagValue> source, Class<T> type) {
        Object raw = source.value().rawValue();
        Object converted = raw;
        if (type == Double.class && raw instanceof Number number) converted = number.doubleValue();
        if (!type.isInstance(converted)) {
            throw new IllegalStateException("feature flag type mismatch for " + source.flagKey());
        }
        return new CpfFeatureFlagResult<>(source.flagKey(), type.cast(converted), source.variant(),
                source.reasonCode(), source.source(), source.revision(), source.evaluatedAt());
    }

    @SuppressWarnings("unchecked")
    private static <T> CpfFeatureFlagResult<T> cast(CpfFeatureFlagResult<?> result) {
        return (CpfFeatureFlagResult<T>) result;
    }
}
