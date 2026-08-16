package com.cpf.integration.realtime;

import java.util.function.Consumer;

/** 다중 인스턴스 fan-out을 위한 provider-neutral backplane SPI입니다. */
public interface CpfRealtimeBackplane {
    void publish(String originInstanceId, CpfRealtimeEvent event);
    AutoCloseable subscribe(Consumer<RemoteEvent> consumer);

    record RemoteEvent(String originInstanceId, CpfRealtimeEvent event) {
        public RemoteEvent {
            if (originInstanceId == null || originInstanceId.isBlank()) throw new IllegalArgumentException("originInstanceId");
            if (event == null) throw new IllegalArgumentException("event");
        }
    }
}
