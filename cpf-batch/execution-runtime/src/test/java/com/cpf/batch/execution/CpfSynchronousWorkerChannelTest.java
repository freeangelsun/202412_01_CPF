package com.cpf.batch.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.support.MessageBuilder;

class CpfSynchronousWorkerChannelTest {
    @Test
    void sendReturnsAfterSubscriberCompletesOnCallerThread() {
        CpfSynchronousWorkerChannel channel = new CpfSynchronousWorkerChannel();
        List<String> order = new ArrayList<>();
        long caller = Thread.currentThread().threadId();
        channel.subscribe(message -> {
            assertEquals(caller, Thread.currentThread().threadId());
            order.add("handler");
        });
        channel.send(MessageBuilder.withPayload("work").build());
        order.add("returned");
        assertEquals(List.of("handler", "returned"), order);
    }
}
