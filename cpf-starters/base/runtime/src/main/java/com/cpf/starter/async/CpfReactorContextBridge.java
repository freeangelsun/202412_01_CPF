package com.cpf.starter.async;

import com.cpf.core.api.context.CpfContextSnapshot;
import com.cpf.core.api.context.CpfContexts;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

/** Reactor callback마다 캡처된 Context를 lexical scope로 복원하는 Foundation adapter입니다. */
public final class CpfReactorContextBridge {
    private CpfReactorContextBridge() { }
    public static <T> Mono<T> bindSnapshot(Mono<T> source, CpfContextSnapshot snapshot) {
        Objects.requireNonNull(source,"source"); Objects.requireNonNull(snapshot,"snapshot");
        return Mono.create(sink -> {
            AtomicReference<Disposable> upstream=new AtomicReference<>();
            sink.onCancel(() -> { var d=upstream.get(); if(d!=null) runBound(snapshot,d::dispose); });
            sink.onDispose(() -> { var d=upstream.getAndSet(null); if(d!=null&&!d.isDisposed()) runBound(snapshot,d::dispose); });
            Disposable disposable;
            try (AutoCloseable _=CpfContexts.bind(snapshot)) {
                disposable=source.subscribe(v->runBound(snapshot,()->sink.success(v)),e->runBound(snapshot,()->sink.error(e)),()->runBound(snapshot,sink::success));
            } catch (Exception e) { sink.error(e); return; }
            upstream.set(disposable);
        });
    }
    private static void runBound(CpfContextSnapshot snapshot,Runnable action){ CpfContexts.run(snapshot,action); }
}
