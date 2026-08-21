package com.cpf.foundation.money;
import java.math.BigDecimal;
import java.util.Objects;
/** 금액 연산에 동일한 scale과 rounding 정책을 적용하는 공개 유틸리티입니다. */
public final class CpfAmounts {
    private CpfAmounts() {}
    public static BigDecimal normalize(BigDecimal value, CpfRoundingPolicy policy) { return Objects.requireNonNull(value,"value").setScale(policy.scale(), policy.mode()); }
    public static BigDecimal add(BigDecimal left, BigDecimal right, CpfRoundingPolicy policy) { return normalize(left.add(right), policy); }
    public static BigDecimal multiply(BigDecimal left, BigDecimal right, CpfRoundingPolicy policy) { return normalize(left.multiply(right), policy); }
}
