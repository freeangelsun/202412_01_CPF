package com.cpf.data.persistence.sql;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Hikari 계열 Pool의 안전한 Runtime 조정을 담당합니다.
 * 지원하지 않는 WAS/JNDI Pool을 성공으로 처리하지 않으며 부분 적용 시 기존 값으로 복구합니다.
 */
public final class CpfConnectionPoolRuntimeController {
    private final List<DataSource> dataSources;

    public CpfConnectionPoolRuntimeController(List<DataSource> dataSources) {
        this.dataSources = List.copyOf(dataSources == null ? List.of() : dataSources.stream().filter(Objects::nonNull).toList());
        if (this.dataSources.isEmpty()) throw new IllegalArgumentException("관리할 DataSource가 없습니다.");
    }

    public Result apply(Policy policy) {
        Objects.requireNonNull(policy, "policy");
        policy.validate();
        List<Target> targets = dataSources.stream().map(Target::resolve).toList();
        List<Previous> previous = targets.stream().map(value -> value.snapshot()).toList();
        int applied = 0;
        try {
            for (Target target : targets) {
                target.apply(policy);
                applied++;
            }
            if (policy.softEvict()) targets.forEach(value -> value.softEvict());
            return new Result(targets.size(), policy);
        } catch (RuntimeException ex) {
            List<RuntimeException> rollbackFailures = new ArrayList<>();
            for (int index = applied - 1; index >= 0; index--) {
                try { targets.get(index).restore(previous.get(index)); }
                catch (RuntimeException rollbackFailure) { rollbackFailures.add(rollbackFailure); }
            }
            IllegalStateException failure = new IllegalStateException("Connection Pool runtime 적용에 실패하여 기존 값으로 복구했습니다.", ex);
            rollbackFailures.forEach(failure::addSuppressed);
            throw failure;
        }
    }

    /** Connection Pool Runtime 변경 시 검증·적용할 목표 값을 고정하는 정책 record입니다. */
    public record Policy(
            int maximumPoolSize,
            int minimumIdle,
            long connectionTimeoutMillis,
            long validationTimeoutMillis,
            long idleTimeoutMillis,
            long maxLifetimeMillis,
            boolean softEvict) {
        private void validate() {
            if (maximumPoolSize < 1 || maximumPoolSize > 10000) throw new IllegalArgumentException("maximumPoolSize 범위 오류");
            if (minimumIdle < 0 || minimumIdle > maximumPoolSize) throw new IllegalArgumentException("minimumIdle 범위 오류");
            if (connectionTimeoutMillis < 250 || connectionTimeoutMillis > 600000) throw new IllegalArgumentException("connectionTimeout 범위 오류");
            if (validationTimeoutMillis < 250 || validationTimeoutMillis > connectionTimeoutMillis) throw new IllegalArgumentException("validationTimeout 범위 오류");
            if (idleTimeoutMillis < 0 || idleTimeoutMillis > 86_400_000L) throw new IllegalArgumentException("idleTimeout 범위 오류");
            if (maxLifetimeMillis < 30000 || maxLifetimeMillis > 86_400_000L) throw new IllegalArgumentException("maxLifetime 범위 오류");
        }
    }

    /** Connection Pool Runtime 변경 결과와 실제 적용된 정책을 반환하는 결과 record입니다. */
    public record Result(int controlledPoolCount, Policy appliedPolicy) {}

    private record Previous(
            int maximumPoolSize,
            int minimumIdle,
            long connectionTimeoutMillis,
            long validationTimeoutMillis,
            long idleTimeoutMillis,
            long maxLifetimeMillis) {}

    private static final class Target {
        private final Object configBean;
        private final Object poolBean;
        private final Method getMaximumPoolSize;
        private final Method setMaximumPoolSize;
        private final Method getMinimumIdle;
        private final Method setMinimumIdle;
        private final Method getConnectionTimeout;
        private final Method setConnectionTimeout;
        private final Method getValidationTimeout;
        private final Method setValidationTimeout;
        private final Method getIdleTimeout;
        private final Method setIdleTimeout;
        private final Method getMaxLifetime;
        private final Method setMaxLifetime;
        private final Method softEvictConnections;

