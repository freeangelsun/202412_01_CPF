package com.cpf.platform.operations.api.featureflag;

/** Typed flag value with no OpenFeature SDK type in the public API. */
/** CpfFeatureFlagValue 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public sealed interface CpfFeatureFlagValue permits CpfFeatureFlagValue.BooleanValue, CpfFeatureFlagValue.StringValue, CpfFeatureFlagValue.IntegerValue, CpfFeatureFlagValue.DecimalValue {
    Object rawValue();
    record BooleanValue(boolean value) implements CpfFeatureFlagValue { public Object rawValue() { return value; } }
    record StringValue(String value) implements CpfFeatureFlagValue { public StringValue { if (value == null) throw new IllegalArgumentException("value is required"); } public Object rawValue() { return value; } }
    record IntegerValue(long value) implements CpfFeatureFlagValue { public Object rawValue() { return value; } }
    record DecimalValue(double value) implements CpfFeatureFlagValue { public DecimalValue { if (!Double.isFinite(value)) throw new IllegalArgumentException("value must be finite"); } public Object rawValue() { return value; } }
}
