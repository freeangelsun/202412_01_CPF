package com.cpf.starter.runtime;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Capability별 Cardinality 정책을 적용하는 Thread-safe Provider Binding Registry입니다.
 * <p>기본 정책은 기존 호환을 위해 {@code SINGLE_DEFAULT_REQUIRED}이며,
 * 기관별 Client처럼 Named Binding이 여러 개인 Capability는 등록 전에 정책을 명시합니다.</p>
 */
public final class CpfCapabilityBindingRegistry {
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, CpfCapabilityBinding>> bindings = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, CpfCapabilityBindingCardinality> cardinalities = new ConcurrentHashMap<>();

    public void configureCardinality(String capability, CpfCapabilityBindingCardinality cardinality) {
        String key = requireText(capability, "capability");
        java.util.Objects.requireNonNull(cardinality, "cardinality");
        CpfCapabilityBindingCardinality previous = cardinalities.putIfAbsent(key, cardinality);
        if (previous != null && previous != cardinality) {
            throw new IllegalStateException("Conflicting CPF binding cardinality: " + key + " (existing=" + previous + ", requested=" + cardinality + ")");
        }
        validate(key, bindings.getOrDefault(key, new ConcurrentHashMap<>()), cardinality, false);
    }

    public CpfCapabilityBindingCardinality cardinality(String capability) {
        return cardinalities.getOrDefault(requireText(capability, "capability"),
                CpfCapabilityBindingCardinality.SINGLE_DEFAULT_REQUIRED);
    }

    public void register(CpfCapabilityBinding binding) {
        java.util.Objects.requireNonNull(binding, "binding");
        bindings.compute(binding.capability(), (capability, current) -> {
            ConcurrentHashMap<String, CpfCapabilityBinding> candidate = current == null
                    ? new ConcurrentHashMap<>() : new ConcurrentHashMap<>(current);
            CpfCapabilityBinding existing = candidate.putIfAbsent(binding.name(), binding);
            if (existing != null && !existing.equals(binding)) {
                throw new IllegalStateException("Conflicting CPF binding: " + binding.capability() + "/" + binding.name());
            }
            validate(capability, candidate, cardinality(capability), false);
            return candidate;
        });
    }

    public CpfCapabilityBinding require(String capability, String name) {
        var capabilityBindings = bindings.get(capability);
        if (capabilityBindings == null || !capabilityBindings.containsKey(name)) {
            throw new IllegalStateException("Missing CPF binding: " + capability + "/" + name);
        }
        return capabilityBindings.get(name);
    }

    public CpfCapabilityBinding requireDefault(String capability) {
        CpfCapabilityBindingCardinality policy = cardinality(capability);
        if (policy == CpfCapabilityBindingCardinality.EXPLICIT_ONLY
                || policy == CpfCapabilityBindingCardinality.INTERNAL_NO_PUBLIC_BINDING) {
            throw new IllegalStateException("CPF capability does not expose a default binding: " + capability + " (cardinality=" + policy + ")");
        }
        List<CpfCapabilityBinding> defaults = list(capability).stream().filter(value -> value.defaultBinding()).toList();
        if (defaults.size() != 1) {
            throw new IllegalStateException("CPF capability default binding is not uniquely available: " + capability + " (found=" + defaults.size() + ", cardinality=" + policy + ")");
        }
        return defaults.getFirst();
    }

    public Optional<CpfCapabilityBinding> find(String capability, String name) {
        var capabilityBindings = bindings.get(capability);
        return capabilityBindings == null ? Optional.empty() : Optional.ofNullable(capabilityBindings.get(name));
    }

    public List<CpfCapabilityBinding> list(String capability) {
        var capabilityBindings = bindings.get(capability);
        if (capabilityBindings == null) return List.of();
        return capabilityBindings.values().stream().sorted(Comparator.comparing(value -> value.name())).toList();
    }

    public Map<String, List<CpfCapabilityBinding>> snapshot() {
        Map<String, List<CpfCapabilityBinding>> copy = new LinkedHashMap<>();
        bindings.keySet().stream().sorted().forEach(capability -> copy.put(capability, list(capability)));
        return Map.copyOf(copy);
    }

    public void validateAll() {
        new ArrayList<>(bindings.entrySet()).forEach(entry ->
                validate(entry.getKey(), entry.getValue(), cardinality(entry.getKey()), true));
    }

    private static void validate(String capability, Map<String, CpfCapabilityBinding> capabilityBindings,
            CpfCapabilityBindingCardinality policy, boolean startup) {
        long defaultCount = capabilityBindings.values().stream().filter(value -> value.defaultBinding()).count();
        if (defaultCount > 1) throw new IllegalStateException("Multiple default CPF bindings: " + capability);
        switch (policy) {
            case SINGLE_DEFAULT_REQUIRED -> {
                if (startup && !capabilityBindings.isEmpty() && defaultCount != 1) {
                    throw new IllegalStateException("CPF capability requires exactly one default binding at startup: " + capability + " (found=" + defaultCount + ")");
                }
            }
            case NAMED_MULTI_OPTIONAL_DEFAULT -> { /* 0..1 default, many named bindings */ }
            case EXPLICIT_ONLY -> {
                if (defaultCount != 0) throw new IllegalStateException("EXPLICIT_ONLY capability cannot declare a default binding: " + capability);
            }
            case INTERNAL_NO_PUBLIC_BINDING -> {
                if (!capabilityBindings.isEmpty()) throw new IllegalStateException("INTERNAL_NO_PUBLIC_BINDING capability cannot register public bindings: " + capability);
            }
        }
    }

    private static String requireText(String value, String label) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(label + " must not be blank");
        return value.trim();
    }
}
