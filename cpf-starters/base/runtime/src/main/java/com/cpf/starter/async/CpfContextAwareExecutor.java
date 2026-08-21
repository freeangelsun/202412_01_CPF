package com.cpf.starter.async;
import java.util.concurrent.Executor; import java.util.Objects;
/** CPF 거래 Context를 보존한 채 일반 Executor 작업을 실행하는 공개 비동기 실행기입니다. */
public final class CpfContextAwareExecutor implements Executor {private final Executor delegate;private final CpfAsyncContextPropagation propagation;public CpfContextAwareExecutor(Executor d,CpfAsyncContextPropagation p){delegate=Objects.requireNonNull(d);propagation=Objects.requireNonNull(p);}public void execute(Runnable r){delegate.execute(propagation.wrap(r,CpfAsyncForkType.EXECUTOR));}}
