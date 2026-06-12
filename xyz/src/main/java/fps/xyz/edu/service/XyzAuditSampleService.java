package fps.xyz.edu.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 분리 트랜잭션 예시를 보여주기 위한 감사 샘플 서비스입니다.
 *
 * <p>실무에서는 이 서비스가 감사 테이블에 INSERT를 수행합니다. 현재 XYZ는 교육용 모듈이므로
 * 메모리 목록에 감사 메시지를 저장하되, 메서드 선언은 {@code REQUIRES_NEW}로 두어
 * 분리 트랜잭션을 어떤 식으로 선언하는지 보여줍니다.</p>
 */
@Service
public class XyzAuditSampleService {
    private final List<String> auditMessages = new ArrayList<>();

    /**
     * 원거래와 분리된 새 트랜잭션으로 감사 이력을 남기는 샘플입니다.
     *
     * @param message 감사 메시지
     */
    @Transactional(transactionManager = "cmnTransactionManager", propagation = Propagation.REQUIRES_NEW)
    public void writeAuditRequiresNew(String message) {
        auditMessages.add(message);
    }

    /**
     * 현재 메모리에 저장된 감사 샘플 메시지를 조회합니다.
     *
     * @return 감사 메시지 목록
     */
    public List<String> getAuditMessages() {
        return List.copyOf(auditMessages);
    }
}
