package com.cpf.core.api.reliability;
import java.util.List;
public record CpfEventSchemaCompatibilityResult(boolean compatible, List<String> violations) {
    public CpfEventSchemaCompatibilityResult { violations = violations == null ? List.of() : List.copyOf(violations); }
}
