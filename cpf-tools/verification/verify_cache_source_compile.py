#!/usr/bin/env python3
"""Compile the Redis/Valkey cache source surface with isolated API stubs.

This is a low-cost syntax/type-contract fence for environments where the full
Gradle dependency graph is unavailable. It does NOT replace the real Gradle
compile/test gate; that remains mandatory for release evidence.
"""
from __future__ import annotations

import shutil
import subprocess
import sys
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
MODULES = [
    ROOT / "cpf-starters/data/cache/spring-data-redis",
    ROOT / "cpf-starters/data/cache/redis",
    ROOT / "cpf-starters/data/cache/valkey",
]

STUBS = {
    "org/springframework/boot/autoconfigure/AutoConfiguration.java": "package org.springframework.boot.autoconfigure; public @interface AutoConfiguration {}",
    "org/springframework/boot/autoconfigure/condition/ConditionalOnBean.java": "package org.springframework.boot.autoconfigure.condition; public @interface ConditionalOnBean { Class<?>[] value() default {}; }",
    "org/springframework/boot/autoconfigure/condition/ConditionalOnMissingBean.java": "package org.springframework.boot.autoconfigure.condition; public @interface ConditionalOnMissingBean { Class<?>[] value() default {}; String[] name() default {}; }",
    "org/springframework/boot/autoconfigure/condition/ConditionalOnProperty.java": "package org.springframework.boot.autoconfigure.condition; public @interface ConditionalOnProperty { String prefix() default \"\"; String[] name() default {}; String havingValue() default \"\"; boolean matchIfMissing() default false; }",
    "org/springframework/boot/context/properties/EnableConfigurationProperties.java": "package org.springframework.boot.context.properties; public @interface EnableConfigurationProperties { Class<?>[] value() default {}; }",
    "org/springframework/boot/context/properties/ConfigurationProperties.java": "package org.springframework.boot.context.properties; public @interface ConfigurationProperties { String value() default \"\"; String prefix() default \"\"; }",
    "org/springframework/context/annotation/Bean.java": "package org.springframework.context.annotation; public @interface Bean { String[] value() default {}; }",
    "org/springframework/beans/factory/SmartInitializingSingleton.java": "package org.springframework.beans.factory; public interface SmartInitializingSingleton { void afterSingletonsInstantiated(); }",
    "org/springframework/core/env/Environment.java": "package org.springframework.core.env; public interface Environment { <T> T getProperty(String key, Class<T> targetType, T defaultValue); }",
    "org/springframework/boot/health/contributor/HealthIndicator.java": "package org.springframework.boot.health.contributor; public interface HealthIndicator { Health health(); }",
    "org/springframework/boot/health/contributor/Health.java": "package org.springframework.boot.health.contributor; public final class Health { public static Builder up(){return new Builder();} public static Builder down(){return new Builder();} public static final class Builder { public Builder withDetail(String k,Object v){return this;} public Health build(){return new Health();} } }",
    "org/springframework/data/redis/core/Cursor.java": "package org.springframework.data.redis.core; public interface Cursor<T> extends java.util.Iterator<T>, AutoCloseable { @Override void close(); }",
    "org/springframework/data/redis/core/ScanOptions.java": "package org.springframework.data.redis.core; public final class ScanOptions { public static Builder scanOptions(){return new Builder();} public static final class Builder { public Builder match(String p){return this;} public Builder count(long c){return this;} public ScanOptions build(){return new ScanOptions();} } }",
    "org/springframework/data/redis/connection/RedisConnection.java": "package org.springframework.data.redis.connection; import org.springframework.data.redis.core.*; public interface RedisConnection extends AutoCloseable { String ping(); Cursor<byte[]> scan(ScanOptions o); @Override void close(); }",
    "org/springframework/data/redis/connection/RedisConnectionFactory.java": "package org.springframework.data.redis.connection; public interface RedisConnectionFactory { RedisConnection getConnection(); }",
    "org/springframework/data/redis/connection/Message.java": "package org.springframework.data.redis.connection; public interface Message { byte[] getBody(); }",
    "org/springframework/data/redis/connection/MessageListener.java": "package org.springframework.data.redis.connection; public interface MessageListener { void onMessage(Message message, byte[] pattern); }",
    "org/springframework/data/redis/core/ValueOperations.java": "package org.springframework.data.redis.core; import java.time.Duration; public interface ValueOperations<K,V> { V get(Object k); void set(K k,V v,Duration ttl); Long increment(K k); Boolean setIfAbsent(K k,V v,Duration ttl); }",
    "org/springframework/data/redis/core/script/DefaultRedisScript.java": "package org.springframework.data.redis.core.script; public class DefaultRedisScript<T> { public DefaultRedisScript(String script, Class<T> resultType){} }",
    "org/springframework/data/redis/core/StringRedisTemplate.java": "package org.springframework.data.redis.core; import java.util.*; import org.springframework.data.redis.connection.*; import org.springframework.data.redis.core.script.*; public class StringRedisTemplate { public StringRedisTemplate(){} public StringRedisTemplate(RedisConnectionFactory f){} public ValueOperations<String,String> opsForValue(){return null;} public Boolean delete(String k){return null;} public Long delete(Collection<String> keys){return null;} public Long convertAndSend(String ch,String msg){return null;} public RedisConnectionFactory getConnectionFactory(){return null;} public <T> T execute(DefaultRedisScript<T> script, List<String> keys, Object... args){return null;} }",
    "org/springframework/data/redis/listener/ChannelTopic.java": "package org.springframework.data.redis.listener; public class ChannelTopic { public ChannelTopic(String s){} }",
    "org/springframework/data/redis/listener/RedisMessageListenerContainer.java": "package org.springframework.data.redis.listener; import org.springframework.data.redis.connection.*; public class RedisMessageListenerContainer { public void setConnectionFactory(RedisConnectionFactory f){} public void addMessageListener(MessageListener l, ChannelTopic t){} }",
    "org/springframework/dao/DataIntegrityViolationException.java": "package org.springframework.dao; public class DataIntegrityViolationException extends RuntimeException { public DataIntegrityViolationException(String s){super(s);} }",
    "org/springframework/jdbc/core/PreparedStatementCreator.java": "package org.springframework.jdbc.core; public interface PreparedStatementCreator { java.sql.PreparedStatement createPreparedStatement(java.sql.Connection con) throws java.sql.SQLException; }",
    "org/springframework/jdbc/core/RowMapper.java": "package org.springframework.jdbc.core; public interface RowMapper<T> { T mapRow(java.sql.ResultSet rs,int rowNum) throws java.sql.SQLException; }",
    "org/springframework/jdbc/support/KeyHolder.java": "package org.springframework.jdbc.support; public interface KeyHolder { Number getKey(); }",
    "org/springframework/jdbc/support/GeneratedKeyHolder.java": "package org.springframework.jdbc.support; public class GeneratedKeyHolder implements KeyHolder { public Number getKey(){return null;} }",
    "org/springframework/jdbc/core/JdbcTemplate.java": "package org.springframework.jdbc.core; import java.util.*; import javax.sql.DataSource; import org.springframework.jdbc.support.KeyHolder; public class JdbcTemplate { public int update(PreparedStatementCreator psc, KeyHolder kh){return 0;} public int update(String sql,Object... args){return 0;} public <T> List<T> query(String sql, RowMapper<T> rm, Object... args){return List.of();} public <T> T queryForObject(String sql, Class<T> type,Object... args){return null;} public DataSource getDataSource(){return null;} }",
    "org/junit/jupiter/api/Test.java": "package org.junit.jupiter.api; public @interface Test {}",
    "org/junit/jupiter/api/Assertions.java": "package org.junit.jupiter.api; public final class Assertions { public static void assertTrue(boolean v){} public static void assertFalse(boolean v){} public static void assertArrayEquals(byte[] e,byte[] a){} public static void assertEquals(Object e,Object a){} public static void assertEquals(Object e,Object a,String m){} public static void assertEquals(long e,long a){} public static void assertEquals(long e,long a,String m){} public static <T extends Throwable> T assertThrows(Class<T> c, Executable e){return null;} public static void assertDoesNotThrow(Executable e){} public interface Executable { void execute() throws Throwable; } }",
    "org/mockito/Mockito.java": "package org.mockito; import org.mockito.stubbing.OngoingStubbing; import org.mockito.verification.VerificationMode; public final class Mockito { public static <T> T mock(Class<T> c){return null;} public static <T> OngoingStubbing<T> when(T call){return null;} public static <T> T verify(T mock){return mock;} public static <T> T verify(T mock, VerificationMode mode){return mock;} public static VerificationMode never(){return null;} }",
    "org/mockito/ArgumentMatchers.java": "package org.mockito; public final class ArgumentMatchers { public static String anyString(){return null;} public static <T> T any(Class<T> c){return null;} public static <T> T eq(T value){return value;} }",
    "org/mockito/ArgumentCaptor.java": "package org.mockito; public final class ArgumentCaptor<T> { public static <T> ArgumentCaptor<T> forClass(Class<T> c){return new ArgumentCaptor<>();} public T capture(){return null;} public T getValue(){return null;} }",
    "org/mockito/verification/VerificationMode.java": "package org.mockito.verification; public interface VerificationMode {}",
    "org/mockito/stubbing/OngoingStubbing.java": "package org.mockito.stubbing; public interface OngoingStubbing<T> { OngoingStubbing<T> thenReturn(T value, T... values); OngoingStubbing<T> thenThrow(Throwable... t); }",
}


