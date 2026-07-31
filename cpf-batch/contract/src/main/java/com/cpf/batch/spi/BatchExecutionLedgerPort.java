package com.cpf.batch.spi;

import com.cpf.batch.api.BatchApprovedLaunchRequest;
import com.cpf.batch.api.BatchExecutionLink;
import java.util.List;

/** CPF 승인·감사 원장과 Spring Batch Metadata를 연결하는 Control Plane SPI입니다. */
public interface BatchExecutionLedgerPort {
    String reserve(BatchApprovedLaunchRequest request);
    void bind(BatchExecutionLink link);
    void recordUnknown(String cpfExecutionId, String reasonCode, String detail);
    List<BatchExecutionLink> findByCpfExecutionId(String cpfExecutionId);
}
