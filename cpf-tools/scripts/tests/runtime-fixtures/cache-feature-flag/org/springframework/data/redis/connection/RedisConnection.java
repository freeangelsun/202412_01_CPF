package org.springframework.data.redis.connection;
import org.springframework.data.redis.core.Cursor; import org.springframework.data.redis.core.ScanOptions;
public interface RedisConnection extends AutoCloseable { String ping(); Cursor<byte[]> scan(ScanOptions options); @Override void close(); }
