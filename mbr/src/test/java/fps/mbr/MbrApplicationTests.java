package fps.mbr;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * MBR 모듈 기본 Spring Boot 테스트
 * 
 * @author FPS Team
 * @version 1.0.0
 */
@SpringBootTest(properties = {
        "fps.cmn.cache.preload-enabled=false",
        "fps.cmn.cache.event-poll-enabled=false"
})
class MbrApplicationTests {
    
    @Test
    void contextLoads() {
        // 애플리케이션 정상 로드 확인
    }
}
