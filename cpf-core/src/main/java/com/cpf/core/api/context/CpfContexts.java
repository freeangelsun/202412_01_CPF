package com.cpf.core.api.context;

import com.cpf.core.spi.context.CpfContextRuntimeProvider;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.concurrent.Callable;

/**
 * 일반 개발자가 사용하는 CPF Context 접근 Facade입니다.
 *
 * <p>Public API는 current/require/capture/bind/run/call만 제공합니다. Runtime 설치·Registry·Accessor는
 * 노출하지 않으며 실제 저장 방식은 Base Starter/Testkit Provider가 소유합니다.</p>
 */
public final class CpfContexts {
    private CpfContexts() { }

    /** 현재 lexical 실행 범위의 Context를 반환합니다. */
    public static CpfContext current() { return ProviderHolder.PROVIDER.current(); }

    /** 관리 실행에서 현재 Context가 없으면 fail-closed 합니다. */
    public static CpfContext requireCurrent() {
        CpfContext current = current();
        if (current == null) throw new IllegalStateException("Managed CPF execution has no bound context");
        return current;
    }

    /** 현재 Context를 불변 Snapshot으로 캡처합니다. Context가 없으면 {@code null}입니다. */
    public static CpfContextSnapshot snapshot() {
        CpfContext current = current();
        return current == null ? null : CpfContextSnapshot.capture(current);
    }

    /** 현재 Context를 불변 Snapshot으로 캡처하며 Context가 없으면 실패합니다. */
    public static CpfContextSnapshot requireSnapshot() { return CpfContextSnapshot.capture(requireCurrent()); }

    /** 비동기/Boundary 전달을 위한 표준 capture API입니다. */
    public static CpfContextSnapshot capture() { return requireSnapshot(); }

    /** Snapshot을 현재 lexical 범위에 바인딩하고 close 시 이전 Context를 복원합니다. */
    public static AutoCloseable bind(CpfContextSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot");
        return ProviderHolder.PROVIDER.bind(snapshot.context());
    }

    /** 지정 Snapshot 범위에서 Runnable을 실행하며 finally restore를 보장합니다. */
    public static void run(CpfContextSnapshot snapshot, Runnable runnable) {
        Objects.requireNonNull(runnable, "runnable");
        try (AutoCloseable ignored = bind(snapshot)) { runnable.run(); }
        catch (RuntimeException e) { throw e; }
        catch (Exception e) { throw new IllegalStateException("CPF context scope close failed", e); }
    }

    /** 지정 Snapshot 범위에서 Callable을 실행하며 finally restore를 보장합니다. */
    public static <T> T call(CpfContextSnapshot snapshot, Callable<T> callable) throws Exception {
        Objects.requireNonNull(callable, "callable");
        try (AutoCloseable ignored = bind(snapshot)) { return callable.call(); }
    }

    public static String transactionId() { return requireCurrent().transactionId(); }
    public static String currentTransactionId() { var c = current(); return c == null ? null : c.transactionId(); }
    /** 현재 논리 거래가 실행 중인 Canonical operationId를 반환합니다. */
    public static String operationId() { var c = current(); return c == null ? null : c.operationId(); }
    /** 현재 분산 Trace 식별자를 반환합니다. */
    public static String traceId() { var c = current(); return c == null ? null : c.traceId(); }
    /** transactionId를 최초 생성한 원본 System Code를 반환합니다. */
    public static String originalSystemCode() { var c = current(); return c == null ? null : c.originalSystemCode(); }
    /** 현재 요청을 실제 처리하는 System Code를 반환합니다. */
    public static String systemCode() { var c = current(); return c == null ? null : c.systemCode(); }
    /** 바로 직전 호출자의 System Code를 반환합니다. */
    public static String callerSystemCode() { var c = current(); return c == null ? null : c.callerSystemCode(); }
    /** 현재 호출 대상 System Code를 반환합니다. */
    public static String targetSystemCode() { var c = current(); return c == null ? null : c.targetSystemCode(); }
    /** 현재 호출 대상의 Canonical operationId를 반환합니다. */
    public static String targetOperationId() { var c = current(); return c == null ? null : c.targetOperationId(); }
    /** currentExecutionId 작업을 CPF 표준 계약에 따라 수행한다. */
    public static String currentExecutionId() { var c = current(); return c == null ? null : c.executionId(); }
    public static String currentSegmentId() { var c = current(); return c == null ? null : c.segmentId(); }
    public static String idempotencyKey() { var c = current(); return c == null ? null : c.idempotencyKey(); }
    /** 현재 Operation의 불변 거래 sequence입니다. Sequence 생성은 Boundary/Owner가 담당합니다. */
    public static long transactionSequence() { var c = requireCurrent(); return c.operation() == null ? 1L : c.operation().transactionSequence(); }
    public static String userId() { var c = current(); return c == null ? null : c.subjectId(); }
    public static String operatorId() { var c = current(); return c == null ? null : c.actorId(); }
    public static String tenantId() { var c = current(); return c == null ? null : c.tenantId(); }

    private static final class ProviderHolder {
        private static final CpfContextRuntimeProvider PROVIDER = loadProvider();
    }

    private static CpfContextRuntimeProvider loadProvider() {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) loader = CpfContexts.class.getClassLoader();
        List<CpfContextRuntimeProvider> providers = new ArrayList<>();
        ServiceLoader.load(CpfContextRuntimeProvider.class, loader).forEach(providers::add);
        if (providers.isEmpty()) return UnavailableProvider.INSTANCE;
        providers.sort(Comparator.comparingInt(CpfContextRuntimeProvider::priority).reversed()
                .thenComparing(p -> p.getClass().getName()));
        int top = providers.get(0).priority();
        long sameTop = providers.stream().filter(p -> p.priority() == top).count();
        if (sameTop > 1) {
            throw new IllegalStateException("Multiple CPF context runtime providers have the same highest priority: "
                    + providers.stream().filter(p -> p.priority() == top).map(p -> p.getClass().getName()).toList());
        }
        return providers.get(0);
    }

    /** UnavailableProvider 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
    private enum UnavailableProvider implements CpfContextRuntimeProvider {
        INSTANCE;
        @Override public int priority() { return Integer.MIN_VALUE; }
        @Override public CpfContext current() { return null; }
        @Override public AutoCloseable bind(CpfContext context) {
            throw new IllegalStateException("CPF context runtime provider is not available; add cpf-starter or cpf-testkit");
        }
    }
}
