package cpf.xyz.edu.transaction.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * XYZ 트랜잭션 교육에서 감사성 보조 처리를 분리해서 보여주는 서비스입니다.
 *
 * <p>실제 업무에서는 감사 로그를 DB에 적재하지만, EDU 모듈은 개발자가 트랜잭션 전파 방식을 빠르게
 * 확인할 수 있도록 메모리 목록에 메시지를 보관합니다. 이 클래스는 운영 저장소가 아니라 교육 전용
 * 동작을 명확히 드러내기 위해 edu 패키지 아래에만 둡니다.</p>
 */
@Service
public class XyzTransactionEducationAuditService {
    private final List<String> auditMessages = new ArrayList<>();

    /**
     * 주 트랜잭션과 분리된 감사 기록 흐름을 보여줍니다.
     *
     * @param message 감사 교육 메시지
     */
    @Transactional(transactionManager = "cmnTransactionManager", propagation = Propagation.REQUIRES_NEW)
    public void writeAuditRequiresNew(String message) {
        auditMessages.add(message);
    }

    /**
     * 교육 실행 중 누적된 감사 메시지를 복사본으로 반환합니다.
     *
     * @return 감사 메시지 목록
     */
    public List<String> getAuditMessages() {
        return List.copyOf(auditMessages);
    }
}
