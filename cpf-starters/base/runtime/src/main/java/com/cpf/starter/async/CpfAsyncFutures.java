package com.cpf.starter.async;
import java.util.concurrent.*; import java.util.function.Supplier;
/** CPF 거래 Context를 보존해 CompletableFuture 비동기 작업을 실행하는 공개 유틸리티입니다. */
public final class CpfAsyncFutures {private CpfAsyncFutures(){}public static <T> CompletableFuture<T> supplyAsync(Supplier<T> s,Executor e,CpfAsyncContextPropagation p){CompletableFuture<T> f=new CompletableFuture<>();e.execute(p.wrap(()->{try{f.complete(s.get());}catch(Throwable x){f.completeExceptionally(x);}},CpfAsyncForkType.COMPLETABLE_FUTURE));return f;}}
