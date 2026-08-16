package com.cpf.education.operations.runtime;
import com.cpf.education.operations.runtime.model.EduFailurePoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

public abstract class AbstractManualEduConcurrencyTest extends AbstractManualEduTestSupport {
    @TempDir Path directory;

    @Test void concurrentDuplicateRequestsCreateOneDurableOperation() throws Exception {
        var definition = handler().definition();
        var payload = validPayload();
        String idempotencyKey = "concurrent-" + definition.requirementId();
        var command = new com.cpf.education.operations.runtime.model.EduExecutionCommand(
                "business-" + definition.requirementId(), idempotencyKey, 0, "tester",
                Set.of(definition.requiredRole()), "TENANT-A", "concurrency verification",
                UUID.randomUUID().toString(), UUID.randomUUID().toString(), payload,
                EduFailurePoint.NONE, true, true);
        ExecutorService pool = Executors.newFixedThreadPool(8);
        try {
            List<Future<String>> futures = new ArrayList<>();
            for (int i=0;i<8;i++) futures.add(pool.submit(() -> service(directory).execute(
                    definition.requirementId(), command).operationId()));
            Set<String> operationIds = new HashSet<>();
            for (Future<String> future : futures) operationIds.add(future.get(30, TimeUnit.SECONDS));
            assertEquals(1, operationIds.size());
            assertEquals(1, service(directory).find(definition.requirementId(), 100).size());
        } finally {
            pool.shutdownNow();
        }
    }
}
