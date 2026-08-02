package com.cpf.core.common.http;
import java.util.*;import java.util.concurrent.atomic.AtomicReference;
/** Webhook Callback Client가 실제 소비하는 immutable Runtime snapshot입니다. */
public final class CpfWebhookRuntimePolicy{
 private final AtomicReference<Snapshot>ref=new AtomicReference<>(new Snapshot(0,Map.of()));public Snapshot current(){return ref.get();}
 public Snapshot replace(long version,Map<String,Callback>callbacks){LinkedHashMap<String,Callback>m=new LinkedHashMap<>();if(callbacks!=null)callbacks.forEach((k,v)->{if(v==null)throw new IllegalArgumentException("null webhook");String id=k==null||k.isBlank()?v.callbackId():k;Callback n=v.normalize(id);if(m.putIfAbsent(n.callbackId(),n)!=null)throw new IllegalArgumentException("callbackId 중복");});Snapshot n=new Snapshot(version,Map.copyOf(m));ref.set(n);return n;}
 public Callback require(String id){Callback c=ref.get().callbacks().get(id);if(c==null||!c.active())throw new IllegalArgumentException("Webhook callback이 없거나 비활성입니다.");return c;}
 public record Callback(String callbackId,String serviceId,String path,String signatureRef,String idempotencyHeader,int timeoutMillis,int retryCount,boolean active){public Callback{if(timeoutMillis<1||timeoutMillis>300000||retryCount<0||retryCount>10)throw new IllegalArgumentException("webhook policy 범위 오류");}private Callback normalize(String id){if(id==null||id.isBlank()||serviceId==null||serviceId.isBlank()||path==null||path.isBlank())throw new IllegalArgumentException("callbackId/serviceId/path 필수");String p=path.trim();if(!p.startsWith("/"))p="/"+p;return new Callback(id.trim(),serviceId.trim(),p,signatureRef==null?"":signatureRef.trim(),idempotencyHeader==null||idempotencyHeader.isBlank()?"Idempotency-Key":idempotencyHeader.trim(),timeoutMillis,retryCount,active);}}
 public record Snapshot(long version,Map<String,Callback>callbacks){public Snapshot{callbacks=callbacks==null?Map.of():Map.copyOf(callbacks);}}
}
