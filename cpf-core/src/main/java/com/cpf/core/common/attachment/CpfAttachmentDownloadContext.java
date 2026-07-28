package com.cpf.core.common.attachment;
import java.time.Instant;import java.util.function.Supplier;
/** 기존 read 시그니처를 유지하면서 검증된 다운로드 권한 Context를 전달합니다. */
public final class CpfAttachmentDownloadContext{
 private static final ThreadLocal<Context> CURRENT=new ThreadLocal<>();private CpfAttachmentDownloadContext(){}
 public static <T>T with(Context context,Supplier<T> action){Context old=CURRENT.get();CURRENT.set(context);try{return action.get();}finally{if(old==null)CURRENT.remove();else CURRENT.set(old);}}
 public static Context current(){return CURRENT.get();}
 public record Context(String operatorId,boolean permitted,String approvalId,Instant requestedAt,Instant expiresAt){public Context{requestedAt=requestedAt==null?Instant.now():requestedAt;}}
}
