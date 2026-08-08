package com.cpf.reference.online.modern;

import com.cpf.core.api.attachment.CpfObjectStorageOperations;
import com.cpf.core.api.health.CpfHealthSnapshotProvider;
import com.cpf.core.api.locking.CpfLockManager;
import com.cpf.core.api.reliability.CpfEventSchemaRegistry;
import com.cpf.core.api.security.CpfSessionOperations;
import com.cpf.starter.integration.realtime.CpfRealtimeHub;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/edu/modern")
public class ReferenceModernCapabilityController {
    private final ObjectProvider<CpfHealthSnapshotProvider> health;
    private final ObjectProvider<CpfSessionOperations> sessions;
    private final ObjectProvider<CpfObjectStorageOperations> storage;
    private final ObjectProvider<CpfEventSchemaRegistry> schemas;
    private final ObjectProvider<CpfRealtimeHub> realtime;
    private final ObjectProvider<CpfLockManager> locks;
    public ReferenceModernCapabilityController(ObjectProvider<CpfHealthSnapshotProvider> health,
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
