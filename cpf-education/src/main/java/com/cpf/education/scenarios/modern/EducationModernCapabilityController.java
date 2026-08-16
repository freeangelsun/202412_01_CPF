package com.cpf.education.scenarios.modern;
import com.cpf.file.objectstorage.api.CpfObjectStorageOperations;
import com.cpf.platform.operations.api.health.CpfHealthSnapshotProvider;
import com.cpf.data.lock.api.CpfLockManager;
import com.cpf.messaging.schema.api.CpfEventSchemaRegistry;
import com.cpf.security.api.CpfSessionOperations;
import com.cpf.integration.realtime.CpfRealtimeHub;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/edu/modern")
/** EducationModernCapabilityController 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public class EducationModernCapabilityController {
    private final ObjectProvider<CpfHealthSnapshotProvider> health;
    private final ObjectProvider<CpfSessionOperations> sessions;
    private final ObjectProvider<CpfObjectStorageOperations> storage;
    private final ObjectProvider<CpfEventSchemaRegistry> schemas;
    private final ObjectProvider<CpfRealtimeHub> realtime;
    private final ObjectProvider<CpfLockManager> locks;
    /** EducationModernCapabilityController 작업을 CPF 표준 계약에 따라 수행한다. */
    public EducationModernCapabilityController(ObjectProvider<CpfHealthSnapshotProvider> health,
            ObjectProvider<CpfSessionOperations> sessions, ObjectProvider<CpfObjectStorageOperations> storage,
            ObjectProvider<CpfEventSchemaRegistry> schemas, ObjectProvider<CpfRealtimeHub> realtime,
            ObjectProvider<CpfLockManager> locks) {
        this.health=health;this.sessions=sessions;this.storage=storage;this.schemas=schemas;this.realtime=realtime;this.locks=locks;
    }
    @GetMapping("/health") ResponseEntity<?> health(){var x=health.getIfAvailable();return x==null?disabled("health"):ResponseEntity.ok(x.snapshot());}
    @PostMapping("/session") ResponseEntity<?> session(@RequestParam String tenantId,@RequestParam String principalId){var x=sessions.getIfAvailable();return x==null?disabled("session-valkey"):ResponseEntity.ok(x.create(tenantId,principalId,Duration.ofMinutes(5),Map.of("edu","true")));}
    @GetMapping("/storage/head") ResponseEntity<?> head(@RequestParam String tenantId,@RequestParam String bucket,@RequestParam String key){var x=storage.getIfAvailable();return x==null?disabled("object-storage"):ResponseEntity.ok(x.head(tenantId,bucket,key).orElse(null));}
    @GetMapping("/schema/{subject}") ResponseEntity<?> schema(@PathVariable String subject){var x=schemas.getIfAvailable();return x==null?disabled("schema-governance"):ResponseEntity.ok(x.latest(subject).orElse(null));}
    @PostMapping("/realtime/{topic}") ResponseEntity<?> publish(@PathVariable String topic,@RequestParam String tenantId,@RequestBody Map<String,Object> payload){var x=realtime.getIfAvailable();return x==null?disabled("realtime"):ResponseEntity.ok(Map.of("delivered",x.publish(topic,tenantId,UUID.randomUUID().toString(),payload)));}
    @PostMapping("/lock/{key}") ResponseEntity<?> lock(@PathVariable String key,@RequestParam String owner){var x=locks.getIfAvailable();if(x==null)return disabled("lock");var out=x.acquire(key,owner,UUID.randomUUID().toString(),Duration.ofSeconds(15));return ResponseEntity.ok(out);}
    private static ResponseEntity<Map<String,String>> disabled(String capability){return ResponseEntity.status(503).body(Map.of("capability",capability,"status","DISABLED"));}
}
