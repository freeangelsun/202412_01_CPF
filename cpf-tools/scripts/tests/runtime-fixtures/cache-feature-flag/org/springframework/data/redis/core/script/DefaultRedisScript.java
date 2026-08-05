package org.springframework.data.redis.core.script;
public final class DefaultRedisScript<T> { private final String text; private final Class<T> type; public DefaultRedisScript(String text,Class<T> type){this.text=text;this.type=type;} public String getScriptAsString(){return text;} public Class<T> getResultType(){return type;} }
