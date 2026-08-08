package com.cpf.core.api.security.audit;

import java.util.List;
import java.util.Optional;

/** 감사 레코드와 Durable Head를 원자적으로 저장하는 Append-only 저장소 계약입니다. */
public interface CpfTamperAuditStore {
    Optional<CpfTamperAuditRecord> latest();
    CpfTamperAuditHead head();
    boolean append(String expectedPreviousHash, CpfTamperAuditRecord record);
    List<CpfTamperAuditRecord> scan(long fromSequence, int limit);
}
