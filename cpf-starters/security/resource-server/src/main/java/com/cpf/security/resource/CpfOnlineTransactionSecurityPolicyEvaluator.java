package com.cpf.security.resource;

import com.cpf.core.api.context.CpfContext;
import com.cpf.foundation.annotation.CpfOnlineTransaction;
import com.cpf.security.api.audit.CpfAuthorizationAuditEvent;
import com.cpf.security.api.audit.CpfAuthorizationAuditSink;
import com.cpf.web.api.CpfOnlineTransactionPolicyEvaluator;
import java.time.Clock;
import java.util.Objects;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/** Resource Server 환경에서 @CpfOnlineTransaction의 권한과 감사 사유를 실제 집행합니다. */
public final class CpfOnlineTransactionSecurityPolicyEvaluator implements CpfOnlineTransactionPolicyEvaluator {
    public static final String AUDIT_REASON_HEADER = "X-CPF-Audit-Reason";
    public static final String AUDIT_REASON_ATTRIBUTE = "cpf.audit.reason";
    private static final int MAX_AUDIT_REASON_LENGTH = 512;

    private final CpfAuthorizationAuditSink audit;
    private final Clock clock;

    /** CpfOnlineTransactionSecurityPolicyEvaluator 작업을 CPF 표준 계약에 따라 수행한다. */
    public CpfOnlineTransactionSecurityPolicyEvaluator(CpfAuthorizationAuditSink audit, Clock clock) {
        this.audit = Objects.requireNonNull(audit, "audit");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @Override public boolean supports(String ownerDomain) { return ownerDomain != null && !ownerDomain.isBlank(); }

    @Override
    public void verify(CpfOnlineTransaction tx, CpfContext context) {
        Objects.requireNonNull(tx, "transaction");
        Objects.requireNonNull(context, "context");
        try {
            verifyPermission(tx.requiredPermission());
            verifyAuditReason(tx.auditReasonRequired());
            audit.record(event(tx, context, true, "GRANTED"));
        // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
        } catch (RuntimeException e) {
            audit.record(event(tx, context, false, safeReason(e)));
            throw e;
        }
    }

    private static void verifyPermission(String permission) {
        if (permission == null || permission.isBlank()) return;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("CPF_ONLINE_TX_UNAUTHENTICATED");
        }
        String required = permission.trim();
        boolean granted = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                .anyMatch(required::equals);
        if (!granted) throw new SecurityException("CPF_ONLINE_TX_PERMISSION_DENIED");
    }

    private static void verifyAuditReason(boolean required) {
        if (!required) return;
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs)) {
            throw new SecurityException("CPF_ONLINE_TX_AUDIT_REASON_CONTEXT_MISSING");
        }
        String reason = attrs.getRequest().getHeader(AUDIT_REASON_HEADER);
        if (reason == null || reason.isBlank()) throw new SecurityException("CPF_ONLINE_TX_AUDIT_REASON_REQUIRED");
        String normalized = reason.trim();
        if (normalized.length() > MAX_AUDIT_REASON_LENGTH || normalized.chars().anyMatch(Character::isISOControl)) {
            throw new SecurityException("CPF_ONLINE_TX_AUDIT_REASON_INVALID");
        }
        attrs.getRequest().setAttribute(AUDIT_REASON_ATTRIBUTE, normalized);
    }

    private CpfAuthorizationAuditEvent event(CpfOnlineTransaction tx, CpfContext c, boolean allowed, String reason) {
        return new CpfAuthorizationAuditEvent("ONLINE_TRANSACTION", tx.id(), c.transactionId(), c.executionId(),
                c.subjectId(), c.actorId(), allowed, reason, clock.instant());
    }

    private static String safeReason(RuntimeException e) {
        String message = e.getMessage();
        return message == null || message.isBlank() ? e.getClass().getSimpleName() : message.substring(0, Math.min(160, message.length()));
    }
}
