package com.cpf.batch.execution;

/** Kafka listener와 durable inbound 처리 사이의 ACK 경계 계약입니다. */
interface CpfBatchInboundHandler {
    boolean request(String json);
    boolean reply(String json);
}
