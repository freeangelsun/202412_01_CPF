package fps.cmn.ref.service;

import fps.cmn.ref.mapper.CacheRefreshEventMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * CMN 캐시 변경 이벤트를 발행하는 서비스입니다.
 *
 * <p>CRUD 자체는 코드/메시지/설정 서비스가 수행하고,
 * 이 서비스는 다른 WAS가 변경 사실을 알 수 있도록 이벤트 테이블에 기록만 담당합니다.</p>
 */
@Service
public class CacheRefreshEventPublisher {
    private static final Logger logger = LoggerFactory.getLogger(CacheRefreshEventPublisher.class);

    private final CacheRefreshEventMapper cacheRefreshEventMapper;

    @Value("${fps.framework.was-id:local}")
    private String wasId;

    public CacheRefreshEventPublisher(CacheRefreshEventMapper cacheRefreshEventMapper) {
        this.cacheRefreshEventMapper = cacheRefreshEventMapper;
    }

    /**
     * 캐시 리프레시 이벤트를 발행합니다.
     *
     * <p>이벤트 발행은 별도 트랜잭션으로 처리합니다.
     * 이벤트 테이블이 아직 생성되지 않은 로컬 환경에서도 업무 CRUD가 실패하지 않도록
     * 예외는 경고 로그로만 남깁니다.</p>
     *
     * @param cacheName 변경된 캐시 이름입니다.
     * @param eventType 변경 유형입니다.
     * @param eventKey 변경된 업무 키입니다.
     * @param requestUser 변경 요청 사용자입니다.
     */
    @Transactional(transactionManager = "cmnTransactionManager", propagation = Propagation.REQUIRES_NEW)
    public void publish(String cacheName, String eventType, String eventKey, String requestUser) {
        publishNow(cacheName, eventType, eventKey, requestUser);
    }

    /**
     * 현재 업무 트랜잭션이 정상 커밋된 뒤 캐시 리프레시 이벤트를 발행합니다.
     *
     * <p>CRUD 데이터가 커밋되기 전에 다른 WAS가 이벤트를 먼저 읽으면
     * 오래된 데이터를 다시 캐시할 수 있습니다. 그래서 트랜잭션 안에서는
     * afterCommit 훅으로 지연 발행하고, 트랜잭션 밖에서는 즉시 발행합니다.</p>
     *
     * @param cacheName 변경된 캐시 이름입니다.
     * @param eventType 변경 유형입니다.
     * @param eventKey 변경된 업무 키입니다.
     * @param requestUser 변경 요청 사용자입니다.
     */
    public void publishAfterCommit(String cacheName, String eventType, String eventKey, String requestUser) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publishNow(cacheName, eventType, eventKey, requestUser);
                }
            });
            return;
        }

        publish(cacheName, eventType, eventKey, requestUser);
    }

    private void publishNow(String cacheName, String eventType, String eventKey, String requestUser) {
        String publishedBy = hasText(requestUser) ? requestUser : "SYSTEM";
        try {
            cacheRefreshEventMapper.insertEvent(cacheName, eventType, eventKey, wasId, publishedBy);
            logger.info("CMN cache refresh event published. cacheName={}, eventType={}, eventKey={}",
                    cacheName, eventType, eventKey);
        } catch (RuntimeException ex) {
            logger.warn("CMN cache refresh event publish skipped. cacheName={}, eventType={}, eventKey={}, reason={}",
                    cacheName, eventType, eventKey, ex.getMessage());
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
