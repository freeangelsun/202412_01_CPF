package com.cpf.reference.optional.operations.config;

/**
 * Non-executable reference metadata for ADM scenarios that are owned by cpf-admin
 * or merged into another EDU scenario. This type intentionally is not an EDU runtime handler.
 */
public record EduAdmRedirectMetadata(
        String requirementId,
        String architectureDecision,
        String targetOwner,
        String requiredRole,
        boolean executable) {
    public EduAdmRedirectMetadata {
        if (requirementId == null || requirementId.isBlank()) throw new IllegalArgumentException("requirementId");
        if (architectureDecision == null || architectureDecision.isBlank()) throw new IllegalArgumentException("architectureDecision");
        if (targetOwner == null || targetOwner.isBlank()) throw new IllegalArgumentException("targetOwner");
        if (requiredRole == null || requiredRole.isBlank()) throw new IllegalArgumentException("requiredRole");
        if (executable) throw new IllegalArgumentException("redirect metadata must be non-executable");
    }
}
