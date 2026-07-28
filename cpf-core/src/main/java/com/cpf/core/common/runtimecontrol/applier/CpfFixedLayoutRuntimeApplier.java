package com.cpf.core.common.runtimecontrol.applier;
import com.cpf.core.api.fixedlength.CpfFixedLengthLayoutRegistry;
import com.cpf.core.api.runtimecontrol.*;
import java.util.Map;
public final class CpfFixedLayoutRuntimeApplier implements CpfRuntimeChangeApplier {
 private final CpfFixedLengthLayoutRegistry registry;private final CpfFixedLengthLayoutPayloadDecoder decoder=new CpfFixedLengthLayoutPayloadDecoder();
 public CpfFixedLayoutRuntimeApplier(CpfFixedLengthLayoutRegistry registry){this.registry=registry;}
 public String changeType(){return "FIXED_LAYOUT";}public boolean supportsIdempotentReplay(){return true;}public boolean snapshotCapable(){return true;}
 @SuppressWarnings("unchecked") public CpfRuntimeApplyResult apply(CpfRuntimeDelivery d){try{Object raw=d.payload().get("layout");if(!(raw instanceof Map<?,?> m))throw new IllegalArgumentException("layout object 필수");registry.upsert(d.desiredVersion(),decoder.decode((Map<String,Object>)m),String.valueOf(d.payload().getOrDefault("expectedRegistryHash","")));return CpfRuntimeApplyResult.success(d.payloadHash());}catch(RuntimeException e){return CpfRuntimeApplyResult.failure("FIXED_LAYOUT_INVALID","Fixed layout payload/compatibility 오류");}}
}
