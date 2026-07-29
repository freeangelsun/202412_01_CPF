package com.cpf.member.sampleitem.port;

import com.cpf.member.sampleitem.dto.*;

/** 변경·멱등성·낙관적 잠금 책임을 소유하는 Generated Domain Command Port입니다. */
public interface MemberCommandPort {
    MemberSampleItem create(MemberSampleCommand command,
            String transactionId, String idempotencyKey, long transactionSequence, String actor);
    MemberSampleItem update(long sampleItemId, MemberSampleCommand command,
            String transactionId, String idempotencyKey, long transactionSequence, String actor);
    MemberDeleteResult delete(long sampleItemId, long expectedVersion,
            String transactionId, String idempotencyKey, long transactionSequence, String actor);
    boolean verifyRollback(MemberSampleCommand command,
            String transactionId, String idempotencyKey, long transactionSequence, String actor);
}