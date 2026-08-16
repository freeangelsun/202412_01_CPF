package com.cpf.admin.approval.repository;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/** Source-level regression guard for process-kill recovery semantics. Runtime DB3 races are release gates. */
class AdmApprovalRecoveryRepositoryContractTest {
    @Test
    void staleRunningReservationConvergesToUnknownWithoutMutationReplay() throws Exception {
        String source=Files.readString(Path.of("src/main/java/com/cpf/admin/approval/repository/AdmApprovalRepository.java"));
        assertThat(source).contains("LEASE_EXPIRES_AT","EXECUTION_LEASE_EXPIRED","RECOVERY_REQUIRED_YN='Y'");
        assertThat(source).contains("COMMAND_REQUEST_ID=?", "LEASE_OWNER=?", "FENCE_TOKEN=?");
        assertThat(source).contains("APPROVAL_STATUS='UNKNOWN'");
        assertThat(source).doesNotContain("sweepExpiredExecutions(Instant now,int maxRows,String operatorId){\n        port.execute");
    }
}
