package com.cpf.gateway.context;

import com.cpf.core.api.context.CpfContext;
import com.cpf.core.api.context.CpfContextSnapshot;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.foundation.id.spi.CpfExecutionIdGenerator;
import java.time.Instant;
import java.util.Objects;
import org.springframework.stereotype.Component;

/**
 * Web Profile이 만든 request root에 Gateway의 인증 결과와 upstream child execution을 반영합니다.
 * route/target 메타데이터는 Gateway Owner가 별도로 보유하며 Core Snapshot에 삽입하지 않습니다.
 */
@Component
public final class CpfGatewayContextFactory {
    private final CpfExecutionIdGenerator executionIds;

    public CpfGatewayContextFactory(CpfExecutionIdGenerator executionIds) {
        this.executionIds = Objects.requireNonNull(executionIds, "executionIds");
    }

    public EnrichedContext enrichCurrent(
            boolean authenticated,
            String subject,
            String actor,
            String authenticationContextId,
            String tenant,
            String standardExecutionId,
            String route,
            String version,
            String targetService,
            String gatewayInstance,
            Instant deadline) {
        CpfContextSnapshot current = CpfContexts.requireSnapshot();
        if (authenticated && (subject == null || subject.isBlank())) {
            throw new IllegalArgumentException("authenticated principal required");
        }
        CpfContext.CpfIdentityContext identity = authenticated
                ? new CpfContext.CpfIdentityContext(subject, actor, CpfContext.CpfPrincipalType.USER,
                        authenticationContextId, null, null, Instant.now())
                : new CpfContext.CpfIdentityContext(null, null, CpfContext.CpfPrincipalType.ANONYMOUS);
        CpfContext.CpfTenantContext tenantContext = tenant == null || tenant.isBlank()
                ? null : new CpfContext.CpfTenantContext(tenant);
        CpfContext context = current.context().withIdentityAndTenant(identity, tenantContext);
        if (deadline != null && (context.execution().deadline() == null || deadline.isBefore(context.execution().deadline()))) {
            var execution = context.execution();
            var bounded = new CpfContext.CpfExecutionContext(
                    execution.standardExecutionId(), execution.executionId(), execution.rootExecutionId(),
                    execution.parentExecutionId(), execution.segmentId(), execution.parentSegmentId(),
                    execution.executionType(), execution.attempt(), execution.callDepth(), execution.startedAt(),
                    deadline, execution.cancellationMode());
            context = new CpfContext(context.transaction(), bounded, context.operation(), context.identity(), context.tenant());
        }
        CpfGatewayContext gateway = new CpfGatewayContext(
                "HTTP", route, version, targetService, gatewayInstance,
                authenticated ? "AUTHENTICATED" : "ANONYMOUS",
                authenticated ? subject : null,
                tenantContext == null ? null : tenantContext.tenantId());
        return new EnrichedContext(CpfContextSnapshot.capture(context), gateway);
    }

    public AutoCloseable bindRoute(
            boolean authenticated, String subject, String actor, String tenant, String route,
            String version, String targetService, String gatewayInstance) {
        CpfContext current = CpfContexts.requireCurrent();
        return CpfContexts.bind(enrichCurrent(
                authenticated, subject, actor, null, tenant,
                current.execution().standardExecutionId(), route, version, targetService, gatewayInstance,
                current.execution().deadline()).snapshot());
    }

    public UpstreamAttempt bindUpstreamAttempt(
            String standardExecutionId, int attempt, String targetService, String targetInstance, Instant deadline) {
        CpfContext parent = CpfContexts.requireCurrent();
        Instant now = Instant.now();
        CpfContext.CpfExecutionContext child = parent.execution().child(
                standardExecutionId,
                executionIds.newExecutionId(),
                executionIds.newSegmentId(),
                CpfContext.CpfExecutionType.INTEGRATION,
                Math.max(1, attempt), now, deadline);
        CpfContext childContext = parent.child(child, parent.operation());
        CpfGatewayContext metadata = new CpfGatewayContext(
                "UPSTREAM", null, null, targetService, targetInstance, "TRUSTED_INTERNAL",
                parent.identity() == null ? null : parent.identity().subjectId(),
                parent.tenant() == null ? null : parent.tenant().tenantId());
        CpfContextSnapshot snapshot = CpfContextSnapshot.capture(childContext);
        return new UpstreamAttempt(CpfContexts.bind(snapshot), snapshot, metadata);
    }

    public record EnrichedContext(CpfContextSnapshot snapshot, CpfGatewayContext gateway) { }
    public record UpstreamAttempt(AutoCloseable scope, CpfContextSnapshot snapshot, CpfGatewayContext gateway)
            implements AutoCloseable {
        @Override public void close() throws Exception { scope.close(); }
    }
}
