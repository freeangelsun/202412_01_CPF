package com.cpf.starter.security.runtimecontrol;

import com.cpf.core.api.runtimecontrol.CpfRuntimeApplyResult;
import com.cpf.core.api.runtimecontrol.CpfRuntimeChangeApplier;
import com.cpf.core.api.runtimecontrol.CpfRuntimeDelivery;
import com.cpf.starter.runtimecontrol.spi.CpfRuntimePayloadReader;
import com.cpf.core.common.security.password.CpfPasswordRuntimePolicy;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** 신규/변경 비밀번호에 실제 집행되는 Complexity Policy를 hot-apply합니다. */
public final class CpfPasswordPolicyRuntimeApplier implements CpfRuntimeChangeApplier {
    public static final String CHANGE_TYPE = "PASSWORD_POLICY";
    private final CpfPasswordRuntimePolicy policy;
    public CpfPasswordPolicyRuntimeApplier(CpfPasswordRuntimePolicy policy) { this.policy = policy; }
    @Override public String changeType() { return CHANGE_TYPE; }
    @Override public boolean supportsIdempotentReplay() { return true; }
    @Override public boolean snapshotCapable() { return true; }

    @Override
    public CpfRuntimeApplyResult apply(CpfRuntimeDelivery delivery) {
        if (CpfRuntimePayloadReader.contains(delivery.payload(), "pepper")
                || CpfRuntimePayloadReader.contains(delivery.payload(), "iterations")
                || CpfRuntimePayloadReader.contains(delivery.payload(), "keyBits")) {
            return CpfRuntimeApplyResult.failure("PASSWORD_HASHING_MATERIAL_NOT_HOT_APPLICABLE",
                    "PBKDF2 material 변경은 배포/재기동 절차로 수행해야 합니다.");
        }
        try {
            CpfPasswordRuntimePolicy.Snapshot current = policy.current();
            long version = number(
                    CpfRuntimePayloadReader.value(delivery.payload(), "version"),
                    delivery.desiredVersion());
            CpfPasswordRuntimePolicy.Snapshot applied = policy.replace(
                    version,
                    integer(CpfRuntimePayloadReader.value(delivery.payload(), "minLength"), current.minLength()),
                    integer(CpfRuntimePayloadReader.value(delivery.payload(), "maxLength"), current.maxLength()),
                    bool(CpfRuntimePayloadReader.value(delivery.payload(), "requireUppercase"), current.requireUppercase()),
                    bool(CpfRuntimePayloadReader.value(delivery.payload(), "requireLowercase"), current.requireLowercase()),
                    bool(CpfRuntimePayloadReader.value(delivery.payload(), "requireDigit"), current.requireDigit()),
                    bool(CpfRuntimePayloadReader.value(delivery.payload(), "requireSpecial"), current.requireSpecial()),
                    strings(CpfRuntimePayloadReader.value(delivery.payload(), "forbiddenFragments")));
            if (applied.version() != version) return CpfRuntimeApplyResult.failure("PASSWORD_POLICY_NOT_CONFIRMED", "Password policy version 불일치");
            return CpfRuntimeApplyResult.success(delivery.payloadHash());
        } catch (RuntimeException ex) {
            return CpfRuntimeApplyResult.failure("PASSWORD_POLICY_INVALID", "Password policy payload가 유효하지 않습니다.");
        }
    }
    private Set<String> strings(Object raw) { if (!(raw instanceof List<?> l)) return Set.of(); LinkedHashSet<String>s=new LinkedHashSet<>(); for(Object v:l)if(v!=null)s.add(String.valueOf(v)); return Set.copyOf(s); }
    private int integer(Object v,int f){return v instanceof Number n?n.intValue():v==null?f:Integer.parseInt(String.valueOf(v));}
    private long number(Object v,long f){return v instanceof Number n?n.longValue():v==null?f:Long.parseLong(String.valueOf(v));}
    private boolean bool(Object v,boolean f){return v instanceof Boolean b?b:v==null?f:Boolean.parseBoolean(String.valueOf(v));}
}
