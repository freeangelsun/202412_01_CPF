package com.cpf.core.common.logging.segment;

import com.cpf.core.common.logging.fallback.TransactionSegmentFallbackStore;
import com.cpf.core.mapper.common.logging.TransactionSegmentMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 거래 구간 로그를 업무 트랜잭션과 분리된 CPF 트랜잭션으로 저장합니다.
 *
 * <p>별도 Spring bean으로 분리해 같은 클래스 내부 호출로 트랜잭션 프록시가
 * 우회되지 않도록 합니다. 업무 처리의 commit 또는 rollback 여부와 무관하게
 * segment 시작과 종료 상태를 독립적으로 남기는 것이 이 서비스의 책임입니다.</p>
 */
@Service
public class TransactionSegmentPersistenceService {
    private final TransactionSegmentMapper mapper;
    private final TransactionSegmentFallbackStore fallbackStore;

    public TransactionSegmentPersistenceService(
            TransactionSegmentMapper mapper,
            TransactionSegmentFallbackStore fallbackStore) {
        this.mapper = mapper;
        this.fallbackStore = fallbackStore;
    }

    @Transactional(transactionManager = "cpfTransactionManager", propagation = Propagation.REQUIRES_NEW)
    public void insert(TransactionSegmentRecord record) {
        try {
            mapper.insertSegment(record);
        } catch (RuntimeException ex) {
            preserveStart(record, ex);
            throw ex;
        }
    }

    @Transactional(transactionManager = "cpfTransactionManager", propagation = Propagation.REQUIRES_NEW)
    public void updateEnd(TransactionSegmentRecord record) {
        try {
            if (mapper.updateSegmentEnd(record) == 0) {
                throw new IllegalStateException("종료할 거래 구간 시작 레코드가 없습니다.");
            }
        } catch (RuntimeException ex) {
            preserveEnd(record, ex);
            throw ex;
        }
    }

    /**
     * recovery worker 전용 시작 저장입니다. 실패 시 journal을 다시 만들지 않습니다.
     */
    @Transactional(transactionManager = "cpfTransactionManager", propagation = Propagation.REQUIRES_NEW)
    public void insertRecovered(TransactionSegmentRecord record) {
        mapper.insertSegment(record);
    }

    /**
     * recovery worker 전용 종료 저장입니다. START가 아직 없으면 재시도 대상으로 남깁니다.
     */
    @Transactional(transactionManager = "cpfTransactionManager", propagation = Propagation.REQUIRES_NEW)
    public void updateEndRecovered(TransactionSegmentRecord record) {
        if (mapper.countByTransactionSegmentId(record.getTransactionSegmentId()) == 0) {
            throw new IllegalStateException("거래 구간 START 복구가 완료되지 않았습니다.");
        }
        if (mapper.updateSegmentEnd(record) == 0) {
            throw new IllegalStateException("거래 구간 END 복구 대상을 찾지 못했습니다.");
        }
    }

    private void preserveStart(TransactionSegmentRecord record, RuntimeException original) {
        try {
            fallbackStore.enqueueStart(record, original);
        } catch (RuntimeException fallbackFailure) {
            original.addSuppressed(fallbackFailure);
        }
    }

    private void preserveEnd(TransactionSegmentRecord record, RuntimeException original) {
        try {
            fallbackStore.enqueueEnd(record, original);
        } catch (RuntimeException fallbackFailure) {
            original.addSuppressed(fallbackFailure);
        }
    }
}
