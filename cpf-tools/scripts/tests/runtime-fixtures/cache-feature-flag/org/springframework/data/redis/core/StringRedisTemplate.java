package org.springframework.data.redis.core;
import java.nio.charset.StandardCharsets; import java.time.*; import java.util.*; import java.util.concurrent.ConcurrentHashMap; import java.util.concurrent.atomic.AtomicLong;
import org.springframework.data.redis.connection.*; import org.springframework.data.redis.core.script.DefaultRedisScript;
public class StringRedisTemplate {
 private final Map<String,String> values=new ConcurrentHashMap<>(); private final Map<String,Instant> expiry=new ConcurrentHashMap<>(); private final Map<String,AtomicLong> counters=new ConcurrentHashMap<>(); private final List<String> published=new ArrayList<>();
 private final ValueOperations<String,String> operations=new ValueOperations<>(){
  public String get(String key){expire(key);return values.get(key);} public void set(String key,String value,Duration ttl){values.put(key,value);expiry.put(key,Instant.now().plus(ttl));}
  public Boolean setIfAbsent(String key,String value,Duration ttl){expire(key);if(values.putIfAbsent(key,value)==null){expiry.put(key,Instant.now().plus(ttl));return true;}return false;}
  public Long increment(String key){return counters.computeIfAbsent(key,k->new AtomicLong()).incrementAndGet();}
 };
 public ValueOperations<String,String> opsForValue(){return operations;} public Boolean delete(String key){expiry.remove(key);return values.remove(key)!=null;} public Long delete(Collection<String> keys){long n=0;for(String k:keys)if(delete(k))n++;return n;}
 public Long convertAndSend(String channel,String message){published.add(channel+":"+message);return 1L;} public List<String> published(){return List.copyOf(published);}
 @SuppressWarnings("unchecked") public <T>T execute(DefaultRedisScript<T> script,List<String> keys,Object... args){String key=keys.get(0),expected=String.valueOf(args[0]);Long result=expected.equals(operations.get(key))&&delete(key)?1L:0L;return (T)result;}
 public RedisConnectionFactory getConnectionFactory(){return ()->new RedisConnection(){public String ping(){return "PONG";} public Cursor<byte[]> scan(ScanOptions o){String prefix=o.pattern().endsWith("*")?o.pattern().substring(0,o.pattern().length()-1):o.pattern();List<byte[]> list=values.keySet().stream().filter(k->k.startsWith(prefix)).map(k->k.getBytes(StandardCharsets.UTF_8)).toList();Iterator<byte[]> it=list.iterator();return new Cursor<>(){public boolean hasNext(){return it.hasNext();}public byte[] next(){return it.next();}public void close(){}};}public void close(){}};}
 private void expire(String key){Instant e=expiry.get(key);if(e!=null&&!e.isAfter(Instant.now()))delete(key);}
}
