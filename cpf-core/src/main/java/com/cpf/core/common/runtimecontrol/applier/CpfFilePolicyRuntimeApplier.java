package com.cpf.core.common.runtimecontrol.applier;
import com.cpf.core.api.runtimecontrol.*;import com.cpf.core.common.filetransfer.CpfFileTransferRuntimeState;import java.util.*;
/** 실제 FileTransferEngine의 검증 정책을 hot-apply합니다. */
public final class CpfFilePolicyRuntimeApplier implements CpfRuntimeChangeApplier{
 private final CpfFileTransferRuntimeState state;public CpfFilePolicyRuntimeApplier(CpfFileTransferRuntimeState state){this.state=state;}
 public String changeType(){return "FILE_POLICY";}public boolean supportsIdempotentReplay(){return true;}public boolean snapshotCapable(){return true;}
 public CpfRuntimeApplyResult apply(CpfRuntimeDelivery d){try{var p=d.payload();var policy=new CpfFileTransferRuntimeState.FilePolicy(num(p.get("maxFileSize"),1073741824L),set(p.get("allowedExtensions")),set(p.get("allowedMimeTypes")),bool(p.get("checksumRequired"),true),bool(p.get("scanRequired"),false),bool(p.get("quarantineOnFailure"),true));state.replacePolicy(policy);return CpfRuntimeApplyResult.success(d.payloadHash());}catch(RuntimeException e){return CpfRuntimeApplyResult.failure("FILE_POLICY_INVALID","File policy payload가 유효하지 않습니다.");}}
 private long num(Object v,long f){return v instanceof Number n?n.longValue():v==null?f:Long.parseLong(String.valueOf(v));}private boolean bool(Object v,boolean f){return v instanceof Boolean b?b:v==null?f:Boolean.parseBoolean(String.valueOf(v));}private Set<String>set(Object v){if(!(v instanceof List<?>l))return Set.of();LinkedHashSet<String>s=new LinkedHashSet<>();for(Object x:l)if(x!=null)s.add(String.valueOf(x));return Set.copyOf(s);}
}
