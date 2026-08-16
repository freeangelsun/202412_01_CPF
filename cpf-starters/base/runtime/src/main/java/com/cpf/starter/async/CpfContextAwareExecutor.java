package com.cpf.starter.async;
import java.util.concurrent.Executor; import java.util.Objects;
public final class CpfContextAwareExecutor implements Executor {private final Executor delegate;private final CpfAsyncContextPropagation propagation;public CpfContextAwareExecutor(Executor d,CpfAsyncContextPropagation p){delegate=Objects.requireNonNull(d);propagation=Objects.requireNonNull(p);}public void execute(Runnable r){delegate.execute(propagation.wrap(r,CpfAsyncForkType.EXECUTOR));}}
