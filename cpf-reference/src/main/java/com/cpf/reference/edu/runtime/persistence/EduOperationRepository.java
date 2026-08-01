package com.cpf.reference.edu.runtime.persistence;
import com.cpf.reference.edu.runtime.model.*;
import java.time.Instant;
import java.util.*;
public interface EduOperationRepository {
    EduCreateResult create(EduOperationRecord record);
    Optional<EduOperationRecord> find(String operationId);
    Optional<EduOperationRecord> findByIdempotency(String requirementId, String idempotencyKey);
    List<EduOperationRecord> findByRequirement(String requirementId, int limit);
    EduOperationRecord save(EduOperationRecord record, long expectedRecordVersion);
    void appendAudit(EduAuditRecord audit);
    List<EduAuditRecord> audits(String operationId);
    void saveTarget(EduTargetRecord target);
    List<EduTargetRecord> targets(String operationId);
    void enqueue(EduOutboxRecord event);
    void saveOutbox(EduOutboxRecord event);
    List<EduOutboxRecord> outbox(String operationId);
    long claimLease(String leaseKey, String ownerId, Instant expiresAt);
}
