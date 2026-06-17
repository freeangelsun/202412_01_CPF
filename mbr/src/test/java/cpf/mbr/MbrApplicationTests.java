package cpf.mbr;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * MBR ⑤뱢 湲곕낯 Spring Boot ?뚯뒪??
 * 
 * @author FPS Team
 * @version 1.0.0
 */
@SpringBootTest(properties = {
        "cpf.cmn.cache.preload-enabled=false",
        "cpf.cmn.cache.event-poll-enabled=false"
})
class MbrApplicationTests {
    
    @Test
    void contextLoads() {
        // ?좏뵆由ъ??댁뀡 ?뺤긽 濡쒕뱶 ?뺤씤
    }
}

