package com.cpf.foundation.money;
import java.math.RoundingMode;
/** 금액 연산에 적용할 소수점 scale과 RoundingMode를 정의하는 공개 정책 값입니다. */
public record CpfRoundingPolicy(int scale, RoundingMode mode) {
    public CpfRoundingPolicy { if (scale < 0) throw new IllegalArgumentException("scale must be >= 0"); if (mode == null) throw new IllegalArgumentException("mode is required"); }
    public static CpfRoundingPolicy currency(int scale) { return new CpfRoundingPolicy(scale, RoundingMode.HALF_UP); }
}
