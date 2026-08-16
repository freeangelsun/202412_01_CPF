package com.cpf.integration.realtime;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/** 단일 JVM 및 Test용 backplane입니다. 다중 인스턴스 배포에서는 별도 shared provider로 대체합니다. */
public final class CpfLocalRealtimeBackplane implements CpfRealtimeBackplane {
    private final CopyOnWriteArrayList<Consumer<RemoteEvent>> consumers = new CopyOnWriteArrayList<>();

    @Override
    public void publish(String originInstanceId, CpfRealtimeEvent event) {
        RemoteEvent remote = new RemoteEvent(originInstanceId, event);
        for (Consumer<RemoteEvent> consumer : consumers) consumer.accept(remote);
    }

    @Override
    public AutoCloseable subscribe(Consumer<RemoteEvent> consumer) {
        consumers.add(consumer);
        return () -> consumers.remove(consumer);
    }
}
