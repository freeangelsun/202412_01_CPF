package com.cpf.core.api.security;

import java.util.List;
import java.util.Map;

public final class CpfStructuredMaskingHarness {
    private CpfStructuredMaskingHarness() {}

    public static void main(String[] args) {
        if (CpfMasking.policyVersion() < 2) throw new AssertionError("policy version not advanced");
        if (CpfMasking.classifyField("resident_no") != CpfSensitiveData.Classification.PII) {
            throw new AssertionError("PII classification failed");
        }
        if (CpfMasking.classifyField("accessToken") != CpfSensitiveData.Classification.CREDENTIAL) {
            throw new AssertionError("credential classification failed");
        }
        if (CpfMasking.classifyField("private_key") != CpfSensitiveData.Classification.SECRET) {
            throw new AssertionError("secret classification failed");
        }
        Map<String, Object> source = Map.of(
                "name", "Kim",
                "email", "user@example.com",
                "accessToken", "abc.def.ghi",
                "nested", List.of(Map.of("phone", "010-1234-5678", "safe", "ok")));
        Map<String, Object> masked = CpfMasking.detail(source);
        if (!"u***@example.com".equals(masked.get("email"))) throw new AssertionError("email not masked: " + masked);
        if (!"[REDACTED]".equals(masked.get("accessToken"))) throw new AssertionError("credential not redacted");
        String rendered = masked.toString();
        for (String leaked : List.of("user@example.com", "abc.def.ghi", "010-1234-5678")) {
            if (rendered.contains(leaked)) throw new AssertionError("structured masking leaked " + leaked);
        }
        if (!source.get("accessToken").equals("abc.def.ghi")) throw new AssertionError("caller object mutated");
        try {
            masked.put("new", "value");
            throw new AssertionError("masked detail must be immutable");
        } catch (UnsupportedOperationException expected) {
            // expected
        }
        List<Object> list = CpfMasking.list(List.of(source));
        if (list.size() != 1 || list.toString().contains("abc.def.ghi")) {
            throw new AssertionError("list masking failed: " + list);
        }
        System.out.println("CPF_STRUCTURED_MASKING_HARNESS_PASS");
    }
}
