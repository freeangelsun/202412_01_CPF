package com.cpf.core.common.logging.spi;

import com.cpf.core.common.logging.lineage.CpfTransactionLineageRecord;

/** Downstream persistence SPI for the canonical normalized transaction-lineage projection. */
public interface CpfTransactionLineageProjectionPort {
    void upsert(CpfTransactionLineageRecord record);
}
