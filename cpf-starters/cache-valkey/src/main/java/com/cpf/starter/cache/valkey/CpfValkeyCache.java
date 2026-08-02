package com.cpf.starter.cache.valkey;import java.time.Duration;import java.util.Optional;import org.springframework.data.redis.core.StringRedisTemplate;
public final class CpfValkeyCache {private final StringRedisTemplate redis;private final CpfValkeyProperties p;public CpfValkeyCache(StringRedisTemplate r,CpfValkeyProperties p){redis=r;this.p=p;}
 public void put(String key,String value,Duration ttl){requireKey(key);redis.opsForValue().set(p.getKeyPrefix()+key,value,ttl==null?p.getDefaultTtl():ttl);}
 public Optional<String> get(String key){requireKey(key);return Optional.ofNullable(redis.opsForValue().get(p.getKeyPrefix()+key));}
 public boolean evict(String key){requireKey(key);Boolean removed=redis.delete(p.getKeyPrefix()+key);redis.convertAndSend(p.getInvalidationChannel(),key);return Boolean.TRUE.equals(removed);}private static void requireKey(String k){if(k==null||k.isBlank())throw new IllegalArgumentException("cache key is required");}}
