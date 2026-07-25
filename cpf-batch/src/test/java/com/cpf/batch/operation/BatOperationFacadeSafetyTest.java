package com.cpf.batch.operation;

import org.junit.jupiter.api.Test;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import static org.junit.jupiter.api.Assertions.*;

/** BAT Ghost lock 해제의 lock-key 계산이 Runtime LockManager와 동일한지 회귀 검증합니다. */
class BatOperationFacadeSafetyTest {
    @Test
    void ghostLockKeyUsesSameRuntimeContract() throws Exception {
        Method m=BatOperationFacade.class.getDeclaredMethod("buildLockKey",String.class,String.class);m.setAccessible(true);
        String params="{\"runId\":1}";
        String hash=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(params.getBytes(StandardCharsets.UTF_8)));
        assertEquals("batch:job:JOB_A:"+hash,m.invoke(null,"JOB_A",params));
        String spaced="  {\"runId\":1}  ";
        String spacedHash=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(spaced.getBytes(StandardCharsets.UTF_8)));
        assertEquals("batch:job:JOB_A:"+spacedHash,m.invoke(null,"  JOB_A  ",spaced),
                "jobId만 trim하고 non-blank jobParameters는 Runtime LockManager와 동일하게 원문 hash해야 함");
    }
}
