package com.cpf.starter.async;
import java.util.concurrent.*; import java.util.function.Supplier;
public final class CpfAsyncFutures {private CpfAsyncFutures(){}public static <T> CompletableFuture<T> supplyAsync(Supplier<T> s,Executor e,CpfAsyncContextPropagation p){CompletableFuture<T> f=new CompletableFuture<>();e.execute(p.wrap(()->{try{f.complete(s.get());}catch(Throwable x){f.completeExceptionally(x);}},CpfAsyncForkType.COMPLETABLE_FUTURE));return f;}}
