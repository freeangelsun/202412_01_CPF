package com.cpf.data.lock.valkey;

import com.cpf.data.lock.api.CpfLockManager;
import com.cpf.data.lock.spi.CpfLockStore;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;

/** Redis/Valkey CAS store using WATCH/MULTI for multi-instance lock transitions. */
public final class ValkeyCpfLockStore implements CpfLockStore {
 private final StringRedisTemplate redis; private final String namespace; private final int maxRetries;
 public ValkeyCpfLockStore(StringRedisTemplate redis,String namespace,int maxRetries){this.redis=redis;this.namespace=namespace;this.maxRetries=Math.max(1,maxRetries);}
 @Override public UpdateResult update(String key,UnaryOperator<StoredLock> transition){String rk=lockKey(key);for(int attempt=1;attempt<=maxRetries;attempt++){UpdateResult result=redis.execute(new SessionCallback<>(){public UpdateResult execute(RedisOperations operations){operations.watch(rk);StoredLock before=decode(key,operations.opsForHash().entries(rk));StoredLock after=transition.apply(before);if(after==before||java.util.Objects.equals(after,before)){operations.unwatch();return new UpdateResult(before,after);}operations.multi();if(after==null)operations.delete(rk);else operations.opsForHash().putAll(rk,encode(after));List exec=operations.exec();return exec==null?null:new UpdateResult(before,after);}});if(result!=null)return result;}throw new IllegalStateException("Valkey lock CAS conflict after "+maxRetries+" retries");}
 @Override public Optional<StoredLock> find(String key){return Optional.ofNullable(decode(key,redis.opsForHash().entries(lockKey(key))));}
 @Override public List<StoredLock> list(int limit){if(limit<1)return List.of();var keys=redis.keys(namespace+":lock:*");if(keys==null)return List.of();var result=new ArrayList<StoredLock>();for(String rk:keys){if(rk.endsWith(":fence"))continue;String key=rk.substring((namespace+":lock:").length());var found=find(key);found.ifPresent(result::add);if(result.size()>=limit)break;}return List.copyOf(result);}
 @Override public long nextFence(String key){Long value=redis.opsForValue().increment(lockKey(key)+":fence");if(value==null||value<1)throw new IllegalStateException("Valkey fencing increment failed");return value;}
 private String lockKey(String key){if(key==null||key.isBlank())throw new IllegalArgumentException("lock key required");return namespace+":lock:"+key.replaceAll("[^A-Za-z0-9._:-]","_");}
 private static Map<String,String> encode(StoredLock s){var m=new HashMap<String,String>();m.put("ownerId",s.ownerId()==null?"":s.ownerId());m.put("requestId",s.requestId()==null?"":s.requestId());m.put("fencingToken",Long.toString(s.fencingToken()));m.put("ownerEpoch",Long.toString(s.ownerEpoch()));m.put("rowVersion",Long.toString(s.rowVersion()));m.put("acquiredAt",s.acquiredAt().toString());m.put("leaseUntil",s.leaseUntil().toString());m.put("state",s.state().name());m.put("lastReason",s.lastReason()==null?"":s.lastReason());m.put("lastAuditId",s.lastAuditId()==null?"":s.lastAuditId());return m;}
 private static StoredLock decode(String key,Map<Object,Object> raw){if(raw==null||raw.isEmpty())return null;Map<String,String> m=new HashMap<>();raw.forEach((k,v)->m.put(String.valueOf(k),String.valueOf(v)));return new StoredLock(key,blank(m.get("ownerId")),blank(m.get("requestId")),Long.parseLong(m.get("fencingToken")),Long.parseLong(m.get("ownerEpoch")),Long.parseLong(m.get("rowVersion")),Instant.parse(m.get("acquiredAt")),Instant.parse(m.get("leaseUntil")),CpfLockManager.State.valueOf(m.get("state")),blank(m.get("lastReason")),blank(m.get("lastAuditId")));}
 private static String blank(String s){return s==null||s.isBlank()?null:s;}
}
