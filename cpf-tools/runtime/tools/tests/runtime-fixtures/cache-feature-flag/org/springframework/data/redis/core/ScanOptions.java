package org.springframework.data.redis.core;
public final class ScanOptions {
 private final String pattern; private final long count;
 private ScanOptions(String pattern,long count){this.pattern=pattern;this.count=count;}
 public String pattern(){return pattern;} public long count(){return count;}
 public static Builder scanOptions(){return new Builder();}
 public static final class Builder { private String pattern="*"; private long count=10; public Builder match(String p){pattern=p;return this;} public Builder count(long c){count=c;return this;} public ScanOptions build(){return new ScanOptions(pattern,count);} }
}
