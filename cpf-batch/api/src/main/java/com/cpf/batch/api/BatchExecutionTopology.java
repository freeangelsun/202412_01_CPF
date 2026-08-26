package com.cpf.batch.api;

/** Spring Batch가 실제 실행할 CPF topology입니다. Kafka/Broker 기반 Remote Execution은 지원하지 않습니다. */
public enum BatchExecutionTopology {
    LOCAL,
    PARALLEL_STEPS,
    LOCAL_PARTITION
}
