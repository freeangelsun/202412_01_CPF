package cpf.pfw.common.logging.segment;

import cpf.pfw.mapper.common.logging.TransactionSegmentMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 거래 구간 로그를 업무 트랜잭션과 분리된 PFW 트랜잭션으로 저장합니다.
 *
 * <p>별도 Spring bean으로 분리해 같은 클래스 내부 호출로 트랜잭션 프록시가
 * 우회되지 않도록 합니다. 업무 처리의 commit 또는 rollback 여부와 무관하게
 * segment 시작과 종료 상태를 독립적으로 남기는 것이 이 서비스의 책임입니다.</p>
 */
@Service
public class TransactionSegmentPersistenceService {
    private final TransactionSegmentMapper mapper;

    public TransactionSegmentPersistenceService(TransactionSegmentMapper mapper) {
        this.mapper = mapper;
    }

    @Transactional(transactionManager = "pfwTransactionManager", propagation = Propagation.REQUIRES_NEW)
    public void insert(TransactionSegmentRecord record) {
        mapper.insertSegment(record);
    }

    @Transactional(transactionManager = "pfwTransactionManager", propagation = Propagation.REQUIRES_NEW)
    public void updateEnd(TransactionSegmentRecord record) {
        mapper.updateSegmentEnd(record);
    }
}
