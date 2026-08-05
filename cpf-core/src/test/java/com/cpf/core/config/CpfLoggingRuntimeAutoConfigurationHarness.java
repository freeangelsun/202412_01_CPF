package com.cpf.core.config;
import com.cpf.core.api.logging.CpfDynamicLogLevelOperations;
import com.cpf.core.api.observability.CpfTelemetry;
import com.cpf.core.api.security.CpfSensitiveDataAccessOperations.AccessRequestCommand;
import com.cpf.core.api.security.CpfMaskingPolicyApproval;
import com.cpf.core.api.security.CpfMaskingPolicyResult;
import com.cpf.core.api.security.CpfMaskingPolicyUpdateCommand;
import com.cpf.core.common.logging.file.CpfFileLogWriter;
import com.cpf.core.api.security.CpfSensitiveDataAccessOperations.AccessStatus;
import com.cpf.core.internal.security.InMemoryCpfSensitiveDataAccessStore;
import com.cpf.core.common.logging.CpfTraceSamplingPolicy;
import com.cpf.core.common.logging.DefaultCpfDynamicLogLevelOperations;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.env.Environment;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.function.Supplier;
public final class CpfLoggingRuntimeAutoConfigurationHarness {
    private CpfLoggingRuntimeAutoConfigurationHarness() {}
    public static void main(String[] args) {
        Environment environment = new Environment() {
            @Override public <T> T getProperty(String key, Class<T> type, T defaultValue) {
                if ("cpf.logging.dynamic-level.max-ttl".equals(key)) return type.cast(Duration.ofMinutes(30));
                if ("cpf.logging.dynamic-level.max-active-rules".equals(key)) return type.cast(Integer.valueOf(3));
                if ("cpf.logging.dynamic-level.max-audit-records".equals(key)) return type.cast(Integer.valueOf(5));
                if ("cpf.tracing.sampling.maximum-override-entries".equals(key)) return type.cast(Integer.valueOf(7));
                if ("cpf.tracing.max-active-spans".equals(key)) return type.cast(Integer.valueOf(7));
                if ("cpf.security.sensitive-access.in-memory.maximum-grants".equals(key))
                    return type.cast(Integer.valueOf(2));
                if ("cpf.security.sensitive-access.in-memory.terminal-retention".equals(key))
                    return type.cast(Duration.ofMinutes(10));
                if ("cpf.security.masking-policy.in-memory.maximum-history".equals(key))
                    return type.cast(Integer.valueOf(4));
                if ("cpf.security.masking-policy.in-memory.maximum-command-records".equals(key))
                    return type.cast(Integer.valueOf(16));
                if ("cpf.security.masking-policy.in-memory.command-ttl".equals(key))
                    return type.cast(Duration.ofHours(1));
                return defaultValue;
            }
        };
        Clock fixedClock = Clock.fixed(Instant.parse("2026-08-05T00:00:00Z"), ZoneOffset.UTC);
        ObjectProvider<Clock> clockProvider = new FixedProvider<>(fixedClock);
        CpfLoggingRuntimeAutoConfiguration configuration = new CpfLoggingRuntimeAutoConfiguration();
        DefaultCpfDynamicLogLevelOperations operations =
                configuration.cpfDynamicLogLevelOperations(environment, clockProvider);
        CpfTraceSamplingPolicy policy = configuration.cpfTraceSamplingPolicy(
                new FixedProvider<>(operations), environment);
        CpfTelemetry telemetry = configuration.cpfTelemetry(environment, clockProvider);
        InMemoryCpfSensitiveDataAccessStore accessStore =
                configuration.cpfSensitiveDataAccessStore(environment, clockProvider);
        var accessManager = configuration.cpfSensitiveDataAccessOperations(
                accessStore, (action, result, grant, actor, at, error) -> { }, clockProvider);
        var accessResult = accessManager.request(new AccessRequestCommand(
                "autoconfig-raw-001", "autoconfig-idem-001", "operator-auto",
                "TRANSACTION_LOG", "a".repeat(64), "DETAIL:ERROR",
                "자동 구성 민감정보 승인 경로를 검증하는 요청 사유"));
        CpfFileLogWriter fileWriter = new CpfFileLogWriter();
        var maskingStore = configuration.cpfMaskingPolicyStore(environment, clockProvider);
        var maskingAudit = configuration.cpfMaskingPolicyAuditSink(fileWriter);
        var maskingManager = configuration.cpfMaskingPolicyOperations(
                maskingStore, maskingAudit, clockProvider);
        long maskVersion = maskingManager.current().version();
        CpfMaskingPolicyUpdateCommand maskCommand = new CpfMaskingPolicyUpdateCommand(
                "mask-auto-0001", maskVersion, java.util.Set.of("password", "autosecret"),
                1024, true, "mask-requester", "자동 구성 마스킹 정책 경로 검증 사유입니다", null);
        maskCommand = new CpfMaskingPolicyUpdateCommand(maskCommand.commandId(),
                maskCommand.expectedVersion(), maskCommand.sensitiveKeys(), maskCommand.maxLength(),
                maskCommand.maskBearerToken(), maskCommand.actor(), maskCommand.reason(),
                new CpfMaskingPolicyApproval(maskCommand.commandHash(), "mask-approver",
                        fixedClock.instant().minusSeconds(1), fixedClock.instant().plusSeconds(60)));
        CpfMaskingPolicyResult maskResult = maskingManager.update(maskCommand);
        if (operations == null || policy == null || telemetry == null) {
            throw new AssertionError("logging/tracing runtime beans missing");
        }
        if (((Number) telemetry.status().get("maxActiveSpans")).intValue() != 7) {
            throw new AssertionError("tracing resource bound not applied");
        }
        if (accessResult.status() != AccessStatus.PENDING
                || accessStore.snapshot().maximumGrants() != 2
                || !Duration.ofMinutes(10).equals(accessStore.snapshot().terminalRetention())) {
            throw new AssertionError("sensitive access runtime wiring not applied");
        }
        if (maskResult.status() != CpfMaskingPolicyResult.Status.APPLIED
                || maskingStore.runtimeStatus().maximumHistory() != 4
                || maskingStore.runtimeStatus().maximumCommandRecords() != 16) {
            throw new AssertionError("masking policy runtime wiring not applied");
        }
        CpfDynamicLogLevelOperations contract = operations;
        if (contract.findActiveRules() == null) throw new AssertionError("public contract unavailable");
        if (telemetry instanceof AutoCloseable closeable) {
            try { closeable.close(); } catch (Exception failure) { throw new AssertionError(failure); }
        }
        System.out.println("CPF_LOGGING_AUTOCONFIG_HARNESS_PASS");
    }
    private record FixedProvider<T>(T value) implements ObjectProvider<T> {
        @Override public T getIfAvailable() { return value; }
        @Override public T getIfUnique(Supplier<T> defaultSupplier) {
            return value == null ? defaultSupplier.get() : value;
        }
    }
}
