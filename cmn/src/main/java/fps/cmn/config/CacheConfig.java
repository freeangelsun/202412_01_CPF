package fps.cmn.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.TimeUnit;

/**
 * CacheConfig.java
 *
 * - Caffeine 캐시를 사용한 Spring CacheManager 설정
 * - CMN 모듈은 독립 실행되지 않으며, ACC 등 다른 모듈에 의해 사용됩니다.
 * - Caffeine의 고성능, 저지연 특성을 활용합니다.
 */
@Configuration
@EnableCaching // Spring Cache 기능 활성화
@EnableScheduling // CMN 캐시 주기 갱신과 DB 이벤트 기반 리프레시 리스너를 활성화
public class CacheConfig {

    /**
     * CacheManager 빈 생성
     *
     * - CaffeineCacheManager를 통해 Caffeine 캐시를 설정합니다.
     * - 초기 용량, 최대 항목 수, 만료 정책 등을 정의합니다.
     *
     * @return CacheManager Spring에서 사용할 캐시 매니저 빈
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("codeCache", "messageCache", "configCache");

        // Caffeine 캐시 설정
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .initialCapacity(100)   // 초기 캐시 항목 수
                .maximumSize(1000)      // 최대 캐시 항목 수
                // expireAfterWrite / expireAfterAccess 두 설정을 사용하지 않으면 캐시 항목은 명시적으로 갱신될 때까지 유지
                .expireAfterWrite(10, TimeUnit.MINUTES) // 항목 쓰기 후 10분 만료
                .recordStats());        // 캐시 통계 기록 활성화

        return cacheManager;
    }
}
