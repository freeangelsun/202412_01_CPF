package com.cpf.batch.spi;

/** 오래된 Manager/Worker 결과가 최신 실행 원장을 덮지 못하도록 하는 Fencing SPI입니다. */
public interface BatchFencingPort {
    void assertCurrent(String jobId, String cpfExecutionId, long fencingToken);
}
