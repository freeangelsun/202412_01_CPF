package com.cpf.batch.edu.chunk;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BatChunkJobEducationSampleTest {

    @Test
    void chunkPlanCalculatesChunkCount() {
        BatChunkJobEducationSample.ChunkPlan plan = new BatChunkJobEducationSample().plan(25, 10);

        assertThat(plan.chunkCount()).isEqualTo(3);
        assertThat(plan.phases()).containsExactly("read", "process", "write");
    }
}
