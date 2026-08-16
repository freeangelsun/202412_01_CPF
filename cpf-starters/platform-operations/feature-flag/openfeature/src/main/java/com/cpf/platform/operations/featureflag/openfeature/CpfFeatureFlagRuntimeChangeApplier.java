package com.cpf.platform.operations.featureflag.openfeature;

import com.cpf.platform.operations.api.featureflag.CpfFeatureFlagOperations;
import com.cpf.platform.operations.api.featureflag.CpfFeatureFlagResult;
import com.cpf.platform.operations.api.featureflag.CpfFeatureFlagValue;
import com.cpf.platform.operations.runtimecontrol.CpfRuntimeApplyResult;
import com.cpf.platform.operations.runtimecontrol.CpfRuntimeChangeApplier;
import com.cpf.platform.operations.runtimecontrol.CpfRuntimeDelivery;

import java.util.Objects;
import java.util.Set;

/**
 * Runtime Control delivery를 Feature Flag kill-switch 실제 Consumer에 연결합니다.
 *
 * <p>지원 payload schema 1:</p>
 * <pre>{@code
 * {"flagKey":"payments.write.enabled","enabled":false,"reason":"incident mitigation"}
 * }</pre>
 */
final class CpfFeatureFlagRuntimeChangeApplier implements CpfRuntimeChangeApplier {
    private static final Set<String> SUPPORTED_FIELDS = Set.of("flagKey", "enabled", "reason");
    private static final int MAX_FLAG_KEY_LENGTH = 200;
    private static final int MAX_REASON_LENGTH = 500;

    private final CpfFeatureFlagOperations operations;

    CpfFeatureFlagRuntimeChangeApplier(CpfFeatureFlagOperations operations) {
        this.operations = Objects.requireNonNull(operations, "operations");
    }

    @Override
    public String changeType() {
        return "CONFIG_PARAMETER_FEATURE_FLAG";
    }

    @Override
    public boolean supportsIdempotentReplay() {
        return true;
    }

    @Override
    public boolean snapshotCapable() {
        return false;
    }

    @Override
    public CpfRuntimeApplyResult apply(CpfRuntimeDelivery delivery) {
        final Command command;
        try {
            command = parse(delivery);
        } catch (RuntimeException invalid) {
            return CpfRuntimeApplyResult.failure(
                    "FEATURE_FLAG_INVALID",
                    "Feature Flag Runtime payload가 schema/allowlist 계약을 위반했습니다.");
        }

        try {
            if (alreadyApplied(command)) {
                return CpfRuntimeApplyResult.success(delivery.payloadHash());
            }
        } catch (IllegalArgumentException notFound) {
            // 신규 flag kill-switch는 find가 not-found일 수 있으므로 실제 변경을 계속합니다.
        } catch (RuntimeException lookupFailure) {
            return CpfRuntimeApplyResult.failure(
                    "FEATURE_FLAG_LOOKUP_FAILED",
                    "Feature Flag 현재 상태 조회에 실패했습니다.");
        }

        String operatorId = "runtime-control:" + delivery.changeId();
        try {
            operations.setKillSwitch(command.flagKey(), command.enabled(), operatorId, command.reason());
            if (!alreadyApplied(command)) {
                return CpfRuntimeApplyResult.unknown(
                        "FEATURE_FLAG_VERIFY_UNKNOWN",
                        "Feature Flag 변경 호출 후 실제 상태가 일치하지 않아 결과를 확정할 수 없습니다.");
            }
            return CpfRuntimeApplyResult.success(delivery.payloadHash());
        } catch (RuntimeException sideEffectUnknown) {
            return CpfRuntimeApplyResult.unknown(
                    "FEATURE_FLAG_APPLY_UNKNOWN",
                    "Feature Flag 변경 중 side effect 발생 여부를 확정할 수 없습니다.");
        }
    }

    private boolean alreadyApplied(Command command) {
        CpfFeatureFlagResult<CpfFeatureFlagValue> actual = operations.find(command.flagKey());
        if (actual.source() != CpfFeatureFlagResult.Source.KILL_SWITCH) {
            return false;
        }
        return actual.value() instanceof CpfFeatureFlagValue.BooleanValue value
                && value.value() == command.enabled();
    }

    private Command parse(CpfRuntimeDelivery delivery) {
        if (delivery.payloadSchemaVersion() != 1) {
            throw new IllegalArgumentException("지원하지 않는 Feature Flag payload schema version");
        }
        for (String field : delivery.payload().fieldNames()) {
            if (!SUPPORTED_FIELDS.contains(field)) {
                throw new IllegalArgumentException(
                        "지원하지 않는 Feature Flag payload field: " + field);
            }
        }

        String flagKeyValue = delivery.payload().text("flagKey", null);
        if (flagKeyValue == null) {
            throw new IllegalArgumentException("flagKey는 필수입니다.");
        }
        String flagKey = flagKeyValue.trim();
        if (flagKey.isEmpty() || flagKey.length() > MAX_FLAG_KEY_LENGTH) {
            throw new IllegalArgumentException("flagKey 길이가 올바르지 않습니다.");
        }
        if (!delivery.payload().contains("enabled")) {
            throw new IllegalArgumentException("enabled는 필수입니다.");
        }
        boolean enabled = delivery.payload().booleanValue("enabled", false);

        String reasonValue = delivery.payload().text(
                "reason", "Runtime Control kill-switch apply");
        String reason = reasonValue.trim();
        if (reason.isEmpty() || reason.length() > MAX_REASON_LENGTH) {
            throw new IllegalArgumentException("reason 길이가 올바르지 않습니다.");
        }
        return new Command(flagKey, enabled, reason);
    }

    private record Command(String flagKey, boolean enabled, String reason) {}
}
