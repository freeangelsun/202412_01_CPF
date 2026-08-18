package com.cpf.education.contract;

import com.cpf.education.contract.support.ReferenceFixtureIdempotencyRepository;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Oracle/PostgreSQL/MariaDB reference fixture에서 idempotency replay/conflict를 실제로 검증합니다. */
class ReferenceFixtureLiveIdempotencyTest {
    @Test
    void sameKeyReplayAndDifferentHashConflict() throws Exception {
        Assumptions.assumeTrue("true".equalsIgnoreCase(System.getenv("CPF_EDU_LIVE_DB_TEST")));
        String url=required("CPF_EDU_LIVE_JDBC_URL"), user=required("CPF_EDU_LIVE_DB_USERNAME"), password=required("CPF_EDU_LIVE_DB_PASSWORD"), output=required("CPF_EDU_LIVE_RESULT_PATH");
        String requirement="EDU-REF-LIVE", key="K-"+Long.toUnsignedString(System.nanoTime(),36);
        var repository=new ReferenceFixtureIdempotencyRepository();
        int beforeCleanup=0, afterCleanup=0; boolean replay=false, conflict=false;
        try(var connection=DriverManager.getConnection(url,user,password)) {
            try {
                assertEquals(ReferenceFixtureIdempotencyRepository.RegisterResult.INSERTED,repository.register(connection,requirement,key,"HASH-A"));
                replay=repository.register(connection,requirement,key,"HASH-A")==ReferenceFixtureIdempotencyRepository.RegisterResult.REPLAY;
                try { repository.register(connection,requirement,key,"HASH-B"); fail("different hash must conflict"); } catch(IllegalStateException expected){ conflict="IDEMPOTENCY_PAYLOAD_CONFLICT".equals(expected.getMessage()); }
                beforeCleanup=repository.count(connection,requirement,key);
            } finally {
                repository.delete(connection,requirement,key); afterCleanup=repository.count(connection,requirement,key);
            }
        }
        String json="{\"status\":\"PASS\",\"sameHashReplay\":"+replay+",\"differentHashConflict\":"+conflict+",\"rowCountBeforeCleanup\":"+beforeCleanup+",\"cleanupRowCount\":"+afterCleanup+"}\n";
        Files.writeString(Path.of(output),json,StandardCharsets.UTF_8);
        assertTrue(replay); assertTrue(conflict); assertEquals(1,beforeCleanup); assertEquals(0,afterCleanup);
    }
    private static String required(String name){String value=System.getenv(name);if(value==null||value.isBlank())throw new IllegalStateException(name+" is required");return value;}
}
