package cpf.cmn.ref.service;

import cpf.cmn.ref.mapper.CacheRefreshEventMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * CMN 罹먯떆 蹂寃??대깽?몃? 諛쒗뻾?섎뒗 ?쒕퉬?ㅼ엯?덈떎.
 *
 * <p>CRUD ?먯껜??肄붾뱶/硫붿떆吏/?ㅼ젙 ?쒕퉬?ㅺ? ?섑뻾?섍퀬,
 * ???쒕퉬?ㅻ뒗 ?ㅻⅨ WAS媛 蹂寃??ъ떎???????덈룄濡??대깽???뚯씠釉붿뿉 湲곕줉留??대떦?⑸땲??</p>
 */
@Service
public class CacheRefreshEventPublisher {
    private static final Logger logger = LoggerFactory.getLogger(CacheRefreshEventPublisher.class);

    private final CacheRefreshEventMapper cacheRefreshEventMapper;

    @Value("${cpf.framework.was-id:local}")
    private String wasId;

    public CacheRefreshEventPublisher(CacheRefreshEventMapper cacheRefreshEventMapper) {
        this.cacheRefreshEventMapper = cacheRefreshEventMapper;
    }

    /**
     * 罹먯떆 由ы봽?덉떆 ?대깽?몃? 諛쒗뻾?⑸땲??
     *
     * <p>?대깽??諛쒗뻾? 蹂꾨룄 ?몃옖??뀡?쇰줈 泥섎━?⑸땲??
     * ?대깽???뚯씠釉붿씠 ?꾩쭅 ?앹꽦?섏? ?딆? 濡쒖뺄 ?섍꼍?먯꽌???낅Т CRUD媛 ?ㅽ뙣?섏? ?딅룄濡?     * ?덉쇅??寃쎄퀬 濡쒓렇濡쒕쭔 ?④퉩?덈떎.</p>
     *
     * @param cacheName 蹂寃쎈맂 罹먯떆 ?대쫫?낅땲??
     * @param eventType 蹂寃??좏삎?낅땲??
     * @param eventKey 蹂寃쎈맂 ?낅Т ?ㅼ엯?덈떎.
     * @param requestUser 蹂寃??붿껌 ?ъ슜?먯엯?덈떎.
     */
    @Transactional(transactionManager = "cmnTransactionManager", propagation = Propagation.REQUIRES_NEW)
    public void publish(String cacheName, String eventType, String eventKey, String requestUser) {
        publishNow(cacheName, eventType, eventKey, requestUser);
    }

    /**
     * ?꾩옱 ?낅Т ?몃옖??뀡???뺤긽 而ㅻ컠????罹먯떆 由ы봽?덉떆 ?대깽?몃? 諛쒗뻾?⑸땲??
     *
     * <p>CRUD ?곗씠?곌? 而ㅻ컠?섍린 ?꾩뿉 ?ㅻⅨ WAS媛 ?대깽?몃? 癒쇱? ?쎌쑝硫?     * ?ㅻ옒???곗씠?곕? ?ㅼ떆 罹먯떆?????덉뒿?덈떎. 洹몃옒???몃옖??뀡 ?덉뿉?쒕뒗
     * afterCommit ?낆쑝濡?吏??諛쒗뻾?섍퀬, ?몃옖??뀡 諛뽰뿉?쒕뒗 利됱떆 諛쒗뻾?⑸땲??</p>
     *
     * @param cacheName 蹂寃쎈맂 罹먯떆 ?대쫫?낅땲??
     * @param eventType 蹂寃??좏삎?낅땲??
     * @param eventKey 蹂寃쎈맂 ?낅Т ?ㅼ엯?덈떎.
     * @param requestUser 蹂寃??붿껌 ?ъ슜?먯엯?덈떎.
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

