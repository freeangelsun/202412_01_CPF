package cpf.mbr;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * CPF 기능 설명입니다.
 * 
 * @author CPF Team
 * @version 1.0.0
 */
@SpringBootTest(properties = {
        "cpf.cmn.cache.preload-enabled=false",
        "cpf.cmn.cache.event-poll-enabled=false"
})
class MbrApplicationTests {
    
    @Test
    void contextLoads() {
        // CPF 기능 설명입니다.
    }
}

