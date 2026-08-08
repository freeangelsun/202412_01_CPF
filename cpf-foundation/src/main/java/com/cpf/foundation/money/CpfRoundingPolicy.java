package com.cpf.foundation.money;
import java.math.RoundingMode;
public record CpfRoundingPolicy(int scale, RoundingMode mode) {
    public CpfRoundingPolicy { if (scale < 0) throw new IllegalArgumentException("scale must be >= 0"); if (mode == null) throw new IllegalArgumentException("mode is required"); }
    public static CpfRoundingPolicy currency(int scale) { return new CpfRoundingPolicy(scale, RoundingMode.HALF_UP); }
}