def java_files(kind: str) -> list[Path]:
    files: list[Path] = []
    for module in MODULES:
        base = module / "src" / kind / "java"
        if base.exists():
            files.extend(sorted(base.rglob("*.java")))
    return files


def run() -> int:
    javac = shutil.which("javac")
    if not javac:
        print("CPF_CACHE_SOURCE_COMPILE=UNVERIFIED reason=javac_not_found")
        return 2
    main = sorted((ROOT / "cpf-starters/data/src/main/java/com/cpf/data/cache/api").glob("*.java")) + java_files("main")
    test = java_files("test")
    if not main or not test:
        print(f"CPF_CACHE_SOURCE_COMPILE=FAIL reason=source_missing main={len(main)} test={len(test)}")
        return 1

    with tempfile.TemporaryDirectory(prefix="cpf-cache-compile-") as td:
        tmp = Path(td)
        stub_src = tmp / "stub-src"
        classes = tmp / "classes"
        test_classes = tmp / "test-classes"
        for rel, content in STUBS.items():
            p = stub_src / rel
            p.parent.mkdir(parents=True, exist_ok=True)
            p.write_text(content + "\n", encoding="utf-8")
        stub_files = sorted(stub_src.rglob("*.java"))
        classes.mkdir()
        stub_compile = subprocess.run([javac, "-encoding", "UTF-8", "-Xlint:none", "-d", str(classes), *map(str, stub_files)], text=True, capture_output=True)
        if stub_compile.returncode:
            sys.stdout.write(stub_compile.stdout)
            sys.stderr.write(stub_compile.stderr)
            print("CPF_CACHE_SOURCE_COMPILE=FAIL phase=verification_stub")
            return 1
        cp1 = subprocess.run([javac, "-encoding", "UTF-8", "-Xlint:all", "-cp", str(classes), "-d", str(classes), *map(str, main)], text=True, capture_output=True)
        if cp1.returncode:
            sys.stdout.write(cp1.stdout)
            sys.stderr.write(cp1.stderr)
            print(f"CPF_CACHE_SOURCE_COMPILE=FAIL phase=main main={len(main)} test={len(test)}")
            return 1
        test_classes.mkdir()
        cp2 = subprocess.run([javac, "-encoding", "UTF-8", "-Xlint:all", "-cp", str(classes), "-d", str(test_classes), *map(str, test)], text=True, capture_output=True)
        if cp2.returncode:
            sys.stdout.write(cp2.stdout)
            sys.stderr.write(cp2.stderr)
            print(f"CPF_CACHE_SOURCE_COMPILE=FAIL phase=test main={len(main)} test={len(test)}")
            return 1
        # Warnings are surfaced so the release gate can decide whether a real Gradle compile is still required.
        warnings = sum(s.count("warning:") for s in (cp1.stderr, cp2.stderr))
        print(f"CPF_CACHE_SOURCE_COMPILE=PASS main={len(main)} test={len(test)} warnings={warnings} javac={Path(javac).name}")
        return 0


if __name__ == "__main__":
    raise SystemExit(run())
