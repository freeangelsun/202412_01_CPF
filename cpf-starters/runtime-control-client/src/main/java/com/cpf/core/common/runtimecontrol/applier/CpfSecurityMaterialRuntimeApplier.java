package com.cpf.core.common.runtimecontrol.applier;

import com.cpf.core.api.runtimecontrol.CpfRuntimeApplyResult;
import com.cpf.core.api.runtimecontrol.CpfRuntimeChangeApplier;
import com.cpf.core.api.runtimecontrol.CpfRuntimeDelivery;
import com.cpf.core.common.runtimecontrol.CpfRuntimePayloadJson;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** 실제 Provider reload SPI를 호출하는 Security material 공통 Applier입니다. */
public final class CpfSecurityMaterialRuntimeApplier implements CpfRuntimeChangeApplier {
    @FunctionalInterface public interface ReloadAction { String reload(Set<String> references, long desiredVersion); }
    private final String changeType;
    private final ReloadAction action;
    public CpfSecurityMaterialRuntimeApplier(String changeType, ReloadAction action) { this.changeType=changeType; this.action=action; }
    @Override public String changeType(){return changeType;}
    @Override public boolean supportsIdempotentReplay(){return true;}
    @Override public boolean snapshotCapable(){return true;}
    @Override public CpfRuntimeApplyResult apply(CpfRuntimeDelivery delivery){
        try {
            Set<String> refs = references(
                    CpfRuntimePayloadJson.value(delivery.payload(), "credentialReferences"));
            if(refs.isEmpty()) return CpfRuntimeApplyResult.failure("SECURITY_REFERENCE_REQUIRED","credentialReferences가 필요합니다.");
            String actualHash=action.reload(refs,delivery.desiredVersion());
            if(actualHash==null||actualHash.isBlank()) return CpfRuntimeApplyResult.failure("SECURITY_RELOAD_NOT_CONFIRMED","Provider가 적용 hash를 반환하지 않았습니다.");
            return CpfRuntimeApplyResult.success(actualHash);
        } catch(RuntimeException ex){ return CpfRuntimeApplyResult.failure("SECURITY_RELOAD_FAILED","Security material reload에 실패했습니다."); }
    }
    private Set<String> references(Object raw){if(!(raw instanceof List<?>l))return Set.of();LinkedHashSet<String>s=new LinkedHashSet<>();for(Object v:l)if(v!=null&&!String.valueOf(v).isBlank())s.add(String.valueOf(v).trim());return Set.copyOf(s);}
}
