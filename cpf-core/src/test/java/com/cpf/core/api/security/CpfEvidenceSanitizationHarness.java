package com.cpf.core.api.security;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/** Executable resource-boundary and cycle-safety gate for audit/evidence sanitization. */
public final class CpfEvidenceSanitizationHarness {
    private CpfEvidenceSanitizationHarness() { }

    public static void main(String[] args) {
        if (CpfMasking.policyVersion() < 3) {
            throw new AssertionError("resource-bounded masking policy version not active");
        }
        verifiesCyclesAndDeepImmutability();
        verifiesInfiniteIterableIsBounded();
        verifiesTextAndAggregateBudgets();
        verifiesNullValuesRemainSafe();
        verifiesContainerPiiAndHostileObjectsFailClosed();
        verifiesDepthLimit();
        System.out.println("CPF_EVIDENCE_SANITIZATION_HARNESS_PASS");
    }

    private static void verifiesCyclesAndDeepImmutability() {
        Map<String, Object> root = new LinkedHashMap<>();
        List<Object> nested = new ArrayList<>();
        root.put("nested", nested);
        nested.add(root);
        Object sanitized = CpfMasking.structured(root);
        String rendered = String.valueOf(sanitized);
        if (!rendered.contains("[CYCLE]")) {
            throw new AssertionError("cycle was not terminated: " + rendered);
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) sanitized;
        try {
            map.put("unsafe", "value");
            throw new AssertionError("sanitized map must be immutable");
        } catch (UnsupportedOperationException expected) {
            // expected
        }
        @SuppressWarnings("unchecked")
        List<Object> list = (List<Object>) map.get("nested");
        try {
            list.add("unsafe");
            throw new AssertionError("nested sanitized list must be immutable");
        } catch (UnsupportedOperationException expected) {
            // expected
        }
    }

    private static void verifiesInfiniteIterableIsBounded() {
        Iterable<String> infinite = () -> new Iterator<>() {
            @Override public boolean hasNext() { return true; }
            @Override public String next() {
                if (!hasNext()) throw new NoSuchElementException();
                return "safe";
            }
        };
        List<Object> result = CpfMasking.list(infinite);
        if (result.size() > 10_001 || !result.contains("[TRUNCATED_ITEMS]")) {
            throw new AssertionError("infinite iterable was not bounded: size=" + result.size());
        }
    }

    private static void verifiesTextAndAggregateBudgets() {
        String longSecret = "token=do-not-leak " + "x".repeat(100_000);
        String one = CpfSensitiveData.sanitizeAuditText(longSecret);
        if (one.length() > 65_536 || one.contains("do-not-leak") || !one.contains("TRUNCATED")) {
            throw new AssertionError("single text bound/redaction failed");
        }

        List<String> many = new ArrayList<>();
        for (int i = 0; i < 200; i++) many.add(("row-" + i + "-").repeat(2_000));
        String aggregate = CpfSensitiveData.sanitizeAuditSnapshotText(many);
        if (aggregate.length() > 262_144 || !aggregate.contains("TRUNCATED")) {
            throw new AssertionError("aggregate evidence budget failed: " + aggregate.length());
        }
    }

    private static void verifiesNullValuesRemainSafe() {
        Map<String, Object> source = new LinkedHashMap<>();
        source.put("nullable", null);
        Map<String, Object> detail = CpfMasking.detail(source);
        if (!detail.containsKey("nullable") || detail.get("nullable") != null) {
            throw new AssertionError("null map value was not preserved");
        }
        List<Object> list = new ArrayList<>();
        list.add(null);
        List<Object> masked = CpfMasking.list(list);
        if (masked.size() != 1 || masked.get(0) != null) {
            throw new AssertionError("null list value was not preserved");
        }
    }

    private static void verifiesContainerPiiAndHostileObjectsFailClosed() {
        Map<String, Object> recursive = new LinkedHashMap<>();
        recursive.put("self", recursive);
        Map<String, Object> source = new LinkedHashMap<>();
        source.put("email", recursive);
        source.put("safe", new Object() {
            @Override public String toString() { throw new IllegalStateException("secret=raw"); }
        });
        String rendered = String.valueOf(CpfMasking.detail(source));
        if (!rendered.contains("email=[MASKED]") || !rendered.contains("safe=[UNREPRESENTABLE]")) {
            throw new AssertionError("container PII or hostile object did not fail closed: " + rendered);
        }
        if (rendered.contains("secret=raw")) throw new AssertionError("hostile exception leaked");
    }

    private static void verifiesDepthLimit() {
        List<Object> root = new ArrayList<>();
        List<Object> cursor = root;
        for (int i = 0; i < 40; i++) {
            List<Object> next = new ArrayList<>();
            cursor.add(next);
            cursor = next;
        }
        String rendered = String.valueOf(CpfMasking.structured(root));
        if (!rendered.contains("[DEPTH_LIMIT]")) {
            throw new AssertionError("depth limit missing: " + rendered);
        }
    }
}
