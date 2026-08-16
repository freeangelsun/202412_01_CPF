package com.cpf.platform.operations.observability.internal.logging;

import com.cpf.security.api.CpfMaskingRuntime;

import com.cpf.platform.operations.observability.api.logging.DynamicLogLevelRule;
import java.util.Map;

/** Observability Owner 내부의 마스킹·Trace Sampling 경계 회귀 검증이다. */
public final class CpfObservabilitySafetyHarness {
    private CpfObservabilitySafetyHarness() { }

    public static void main(String[] args) {
        maskingBoundaries();
        traceSamplingVersioning();
        System.out.println("CPF_OBSERVABILITY_SAFETY_HARNESS_PASS");
    }

    private static void maskingBoundaries() {
        String source = "{\"password\":\"a\\\"b\",\"nested\":\"{\\\"token\\\":\\\"inside\\\"}\"} "
                + "Authorization: Basic dXNlcjpwYXNz token='space containing token' "
                + "<secret>xml-value</secret> eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.signature123 "
                + "-----BEGIN PRIVATE KEY-----raw-private-material-----END PRIVATE KEY-----";
        String masked = CpfMaskingRuntime.mask(source);
        check(!masked.contains("a\\\"b"), "escaped json masked");
        check(!masked.contains("inside"), "nested escaped json masked");
        check(!masked.contains("dXNlcjpwYXNz"), "basic authorization masked");
        check(!masked.contains("space containing token"), "quoted key value masked");
        check(!masked.contains("xml-value"), "xml secret masked");
        check(!masked.contains("eyJhbGci"), "standalone jwt masked");
        check(!masked.contains("raw-private-material"), "private key masked");
    }

    private static void traceSamplingVersioning() {
        CpfTraceSamplingPolicy policy = new CpfTraceSamplingPolicy();
        CpfTraceSamplingPolicy.Snapshot versionOne = policy.replace(1, 0.5d, Map.of(), Map.of(), true);
        check(policy.replace(1, 0.5d, Map.of(), Map.of(), true).equals(versionOne),
                "same-version identical policy is idempotent");
        boolean conflict = false;
        try {
            policy.replace(1, 0.6d, Map.of(), Map.of(), true);
        } catch (IllegalStateException expected) {
            conflict = true;
        }
        check(conflict, "same-version different policy conflicts");
        boolean rollback = false;
        try {
            policy.replace(0, 0.5d, Map.of(), Map.of(), true);
        } catch (IllegalArgumentException expected) {
            rollback = true;
        }
        check(rollback, "sampling version rollback rejected");
        check(policy.shouldSample("", "business", "module", true, (DynamicLogLevelRule) null),
                "missing transaction id is retained for anomaly evidence");
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
