package com.cpf.core.common.database;

/** Replica 연결 가능성과 적용 지연을 조회하는 내부 Runtime Port입니다. */
public interface CpfReplicaHealthMonitor {
    Status current();

    record Status(boolean healthy, long lagMillis, String reason) {
        public static Status unavailable(String reason) { return new Status(false, Long.MAX_VALUE, reason); }
        public static Status healthy(long lagMillis) { return new Status(true, Math.max(0L, lagMillis), null); }
    }
}
