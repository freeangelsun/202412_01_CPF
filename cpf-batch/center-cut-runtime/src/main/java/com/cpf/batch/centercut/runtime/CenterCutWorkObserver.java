package com.cpf.batch.centercut.runtime;

/** Worker executable가 DB Center-Cut 처리상태를 Runtime health/trace에 결합하는 관찰 계약입니다. */
public interface CenterCutWorkObserver {
    default void claimed(JdbcCenterCutClaimRepository.Claim claim, JdbcCenterCutClaimRepository.Work work) { }
    default void released(JdbcCenterCutClaimRepository.Claim claim) { }
    default void repositoryHealthy() { }
    default void repositoryFailure(RuntimeException failure) { }
    default void leaseLost(JdbcCenterCutClaimRepository.Claim claim) { }
}
