package com.cpf.core.spi.security;

import com.cpf.core.api.security.CpfSensitiveDataAccessOperations.AccessGrant;
import com.cpf.core.api.security.CpfSensitiveDataAccessOperations.AccessStatus;

import java.time.Instant;

/** 민감정보 원문 조회 승인·거절·소비를 append-only 감사 저장소로 전달하는 SPI입니다. */
@FunctionalInterface
public interface CpfSensitiveDataAccessAuditSink {
    void record(String action, AccessStatus result, AccessGrant grant, String actorId, Instant occurredAt, String errorCode);

    default boolean available() {
        return true;
    }

    static CpfSensitiveDataAccessAuditSink unavailable() {
        return new CpfSensitiveDataAccessAuditSink() {
            @Override
            public void record(String action, AccessStatus result, AccessGrant grant,
                    String actorId, Instant occurredAt, String errorCode) {
                throw new IllegalStateException("sensitive-data audit sink is unavailable");
            }

            @Override
            public boolean available() {
                return false;
            }
        };
    }
}
