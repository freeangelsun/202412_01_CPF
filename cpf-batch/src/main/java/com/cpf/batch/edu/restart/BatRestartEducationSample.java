package com.cpf.batch.edu.restart;

/**
 * checkpoint 기반 restart 위치를 계산하는 샘플입니다.
 */
public class BatRestartEducationSample {

    public RestartPoint restartFrom(long lastCommittedItemNo) {
        long nextItemNo = Math.max(lastCommittedItemNo + 1, 1);
        return new RestartPoint(lastCommittedItemNo, nextItemNo, "checkpoint");
    }

    public record RestartPoint(long lastCommittedItemNo, long nextItemNo, String basis) {
    }
}
