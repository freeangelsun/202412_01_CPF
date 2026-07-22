package com.cpf.batch.edu.chunk;

import java.util.List;

/**
 * 대량 데이터를 chunk 단위로 나눠 처리하는 교육 샘플입니다.
 */
public class BatChunkJobEducationSample {

    public ChunkPlan plan(int totalCount, int chunkSize) {
        if (totalCount < 0 || chunkSize < 1) {
            throw new IllegalArgumentException("totalCount는 0 이상, chunkSize는 1 이상이어야 합니다.");
        }
        int chunkCount = (int) Math.ceil(totalCount / (double) chunkSize);
        return new ChunkPlan(totalCount, chunkSize, chunkCount, List.of("read", "process", "write"));
    }

    public record ChunkPlan(int totalCount, int chunkSize, int chunkCount, List<String> phases) {
    }
}
