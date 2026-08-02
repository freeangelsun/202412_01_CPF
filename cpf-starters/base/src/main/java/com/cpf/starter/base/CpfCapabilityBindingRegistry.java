package com.cpf.starter.base;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe provider registry. Ambiguous defaults and duplicate named bindings fail closed.
 */
public final class CpfCapabilityBindingRegistry {
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, CpfCapabilityBinding>> bindings = new ConcurrentHashMap<>();

    public void register(CpfCapabilityBinding binding) {
        java.util.Objects.requireNonNull(binding, "binding");
        bindings.compute(binding.capability(), (capability, current) -> {
            ConcurrentHashMap<String, CpfCapabilityBinding> candidate = current == null
                    ? new ConcurrentHashMap<>() : new ConcurrentHashMap<>(current);
            CpfCapabilityBinding existing = candidate.putIfAbsent(binding.name(), binding);
            if (existing != null && !existing.equals(binding)) {
                throw new IllegalStateException("Conflicting CPF binding: " + binding.capability() + "/" + binding.name());
            }
            validateDefaultCount(capability, candidate);
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
        List<CpfCapabilityBinding> defaults = list(capability).stream().filter(CpfCapabilityBinding::defaultBinding).toList();
        if (defaults.size() != 1) {
            throw new IllegalStateException("CPF capability requires exactly one default binding: " + capability + " (found=" + defaults.size() + ")");
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
        return capabilityBindings.values().stream().sorted(Comparator.comparing(CpfCapabilityBinding::name)).toList();
    }

    public Map<String, List<CpfCapabilityBinding>> snapshot() {
        Map<String, List<CpfCapabilityBinding>> copy = new LinkedHashMap<>();
        bindings.keySet().stream().sorted().forEach(capability -> copy.put(capability, list(capability)));
        return Map.copyOf(copy);
    }

    public void validateAll() {
        new ArrayList<>(bindings.entrySet()).forEach(entry -> validateDefaultCount(entry.getKey(), entry.getValue()));
    }

    private static void validateDefaultCount(String capability, Map<String, CpfCapabilityBinding> capabilityBindings) {
        long defaultCount = capabilityBindings.values().stream().filter(CpfCapabilityBinding::defaultBinding).count();
        if (defaultCount > 1) {
            throw new IllegalStateException("Multiple default CPF bindings: " + capability);
        }
    }
}
