package com.cpf.batch.control.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Controller 명령의 행위자 필드를 인증된 Principal로 재구성합니다.
 */
@Component
public final class BatVerifiedActorResolver {
    public BatAuthenticatedIdentity identity(HttpServletRequest request) {
        Authentication authentication = authentication();
        BatAuthenticatedIdentity identity = identity(authentication);
        if (request == null || request.getUserPrincipal() == null) {
            throw new AuthenticationCredentialsNotFoundException(
                    "BAT verified request principal is required");
        }
        String requestPrincipal = request.getUserPrincipal().getName();
        if (requestPrincipal == null || !requestPrincipal.equals(authentication.getName())) {
            throw new AuthenticationCredentialsNotFoundException(
                    "BAT request principal does not match the security context");
        }
        return identity;
    }

    public String actor(HttpServletRequest request, String bodyValue, String fieldName) {
        Authentication authentication = authentication();
        identity(request);
        String verified = required(authentication.getName(), "verified operator");
        rejectMismatch(bodyValue, verified, fieldName);
        return verified;
    }

    public ApprovedActors approved(
            HttpServletRequest request,
            String bodyRequestedBy,
            String bodyApprovedBy,
            String bodyApprovalRequestId) {
        Authentication authentication = authentication();
        BatAuthenticatedIdentity identity = identity(authentication);
        if (!"ADM".equalsIgnoreCase(identity.callerService())) {
            throw new AccessDeniedException("Approved BAT command requires authenticated ADM caller");
        }
        String approvedBy = actor(request, bodyApprovedBy, "approvedBy");
        String requestedBy = required(identity.approvalRequesterId(), "verified approval requester");
        String approvalRequestId = required(identity.approvalRequestId(), "verified approval request id");
        rejectMismatch(bodyRequestedBy, requestedBy, "requestedBy");
        rejectMismatch(bodyApprovalRequestId, approvalRequestId, "approvalRequestId");
        if (requestedBy.equals(approvedBy)) {
            throw new AccessDeniedException("BAT requester and approver must be different");
        }
        return new ApprovedActors(requestedBy, approvedBy, approvalRequestId);
    }

    private static Authentication authentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationCredentialsNotFoundException("BAT authentication is required");
        }
        return authentication;
    }

    private static BatAuthenticatedIdentity identity(Authentication authentication) {
        if (!(authentication.getDetails() instanceof BatAuthenticatedIdentity identity)) {
            throw new AuthenticationCredentialsNotFoundException(
                    "BAT authenticated identity details are required");
        }
        return identity;
    }

    private static void rejectMismatch(String supplied, String verified, String fieldName) {
        if (supplied != null && !supplied.isBlank() && !supplied.trim().equals(verified)) {
            throw new AccessDeniedException(fieldName + " does not match the verified BAT identity");
        }
    }

    private static String required(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new AuthenticationCredentialsNotFoundException(fieldName + " is required");
        }
        return value.trim();
    }

    public record ApprovedActors(
            String requestedBy,
            String approvedBy,
            String approvalRequestId) {
    }
}
