package com.cpf.batch.api;

/** Spring Batch가 실제 실행할 CPF topology입니다. */
public enum BatchExecutionTopology {
    LOCAL,
    PARALLEL_STEPS,
    LOCAL_PARTITION,
    REMOTE_PARTITION,
    REMOTE_CHUNK,
    REMOTE_STEP
}
