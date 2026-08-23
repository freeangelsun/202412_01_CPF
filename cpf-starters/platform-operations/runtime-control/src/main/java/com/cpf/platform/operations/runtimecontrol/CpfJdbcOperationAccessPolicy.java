package com.cpf.platform.operations.runtimecontrol;

import com.cpf.data.persistence.api.database.CpfVendorSqlCatalog;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalogProvider;
import com.cpf.foundation.execution.api.CpfOperationAccessPolicy;
import com.cpf.platform.operations.channelregistry.application.CpfChannelPolicyService;
import com.cpf.platform.operations.channelregistry.model.CpfChannelPolicyDecision;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

/** Canonical Operation Policy를 LKG로 캐시하고 Controller 전 fail-close로 평가합니다. */
public final class CpfJdbcOperationAccessPolicy implements CpfOperationAccessPolicy {
    public enum Status { CURRENT, STALE, EXPIRED, REFRESH_FAILED }

    public record RuntimeStatus(
            long policyVersion, Instant loadedAt, Instant expiresAt, Status status, String reason) {}

    private record SystemEntry(String domain, boolean enabled) {}

    private record OperationEntry(
            boolean enabled, boolean allCallers, boolean channelPolicyRequired, long version) {}

    private record Snapshot(
            long version,
            Instant loadedAt,
            Instant expiresAt,
            Status status,
            Map<String, SystemEntry> systems,
            Map<String, Boolean> domainAccess,
            Map<String, OperationEntry> operations,
            Map<String, Boolean> callerAccess) {}

    private final JdbcTemplate jdbc;
    private final CpfVendorSqlCatalog sql;
    private final Duration refreshInterval;
    private final Duration maxStale;
    private final Clock clock;
    private final CpfChannelPolicyService channelPolicies;
    private final AtomicReference<Snapshot> snapshot = new AtomicReference<>();
    private volatile Instant nextRefresh = Instant.EPOCH;

    public CpfJdbcOperationAccessPolicy(
            JdbcTemplate jdbc,
            CpfVendorSqlCatalogProvider catalogs,
            Duration refreshInterval,
            Duration maxStale,
            Clock clock,
            CpfChannelPolicyService channelPolicies) {
        this.jdbc = Objects.requireNonNull(jdbc, "jdbc");
        this.sql = Objects.requireNonNull(catalogs, "catalogs").forModule("cpf");
        this.refreshInterval = positive(refreshInterval, "refreshInterval");
        this.maxStale = positive(maxStale, "maxStale");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.channelPolicies = channelPolicies;
        refreshRequired();
    }

    /** Channel Registry가 선택되지 않은 구성과의 호환 생성자입니다. Required Channel은 fail-close됩니다. */
    public CpfJdbcOperationAccessPolicy(
            JdbcTemplate jdbc, CpfVendorSqlCatalogProvider catalogs,
            Duration refreshInterval, Duration maxStale, Clock clock) {
        this(jdbc, catalogs, refreshInterval, maxStale, clock, null);
    }

    @Override
    public Decision evaluate(Request request) {
        Objects.requireNonNull(request, "request");
        refreshIfDue();
        Snapshot current = snapshot.get();
        Instant now = clock.instant();
        if (current == null || !now.isBefore(current.expiresAt())) {
            return Decision.deny("OPERATION_POLICY_EXPIRED", current == null ? -1 : current.version());
        }

        String target = code(request.targetSystemCode());
        SystemEntry targetSystem = current.systems().get(target);
        if (targetSystem == null) return Decision.deny("TARGET_SYSTEM_NOT_REGISTERED", current.version());
        if (!targetSystem.enabled()) return Decision.deny("TARGET_SYSTEM_DISABLED", current.version());

        OperationEntry operation = current.operations().get(request.operationId());
        if (operation == null) return Decision.deny("OPERATION_NOT_REGISTERED", current.version());
        if (!operation.enabled()) return Decision.deny("OPERATION_DISABLED", operation.version());
        long version = Math.max(current.version(), operation.version());

        if (request.trustedInternal()) {
            // System/Domain identity is trusted registry metadata, not a reinterpretation of Channel headers.
            String caller = code(request.callerSystemCode());
            if (caller == null) return Decision.deny("CALLER_NOT_REGISTERED", version);
            SystemEntry callerSystem = current.systems().get(caller);
            if (callerSystem == null) return Decision.deny("CALLER_NOT_REGISTERED", version);
            if (!callerSystem.enabled()) return Decision.deny("CALLER_DISABLED", version);

            if (!caller.equals(target)) {
                Boolean domainAllowed = current.domainAccess().get(caller + "->" + target);
                if (!Boolean.TRUE.equals(domainAllowed)) return Decision.deny("SYSTEM_DOMAIN_DENY", version);
            }

            if (!operation.allCallers()) {
                Boolean allowed = current.callerAccess().get(request.operationId() + "|" + caller);
                if (!Boolean.TRUE.equals(allowed)) return Decision.deny("OPERATION_CALLER_DENY", version);
            }
            // ALL is symbolic: caller must still be registered/enabled above.
        }

        return evaluateCallerChannel(request, operation, version);
    }

