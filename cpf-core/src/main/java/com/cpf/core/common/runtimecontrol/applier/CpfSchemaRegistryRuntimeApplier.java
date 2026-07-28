package com.cpf.core.common.runtimecontrol.applier;
import com.cpf.core.api.fixedlength.CpfFixedLengthLayout;
import com.cpf.core.api.fixedlength.CpfFixedLengthLayoutRegistry;
import com.cpf.core.api.runtimecontrol.*;
import java.util.*;
public final class CpfSchemaRegistryRuntimeApplier implements CpfRuntimeChangeApplier {
 private final CpfFixedLengthLayoutRegistry registry;private final CpfFixedLengthLayoutPayloadDecoder decoder=new CpfFixedLengthLayoutPayloadDecoder();
 public CpfSchemaRegistryRuntimeApplier(CpfFixedLengthLayoutRegistry registry){this.registry=registry;}
 public String changeType(){return "SCHEMA_REGISTRY";}public boolean supportsIdempotentReplay(){return true;}public boolean snapshotCapable(){return true;}
 @SuppressWarnings("unchecked") public CpfRuntimeApplyResult apply(CpfRuntimeDelivery d){try{Object raw=d.payload().get("layouts");if(!(raw instanceof List<?> list))throw new IllegalArgumentException("layouts array 필수");List<CpfFixedLengthLayout> layouts=new ArrayList<>();for(Object item:list){if(!(item instanceof Map<?,?> m))throw new IllegalArgumentException("layout object 필요");layouts.add(decoder.decode((Map<String,Object>)m));}var mode=CpfFixedLengthLayoutRegistry.Compatibility.valueOf(String.valueOf(d.payload().getOrDefault("compatibility","BACKWARD")).toUpperCase(Locale.ROOT));registry.replaceSnapshot(d.desiredVersion(),layouts,String.valueOf(d.payload().getOrDefault("expectedRegistryHash","")),mode);return CpfRuntimeApplyResult.success(d.payloadHash());}catch(RuntimeException e){return CpfRuntimeApplyResult.failure("SCHEMA_REGISTRY_INVALID","Schema registry payload/hash/compatibility 오류");}}
}
