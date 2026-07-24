package com.cpf.member;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 
 * @author CPF Team
 * @version 1.0.0
 */
@SpringBootTest(properties = {
        "cpf.cmn.cache.preload-enabled=false",
        "cpf.cmn.cache.event-poll-enabled=false",
        "cpf.channel-policy.startup-load-enabled=false",
        "cpf.execution-catalog.db-access-enabled=false"
})
class MbrApplicationTests {
    
    @Test
    void contextLoads() {
    }
}
