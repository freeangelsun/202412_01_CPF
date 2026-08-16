package com.cpf.batch.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.Acknowledgment;

class CpfBatchKafkaWorkerListenerAckBoundaryTest {
    @Test
    void acknowledgeRunsOnlyAfterHandlerCompletion() {
        List<String> order = new ArrayList<>();
        CpfBatchInboundHandler handler = new StubHandler(() -> order.add("handler-complete"));
        Acknowledgment ack = () -> order.add("ack");
        new CpfBatchKafkaWorkerListener(handler).request("{}", ack);
        assertEquals(List.of("handler-complete", "ack"), order);
    }

    @Test
    void handlerOrLedgerFailureNeverAcknowledges() {
        List<String> order = new ArrayList<>();
        CpfBatchInboundHandler handler = new StubHandler(() -> { throw new IllegalStateException("ledger-failure"); });
        Acknowledgment ack = () -> order.add("ack");
        assertThrows(IllegalStateException.class,
                () -> new CpfBatchKafkaWorkerListener(handler).request("{}", ack));
        assertEquals(List.of(), order);
    }


    @Test
    void durableUnknownReturnStillAcknowledgesToPreventBlindRedelivery() {
        List<String> order = new ArrayList<>();
        CpfBatchInboundHandler handler = new CpfBatchInboundHandler() {
            @Override public boolean request(String json) { order.add("unknown-durable"); return false; }
            @Override public boolean reply(String json) { return false; }
        };
        Acknowledgment ack = () -> order.add("ack");

        new CpfBatchKafkaWorkerListener(handler).request("{}", ack);

        assertEquals(List.of("unknown-durable", "ack"), order);
    }

    private record StubHandler(Runnable action) implements CpfBatchInboundHandler {
        @Override public boolean request(String json) { action.run(); return true; }
        @Override public boolean reply(String json) { action.run(); return true; }
    }
}
