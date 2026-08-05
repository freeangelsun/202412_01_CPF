package com.cpf.core.common.runtimecontrol;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CpfRuntimeControlPlaneSqlContractTest {
    @Test
    void changeInsertHasOneCanonicalColumnListAndMatchingValues() {
        String sql = CpfRuntimeControlPlaneRepository.INSERT_CHANGE_SQL;

        assertEquals(1, occurrences(sql, "rollout_mode"));
        assertEquals(22, columnCount(sql));
        assertEquals(22, occurrences(sql, "?"));
        assertFalse(sql.contains(") rollout_mode"));
    }

    @Test
    void claimUpdateRechecksChangeStateAndExpiryAtWriteTime() {
        String sql = CpfRuntimeControlPlaneRepository.CLAIM_DELIVERY_SQL;

        assertEquals(1, occurrences(sql, "delivery_state IN ('PENDING','FAILED')"));
        assertEquals(1, occurrences(sql, "c.change_state IN ('APPLYING','PARTIAL')"));
        assertEquals(1, occurrences(sql, "c.expires_at>CURRENT_TIMESTAMP"));
        assertEquals(1, occurrences(sql, "s.fencing_token=?"));
        assertEquals(1, occurrences(sql, "s.lease_until>CURRENT_TIMESTAMP"));
        assertEquals(4, occurrences(sql, "?"));
    }

    @Test
    void claimCandidatesEnforceGlobalInstanceVersionOrder() {
        String sql = CpfRuntimeControlPlaneRepository.CLAIM_CANDIDATE_SQL;

        assertEquals(1, occurrences(sql, "older.desired_version<d.desired_version"));
        assertEquals(0, occurrences(sql, "older_change.change_type=c.change_type"));
        assertEquals(1, occurrences(sql, "'ACKED','CANCELLED','EXPIRED','SUPERSEDED'"));
    }

    private int columnCount(String sql) {
        int open = sql.indexOf('(');
        int close = sql.indexOf(')', open);
        return sql.substring(open + 1, close).split(",").length;
    }

    private int occurrences(String text, String token) {
        int count = 0;
        for (int index = 0; (index = text.indexOf(token, index)) >= 0; index += token.length()) {
            count++;
        }
        return count;
    }
    @Test void healthUsesAggregateForOldestBacklogAndAvoidsUnboundedRead(){
        String sql=CpfRuntimeControlPlaneRepository.HEALTH_OLDEST_BACKLOG_SQL;
        assertTrue(sql.contains("SELECT MIN(created_at) FROM cpf_runtime_delivery"));
        assertFalse(sql.contains("ORDER BY"));
    }

    @Test
    void acknowledgeRechecksCurrentInstanceFenceAndLeaseAtWriteTime() {
        String sql=CpfRuntimeControlPlaneRepository.ACK_DELIVERY_SQL;
        assertEquals(1,occurrences(sql,"attempt_no=?"));
        assertEquals(1,occurrences(sql,"s.fencing_token=?"));
        assertEquals(1,occurrences(sql,"s.lease_until>CURRENT_TIMESTAMP"));
        assertEquals(11,occurrences(sql,"?"));
    }

}
