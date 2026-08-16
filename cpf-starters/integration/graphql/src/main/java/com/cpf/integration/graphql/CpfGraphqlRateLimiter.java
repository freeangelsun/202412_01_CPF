package com.cpf.integration.graphql;

import java.time.Clock;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/** Bounded per-principal rolling-second limiter for GraphQL ingress. */
public final class CpfGraphqlRateLimiter {
    private final Clock clock;
    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();
    public CpfGraphqlRateLimiter(){this(Clock.systemUTC());}
    CpfGraphqlRateLimiter(Clock clock){this.clock=clock;}
    public boolean allow(String key,int limit){
        if(key==null||key.isBlank())return false;
        if(limit<1)throw new IllegalArgumentException("limit");
        long second=clock.millis()/1000L;
        Window w=windows.compute(key,(k,old)->old==null||old.second!=second?new Window(second):old);
        return w.count.incrementAndGet()<=limit;
    }
    private static final class Window { final long second; final AtomicInteger count=new AtomicInteger(); Window(long second){this.second=second;} }
}
