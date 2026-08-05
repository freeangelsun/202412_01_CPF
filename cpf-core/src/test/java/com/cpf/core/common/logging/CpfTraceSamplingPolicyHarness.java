package com.cpf.core.common.logging;

import com.cpf.core.api.logging.CpfLogLevel;
import com.cpf.core.api.logging.DynamicLogLevelRule;

import java.time.LocalDateTime;
import java.util.Map;

public final class CpfTraceSamplingPolicyHarness {
    private CpfTraceSamplingPolicyHarness() {}

    public static void main(String[] args) {
        CpfTraceSamplingPolicy policy = new CpfTraceSamplingPolicy();
        policy.replace(1L, 0.0d, Map.of("ADM", 1.0d), Map.of("PAY", 0.0d), true);
        if (!policy.shouldSample("t", "X", "ADM", true, null)) throw new AssertionError();
        if (policy.shouldSample("t", "PAY", "ADM", true, null)) throw new AssertionError();
        if (!policy.shouldSample("t", "PAY", "ADM", false, null)) throw new AssertionError();
        LocalDateTime now = LocalDateTime.of(2026, 8, 5, 0, 0);
        DynamicLogLevelRule rule = new DynamicLogLevelRule(
                "r", "t", "PAY", "ADM", CpfLogLevel.DEBUG,
                "reason", "operator", now, now.plusMinutes(1));
        if (!policy.shouldSample("t", "PAY", "ADM", true, rule)) throw new AssertionError();
        boolean first = policy.shouldSample("stable", "B", "C", true, null);
        boolean second = policy.shouldSample("stable", "B", "C", true, null);
        if (first != second) throw new AssertionError();
        System.out.println("CPF_TRACE_SAMPLING_HARNESS_PASS");
    }
}
