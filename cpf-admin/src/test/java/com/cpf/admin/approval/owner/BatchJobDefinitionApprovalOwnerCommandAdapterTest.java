package com.cpf.admin.approval.owner;

import com.cpf.batch.api.BatchJobDefinitionControlPort;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class BatchJobDefinitionApprovalOwnerCommandAdapterTest {
    @Test
    void supportsOnlyExactPublishedDefinitionTuple() {
        BatchJobDefinitionApprovalOwnerCommandAdapter adapter =
                new BatchJobDefinitionApprovalOwnerCommandAdapter(mock(BatchJobDefinitionControlPort.class));

        assertThat(adapter.supports("BAT", "BAT_JOB_PUBLISH", "BAT_JOB_PUBLISH", "BAT_JOB_DEFINITION")).isTrue();
        assertThat(adapter.supports("BAT", "BAT_JOB_PUBLISH", "BAT_JOB_PUBLISH", "BAT_JOB")).isTrue();
        assertThat(adapter.supports("CPF-BATCH", "BAT_JOB_PUBLISH", "BAT_JOB_PUBLISH", "BAT_JOB_DEFINITION")).isFalse();
        assertThat(adapter.supports("BAT-SHADOW", "BAT_JOB_PUBLISH", "BAT_JOB_PUBLISH", "BAT_JOB_DEFINITION")).isFalse();
        assertThat(adapter.supports("BAT", "BAT_JOB_PUBLISH_EXTRA", "BAT_JOB_PUBLISH", "BAT_JOB_DEFINITION")).isFalse();
        assertThat(adapter.supports("BAT", "BAT_JOB_PUBLISH", "BAT_JOB_PUBLISH_EXTRA", "BAT_JOB_DEFINITION")).isFalse();
    }
}
