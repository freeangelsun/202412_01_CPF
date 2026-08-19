package com.cpf.education.contract;

import com.cpf.batch.api.annotation.CpfBatchJob;
import com.cpf.education.batch.chunktransaction.job.CustomerChunkTransactionJob;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** EDU-BATCH-11 Source가 실제 CPF Batch Job 계약을 소비하는지 검증합니다. */
class CustomerChunkTransactionJobTest {
 @Test void canonicalBatchMetadataIsPresent() {
   CpfBatchJob job=CustomerChunkTransactionJob.class.getAnnotation(CpfBatchJob.class);
   assertNotNull(job, "@CpfBatchJob runtime consumer가 필요합니다.");
   assertFalse(job.value().isBlank());
   assertTrue(job.maxConcurrentExecutions() > 0);
 }
}
