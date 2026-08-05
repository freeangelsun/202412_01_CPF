package org.springframework.data.redis.core;
import java.time.Duration;
public interface ValueOperations<K,V> { V get(K key); void set(K key,V value,Duration ttl); Boolean setIfAbsent(K key,V value,Duration ttl); Long increment(K key); }