    private Decision evaluateCallerChannel(Request request, OperationEntry operation, long operationVersion) {
        if (!operation.channelPolicyRequired()) return Decision.allow(operationVersion);
        if (request.callerChannel() == null) return Decision.deny("CHANNEL_REQUIRED", operationVersion);
        if (channelPolicies == null) return Decision.deny("CHANNEL_POLICY_UNAVAILABLE", operationVersion);

        CpfChannelPolicyDecision channel = channelPolicies.evaluateCallerChannel(
                request.operationId(), request.callerChannel(), request.authenticated(), request.signed());
        long version = Math.max(operationVersion, channel.snapshotVersion());
        return channel.allowed() ? Decision.allow(version) : Decision.deny("CHANNEL_POLICY_DENY", version);
    }

    public synchronized RuntimeStatus refresh() {
        try {
            return status(refreshRequired());
        } catch (RuntimeException failure) {
            Snapshot current = snapshot.get();
            Instant now = clock.instant();
            if (current != null && now.isBefore(current.expiresAt())) {
                Snapshot lkg = new Snapshot(
                        current.version(), current.loadedAt(), current.expiresAt(), Status.REFRESH_FAILED,
                        current.systems(), current.domainAccess(), current.operations(), current.callerAccess());
                snapshot.set(lkg);
                nextRefresh = now.plus(refreshInterval);
                return status(lkg);
            }
            if (current != null) {
                snapshot.set(new Snapshot(
                        current.version(), current.loadedAt(), current.expiresAt(), Status.EXPIRED,
                        current.systems(), current.domainAccess(), current.operations(), current.callerAccess()));
            }
            throw failure;
        }
    }

    public RuntimeStatus runtimeStatus() {
        Snapshot current = snapshot.get();
        return current == null
                ? new RuntimeStatus(-1, null, null, Status.EXPIRED, "NO_LKG")
                : status(current);
    }

    private void refreshIfDue() {
        if (!clock.instant().isBefore(nextRefresh)) refresh();
    }

    private Snapshot refreshRequired() {
        Instant now = clock.instant();
        Map<String, SystemEntry> systems = new HashMap<>();
        jdbc.query(
                sql.required("operation-policy-system-find-all"),
                (RowCallbackHandler) rs -> systems.put(code(rs.getString(1)), new SystemEntry(code(rs.getString(2)), yes(rs.getString(3)))));
        if (systems.isEmpty()) throw new IllegalStateException("OPS_SYSTEM_REGISTRY has no catalog data");

        Map<String, Boolean> domain = new HashMap<>();
        jdbc.query(
                sql.required("operation-policy-domain-access-find-all"),
                (RowCallbackHandler) rs -> domain.put(code(rs.getString(1)) + "->" + code(rs.getString(2)), yes(rs.getString(3))));

        Map<String, OperationEntry> operations = new HashMap<>();
        jdbc.query(
                sql.required("operation-policy-find-all"),
                (RowCallbackHandler) rs -> operations.put(rs.getString(1), new OperationEntry(
                        yes(rs.getString(2)), yes(rs.getString(3)), yes(rs.getString(4)), rs.getLong(5))));

        Map<String, Boolean> callers = new HashMap<>();
        jdbc.query(
                sql.required("operation-policy-caller-find-all"),
                (RowCallbackHandler) rs -> callers.put(rs.getString(1) + "|" + code(rs.getString(2)), yes(rs.getString(3))));

        Long version = jdbc.queryForObject(sql.required("operation-policy-version-current"), Long.class);
        Snapshot loaded = new Snapshot(
                version == null ? 0 : version,
                now,
                now.plus(maxStale),
                Status.CURRENT,
                Map.copyOf(systems),
                Map.copyOf(domain),
                Map.copyOf(operations),
                Map.copyOf(callers));
        snapshot.set(loaded);
        nextRefresh = now.plus(refreshInterval);
        return loaded;
    }

    private RuntimeStatus status(Snapshot value) {
        return new RuntimeStatus(value.version(), value.loadedAt(), value.expiresAt(), value.status(), value.status().name());
    }

    private static boolean yes(String value) {
        return "Y".equalsIgnoreCase(value);
    }

    private static String code(String value) {
        return value == null || value.isBlank() ? null : value.trim().toUpperCase(Locale.ROOT);
    }

    private static Duration positive(Duration value, String name) {
        if (value == null || value.isZero() || value.isNegative()) {
            throw new IllegalArgumentException(name + " must be positive");
        }
        return value;
    }
}
