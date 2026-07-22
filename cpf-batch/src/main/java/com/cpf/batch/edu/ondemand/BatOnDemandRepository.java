package com.cpf.batch.edu.ondemand;

import java.util.Optional;

/** 온디맨드 실행 접수 상태를 영속화하는 경계입니다. */
public interface BatOnDemandRepository {
    BatOnDemandStatus createOrFind(BatOnDemandStatus requested, String parametersJson, String reason, String requestUser);

    Optional<BatOnDemandStatus> find(String executionRequestId);

    void markRunning(String executionRequestId);

    void complete(String executionRequestId, String status, Long cpfExecutionId, Long springExecutionId,
                  String resultJson, String failureCode, String failureMessage);
}
