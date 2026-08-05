package org.springframework.data.redis.core;
import java.util.Iterator;
public interface Cursor<T> extends Iterator<T>, AutoCloseable { @Override void close(); }