        private Target(Object dataSource) {
            this.configBean = invokeOptional(dataSource, "getHikariConfigMXBean", dataSource);
            this.poolBean = invokeOptional(dataSource, "getHikariPoolMXBean", null);
            Class<?> type = configBean.getClass();
            this.getMaximumPoolSize = method(type, "getMaximumPoolSize");
            this.setMaximumPoolSize = method(type, "setMaximumPoolSize", int.class);
            this.getMinimumIdle = method(type, "getMinimumIdle");
            this.setMinimumIdle = method(type, "setMinimumIdle", int.class);
            this.getConnectionTimeout = method(type, "getConnectionTimeout");
            this.setConnectionTimeout = method(type, "setConnectionTimeout", long.class);
            this.getValidationTimeout = method(type, "getValidationTimeout");
            this.setValidationTimeout = method(type, "setValidationTimeout", long.class);
            this.getIdleTimeout = method(type, "getIdleTimeout");
            this.setIdleTimeout = method(type, "setIdleTimeout", long.class);
            this.getMaxLifetime = method(type, "getMaxLifetime");
            this.setMaxLifetime = method(type, "setMaxLifetime", long.class);
            this.softEvictConnections = poolBean == null ? null : method(poolBean.getClass(), "softEvictConnections");
        }

        private static Target resolve(DataSource dataSource) {
            Object target = unwrapKnown(dataSource);
            String name = target.getClass().getName().toLowerCase(java.util.Locale.ROOT);
            if (!name.contains("hikari")) {
                throw new IllegalStateException("Runtime Connection Pool 조정을 지원하지 않는 DataSource입니다: " + target.getClass().getName());
            }
            return new Target(target);
        }

        private Previous snapshot() {
            return new Previous(
                    integer(invoke(configBean, getMaximumPoolSize)),
                    integer(invoke(configBean, getMinimumIdle)),
                    number(invoke(configBean, getConnectionTimeout)),
                    number(invoke(configBean, getValidationTimeout)),
                    number(invoke(configBean, getIdleTimeout)),
                    number(invoke(configBean, getMaxLifetime)));
        }

        private void apply(Policy policy) {
            invoke(configBean, setMaximumPoolSize, policy.maximumPoolSize());
            invoke(configBean, setMinimumIdle, policy.minimumIdle());
            invoke(configBean, setConnectionTimeout, policy.connectionTimeoutMillis());
            invoke(configBean, setValidationTimeout, policy.validationTimeoutMillis());
            invoke(configBean, setIdleTimeout, policy.idleTimeoutMillis());
            invoke(configBean, setMaxLifetime, policy.maxLifetimeMillis());
        }

        private void restore(Previous previous) {
            invoke(configBean, setMaximumPoolSize, previous.maximumPoolSize());
            invoke(configBean, setMinimumIdle, previous.minimumIdle());
            invoke(configBean, setConnectionTimeout, previous.connectionTimeoutMillis());
            invoke(configBean, setValidationTimeout, previous.validationTimeoutMillis());
            invoke(configBean, setIdleTimeout, previous.idleTimeoutMillis());
            invoke(configBean, setMaxLifetime, previous.maxLifetimeMillis());
        }

        private void softEvict() {
            if (softEvictConnections == null) throw new IllegalStateException("Pool soft-evict를 지원하지 않습니다.");
            invoke(poolBean, softEvictConnections);
        }

        private static Object unwrapKnown(DataSource dataSource) {
            try {
                if (dataSource.isWrapperFor(dataSource.getClass())) return dataSource.unwrap(dataSource.getClass());
            } catch (Exception ignored) { }
            return dataSource;
        }

        private static Method method(Class<?> type, String name, Class<?>... parameterTypes) {
            try { return type.getMethod(name, parameterTypes); }
            catch (ReflectiveOperationException ex) { throw new IllegalStateException("필수 Pool API가 없습니다: " + name, ex); }
        }

        private static Object invokeOptional(Object target, String methodName, Object fallback) {
            try { return target.getClass().getMethod(methodName).invoke(target); }
            catch (ReflectiveOperationException ex) { return fallback; }
        }

        private static Object invoke(Object target, Method method, Object... args) {
            try { return method.invoke(target, args); }
            catch (ReflectiveOperationException ex) { throw new IllegalStateException("Pool API 호출 실패: " + method.getName(), ex); }
        }

        private static int integer(Object value) { return ((Number) value).intValue(); }
        private static long number(Object value) { return ((Number) value).longValue(); }
    }
}
