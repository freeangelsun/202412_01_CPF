package com.cpf.reference.edu.runtime.model;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Set;
public record EduCapabilityDefinition(
        String requirementId,
        String title,
        EduCapabilityKind kind,
        String owner,
        String requiredRole,
        List<String> requiredFields,
        List<EduWorkflowStep> steps,
        Set<EduFailurePoint> supportedFailures,
        boolean idempotent,
        boolean versioned,
        boolean leaseRequired,
        boolean externalEffect,
        boolean compensationSupported,
        boolean rollbackSupported,
        int maxRetries,
        String manualAnchor) implements Serializable {
    public EduCapabilityDefinition {
        Objects.requireNonNull(requirementId); Objects.requireNonNull(title);
        Objects.requireNonNull(kind); Objects.requireNonNull(owner);
        Objects.requireNonNull(requiredRole); Objects.requireNonNull(requiredFields);
        Objects.requireNonNull(steps); Objects.requireNonNull(supportedFailures);
        Objects.requireNonNull(manualAnchor);
        if (!requirementId.matches("EDU-(DEV|BAT|ADM|BZA|GW|OPS)-\\d{2}"))
            throw new IllegalArgumentException("Invalid EDU requirement id: " + requirementId);
        if (maxRetries < 0) throw new IllegalArgumentException("maxRetries must be >= 0");
        requiredFields = List.copyOf(requiredFields);
        steps = List.copyOf(steps);
        supportedFailures = Set.copyOf(supportedFailures);
    }
}
