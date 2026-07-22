package com.cpf.batch.edu.servicecall;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BatOnlineServiceCallEducationSampleTest {

    @Test
    void batchOnlineCallHeadersContainTraceAndJobExecutionId() {
        assertThat(new BatOnlineServiceCallEducationSample().buildHeaders("T-1", "100"))
                .containsEntry("x-cpf-transaction-global-id", "T-1")
                .containsEntry("x-cpf-batch-job-execution-id", "100");
    }
}
