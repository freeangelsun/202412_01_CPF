package com.cpf.reference.edu.runtime.model;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Immutable executable contract for one Manual-135 education capability.
 *
 * <p>The externally meaningful role is derived from the requirement family.  Historic
 * {@code CPF_REFERENCE_*} literals are accepted only as migration aliases and are never
 * exposed by this record.  This closes the catalog/handler authorization drift at the
 * framework boundary instead of relying on every generated handler to remember a role
 * spelling.</p>
 */
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

        requiredRole = canonicalRequiredRole(requirementId, requiredRole);
        requiredFields = List.copyOf(requiredFields);
        steps = List.copyOf(steps);
        supportedFailures = Set.copyOf(supportedFailures);
    }

    /** Canonical authorization role published by the Manual-135 catalog. */
    public static String canonicalRequiredRole(String requirementId) {
        String family = requirementId.split("-", 3)[1];
        return switch (family) {
            case "DEV" -> "CPF_EDU_DEVELOPER";
            case "BAT" -> "CPF_BATCH_OPERATOR";
            case "ADM" -> "CPF_ADM_OPERATOR";
            case "BZA" -> "CPF_BZA_OPERATOR";
            case "GW" -> "CPF_GATEWAY_OPERATOR";
            case "OPS" -> "CPF_PLATFORM_OPERATOR";
            default -> throw new IllegalArgumentException("Unsupported EDU family: " + family);
        };
    }

    private static String canonicalRequiredRole(String requirementId, String suppliedRole) {
        String canonical = canonicalRequiredRole(requirementId);
        if (canonical.equals(suppliedRole)) return canonical;
        String family = requirementId.split("-", 3)[1];
        String allowedLegacy = switch (family) {
            case "DEV" -> "CPF_REFERENCE_DEVELOPER";
            case "BAT" -> "CPF_REFERENCE_BATCH_OPERATOR";
            case "ADM" -> "CPF_REFERENCE_PLATFORM_OPERATOR";
            case "BZA" -> "CPF_REFERENCE_BACKOFFICE_OPERATOR";
            case "GW" -> "CPF_REFERENCE_GATEWAY_OPERATOR";
            case "OPS" -> "CPF_REFERENCE_PLATFORM_OPERATOR";
            default -> "";
        };
        if (!allowedLegacy.equals(suppliedRole)) {
            throw new IllegalArgumentException(
                    requirementId + " requiredRole mismatch: expected=" + canonical + " supplied=" + suppliedRole);
        }
        return canonical;
    }
}
