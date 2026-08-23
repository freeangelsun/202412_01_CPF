package com.cpf.platform.operations.runtimecontrol.internal;

import com.cpf.platform.operations.runtimecontrol.*;
import com.cpf.platform.operations.runtimecontrol.api.CpfManagedRuntimeSnapshot;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * CPF Runtime Control Plane의 cpfDB 영속화 구현입니다.
 *
 * <p>특정 DB Vendor 문법(ON DUPLICATE KEY/LIMIT/MERGE)에 의존하지 않고 update-first/insert 및
 * conditional update를 사용해 MariaDB/PostgreSQL/Oracle에서 동일한 동시성 의미를 유지합니다.</p>
 */
public class CpfRuntimeControlPlaneRepository {
    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper;

    public CpfRuntimeControlPlaneRepository(
            @Qualifier("cpfJdbcTemplate") ObjectProvider<JdbcTemplate> jdbcProvider,
            ObjectMapper objectMapper) {
        this.jdbc = jdbcProvider.getIfAvailable();
        if (this.jdbc == null) throw new IllegalStateException("Runtime Control Plane에는 cpfJdbcTemplate이 필요합니다.");
        this.objectMapper = objectMapper;
    }

    Optional<Map<String, Object>> findCommand(String commandId) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT operation_id, command_type, request_hash, entity_id, result_state, result_json, expires_at " +
                        "FROM OPS_CONTROL_OPERATION WHERE operation_id=?", commandId);
        return rows.stream().findFirst();
    }

    public void consumeRateLimit(String subjectId, int limitPerMinute) {
        requireText(subjectId, "subjectId");
        int limit = Math.max(1, Math.min(10_000, limitPerMinute));
        Instant now = Instant.now();
        String minute = java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmm")
                .withZone(java.time.ZoneOffset.UTC).format(now);
        String bucket = subjectId + ":" + minute;
        int updated = jdbc.update("UPDATE OPS_RUNTIME_RATE_BUCKET SET request_count=request_count+1," +
                        "updated_at=CURRENT_TIMESTAMP WHERE bucket_key=? AND request_count<?",
                bucket, limit);
        if (updated == 1) return;
        Integer existing = jdbc.queryForObject(
                "SELECT COUNT(*) FROM OPS_RUNTIME_RATE_BUCKET WHERE bucket_key=?", Integer.class, bucket);
        if (existing != null && existing > 0) throw new CpfRuntimeRateLimitException(limit);
        try {
            jdbc.update("INSERT INTO OPS_RUNTIME_RATE_BUCKET " +
                            "(bucket_key,subject_id,window_start,request_count,created_by,updated_by) " +
                            "VALUES (?,?,?,1,'CPF','CPF')",
                    bucket, subjectId, ts(now.truncatedTo(java.time.temporal.ChronoUnit.MINUTES)));
        } catch (DuplicateKeyException duplicate) {
            consumeRateLimit(subjectId, limitPerMinute);
        }
    }

    public boolean insertCommand(String commandId, String commandType, String requestHash, Instant expiresAt) {
        try {
            return jdbc.update("INSERT INTO OPS_CONTROL_OPERATION " +
                            "(operation_id, command_type, request_hash, result_state, expires_at, created_by, updated_by) " +
                            "VALUES (?,?,?,?,?,?,?)",
                    commandId, commandType, requestHash, "PROCESSING", ts(expiresAt), "CPF", "CPF") == 1;
        } catch (DuplicateKeyException duplicate) {
            return false;
        }
    }

    public void completeCommand(String commandId, String entityId, String state, String resultJson) {
        int updated = jdbc.update("UPDATE OPS_CONTROL_OPERATION SET entity_id=?, result_state=?, result_json=?, updated_at=CURRENT_TIMESTAMP " +
                        "WHERE operation_id=? AND request_hash IS NOT NULL",
                entityId, state, resultJson, commandId);
        if (updated != 1) throw new IllegalStateException("operation 완료 상태 갱신 실패: " + commandId);
    }

    public long lockAndNextVersion(Long expectedVersion) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT version_no FROM OPS_RUNTIME_VERSION WHERE version_key='GLOBAL' FOR UPDATE");
        if (rows.isEmpty()) {
            try {
                jdbc.update("INSERT INTO OPS_RUNTIME_VERSION (version_key, version_no, created_by, updated_by) VALUES ('GLOBAL',0,'CPF','CPF')");
            } catch (DuplicateKeyException ignored) {
                // 동시 insert는 재조회합니다.
            }
            rows = jdbc.queryForList("SELECT version_no FROM OPS_RUNTIME_VERSION WHERE version_key='GLOBAL' FOR UPDATE");
        }
        long current = ((Number) rows.getFirst().get("version_no")).longValue();
        if (expectedVersion != null && expectedVersion.longValue() != current) {
            throw new CpfRuntimeVersionConflictException(expectedVersion, current);
        }
        long next = current + 1L;
        if (jdbc.update("UPDATE OPS_RUNTIME_VERSION SET version_no=?, updated_at=CURRENT_TIMESTAMP, updated_by='CPF' " +
                "WHERE version_key='GLOBAL' AND version_no=?", next, current) != 1) {
            throw new CpfRuntimeVersionConflictException(current, current);
        }
        return next;
    }

    public List<String> resolveTargets(String changeType, int payloadSchemaVersion, CpfRuntimeTargetSelector selector) {
        if (selector == null) throw new IllegalArgumentException("Runtime target selector가 필요합니다.");
        boolean broad = blank(selector.environment()).isBlank() && blank(selector.serviceId()).isBlank()
                && blank(selector.groupId()).isBlank() && selector.instanceIds().isEmpty()
                && selector.labels().isEmpty() && blank(selector.zone()).isBlank() && blank(selector.cell()).isBlank();
        if (broad && !selector.allowAll()) {
            throw new IllegalArgumentException("전체 Runtime 대상을 선택하려면 allowAll=true가 필요합니다.");
        }
        LinkedHashSet<String> excluded = new LinkedHashSet<>(selector.excludeInstanceIds());
        List<String> selected = resolveTargets(selector).stream()
                .filter(id -> !excluded.contains(id))
                .filter(id -> supportsCapability(id, changeType, payloadSchemaVersion))
                .toList();
        if (selected.size() > 100_000) {
            throw new IllegalArgumentException("Runtime Change 대상은 최대 100000개입니다. 대상=" + selected.size());
        }
        return selected;
    }

    public List<String> resolveTargets(CpfRuntimeTargetSelector selector) {
        if (selector == null) throw new IllegalArgumentException("Runtime target selector가 필요합니다.");
        LinkedHashSet<String> result = new LinkedHashSet<>();
        if (!selector.instanceIds().isEmpty()) result.addAll(selector.instanceIds());
        if (selector.groupId() != null && !selector.groupId().isBlank()) result.addAll(resolveGroup(selector.groupId()));

        if (result.isEmpty()) {
            StringBuilder sql = new StringBuilder("SELECT i.instance_id FROM OPS_SERVICE_INSTANCE i " +
                    "LEFT JOIN OPS_RUNTIME_INSTANCE_STATE s ON s.instance_id=i.instance_id WHERE i.active_yn='Y'");
            List<Object> args = new ArrayList<>();
            if (selector.serviceId() != null && !selector.serviceId().isBlank()) { sql.append(" AND i.service_id=?"); args.add(selector.serviceId()); }
            if (selector.environment() != null && !selector.environment().isBlank()) { sql.append(" AND i.environment_code=?"); args.add(selector.environment()); }
            if (selector.zone() != null && !selector.zone().isBlank()) { sql.append(" AND i.zone_code=?"); args.add(selector.zone()); }
            if (selector.cell() != null && !selector.cell().isBlank()) { sql.append(" AND i.cell_code=?"); args.add(selector.cell()); }
            if (!selector.includeDraining()) sql.append(" AND COALESCE(i.drain_yn,'N')='N'");
            if (!selector.includeMaintenance()) sql.append(" AND COALESCE(i.maintenance_yn,'N')='N'");
            sql.append(" ORDER BY i.instance_id");
            jdbc.queryForList(sql.toString(), args.toArray()).forEach(row -> result.add(String.valueOf(row.get("instance_id"))));
        }
        if (result.isEmpty()) return List.of();

        // 명시 instance/group 결과도 환경/서비스/운영상태 조건을 다시 검증합니다.
        return result.stream().filter(id -> matchesSelector(id, selector)).toList();
    }

    private boolean matchesSelector(String instanceId, CpfRuntimeTargetSelector selector) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT i.instance_id,i.service_id,COALESCE(i.environment_code,'') environment_code," +
                        "COALESCE(i.zone_code,'') zone_code,COALESCE(i.cell_code,'') cell_code," +
                        "COALESCE(i.drain_yn,'N') drain_yn,COALESCE(i.maintenance_yn,'N') maintenance_yn,s.labels_json " +
                        "FROM OPS_SERVICE_INSTANCE i JOIN OPS_RUNTIME_INSTANCE_STATE s ON s.instance_id=i.instance_id " +
                        "WHERE i.instance_id=? AND i.active_yn='Y'", instanceId);
        if (rows.isEmpty()) return false;
        Map<String,Object> row=rows.getFirst();
        if (selector.serviceId()!=null && !selector.serviceId().isBlank() && !selector.serviceId().equals(String.valueOf(row.get("service_id")))) return false;
        if (selector.environment()!=null && !selector.environment().isBlank() && !selector.environment().equals(String.valueOf(row.get("environment_code")))) return false;
        if (selector.zone()!=null && !selector.zone().isBlank() && !selector.zone().equals(String.valueOf(row.get("zone_code")))) return false;
        if (selector.cell()!=null && !selector.cell().isBlank() && !selector.cell().equals(String.valueOf(row.get("cell_code")))) return false;
        if (!selector.includeDraining() && "Y".equalsIgnoreCase(String.valueOf(row.get("drain_yn")))) return false;
        if (!selector.includeMaintenance() && "Y".equalsIgnoreCase(String.valueOf(row.get("maintenance_yn")))) return false;
        if (!selector.labels().isEmpty()) {
            Map<String,Object> labels = readMapOrEmpty(nullable(row.get("labels_json")));
            for (Map.Entry<String,String> expected : selector.labels().entrySet()) {
                if (!expected.getValue().equals(String.valueOf(labels.get(expected.getKey())))) return false;
            }
        }
        return true;
    }

    private boolean supportsCapability(String instanceId, String changeType, int payloadSchemaVersion) {
        List<Map<String,Object>> rows = jdbc.queryForList(
                "SELECT capabilities_json FROM OPS_RUNTIME_INSTANCE_STATE WHERE instance_id=?", instanceId);
        if (rows.isEmpty()) return false;
        Map<String,Object> capabilities = readMapOrEmpty(nullable(rows.getFirst().get("capabilities_json")));
        Object encoded = capabilities.get(baseChangeType(changeType));
        if (encoded == null) return false;
        String value = String.valueOf(encoded);
        int separator = value.indexOf('|');
        String schema = separator < 0 ? value : value.substring(0, separator);
        try {
            return Integer.parseInt(schema.trim()) == Math.max(1, payloadSchemaVersion);
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private Set<String> resolveGroup(String rootGroupId) {
        LinkedHashSet<String> instances = new LinkedHashSet<>();
        LinkedHashSet<String> visited = new LinkedHashSet<>();
        ArrayList<String> queue = new ArrayList<>(); queue.add(rootGroupId);
        for (int index=0; index<queue.size(); index++) {
            String groupId=queue.get(index);
            if (!visited.add(groupId)) continue;
            if (visited.size()>1000) throw new IllegalStateException("Runtime group 중첩이 허용 한도를 초과했습니다.");
            jdbc.queryForList("SELECT instance_id FROM OPS_RUNTIME_GROUP_MEMBER WHERE group_id=? AND active_yn='Y' ORDER BY instance_id", groupId)
                    .forEach(row -> instances.add(String.valueOf(row.get("instance_id"))));
            jdbc.queryForList("SELECT group_id FROM OPS_RUNTIME_INSTANCE_GROUP WHERE parent_group_id=? AND active_yn='Y' ORDER BY group_id", groupId)
                    .forEach(row -> queue.add(String.valueOf(row.get("group_id"))));
        }
        return instances;
    }

    public void insertChange(String changeId, String operationId, String type, int payloadSchemaVersion,
                             String requestHash, String payloadHash, String payloadJson,
                             String rollbackJson, String targetSnapshotJson, long version, String rolloutMode,
                             int waveSize, int quorumPercent, Instant scheduledAt, Instant expiresAt,
                             String reason, String approvalId, String breakGlassId, String requestedBy, List<String> targets) {
        String state = scheduledAt != null && scheduledAt.isAfter(Instant.now()) ? "SCHEDULED" : "APPLYING";
        jdbc.update("INSERT INTO OPS_RUNTIME_CHANGE " +
                        "(change_id,operation_id,change_type,payload_schema_version,request_hash,payload_hash,payload_json,rollback_payload_json,target_snapshot_json,desired_version," +
                        "rollout_mode,wave_size,quorum_percent,change_state,scheduled_at,expires_at,reason,approval_id,break_glass_id,requested_by,created_by,updated_by) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                changeId, operationId, type, payloadSchemaVersion, requestHash, payloadHash, payloadJson, rollbackJson,
                targetSnapshotJson, version, rolloutMode, waveSize, quorumPercent, state, ts(scheduledAt), ts(expiresAt),
                reason, approvalId, breakGlassId, requestedBy, requestedBy, requestedBy);

        int sequence = 0;
        for (String instanceId : targets) {
            String deliveryId = UUID.randomUUID().toString();
            int inserted = jdbc.update("INSERT INTO OPS_RUNTIME_DELIVERY " +
                            "(delivery_id,change_id,instance_id,sequence_no,desired_version,delivery_state,attempt_no,next_attempt_at,created_by,updated_by) " +
                            "VALUES (?,?,?,?,?,'PENDING',0,CURRENT_TIMESTAMP,?,?)",
                    deliveryId, changeId, instanceId, ++sequence, version, requestedBy, requestedBy);
            if (inserted != 1) throw new IllegalStateException("Runtime delivery 생성 실패: " + instanceId);
            jdbc.update("UPDATE OPS_RUNTIME_INSTANCE_STATE SET desired_version=?, desired_hash=?, drift_state='PENDING', updated_at=CURRENT_TIMESTAMP, updated_by=? WHERE instance_id=?",
                    version, payloadHash, requestedBy, instanceId);
            int featureUpdated = jdbc.update("UPDATE OPS_RUNTIME_INSTANCE_FEATURE_STATE SET desired_version=?,desired_hash=?,drift_state='PENDING'," +
                            "updated_by=?,updated_at=CURRENT_TIMESTAMP WHERE instance_id=? AND change_type=?",
                    version, payloadHash, requestedBy, instanceId, baseChangeType(type));
            if (featureUpdated == 0) {
                try {
                    jdbc.update("INSERT INTO OPS_RUNTIME_INSTANCE_FEATURE_STATE " +
                                    "(instance_id,change_type,desired_version,actual_version,desired_hash,actual_hash,drift_state,created_by,updated_by) " +
                                    "VALUES (?,?,?,0,?,NULL,'PENDING',?,?)",
                            instanceId, baseChangeType(type), version, payloadHash, requestedBy, requestedBy);
                } catch (DuplicateKeyException duplicate) {
                    jdbc.update("UPDATE OPS_RUNTIME_INSTANCE_FEATURE_STATE SET desired_version=?,desired_hash=?,drift_state='PENDING'," +
                                    "updated_by=?,updated_at=CURRENT_TIMESTAMP WHERE instance_id=? AND change_type=?",
                            version, payloadHash, requestedBy, instanceId, baseChangeType(type));
                }
            }
        }
        appendAudit(changeId, "CHANGE_CREATED", requestedBy, reason, requestHash);
    }

    Optional<Map<String,Object>> findChange(String column, String value) {
        if (!"change_id".equals(column) && !"operation_id".equals(column)) throw new IllegalArgumentException("unsupported column");
        List<Map<String,Object>> rows=jdbc.queryForList("SELECT * FROM OPS_RUNTIME_CHANGE WHERE "+column+"=?", value);
        return rows.stream().findFirst();
    }

    public List<CpfRuntimeDelivery> claim(String instanceId, long fencingToken, int requestedLimit) {
        assertFence(instanceId, fencingToken);
        reconcileTemporalChanges();
        int limit = Math.max(1, Math.min(1, requestedLimit));
        List<Map<String,Object>> rows = jdbc.queryForList(
                "SELECT d.delivery_id,d.change_id,d.instance_id,d.sequence_no,d.desired_version,d.attempt_no," +
                        "c.change_type,c.payload_schema_version,c.request_hash,c.payload_hash,c.payload_json,c.expires_at," +
                        "c.rollout_mode,c.wave_size,c.quorum_percent " +
                        "FROM OPS_RUNTIME_DELIVERY d JOIN OPS_RUNTIME_CHANGE c ON c.change_id=d.change_id " +
                        "WHERE d.instance_id=? AND d.delivery_state IN ('PENDING','FAILED') AND d.next_attempt_at<=CURRENT_TIMESTAMP " +
                        "AND c.change_state IN ('APPLYING','PARTIAL') " +
                        "AND (c.scheduled_at IS NULL OR c.scheduled_at<=CURRENT_TIMESTAMP) " +
                        "AND (c.expires_at IS NULL OR c.expires_at>CURRENT_TIMESTAMP) " +
                        "AND NOT EXISTS (" +
                        "  SELECT 1 FROM OPS_RUNTIME_DELIVERY older JOIN OPS_RUNTIME_CHANGE older_change ON older_change.change_id=older.change_id " +
                        "  WHERE older.instance_id=d.instance_id AND older_change.change_type=c.change_type " +
                        "  AND older.desired_version<d.desired_version " +
                        "  AND older.delivery_state NOT IN ('ACKED','CANCELLED','SUPERSEDED')" +
                        ") ORDER BY d.desired_version,d.sequence_no,d.created_at", instanceId);
        ArrayList<CpfRuntimeDelivery> claimed = new ArrayList<>();
        for (Map<String,Object> row : rows) {
            if (claimed.size() >= limit) break;
            if (!isWaveOpen(row)) continue;
            String deliveryId = String.valueOf(row.get("delivery_id"));
            int updated = jdbc.update(
                    "UPDATE OPS_RUNTIME_DELIVERY SET delivery_state='CLAIMED',attempt_no=attempt_no+1," +
                            "fencing_token=?,claimed_at=CURRENT_TIMESTAMP,updated_at=CURRENT_TIMESTAMP " +
                            "WHERE delivery_id=? AND instance_id=? AND delivery_state IN ('PENDING','FAILED')",
                    fencingToken, deliveryId, instanceId);
            if (updated != 1) continue;
            CpfRuntimePayload payload = CpfRuntimePayload.parse(String.valueOf(row.get("payload_json")));
            String payloadHash = String.valueOf(row.get("payload_hash"));
            if (payloadHash == null || payloadHash.isBlank() || "null".equalsIgnoreCase(payloadHash)) {
                payloadHash = CpfRuntimeCanonicalHash.sha256(payload);
            }
            claimed.add(new CpfRuntimeDelivery(deliveryId, String.valueOf(row.get("change_id")),
                    String.valueOf(row.get("change_type")), instanceId,
                    ((Number) row.get("desired_version")).longValue(), fencingToken,
                    String.valueOf(row.get("request_hash")), payloadHash,
                    ((Number) row.getOrDefault("payload_schema_version", 1)).intValue(), payload,
                    ((Number) row.get("attempt_no")).intValue() + 1, toInstant(row.get("expires_at"))));
        }
        return List.copyOf(claimed);
    }

    private boolean isWaveOpen(Map<String,Object> candidate) {
        String rollout = blank(nullable(candidate.get("rollout_mode"))).trim().toUpperCase();
        if (rollout.isBlank() || "ALL_AT_ONCE".equals(rollout)) return true;
        String changeId = String.valueOf(candidate.get("change_id"));
        Integer blocked = jdbc.queryForObject(
                "SELECT COUNT(*) FROM OPS_RUNTIME_DELIVERY WHERE change_id=? " +
                        "AND delivery_state IN ('POISONED','UNKNOWN_RESULT','RESTART_REQUIRED')",
                Integer.class, changeId);
        if (blocked != null && blocked > 0) return false;

        int sequence = ((Number) candidate.get("sequence_no")).intValue();
        int waveSize = Math.max(1, ((Number) candidate.getOrDefault("wave_size", 1)).intValue());
        int quorum = Math.max(1, Math.min(100,
                ((Number) candidate.getOrDefault("quorum_percent", 100)).intValue()));
        int waveStart = ((sequence - 1) / waveSize) * waveSize + 1;
        if (waveStart <= 1) return true;

        List<Map<String,Object>> prior = jdbc.queryForList(
                "SELECT d.instance_id,d.delivery_state,c.change_type " +
                        "FROM OPS_RUNTIME_DELIVERY d JOIN OPS_RUNTIME_CHANGE c ON c.change_id=d.change_id " +
                        "WHERE d.change_id=? AND d.sequence_no<? ORDER BY d.sequence_no",
                changeId, waveStart);
        if (prior.isEmpty()) return true;
        int healthy = 0;
        for (Map<String,Object> row : prior) {
            if (!"ACKED".equals(String.valueOf(row.get("delivery_state")))) return false;
            List<Map<String,Object>> health = jdbc.queryForList(
                    "SELECT f.drift_state,s.lease_until FROM OPS_RUNTIME_INSTANCE_FEATURE_STATE f " +
                            "JOIN OPS_RUNTIME_INSTANCE_STATE s ON s.instance_id=f.instance_id " +
                            "WHERE f.instance_id=? AND f.change_type=?",
                    String.valueOf(row.get("instance_id")), String.valueOf(row.get("change_type")));
            if (!health.isEmpty()) {
                Instant until = toInstant(health.getFirst().get("lease_until"));
                if ("IN_SYNC".equals(String.valueOf(health.getFirst().get("drift_state")))
                        && until != null && until.isAfter(Instant.now())) healthy++;
            }
        }
        return healthy * 100 >= prior.size() * quorum;
    }

    private void reconcileTemporalChanges() {
        jdbc.update("UPDATE OPS_RUNTIME_CHANGE SET change_state='APPLYING',updated_at=CURRENT_TIMESTAMP " +
                "WHERE change_state='SCHEDULED' AND scheduled_at<=CURRENT_TIMESTAMP " +
                "AND (expires_at IS NULL OR expires_at>CURRENT_TIMESTAMP)");
        jdbc.update("UPDATE OPS_RUNTIME_CHANGE SET change_state='EXPIRED',updated_at=CURRENT_TIMESTAMP " +
                "WHERE change_state IN ('SCHEDULED','APPLYING','PARTIAL') AND expires_at<=CURRENT_TIMESTAMP");
        jdbc.update("UPDATE OPS_RUNTIME_DELIVERY SET delivery_state='EXPIRED',updated_at=CURRENT_TIMESTAMP " +
                "WHERE delivery_state IN ('PENDING','FAILED','CLAIMED','RESTART_REQUIRED') " +
                "AND change_id IN (SELECT change_id FROM OPS_RUNTIME_CHANGE WHERE change_state='EXPIRED')");
    }

    public void acknowledge(String deliveryId, String changeId, String instanceId, long fencingToken,
                            long appliedVersion, String actualHash, String state, String errorCode,
                            String message, Instant at) {
        assertFence(instanceId, fencingToken);
        String normalized = blank(state).trim().toUpperCase();
        if (!Set.of("SUCCESS", "ACKED", "FAILED", "UNKNOWN_RESULT", "RESTART_REQUIRED").contains(normalized)) {
            throw new IllegalArgumentException("지원하지 않는 Runtime ACK state: " + state);
        }

        List<Map<String,Object>> currentRows = jdbc.queryForList(
                "SELECT delivery_state,actual_hash,error_code FROM OPS_RUNTIME_DELIVERY " +
                        "WHERE delivery_id=? AND change_id=? AND instance_id=?", deliveryId, changeId, instanceId);
        if (currentRows.isEmpty()) throw new CpfRuntimeFenceException("Runtime delivery를 찾을 수 없습니다: " + deliveryId);
        Map<String,Object> current = currentRows.getFirst();
        String currentState = String.valueOf(current.get("delivery_state"));
        if (Set.of("ACKED", "POISONED", "UNKNOWN_RESULT", "RESTART_REQUIRED").contains(currentState)) {
            boolean sameSuccess = "ACKED".equals(currentState)
                    && ("SUCCESS".equals(normalized) || "ACKED".equals(normalized))
                    && java.util.Objects.equals(nullable(current.get("actual_hash")), actualHash);
            boolean sameTerminal = currentState.equals(normalized)
                    && java.util.Objects.equals(nullable(current.get("error_code")), errorCode);
            if (sameSuccess || sameTerminal) return;
            throw new CpfRuntimeFenceException("이미 terminal 처리된 delivery에 다른 ACK가 수신되었습니다: " + deliveryId);
        }

        boolean success = "SUCCESS".equals(normalized) || "ACKED".equals(normalized);
        String deliveryState;
        if (success) {
            if (actualHash == null || actualHash.isBlank()) {
                normalized = "UNKNOWN_RESULT";
                errorCode = "ACTUAL_HASH_MISSING";
                message = "성공 ACK에 actualHash가 없어 결과불명으로 전환되었습니다.";
                deliveryState = "UNKNOWN_RESULT";
            } else {
                deliveryState = "ACKED";
            }
        } else if ("UNKNOWN_RESULT".equals(normalized)) {
            deliveryState = "UNKNOWN_RESULT";
        } else if ("RESTART_REQUIRED".equals(normalized)) {
            deliveryState = "RESTART_REQUIRED";
        } else {
            Integer attemptValue = jdbc.queryForObject(
                    "SELECT attempt_no FROM OPS_RUNTIME_DELIVERY WHERE delivery_id=?", Integer.class, deliveryId);
            int attempt = attemptValue == null ? 1 : attemptValue;
            deliveryState = isPermanentFailure(errorCode) || attempt >= 8 ? "POISONED" : "FAILED";
        }

        int updated = jdbc.update(
                "UPDATE OPS_RUNTIME_DELIVERY SET delivery_state=?,actual_hash=?,error_code=?,error_message=?," +
                        "acknowledged_at=?,updated_at=CURRENT_TIMESTAMP " +
                        "WHERE delivery_id=? AND change_id=? AND instance_id=? AND fencing_token=? AND delivery_state='CLAIMED'",
                deliveryState, actualHash, errorCode, truncate(message, 900), ts(at),
                deliveryId, changeId, instanceId, fencingToken);
        if (updated != 1) {
            throw new CpfRuntimeFenceException("ACK가 오래되었거나 이미 처리된 delivery입니다. deliveryId=" + deliveryId);
        }

        if ("ACKED".equals(deliveryState)) {
            jdbc.update("UPDATE OPS_RUNTIME_INSTANCE_STATE SET actual_version=?,actual_hash=?," +
                            "drift_state=CASE WHEN desired_version=? AND desired_hash=? THEN 'IN_SYNC' ELSE 'DRIFT' END," +
                            "last_ack_change_id=?,last_ack_at=?,updated_at=CURRENT_TIMESTAMP " +
                            "WHERE instance_id=? AND fencing_token=?",
                    appliedVersion, actualHash, appliedVersion, actualHash, changeId, ts(at), instanceId, fencingToken);
            jdbc.update("UPDATE OPS_RUNTIME_INSTANCE_FEATURE_STATE SET actual_version=?,actual_hash=?," +
                            "drift_state=CASE WHEN desired_version=? AND desired_hash=? THEN 'IN_SYNC' ELSE 'DRIFT' END," +
                            "source_delivery_id=?,updated_at=CURRENT_TIMESTAMP WHERE instance_id=? " +
                            "AND change_type=(SELECT change_type FROM OPS_RUNTIME_CHANGE WHERE change_id=?)",
                    appliedVersion, actualHash, appliedVersion, actualHash, deliveryId, instanceId, changeId);
        } else if ("RESTART_REQUIRED".equals(deliveryState)) {
            jdbc.update("UPDATE OPS_RUNTIME_INSTANCE_STATE SET drift_state='PENDING_RESTART'," +
                    "last_ack_change_id=?,last_ack_at=?,updated_at=CURRENT_TIMESTAMP WHERE instance_id=? AND fencing_token=?",
                    changeId, ts(at), instanceId, fencingToken);
            jdbc.update("UPDATE OPS_RUNTIME_INSTANCE_FEATURE_STATE SET drift_state='PENDING_RESTART'," +
                            "source_delivery_id=?,updated_at=CURRENT_TIMESTAMP WHERE instance_id=? " +
                            "AND change_type=(SELECT change_type FROM OPS_RUNTIME_CHANGE WHERE change_id=?)",
                    deliveryId, instanceId, changeId);
            jdbc.update("UPDATE OPS_SERVICE_INSTANCE SET drain_yn='Y',instance_status='DRAINING'," +
                    "drain_deadline_at=?,updated_at=CURRENT_TIMESTAMP WHERE instance_id=?",
                    ts(Instant.now().plusSeconds(600)), instanceId);
        } else if ("UNKNOWN_RESULT".equals(deliveryState)) {
            jdbc.update("UPDATE OPS_RUNTIME_INSTANCE_STATE SET drift_state='UNKNOWN_RESULT'," +
                    "last_ack_change_id=?,last_ack_at=?,updated_at=CURRENT_TIMESTAMP WHERE instance_id=? AND fencing_token=?",
                    changeId, ts(at), instanceId, fencingToken);
            jdbc.update("UPDATE OPS_RUNTIME_INSTANCE_FEATURE_STATE SET drift_state='UNKNOWN_RESULT'," +
                            "source_delivery_id=?,updated_at=CURRENT_TIMESTAMP WHERE instance_id=? " +
                            "AND change_type=(SELECT change_type FROM OPS_RUNTIME_CHANGE WHERE change_id=?)",
                    deliveryId, instanceId, changeId);
        } else if ("FAILED".equals(deliveryState)) {
            Integer attemptValue = jdbc.queryForObject(
                    "SELECT attempt_no FROM OPS_RUNTIME_DELIVERY WHERE delivery_id=?", Integer.class, deliveryId);
            int attempt = attemptValue == null ? 1 : attemptValue;
            long base = Math.min(300L, Math.max(1L, 1L << Math.min(8, Math.max(0, attempt - 1))));
            long jitter = Math.floorMod(deliveryId.hashCode(), Math.max(1, (int) Math.min(30L, base)));
            jdbc.update("UPDATE OPS_RUNTIME_DELIVERY SET next_attempt_at=?,updated_at=CURRENT_TIMESTAMP WHERE delivery_id=?",
                    ts(Instant.now().plusSeconds(base + jitter)), deliveryId);
        }

        appendAudit(changeId, "ACKED".equals(deliveryState) ? "DELIVERY_ACK" : "DELIVERY_" + deliveryState,
                instanceId, truncate(message, 500), actualHash);
        reconcileChangeState(changeId);
    }

    private boolean isPermanentFailure(String errorCode) {
        String code = blank(errorCode).trim().toUpperCase();
        return Set.of("APPLIER_NOT_FOUND", "PAYLOAD_SCHEMA_UNSUPPORTED", "PAYLOAD_HASH_MISMATCH",
                "VERSION_GAP_REQUIRES_SNAPSHOT", "ACTUAL_HASH_MISSING").contains(code);
    }

    public void cancel(String changeId, String operatorId, String reason) {
        int updated=jdbc.update("UPDATE OPS_RUNTIME_CHANGE SET change_state='CANCELLED',updated_at=CURRENT_TIMESTAMP,updated_by=? " +
                "WHERE change_id=? AND change_state IN ('SCHEDULED','APPLYING','PARTIAL')",operatorId,changeId);
        if (updated!=1) throw new IllegalStateException("취소할 수 없는 Runtime Change입니다. changeId="+changeId);
        jdbc.update("UPDATE OPS_RUNTIME_DELIVERY SET delivery_state='CANCELLED',updated_at=CURRENT_TIMESTAMP WHERE change_id=? AND delivery_state IN ('PENDING','FAILED')",changeId);
        appendAudit(changeId,"CHANGE_CANCELLED",operatorId,reason,null);
    }

    public void markRollbackPending(String changeId, String operatorId, String reason) {
        int updated=jdbc.update("UPDATE OPS_RUNTIME_CHANGE SET change_state='ROLLBACK_PENDING',updated_at=CURRENT_TIMESTAMP,updated_by=? " +
                "WHERE change_id=? AND change_state IN ('SUCCESS','PARTIAL','FAILED','EXPIRED')",operatorId,changeId);
        if (updated!=1) throw new IllegalStateException("Rollback할 수 없는 Runtime Change입니다. changeId="+changeId);
        appendAudit(changeId,"ROLLBACK_REQUESTED",operatorId,reason,null);
    }

    public CpfRuntimeInstanceLease register(CpfRuntimeInstanceRegistration r) {
        requireText(r.instanceId(), "instanceId");
        requireText(r.serviceId(), "serviceId");
        requireText(r.endpointCode(), "endpointCode");
        requireText(r.registrationSource(), "registrationSource");
        long skewMs = Math.abs(java.time.Duration.between(r.agentTime(), Instant.now()).toMillis());
        if (skewMs > 300_000L) {
            throw new IllegalArgumentException("Runtime Agent clock skew가 허용 범위를 초과했습니다. skewMs=" + skewMs);
        }
        ensureServiceAndEndpoint(r);

        List<Map<String,Object>> existing = jdbc.queryForList(
                "SELECT fencing_token,lease_until,registration_source FROM OPS_RUNTIME_INSTANCE_STATE " +
                        "WHERE instance_id=? FOR UPDATE", r.instanceId());
        long fence = existing.isEmpty() ? 1L : ((Number) existing.getFirst().get("fencing_token")).longValue() + 1L;
        if (existing.isEmpty()) {
            // OPS_RUNTIME_INSTANCE_STATE가 OPS_SERVICE_INSTANCE를 FK로 참조하므로 최초 등록에는 identity row가 먼저 필요합니다.
            // 이 row는 active projection이 아니며 authoritative fencing claim이 성공한 뒤에만 UP/Y로 승격합니다.
            claimInactiveServiceInstanceIdentity(r);
        } else {
            requireServiceInstanceProjection(r.instanceId());
            Map<String,Object> current = existing.getFirst();
            Instant currentLease = toInstant(current.get("lease_until"));
            String source = nullable(current.get("registration_source"));
            if (currentLease != null && currentLease.isAfter(Instant.now())) {
                if (source != null && !source.isBlank() && !source.equals(r.registrationSource())) {
                    throw new CpfRuntimeFenceException(
                            "살아 있는 동일 instanceId에 다른 registrationSource가 등록될 수 없습니다: " + r.instanceId());
                }
                assertSameActiveProcess(r);
            }
        }

        Instant lease = Instant.now().plusSeconds(r.leaseSeconds());
        String capabilities = write(r.capabilities());
        String labels = write(r.labels());

        if (existing.isEmpty()) {
            try {
                jdbc.update("INSERT INTO OPS_RUNTIME_INSTANCE_STATE " +
                                "(instance_id,fencing_token,lease_until,desired_version,actual_version,drift_state," +
                                "capabilities_json,labels_json,artifact_version,artifact_commit,runtime_role,registration_source," +
                                "schema_version,config_hash,clock_skew_ms,heartbeat_at,created_by,updated_by) " +
                                "VALUES (?,?,?,0,0,'IN_SYNC',?,?,?,?,?,?,?,?,?,CURRENT_TIMESTAMP,'CPF','CPF')",
                        r.instanceId(), fence, ts(lease), capabilities, labels, blank(r.artifactVersion()),
                        blank(r.artifactCommit()), blank(r.runtimeRole()), blank(r.registrationSource()),
                        blank(r.schemaVersion()), blank(r.configHash()), skewMs);
            } catch (DuplicateKeyException duplicate) {
                // 최초 동시 claim은 한 Process만 성공해야 합니다. 재귀 retry는 두 Process가 모두 등록되는 False Green을 만듭니다.
                throw new CpfRuntimeFenceException("Runtime instance 최초 등록 fencing 충돌: " + r.instanceId());
            }
        } else {
            int updated = jdbc.update("UPDATE OPS_RUNTIME_INSTANCE_STATE SET fencing_token=?,lease_until=?," +
                            "capabilities_json=?,labels_json=?,artifact_version=?,artifact_commit=?,runtime_role=?," +
                            "registration_source=?,schema_version=?,config_hash=?,clock_skew_ms=?,heartbeat_at=CURRENT_TIMESTAMP," +
                            "updated_at=CURRENT_TIMESTAMP WHERE instance_id=? AND fencing_token=?",
                    fence, ts(lease), capabilities, labels, blank(r.artifactVersion()), blank(r.artifactCommit()),
                    blank(r.runtimeRole()), blank(r.registrationSource()), blank(r.schemaVersion()), blank(r.configHash()),
                    skewMs, r.instanceId(), fence - 1);
            if (updated != 1) throw new CpfRuntimeFenceException("Runtime instance 재등록 fencing 충돌: " + r.instanceId());
            jdbc.update("UPDATE OPS_RUNTIME_DELIVERY SET delivery_state='PENDING',fencing_token=NULL," +
                            "next_attempt_at=CURRENT_TIMESTAMP,updated_at=CURRENT_TIMESTAMP " +
                            "WHERE instance_id=? AND delivery_state='RESTART_REQUIRED'",
                    r.instanceId());
        }

        // authoritative fencing claim이 확정된 같은 transaction에서만 Service Registry active projection을 공개합니다.
        upsertServiceInstance(r);
        return lease(r.instanceId());
    }

    public CpfManagedRuntimeSnapshot managedRuntimeSnapshot(String instanceId) {
        requireText(instanceId, "instanceId");
        List<CpfManagedRuntimeSnapshot> rows = jdbc.query(
                "SELECT s.instance_id,i.service_id,s.runtime_role,s.desired_runtime_state,s.actual_runtime_state," +
                        "s.control_row_version,s.fencing_token,s.lease_until,s.heartbeat_at,i.environment_code," +
                        "i.zone_code,i.cell_code,s.artifact_version " +
                        "FROM OPS_RUNTIME_INSTANCE_STATE s JOIN OPS_SERVICE_INSTANCE i ON i.instance_id=s.instance_id " +
                        "WHERE s.instance_id=?",
                (rs,rowNum) -> new CpfManagedRuntimeSnapshot(
                        rs.getString("instance_id"), rs.getString("service_id"), rs.getString("runtime_role"),
                        rs.getString("desired_runtime_state"), rs.getString("actual_runtime_state"),
                        rs.getLong("control_row_version"), rs.getLong("fencing_token"),
                        toInstant(rs.getTimestamp("lease_until")), toInstant(rs.getTimestamp("heartbeat_at")),
                        rs.getString("environment_code"), rs.getString("zone_code"), rs.getString("cell_code"),
                        rs.getString("artifact_version")), instanceId);
        if (rows.size() != 1) {
            throw new IllegalArgumentException("중앙 Runtime Registry에 instance가 없습니다: " + instanceId);
        }
        return rows.getFirst();
    }

    public List<CpfManagedRuntimeSnapshot> managedRuntimeList(java.time.Duration staleAfter) {
        long seconds = Math.max(5L, Math.min(86_400L, staleAfter == null ? 30L : staleAfter.toSeconds()));
        Instant cutoff = Instant.now().minusSeconds(seconds);
        return jdbc.query(
                "SELECT s.instance_id,i.service_id,s.runtime_role,s.desired_runtime_state,s.actual_runtime_state," +
                        "s.control_row_version,s.fencing_token,s.lease_until,s.heartbeat_at,i.environment_code," +
                        "i.zone_code,i.cell_code,s.artifact_version " +
                        "FROM OPS_RUNTIME_INSTANCE_STATE s JOIN OPS_SERVICE_INSTANCE i ON i.instance_id=s.instance_id " +
                        "WHERE s.heartbeat_at IS NOT NULL AND s.heartbeat_at>=? ORDER BY s.instance_id",
                (rs,rowNum) -> new CpfManagedRuntimeSnapshot(
                        rs.getString("instance_id"), rs.getString("service_id"), rs.getString("runtime_role"),
                        rs.getString("desired_runtime_state"), rs.getString("actual_runtime_state"),
                        rs.getLong("control_row_version"), rs.getLong("fencing_token"),
                        toInstant(rs.getTimestamp("lease_until")), toInstant(rs.getTimestamp("heartbeat_at")),
                        rs.getString("environment_code"), rs.getString("zone_code"), rs.getString("cell_code"),
                        rs.getString("artifact_version")), ts(cutoff));
    }

    public long updateManagedDesiredState(String instanceId, String desiredState, long expectedVersion) {
        requireText(instanceId, "instanceId");
        String desired = normalizeManagedDesiredState(desiredState);
        if (expectedVersion < 0) throw new IllegalArgumentException("expectedVersion must be non-negative: " + expectedVersion);
        int updated = jdbc.update(
                "UPDATE OPS_RUNTIME_INSTANCE_STATE SET desired_runtime_state=?,control_row_version=control_row_version+1," +
                        "updated_at=CURRENT_TIMESTAMP WHERE instance_id=? AND control_row_version=?",
                desired, instanceId, expectedVersion);
        if (updated != 1) {
            CpfManagedRuntimeSnapshot current = managedRuntimeSnapshot(instanceId);
            throw new CpfRuntimeVersionConflictException(expectedVersion, current.controlVersion());
        }
        return expectedVersion + 1L;
    }

    public void reportManagedActualState(String instanceId, String actualState) {
        requireText(instanceId, "instanceId");
        String actual = normalizeManagedActualState(actualState);
        int updated = jdbc.update(
                "UPDATE OPS_RUNTIME_INSTANCE_STATE SET actual_runtime_state=?,updated_at=CURRENT_TIMESTAMP " +
                        "WHERE instance_id=? AND lease_until>=CURRENT_TIMESTAMP", actual, instanceId);
        if (updated != 1) {
            throw new CpfRuntimeFenceException("활성 중앙 Runtime lease가 없어 actual state를 보고할 수 없습니다: " + instanceId);
        }
    }

    private static String normalizeManagedDesiredState(String value) {
        String state = blank(value).trim().toUpperCase(java.util.Locale.ROOT);
        if (!Set.of("RUNNING","STOPPED","DRAINING","QUARANTINED","UPGRADING","ROLLING_BACK").contains(state)) {
            throw new IllegalArgumentException("지원하지 않는 Runtime desiredState: " + value);
        }
        return state;
    }

    private static String normalizeManagedActualState(String value) {
        String state = blank(value).trim().toUpperCase(java.util.Locale.ROOT);
        if (!Set.of("STARTING","READY","BUSY","DRAINING","STOPPED","DEGRADED","STALE","UNREACHABLE","FAILED","UNKNOWN").contains(state)) {
            throw new IllegalArgumentException("지원하지 않는 Runtime actualState: " + value);
        }
        return state;
    }

    public CpfRuntimeInstanceLease heartbeat(String instanceId, long fencingToken, String actualHash,
                                             long actualVersion, int leaseSeconds, Instant agentTime) {
        long skewMs = Math.abs(java.time.Duration.between(
                agentTime == null ? Instant.now() : agentTime, Instant.now()).toMillis());
        if (skewMs > 300_000L) {
            throw new CpfRuntimeFenceException("Runtime heartbeat clock skew 초과. instanceId=" + instanceId
                    + ", skewMs=" + skewMs);
        }
        Instant until = Instant.now().plusSeconds(Math.max(10, Math.min(3600, leaseSeconds)));
        int updated = jdbc.update("UPDATE OPS_RUNTIME_INSTANCE_STATE SET lease_until=?,heartbeat_at=CURRENT_TIMESTAMP," +
                        "actual_hash=?,actual_version=?,clock_skew_ms=?," +
                        "drift_state=CASE WHEN desired_version=? AND COALESCE(desired_hash,'')=COALESCE(?,'') " +
                        "THEN 'IN_SYNC' ELSE drift_state END,updated_at=CURRENT_TIMESTAMP " +
                        "WHERE instance_id=? AND fencing_token=?",
                ts(until), actualHash, actualVersion, skewMs, actualVersion, actualHash, instanceId, fencingToken);
        if (updated != 1) throw new CpfRuntimeFenceException("Runtime heartbeat fencing 충돌: " + instanceId);
        jdbc.update("UPDATE OPS_SERVICE_INSTANCE SET instance_status=CASE WHEN COALESCE(drain_yn,'N')='Y' " +
                        "THEN 'DRAINING' ELSE 'UP' END,last_heartbeat_at=CURRENT_TIMESTAMP,updated_at=CURRENT_TIMESTAMP " +
                        "WHERE instance_id=?", instanceId);
        return lease(instanceId);
    }

    public CpfRuntimeInstanceLease heartbeat(String instanceId, long fencingToken, String actualHash,
                                             long actualVersion, int leaseSeconds) {
        return heartbeat(instanceId, fencingToken, actualHash, actualVersion, leaseSeconds, Instant.now());
    }

    public void deregister(String instanceId, long fencingToken, String reason) {
        assertFence(instanceId, fencingToken);
        jdbc.update("UPDATE OPS_RUNTIME_INSTANCE_STATE SET lease_until=CURRENT_TIMESTAMP," +
                        "heartbeat_at=CURRENT_TIMESTAMP,updated_at=CURRENT_TIMESTAMP " +
                        "WHERE instance_id=? AND fencing_token=?", instanceId, fencingToken);
        jdbc.update("UPDATE OPS_SERVICE_INSTANCE SET instance_status='DOWN',active_yn='N'," +
                "updated_at=CURRENT_TIMESTAMP WHERE instance_id=?", instanceId);
    }

    public void reconcileActualState(String instanceId, long fencingToken, List<CpfRuntimeActualState> states) {
        assertFence(instanceId, fencingToken);
        long maxVersion = 0L;
        String maxHash = null;
        for (CpfRuntimeActualState state : states == null ? List.<CpfRuntimeActualState>of() : states) {
            if (state == null || state.changeType() == null || state.changeType().isBlank()
                    || state.actualHash() == null || state.actualHash().isBlank()) continue;
            int updated = jdbc.update("UPDATE OPS_RUNTIME_INSTANCE_FEATURE_STATE SET actual_version=?,actual_hash=?," +
                            "drift_state=CASE WHEN desired_version=? AND desired_hash=? THEN 'IN_SYNC' ELSE 'DRIFT' END," +
                            "source_delivery_id=?,updated_at=CURRENT_TIMESTAMP " +
                            "WHERE instance_id=? AND change_type=?",
                    state.actualVersion(), state.actualHash(), state.actualVersion(), state.actualHash(),
                    state.sourceDeliveryId(), instanceId, state.changeType().trim().toUpperCase());
            if (updated == 0) {
                try {
                    jdbc.update("INSERT INTO OPS_RUNTIME_INSTANCE_FEATURE_STATE " +
                                    "(instance_id,change_type,desired_version,actual_version,desired_hash,actual_hash," +
                                    "drift_state,source_delivery_id,created_by,updated_by) " +
                                    "VALUES (?,?,0,?,?,?,'DRIFT',?,'CPF','CPF')",
                            instanceId, state.changeType().trim().toUpperCase(), state.actualVersion(),
                            null, state.actualHash(), state.sourceDeliveryId());
                } catch (DuplicateKeyException duplicate) {
                    reconcileActualState(instanceId, fencingToken, List.of(state));
                }
            }
            if (state.actualVersion() >= maxVersion) {
                maxVersion = state.actualVersion();
                maxHash = state.actualHash();
            }
        }
        if (maxVersion > 0L) {
            jdbc.update("UPDATE OPS_RUNTIME_INSTANCE_STATE SET actual_version=?,actual_hash=?," +
                            "updated_at=CURRENT_TIMESTAMP WHERE instance_id=? AND fencing_token=?",
                    maxVersion, maxHash, instanceId, fencingToken);
        }
    }

    public long acquireControllerLease(String holderId, int leaseSeconds) {
        requireText(holderId, "holderId");
        int seconds = Math.max(10, Math.min(300, leaseSeconds));
        Instant now = Instant.now();
        Instant until = now.plusSeconds(seconds);
        List<Map<String,Object>> rows = jdbc.queryForList(
                "SELECT holder_id,fencing_token,lease_until FROM OPS_RUNTIME_CONTROLLER_LEASE " +
                        "WHERE lease_key='RUNTIME_CONTROL' FOR UPDATE");
        if (rows.isEmpty()) {
            try {
                jdbc.update("INSERT INTO OPS_RUNTIME_CONTROLLER_LEASE " +
                                "(lease_key,holder_id,fencing_token,lease_until,created_by,updated_by) " +
                                "VALUES ('RUNTIME_CONTROL',?,1,?,'CPF','CPF')",
                        holderId, ts(until));
                return 1L;
            } catch (DuplicateKeyException duplicate) {
                return acquireControllerLease(holderId, leaseSeconds);
            }
        }
        Map<String,Object> row = rows.getFirst();
        String currentHolder = String.valueOf(row.get("holder_id"));
        long currentFence = ((Number) row.get("fencing_token")).longValue();
        Instant currentUntil = toInstant(row.get("lease_until"));
        if (holderId.equals(currentHolder) && currentUntil != null && currentUntil.isAfter(now)) {
            int updated = jdbc.update("UPDATE OPS_RUNTIME_CONTROLLER_LEASE SET lease_until=?," +
                            "last_reconciled_at=CURRENT_TIMESTAMP,updated_at=CURRENT_TIMESTAMP " +
                            "WHERE lease_key='RUNTIME_CONTROL' AND holder_id=? AND fencing_token=?",
                    ts(until), holderId, currentFence);
            return updated == 1 ? currentFence : 0L;
        }
        if (currentUntil == null || !currentUntil.isAfter(now)) {
            long nextFence = currentFence + 1L;
            int updated = jdbc.update("UPDATE OPS_RUNTIME_CONTROLLER_LEASE SET holder_id=?,fencing_token=?," +
                            "lease_until=?,last_reconciled_at=CURRENT_TIMESTAMP,updated_at=CURRENT_TIMESTAMP " +
                            "WHERE lease_key='RUNTIME_CONTROL' AND fencing_token=?",
                    holderId, nextFence, ts(until), currentFence);
            return updated == 1 ? nextFence : 0L;
        }
        return 0L;
    }

    public void reconcileController(String holderId, long fencingToken, int ackTimeoutSeconds) {
        assertControllerFence(holderId, fencingToken);
        reconcileTemporalChanges();
        Instant timeout = Instant.now().minusSeconds(Math.max(10, ackTimeoutSeconds));
        List<Map<String,Object>> timedOut = jdbc.queryForList(
                "SELECT delivery_id,attempt_no FROM OPS_RUNTIME_DELIVERY " +
                        "WHERE delivery_state='CLAIMED' AND claimed_at<?", ts(timeout));
        for (Map<String,Object> row : timedOut) {
            String deliveryId = String.valueOf(row.get("delivery_id"));
            int attempt = ((Number) row.get("attempt_no")).intValue();
            String nextState = attempt >= 8 ? "POISONED" : "FAILED";
            jdbc.update("UPDATE OPS_RUNTIME_DELIVERY SET delivery_state=?,error_code='ACK_TIMEOUT'," +
                            "error_message='Runtime Agent ACK timeout',next_attempt_at=CURRENT_TIMESTAMP," +
                            "updated_at=CURRENT_TIMESTAMP WHERE delivery_id=? AND delivery_state='CLAIMED'",
                    nextState, deliveryId);
        }

        jdbc.update("UPDATE OPS_SERVICE_INSTANCE SET instance_status='DOWN',updated_at=CURRENT_TIMESTAMP " +
                "WHERE instance_id IN (SELECT instance_id FROM OPS_RUNTIME_INSTANCE_STATE " +
                "WHERE lease_until<CURRENT_TIMESTAMP)");

        List<Map<String,Object>> blocked = jdbc.queryForList(
                "SELECT DISTINCT c.change_id,c.rollout_mode,d.delivery_state " +
                        "FROM OPS_RUNTIME_CHANGE c JOIN OPS_RUNTIME_DELIVERY d ON d.change_id=c.change_id " +
                        "WHERE c.change_state IN ('APPLYING','PARTIAL') " +
                        "AND c.rollout_mode IN ('CANARY','WAVE') " +
                        "AND d.delivery_state IN ('POISONED','UNKNOWN_RESULT')");
        for (Map<String,Object> row : blocked) {
            String changeId = String.valueOf(row.get("change_id"));
            String deliveryState = String.valueOf(row.get("delivery_state"));
            String state = "UNKNOWN_RESULT".equals(deliveryState) ? "UNKNOWN_RESULT" : "FAILED";
            jdbc.update("UPDATE OPS_RUNTIME_CHANGE SET change_state=?,updated_at=CURRENT_TIMESTAMP " +
                    "WHERE change_id=? AND change_state IN ('APPLYING','PARTIAL')", state, changeId);
            jdbc.update("UPDATE OPS_RUNTIME_DELIVERY SET delivery_state='CANCELLED',updated_at=CURRENT_TIMESTAMP " +
                    "WHERE change_id=? AND delivery_state IN ('PENDING','FAILED')", changeId);
            appendAudit(changeId, "ROLLOUT_AUTO_STOP", holderId,
                    "Canary/Wave health gate stopped rollout: " + deliveryState, null);
        }
        jdbc.update("DELETE FROM OPS_RUNTIME_RATE_BUCKET WHERE window_start<?",
                ts(Instant.now().minusSeconds(172800)));
        jdbc.update("UPDATE OPS_RUNTIME_CONTROLLER_LEASE SET last_reconciled_at=CURRENT_TIMESTAMP," +
                        "updated_at=CURRENT_TIMESTAMP WHERE lease_key='RUNTIME_CONTROL' " +
                        "AND holder_id=? AND fencing_token=?",
                holderId, fencingToken);
    }

    public List<String> acknowledgedTargets(String changeId) {
        return jdbc.queryForList(
                "SELECT instance_id FROM OPS_RUNTIME_DELIVERY WHERE change_id=? AND delivery_state='ACKED' ORDER BY sequence_no",
                String.class, changeId);
    }

    List<Map<String,Object>> autoRollbackCandidates() {
        return jdbc.queryForList(
                "SELECT change_id,change_type,change_state,approval_id,break_glass_id FROM OPS_RUNTIME_CHANGE " +
                        "WHERE change_state IN ('FAILED','EXPIRED') " +
                        "AND rollback_payload_json IS NOT NULL " +
                        "AND (approval_id IS NOT NULL OR break_glass_id IS NOT NULL) " +
                        "ORDER BY updated_at,change_id");
    }

    boolean selfHealingCircuitOpen(int failureThreshold, Instant since) {
        int threshold = Math.max(1, failureThreshold);
        Integer failures = jdbc.queryForObject(
                "SELECT COUNT(*) FROM OPS_RUNTIME_CHANGE WHERE requested_by='CPF_CONTROLLER' " +
                        "AND change_type LIKE 'ROLLBACK:%' AND change_state IN ('FAILED','UNKNOWN_RESULT') " +
                        "AND created_at>=?",
                Integer.class, ts(since));
        return failures != null && failures >= threshold;
    }

    private void assertControllerFence(String holderId, long fencingToken) {
        List<Map<String,Object>> rows = jdbc.queryForList(
                "SELECT holder_id,fencing_token,lease_until FROM OPS_RUNTIME_CONTROLLER_LEASE " +
                        "WHERE lease_key='RUNTIME_CONTROL'");
        if (rows.isEmpty()) throw new CpfRuntimeFenceException("Runtime Controller lease가 없습니다.");
        Map<String,Object> row = rows.getFirst();
        Instant until = toInstant(row.get("lease_until"));
        if (!holderId.equals(String.valueOf(row.get("holder_id")))
                || ((Number) row.get("fencing_token")).longValue() != fencingToken
                || until == null || until.isBefore(Instant.now())) {
            throw new CpfRuntimeFenceException("Runtime Controller fencing token 또는 lease가 유효하지 않습니다.");
        }
    }

    public CpfRuntimeInstanceLease lease(String instanceId) {
        Map<String,Object> row=jdbc.queryForMap("SELECT instance_id,fencing_token,desired_version,actual_version,desired_hash,actual_hash,drift_state,lease_until FROM OPS_RUNTIME_INSTANCE_STATE WHERE instance_id=?",
                instanceId);
        return new CpfRuntimeInstanceLease(instanceId,number(row.get("fencing_token")),number(row.get("desired_version")),number(row.get("actual_version")),
                nullable(row.get("desired_hash")),nullable(row.get("actual_hash")),String.valueOf(row.get("drift_state")),toInstant(row.get("lease_until")));
    }

    public CpfRuntimeStatus status(String environment,String serviceId) {
        StringBuilder sql=new StringBuilder("SELECT s.instance_id,i.service_id,i.environment_code,i.zone_code,i.cell_code," +
                "s.fencing_token,s.lease_until,s.desired_version,s.actual_version,s.desired_hash,s.actual_hash,s.drift_state," +
                "i.maintenance_yn,i.drain_yn,i.drain_deadline_at,s.heartbeat_at,s.artifact_version,s.artifact_commit," +
                "s.runtime_role,s.registration_source,s.clock_skew_ms " +
                "FROM OPS_RUNTIME_INSTANCE_STATE s JOIN OPS_SERVICE_INSTANCE i ON i.instance_id=s.instance_id WHERE 1=1");
        ArrayList<Object> args=new ArrayList<>();
        if(environment!=null&&!environment.isBlank()){sql.append(" AND i.environment_code=?");args.add(environment);}
        if(serviceId!=null&&!serviceId.isBlank()){sql.append(" AND i.service_id=?");args.add(serviceId);}
        sql.append(" ORDER BY i.service_id,s.instance_id");
        List<CpfRuntimeInstanceStatus> instances=jdbc.query(sql.toString(), (rs,rowNum)->new CpfRuntimeInstanceStatus(
                rs.getString("instance_id"),rs.getString("service_id"),rs.getString("environment_code"),
                rs.getString("zone_code"),rs.getString("cell_code"),rs.getLong("fencing_token"),
                toInstant(rs.getTimestamp("lease_until")),rs.getLong("desired_version"),rs.getLong("actual_version"),
                rs.getString("desired_hash"),rs.getString("actual_hash"),rs.getString("drift_state"),
                "Y".equalsIgnoreCase(rs.getString("maintenance_yn")),"Y".equalsIgnoreCase(rs.getString("drain_yn")),
                toInstant(rs.getTimestamp("drain_deadline_at")),toInstant(rs.getTimestamp("heartbeat_at")),
                rs.getString("artifact_version"),rs.getString("artifact_commit"),rs.getString("runtime_role"),
                rs.getString("registration_source"),rs.getLong("clock_skew_ms")),args.toArray());

        StringBuilder featureSql=new StringBuilder("SELECT f.instance_id,i.service_id,f.change_type,f.desired_version," +
                "f.actual_version,f.desired_hash,f.actual_hash,f.drift_state,f.source_delivery_id,f.updated_at " +
                "FROM OPS_RUNTIME_INSTANCE_FEATURE_STATE f JOIN OPS_SERVICE_INSTANCE i ON i.instance_id=f.instance_id WHERE 1=1");
        ArrayList<Object> featureArgs=new ArrayList<>();
        if(environment!=null&&!environment.isBlank()){featureSql.append(" AND i.environment_code=?");featureArgs.add(environment);}
        if(serviceId!=null&&!serviceId.isBlank()){featureSql.append(" AND i.service_id=?");featureArgs.add(serviceId);}
        featureSql.append(" ORDER BY i.service_id,f.instance_id,f.change_type");
        List<CpfRuntimeFeatureStatus> featureStates=jdbc.query(featureSql.toString(),(rs,rowNum)->new CpfRuntimeFeatureStatus(
                rs.getString("instance_id"),rs.getString("service_id"),rs.getString("change_type"),
                rs.getLong("desired_version"),rs.getLong("actual_version"),rs.getString("desired_hash"),
                rs.getString("actual_hash"),rs.getString("drift_state"),rs.getString("source_delivery_id"),
                toInstant(rs.getTimestamp("updated_at"))),featureArgs.toArray());

        long drift=featureStates.stream().filter(r->Set.of("DRIFT","UNKNOWN_RESULT","PENDING_RESTART").contains(r.driftState())).count();
        long expired=instances.stream().filter(r->r.leaseUntil()!=null&&r.leaseUntil().isBefore(Instant.now())).count();
        List<CpfRuntimeControllerStatus> controllerRows=jdbc.query(
                "SELECT holder_id,fencing_token,lease_until,last_reconciled_at FROM OPS_RUNTIME_CONTROLLER_LEASE WHERE lease_key='RUNTIME_CONTROL'",
                (rs,rowNum)->new CpfRuntimeControllerStatus(rs.getString("holder_id"),rs.getLong("fencing_token"),
                        toInstant(rs.getTimestamp("lease_until")),toInstant(rs.getTimestamp("last_reconciled_at"))));
        List<CpfRuntimeDeliveryCount> deliveries=jdbc.query(
                "SELECT delivery_state,COUNT(*) count_value FROM OPS_RUNTIME_DELIVERY GROUP BY delivery_state",
                (rs,rowNum)->new CpfRuntimeDeliveryCount(rs.getString("delivery_state"),rs.getLong("count_value")));
        return new CpfRuntimeStatus(instances,featureStates,controllerRows.isEmpty()?null:controllerRows.getFirst(),
                deliveries,instances.size(),drift,expired);
    }

    public com.cpf.platform.operations.runtimecontrol.CpfRuntimeControlHealth health(long lagSloSeconds) {
        Instant now=Instant.now();
        int instanceCount=(int)number(jdbc.queryForObject("SELECT COUNT(*) FROM OPS_RUNTIME_INSTANCE_STATE",Long.class));
        int backlog=(int)number(jdbc.queryForObject("SELECT COUNT(*) FROM OPS_RUNTIME_DELIVERY " +
                "WHERE delivery_state IN ('PENDING','FAILED','CLAIMED','RESTART_REQUIRED')",Long.class));
        int poison=(int)number(jdbc.queryForObject("SELECT COUNT(*) FROM OPS_RUNTIME_DELIVERY WHERE delivery_state='POISONED'",Long.class));
        int unknown=(int)number(jdbc.queryForObject("SELECT COUNT(*) FROM OPS_RUNTIME_DELIVERY WHERE delivery_state='UNKNOWN_RESULT'",Long.class));
        int drift=(int)number(jdbc.queryForObject("SELECT COUNT(*) FROM OPS_RUNTIME_INSTANCE_FEATURE_STATE " +
                "WHERE drift_state IN ('DRIFT','UNKNOWN_RESULT','PENDING_RESTART')",Long.class));
        int expired=(int)number(jdbc.queryForObject("SELECT COUNT(*) FROM OPS_RUNTIME_INSTANCE_STATE WHERE lease_until<CURRENT_TIMESTAMP",Long.class));
        List<Map<String,Object>> oldestRows=jdbc.queryForList(
                "SELECT created_at FROM OPS_RUNTIME_DELIVERY WHERE delivery_state IN ('PENDING','FAILED','CLAIMED','RESTART_REQUIRED') ORDER BY created_at");
        long lag=0L;
        if(!oldestRows.isEmpty()){
            Instant oldest=toInstant(oldestRows.getFirst().get("created_at"));
            if(oldest!=null)lag=Math.max(0L,java.time.Duration.between(oldest,now).getSeconds());
        }
        List<Map<String,Object>> controllerRows=jdbc.queryForList(
                "SELECT holder_id,fencing_token,lease_until FROM OPS_RUNTIME_CONTROLLER_LEASE WHERE lease_key='RUNTIME_CONTROL'");
        String controllerId=null;long controllerFence=0L;boolean leaderHealthy=false;
        if(!controllerRows.isEmpty()){
            Map<String,Object> c=controllerRows.getFirst();controllerId=nullable(c.get("holder_id"));
            controllerFence=number(c.get("fencing_token"));
            Instant until=toInstant(c.get("lease_until"));leaderHealthy=until!=null&&until.isAfter(now);
        }
        ArrayList<String> reasons=new ArrayList<>();
        if(!leaderHealthy)reasons.add("CONTROLLER_LEADER_UNAVAILABLE");
        if(poison>0)reasons.add("POISONED_DELIVERY");
        if(unknown>0)reasons.add("UNKNOWN_RESULT");
        if(expired>0)reasons.add("EXPIRED_INSTANCE_LEASE");
        if(lag>lagSloSeconds)reasons.add("DELIVERY_LAG_SLO_EXCEEDED");
        boolean ready=leaderHealthy&&unknown==0;
        String healthStatus=ready?(reasons.isEmpty()?"UP":"DEGRADED"):"DOWN";
        return new com.cpf.platform.operations.runtimecontrol.CpfRuntimeControlHealth(
                ready,healthStatus,controllerId,controllerFence,instanceCount,backlog,poison,unknown,drift,expired,
                lag,lag>lagSloSeconds,reasons,now);
    }

    public CpfRuntimeTargetPreview previewTargets(String changeType,int payloadSchemaVersion,CpfRuntimeTargetSelector selector){
        List<String> base=resolveTargets(selector);
        LinkedHashSet<String> excluded=new LinkedHashSet<>(selector.excludeInstanceIds());
        ArrayList<CpfRuntimeTargetPreviewItem> rows=new ArrayList<>();
        int eligible=0;
        for(String instanceId:base){
            List<Map<String,Object>> meta=jdbc.queryForList(
                    "SELECT i.service_id,i.environment_code,i.zone_code,i.cell_code,i.maintenance_yn,i.drain_yn," +
                            "s.capabilities_json,s.artifact_version,s.artifact_commit,s.runtime_role,s.lease_until " +
                            "FROM OPS_SERVICE_INSTANCE i JOIN OPS_RUNTIME_INSTANCE_STATE s ON s.instance_id=i.instance_id " +
                            "WHERE i.instance_id=?",instanceId);
            if(meta.isEmpty())continue;
            Map<String,Object> row=meta.getFirst();
            Map<String,Object> caps=readMapOrEmpty(nullable(row.get("capabilities_json")));
            Object encoded=caps.get(baseChangeType(changeType));
            boolean schemaSupported=supportsCapability(instanceId,changeType,payloadSchemaVersion);
            boolean manuallyExcluded=excluded.contains(instanceId);
            boolean isEligible=schemaSupported&&!manuallyExcluded;
            if(isEligible)eligible++;
            rows.add(new CpfRuntimeTargetPreviewItem(instanceId,nullable(row.get("service_id")),
                    nullable(row.get("environment_code")),nullable(row.get("zone_code")),nullable(row.get("cell_code")),
                    "Y".equalsIgnoreCase(nullable(row.get("maintenance_yn"))),
                    "Y".equalsIgnoreCase(nullable(row.get("drain_yn"))),nullable(row.get("artifact_version")),
                    nullable(row.get("artifact_commit")),nullable(row.get("runtime_role")),toInstant(row.get("lease_until")),
                    encoded==null?null:String.valueOf(encoded),schemaSupported,manuallyExcluded,isEligible));
        }
        boolean broad=blank(selector.environment()).isBlank()&&blank(selector.serviceId()).isBlank()
                &&blank(selector.groupId()).isBlank()&&selector.instanceIds().isEmpty()
                &&selector.labels().isEmpty()&&blank(selector.zone()).isBlank()&&blank(selector.cell()).isBlank();
        return new CpfRuntimeTargetPreview(baseChangeType(changeType),Math.max(1,payloadSchemaVersion),
                broad&&!selector.allowAll(),base.size(),eligible,rows);
    }

    public List<CpfRuntimeFeatureStatus> featureStates(List<String> instanceIds,String changeType){
        ArrayList<CpfRuntimeFeatureStatus> result=new ArrayList<>();
        for(String instanceId:instanceIds){
            List<CpfRuntimeFeatureStatus> rows=jdbc.query(
                    "SELECT instance_id,change_type,desired_version,actual_version,desired_hash,actual_hash," +
                            "drift_state,source_delivery_id,updated_at FROM OPS_RUNTIME_INSTANCE_FEATURE_STATE " +
                            "WHERE instance_id=? AND change_type=?",
                    (rs,rowNum)->new CpfRuntimeFeatureStatus(rs.getString("instance_id"),null,rs.getString("change_type"),
                            rs.getLong("desired_version"),rs.getLong("actual_version"),rs.getString("desired_hash"),
                            rs.getString("actual_hash"),rs.getString("drift_state"),rs.getString("source_delivery_id"),
                            toInstant(rs.getTimestamp("updated_at"))),instanceId,baseChangeType(changeType));
            result.add(rows.isEmpty()?new CpfRuntimeFeatureStatus(instanceId,null,baseChangeType(changeType),0L,0L,
                    null,null,"UNKNOWN",null,null):rows.getFirst());
        }
        return List.copyOf(result);
    }

    Map<String,Number> deliveryCounts(String changeId) {
        List<Map<String,Object>> rows=jdbc.queryForList("SELECT delivery_state,COUNT(*) cnt FROM OPS_RUNTIME_DELIVERY WHERE change_id=? GROUP BY delivery_state",changeId);
        LinkedHashMap<String,Number> result=new LinkedHashMap<>(); rows.forEach(r->result.put(String.valueOf(r.get("delivery_state")),(Number)r.get("cnt"))); return result;
    }

    /** 해당 변경에서 실제 desired/actual 불일치가 남은 instance 수를 계산합니다. */
    public int driftCount(String changeId) {
        Integer value = jdbc.queryForObject(
                "SELECT COUNT(*) FROM OPS_RUNTIME_INSTANCE_FEATURE_STATE f " +
                        "JOIN OPS_RUNTIME_DELIVERY d ON d.delivery_id=f.source_delivery_id " +
                        "WHERE d.change_id=? AND f.drift_state IN ('DRIFT','UNKNOWN_RESULT','PENDING_RESTART')",
                Integer.class,
                changeId);
        return value == null ? 0 : value;
    }

    private void reconcileChangeState(String changeId) {
        Map<String,Number> counts = deliveryCounts(changeId);
        int total = counts.values().stream().mapToInt(Number::intValue).sum();
        int ack = counts.getOrDefault("ACKED", 0).intValue();
        int failed = counts.getOrDefault("FAILED", 0).intValue();
        int poison = counts.getOrDefault("POISONED", 0).intValue();
        int unknown = counts.getOrDefault("UNKNOWN_RESULT", 0).intValue();
        int restart = counts.getOrDefault("RESTART_REQUIRED", 0).intValue();
        String state;
        if (unknown > 0) state = "UNKNOWN_RESULT";
        else if (ack == total && total > 0) state = "SUCCESS";
        else if (poison > 0 || failed > 0 || restart > 0) state = "PARTIAL";
        else state = "APPLYING";
        jdbc.update("UPDATE OPS_RUNTIME_CHANGE SET change_state=?,updated_at=CURRENT_TIMESTAMP " +
                        "WHERE change_id=? AND change_state NOT IN ('CANCELLED','ROLLBACK_PENDING','ROLLED_BACK','EXPIRED','SUPERSEDED')",
                state, changeId);
    }

    private void assertFence(String instanceId,long fencingToken) {
        List<Map<String,Object>> rows=jdbc.queryForList("SELECT fencing_token,lease_until FROM OPS_RUNTIME_INSTANCE_STATE WHERE instance_id=?",instanceId);
        if(rows.isEmpty()) throw new CpfRuntimeFenceException("등록되지 않은 Runtime instance입니다: "+instanceId);
        Map<String,Object> row=rows.getFirst(); Instant until=toInstant(row.get("lease_until"));
        if(((Number)row.get("fencing_token")).longValue()!=fencingToken || until==null || until.isBefore(Instant.now()))
            throw new CpfRuntimeFenceException("Runtime fencing token 또는 lease가 유효하지 않습니다: "+instanceId);
    }


    Map<String,Object> saveGroup(String groupId,String groupName,String parentGroupId,String environment,String description,
                                        Long expectedVersion,boolean active,String operatorId) {
        requireText(groupId,"groupId"); requireText(groupName,"groupName");
        if (groupId.equals(parentGroupId)) throw new IllegalArgumentException("Runtime Group은 자기 자신을 parent로 지정할 수 없습니다.");
        if (parentGroupId!=null && !parentGroupId.isBlank()) assertNoGroupCycle(groupId,parentGroupId);
        List<Map<String,Object>> rows=jdbc.queryForList("SELECT row_version FROM OPS_RUNTIME_INSTANCE_GROUP WHERE group_id=? FOR UPDATE",groupId);
        if(rows.isEmpty()) {
            if(expectedVersion!=null && expectedVersion!=0L) throw new CpfRuntimeVersionConflictException(expectedVersion,0L);
            jdbc.update("INSERT INTO OPS_RUNTIME_INSTANCE_GROUP(group_id,group_name,parent_group_id,environment_code,description,active_yn,row_version,created_by,updated_by) VALUES (?,?,?,?,?,?,0,?,?)",
                    groupId,groupName,emptyToNull(parentGroupId),emptyToNull(environment),emptyToNull(description),active?"Y":"N",operatorId,operatorId);
        } else {
            long current=((Number)rows.getFirst().get("row_version")).longValue();
            if(expectedVersion==null || expectedVersion.longValue()!=current) throw new CpfRuntimeVersionConflictException(expectedVersion==null?-1L:expectedVersion,current);
            int updated = jdbc.update(
                    "UPDATE OPS_RUNTIME_INSTANCE_GROUP "
                            + "SET group_name=?, parent_group_id=?, environment_code=?, description=?, "
                            + "active_yn=?, row_version=row_version+1, updated_by=?, "
                            + "updated_at=CURRENT_TIMESTAMP WHERE group_id=? AND row_version=?",
                    groupName,
                    emptyToNull(parentGroupId),
                    emptyToNull(environment),
                    emptyToNull(description),
                    active ? "Y" : "N",
                    operatorId,
                    groupId,
                    current);
            if(updated!=1) throw new CpfRuntimeVersionConflictException(current,current);
        }
        return findGroup(groupId).orElseThrow();
    }

    Optional<Map<String,Object>> findGroup(String groupId) {
        List<Map<String,Object>> rows=jdbc
                .queryForList("SELECT group_id,group_name,parent_group_id,environment_code,description,active_yn,row_version,created_at,updated_at FROM OPS_RUNTIME_INSTANCE_GROUP WHERE group_id=?",groupId);
        if(rows.isEmpty()) return Optional.empty();
        Map<String,Object> result=new LinkedHashMap<>(rows.getFirst());
        List<String> members=jdbc.queryForList("SELECT instance_id FROM OPS_RUNTIME_GROUP_MEMBER WHERE group_id=? AND active_yn='Y' ORDER BY instance_id",String.class,groupId);
        result.put("instance_ids",members);
        return Optional.of(result);
    }

    Map<String,Object> changeGroupMember(String groupId,String instanceId,boolean active,String operatorId) {
        requireText(groupId,"groupId"); requireText(instanceId,"instanceId");
        if(findGroup(groupId).isEmpty()) throw new IllegalArgumentException("Runtime Group을 찾을 수 없습니다: "+groupId);
        Integer instanceCount=jdbc.queryForObject("SELECT COUNT(*) FROM OPS_SERVICE_INSTANCE WHERE instance_id=?",Integer.class,instanceId);
        if(instanceCount==null || instanceCount==0) throw new IllegalArgumentException("Runtime Instance를 찾을 수 없습니다: "+instanceId);
        int updated=jdbc.update("UPDATE OPS_RUNTIME_GROUP_MEMBER SET active_yn=?,updated_by=?,updated_at=CURRENT_TIMESTAMP WHERE group_id=? AND instance_id=?",active?"Y":"N",operatorId,groupId,instanceId);
        if(updated==0) {
            try { jdbc.update("INSERT INTO OPS_RUNTIME_GROUP_MEMBER(group_id,instance_id,active_yn,created_by,updated_by) VALUES (?,?,?,?,?)",groupId,instanceId,active?"Y":"N",operatorId,operatorId); }
            catch(DuplicateKeyException duplicate){ return changeGroupMember(groupId,instanceId,active,operatorId); }
        }
        return findGroup(groupId).orElseThrow();
    }

    public void deleteGroup(String groupId,Long expectedVersion,String operatorId) {
        List<Map<String,Object>> rows=jdbc.queryForList("SELECT row_version FROM OPS_RUNTIME_INSTANCE_GROUP WHERE group_id=? FOR UPDATE",groupId);
        if(rows.isEmpty()) return;
        long current=((Number)rows.getFirst().get("row_version")).longValue();
        if(expectedVersion==null || expectedVersion.longValue()!=current) throw new CpfRuntimeVersionConflictException(expectedVersion==null?-1L:expectedVersion,current);
        Integer children=jdbc.queryForObject("SELECT COUNT(*) FROM OPS_RUNTIME_INSTANCE_GROUP WHERE parent_group_id=? AND active_yn='Y'",Integer.class,groupId);
        if(children!=null && children>0) throw new IllegalStateException("활성 child Runtime Group이 있어 삭제할 수 없습니다: "+groupId);
        jdbc.update("DELETE FROM OPS_RUNTIME_GROUP_MEMBER WHERE group_id=?",groupId);
        if(jdbc.update("DELETE FROM OPS_RUNTIME_INSTANCE_GROUP WHERE group_id=? AND row_version=?",groupId,current)!=1) throw new CpfRuntimeVersionConflictException(current,current);
    }

    private void assertNoGroupCycle(String groupId,String parentGroupId) {
        LinkedHashSet<String> seen=new LinkedHashSet<>(); String current=parentGroupId;
        while(current!=null && !current.isBlank()) {
            if(groupId.equals(current)) throw new IllegalArgumentException("Runtime Group parent cycle이 탐지되었습니다: "+groupId);
            if(!seen.add(current) || seen.size()>1000) throw new IllegalArgumentException("Runtime Group parent cycle/깊이 오류가 탐지되었습니다.");
            List<Map<String,Object>> rows=jdbc.queryForList("SELECT parent_group_id FROM OPS_RUNTIME_INSTANCE_GROUP WHERE group_id=?",current);
            current=rows.isEmpty()?null:nullable(rows.getFirst().get("parent_group_id"));
        }
    }

    private void ensureServiceAndEndpoint(CpfRuntimeInstanceRegistration r) {
        Integer serviceCount=jdbc.queryForObject("SELECT COUNT(*) FROM OPS_SERVICE WHERE service_id=? AND use_yn='Y'",Integer.class,r.serviceId());
        if(serviceCount==null || serviceCount!=1) throw new IllegalStateException("Runtime Agent service가 중앙 Registry에 등록되어 있지 않습니다: "+r.serviceId());
        Integer endpointCount=jdbc.queryForObject("SELECT COUNT(*) FROM OPS_SERVICE_ENDPOINT WHERE endpoint_code=? AND service_id=? AND use_yn='Y'",Integer.class,r.endpointCode(),r.serviceId());
        if(endpointCount==null || endpointCount!=1) throw new IllegalStateException("Runtime Agent endpoint가 중앙 Registry에 등록되어 있지 않습니다: "+r.serviceId()+"/"+r.endpointCode());
    }

    /**
     * 활성 lease의 instanceId를 다른 OS process가 재사용하지 못하게 합니다.
     *
     * <p>hostname fallback instanceId는 단일 host/단일 process에서는 편리하지만, 같은 host에서 동일
     * systemCode를 여러 process로 띄우면 충돌합니다. 이 경우 운영자는 MBR01/MBR02처럼 명시 instanceId를
     * 부여해야 합니다. 단순 registrationSource 비교만으로는 AUTO_CONFIGURATION끼리의 충돌을 구분할 수
     * 없으므로 processId와 process 시작시각을 함께 fencing identity로 사용합니다.</p>
     */
    private void requireServiceInstanceProjection(String instanceId) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM OPS_SERVICE_INSTANCE WHERE instance_id=?", Integer.class, instanceId);
        if (count == null || count != 1) {
            throw new CpfRuntimeFenceException(
                    "Runtime authoritative state와 Service Registry projection이 불일치합니다. reconcile 후 재등록해야 합니다: "
                            + instanceId);
        }
    }

    private void claimInactiveServiceInstanceIdentity(CpfRuntimeInstanceRegistration r) {
        String managedServerId = resolveManagedServer(r);
        try {
            jdbc.update(
                    "INSERT INTO OPS_SERVICE_INSTANCE(instance_id,managed_server_id,service_id,endpoint_code,instance_name,base_url,host_name," +
                            "environment_code,zone_code,cell_code,instance_status,weight,priority_no,active_yn,maintenance_yn,drain_yn," +
                            "last_heartbeat_at,system_code,application_name,application_role,runtime_hostname,process_id,java_version,cpf_version," +
                            "application_version,started_at,created_by,updated_by) " +
                            "VALUES (?,?,?,?,?,?,?,?,?,?,'REGISTERING',100,100,'N','N','N',NULL,?,?,?,?,?,?,?,?,?,'CPF','CPF')",
                    r.instanceId(), managedServerId, r.serviceId(), r.endpointCode(), r.instanceId(), r.baseUrl(),
                    emptyToNull(r.runtimeHostname()), blank(r.environment()), blank(r.zone()), blank(r.cell()),
                    emptyToNull(r.systemCode()), emptyToNull(r.applicationName()), emptyToNull(r.applicationRole()),
                    emptyToNull(r.runtimeHostname()), r.processId()==null?null:String.valueOf(r.processId()),
                    emptyToNull(r.javaVersion()), emptyToNull(r.cpfVersion()), emptyToNull(r.applicationVersion()), ts(r.startedAt()));
        } catch (DuplicateKeyException duplicate) {
            throw new CpfRuntimeFenceException(
                    "Runtime instance 최초 identity claim 충돌 또는 partial state가 존재합니다: " + r.instanceId());
        }
    }

    private void assertSameActiveProcess(CpfRuntimeInstanceRegistration r) {
        List<Map<String,Object>> rows = jdbc.queryForList(
                "SELECT system_code,runtime_hostname,application_name,process_id,started_at " +
                        "FROM OPS_SERVICE_INSTANCE WHERE instance_id=?", r.instanceId());
        if (rows.isEmpty()) return;
        Map<String,Object> current = rows.getFirst();
        if (sameProcessIdentity(current, r)) return;
        throw new CpfRuntimeFenceException(
                "살아 있는 동일 instanceId가 다른 Runtime process에서 이미 사용 중입니다. " +
                        "같은 Host의 다중 Process는 cpf.runtime.instance-id/CPF_RUNTIME_INSTANCE_ID를 " +
                        "각 Process에 고유하게 지정해야 합니다: " + r.instanceId());
    }

    static boolean sameProcessIdentity(Map<String,Object> current, CpfRuntimeInstanceRegistration incoming) {
        String currentPid = nullable(current.get("process_id"));
        String incomingPid = incoming.processId() == null ? null : String.valueOf(incoming.processId());
        Instant currentStarted = toInstant(current.get("started_at"));
        Instant incomingStarted = incoming.startedAt();

        boolean pidMatches = currentPid != null && incomingPid != null
                ? currentPid.equals(incomingPid)
                : currentPid == null && incomingPid == null;
        boolean startedMatches = sameProcessStart(currentStarted, incomingStarted);
        return pidMatches && startedMatches
                && sameNullableIdentity(nullable(current.get("system_code")), incoming.systemCode())
                && sameNullableIdentity(nullable(current.get("runtime_hostname")), incoming.runtimeHostname())
                && sameNullableIdentity(nullable(current.get("application_name")), incoming.applicationName());
    }

    private static boolean sameProcessStart(Instant left, Instant right) {
        if (left == null || right == null) return left == null && right == null;
        // DB vendor별 TIMESTAMP fractional precision 차이를 허용하되 다른 process start는 구분합니다.
        return Math.abs(java.time.Duration.between(left, right).toMillis()) < 1_000L;
    }

    private static boolean sameNullableIdentity(String left, String right) {
        String a = left == null || left.isBlank() ? null : left.trim();
        String b = right == null || right.isBlank() ? null : right.trim();
        return java.util.Objects.equals(a, b);
    }

    private void upsertServiceInstance(CpfRuntimeInstanceRegistration r) {
        String managedServerId = resolveManagedServer(r);
        int updated = jdbc.update(
                "UPDATE OPS_SERVICE_INSTANCE SET managed_server_id=?,service_id=?,endpoint_code=?,instance_name=?,base_url=?,host_name=?," +
                        "environment_code=?,zone_code=?,cell_code=?,instance_status='UP',active_yn='Y',last_heartbeat_at=CURRENT_TIMESTAMP," +
                        "system_code=?,application_name=?,application_role=?,runtime_hostname=?,process_id=?,java_version=?,cpf_version=?," +
                        "application_version=?,started_at=?,updated_at=CURRENT_TIMESTAMP WHERE instance_id=?",
                managedServerId,r.serviceId(),r.endpointCode(),r.instanceId(),r.baseUrl(),emptyToNull(r.runtimeHostname()),
                blank(r.environment()),blank(r.zone()),blank(r.cell()),emptyToNull(r.systemCode()),emptyToNull(r.applicationName()),
                emptyToNull(r.applicationRole()),emptyToNull(r.runtimeHostname()),r.processId()==null?null:String.valueOf(r.processId()),
                emptyToNull(r.javaVersion()),emptyToNull(r.cpfVersion()),emptyToNull(r.applicationVersion()),ts(r.startedAt()),r.instanceId());
        if(updated==0) {
            try {
                jdbc.update(
                        "INSERT INTO OPS_SERVICE_INSTANCE(instance_id,managed_server_id,service_id,endpoint_code,instance_name,base_url,host_name," +
                                "environment_code,zone_code,cell_code,instance_status,weight,priority_no,active_yn,maintenance_yn,drain_yn," +
                                "last_heartbeat_at,system_code,application_name,application_role,runtime_hostname,process_id,java_version,cpf_version," +
                                "application_version,started_at,created_by,updated_by) " +
                                "VALUES (?,?,?,?,?,?,?,?,?,?,'UP',100,100,'Y','N','N',CURRENT_TIMESTAMP,?,?,?,?,?,?,?,?,?,'CPF','CPF')",
                        r.instanceId(),managedServerId,r.serviceId(),r.endpointCode(),r.instanceId(),r.baseUrl(),emptyToNull(r.runtimeHostname()),
                        blank(r.environment()),blank(r.zone()),blank(r.cell()),emptyToNull(r.systemCode()),emptyToNull(r.applicationName()),
                        emptyToNull(r.applicationRole()),emptyToNull(r.runtimeHostname()),r.processId()==null?null:String.valueOf(r.processId()),
                        emptyToNull(r.javaVersion()),emptyToNull(r.cpfVersion()),emptyToNull(r.applicationVersion()),ts(r.startedAt()));
            } catch (DuplicateKeyException duplicate) { upsertServiceInstance(r); }
        }
    }

    private String resolveManagedServer(CpfRuntimeInstanceRegistration r) {
        String explicit=emptyToNull(r.managedServerId());
        String identity=emptyToNull(r.managementIdentity());
        if(explicit!=null){
            java.util.List<java.util.Map<String,Object>> rows=jdbc.queryForList(
                    "SELECT management_identity,enabled_yn FROM ops_managed_server WHERE managed_server_id=?",explicit);
            if(rows.isEmpty()) throw new IllegalStateException("등록된 Managed Server가 아닙니다: "+explicit);
            String currentIdentity=nullable(rows.getFirst().get("management_identity"));
            if(identity!=null && currentIdentity!=null && !currentIdentity.isBlank() && !identity.equals(currentIdentity))
                throw new CpfRuntimeFenceException("Managed Server management identity 불일치: "+explicit);
            if(!"Y".equalsIgnoreCase(nullable(rows.getFirst().get("enabled_yn"))))
                throw new CpfRuntimeFenceException("Disabled Managed Server에는 Runtime을 연결할 수 없습니다: "+explicit);
            return explicit;
        }
        if(identity==null) return null; // hostname만으로 자동 merge하지 않습니다.
        java.util.List<java.util.Map<String,Object>> rows=jdbc.queryForList(
                "SELECT managed_server_id FROM ops_managed_server WHERE management_identity=? AND enabled_yn='Y' ORDER BY managed_server_id",identity);
        if(rows.size()>1) throw new IllegalStateException("managementIdentity가 둘 이상의 Managed Server에 연결되어 있습니다.");
        if(rows.size()==1) return nullable(rows.getFirst().get("managed_server_id"));
        // 인증된 Runtime discovery는 승인 전 PENDING inventory만 생성합니다. Production ACTIVE로 자동 승격하지 않습니다.
        String discovered="DISC-"+java.util.UUID.nameUUIDFromBytes((identity+"|"+blank(r.environment())).getBytes(java.nio.charset.StandardCharsets.UTF_8)).toString().substring(0,20).toUpperCase(java.util.Locale.ROOT);
        try{
            jdbc.update("INSERT INTO ops_managed_server(managed_server_id,server_name,display_name,hostname,management_identity,environment_code,server_group,zone_code,description,enabled_yn,status,tags_json,registered_at,registered_by,row_version,created_at,updated_at,updated_by) " +
                            "VALUES (?,?,?,?,?,?,NULL,?,'Authenticated runtime discovery','Y','PENDING','{}',CURRENT_TIMESTAMP,'RUNTIME_DISCOVERY',0,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,'RUNTIME_DISCOVERY')",
                    discovered,discovered,discovered,emptyToNull(r.runtimeHostname()),identity,blank(r.environment()),emptyToNull(r.zone()));
        }catch(DuplicateKeyException ignored){ }
        return discovered;
    }

    public CpfManagedServerPage findManagedServers(String environment, String status, String keyword, int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, Math.min(200, size <= 0 ? 50 : size));
        long from = (long) safePage * safeSize;
        long to = from + safeSize;
        StringBuilder where = new StringBuilder(" WHERE 1=1");
        java.util.List<Object> args = new java.util.ArrayList<>();
        if (environment != null && !environment.isBlank()) { where.append(" AND s.environment_code=?"); args.add(environment.trim()); }
        if (status != null && !status.isBlank()) { where.append(" AND s.status=?"); args.add(status.trim().toUpperCase(java.util.Locale.ROOT)); }
        if (keyword != null && !keyword.isBlank()) {
            where.append(" AND (LOWER(s.server_name) LIKE ? OR LOWER(s.display_name) LIKE ? OR LOWER(COALESCE(s.hostname,'')) LIKE ? OR LOWER(s.managed_server_id) LIKE ?)");
            String q = "%" + keyword.trim().toLowerCase(java.util.Locale.ROOT) + "%";
            args.add(q); args.add(q); args.add(q); args.add(q);
        }
        Long total = jdbc.queryForObject("SELECT COUNT(*) FROM ops_managed_server s" + where, Long.class, args.toArray());
        String sql = "SELECT * FROM (SELECT s.*, " +
                "(SELECT COUNT(*) FROM OPS_SERVICE_INSTANCE i WHERE i.managed_server_id=s.managed_server_id) runtime_count, " +
                "(SELECT COUNT(*) FROM OPS_SERVICE_INSTANCE i WHERE i.managed_server_id=s.managed_server_id AND i.active_yn='Y' " +
                "AND i.instance_status IN ('UP','ACTIVE','DEGRADED','RECOVERING','DRAINING')) active_runtime_count, " +
                "ROW_NUMBER() OVER(ORDER BY s.environment_code,s.server_name,s.managed_server_id) cpf_rn " +
                "FROM ops_managed_server s" + where + ") cpf_page WHERE cpf_rn>? AND cpf_rn<=? ORDER BY cpf_rn";
        java.util.List<Object> pageArgs = new java.util.ArrayList<>(args);
        pageArgs.add(from); pageArgs.add(to);
        java.util.List<CpfManagedServerSnapshot> items = jdbc.queryForList(sql, pageArgs.toArray()).stream().map(this::managedServer).toList();
        return CpfManagedServerPage.of(items, safePage, safeSize, total == null ? 0L : total);
    }

    public CpfManagedServerSnapshot getManagedServer(String managedServerId) {
        requireText(managedServerId,"managedServerId");
        java.util.List<java.util.Map<String,Object>> rows=jdbc.queryForList(
                "SELECT s.*, (SELECT COUNT(*) FROM OPS_SERVICE_INSTANCE i WHERE i.managed_server_id=s.managed_server_id) runtime_count, " +
                "(SELECT COUNT(*) FROM OPS_SERVICE_INSTANCE i WHERE i.managed_server_id=s.managed_server_id AND i.active_yn='Y' " +
                "AND i.instance_status IN ('UP','ACTIVE','DEGRADED','RECOVERING','DRAINING')) active_runtime_count " +
                "FROM ops_managed_server s WHERE s.managed_server_id=?", managedServerId);
        if(rows.isEmpty()) throw new IllegalArgumentException("Managed Server를 찾을 수 없습니다: "+managedServerId);
        return managedServer(rows.getFirst());
    }

    public CpfManagedServerSnapshot saveManagedServer(CpfManagedServerCommand c) {
        if(c==null) throw new IllegalArgumentException("Managed Server command가 필요합니다.");
        requireText(c.managedServerId(),"managedServerId"); requireText(c.serverName(),"serverName");
        requireText(c.displayName(),"displayName"); requireText(c.environment(),"environment");
        requireText(c.reason(),"reason"); requireText(c.operatorId(),"operatorId");
        java.util.List<java.util.Map<String,Object>> rows=jdbc.queryForList(
                "SELECT row_version FROM ops_managed_server WHERE managed_server_id=? FOR UPDATE",c.managedServerId());
        String tags=(c.tagsJson()==null||c.tagsJson().isBlank())?"{}":c.tagsJson();
        // Validate JSON eagerly so malformed tags cannot be persisted and break dashboard projection.
        readMap(tags);
        if(rows.isEmpty()) {
            if(c.expectedVersion()!=null && c.expectedVersion()!=0L) throw new CpfRuntimeVersionConflictException(c.expectedVersion(),0L);
            try {
                jdbc.update("INSERT INTO ops_managed_server(managed_server_id,server_name,display_name,hostname,management_identity,environment_code,server_group,zone_code,location,description,enabled_yn,status,tags_json,registered_at,registered_by,row_version,created_at,updated_at,updated_by) " +
                                "VALUES (?,?,?,?,?,?,?,?,?,?,'Y','REGISTERED',?,CURRENT_TIMESTAMP,?,0,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,?)",
                        c.managedServerId(),c.serverName().trim(),c.displayName().trim(),emptyToNull(c.hostname()),emptyToNull(c.managementIdentity()),
                        c.environment().trim(),emptyToNull(c.serverGroup()),emptyToNull(c.zone()),emptyToNull(c.location()),emptyToNull(c.description()),
                        tags,c.operatorId(),c.operatorId());
            } catch (DuplicateKeyException duplicate) { return saveManagedServer(c); }
        } else {
            long current=((Number)rows.getFirst().get("row_version")).longValue();
            if(c.expectedVersion()==null || c.expectedVersion()!=current) throw new CpfRuntimeVersionConflictException(c.expectedVersion()==null?-1L:c.expectedVersion(),current);
            int updated=jdbc.update("UPDATE ops_managed_server SET server_name=?,display_name=?,hostname=?,management_identity=?,environment_code=?,server_group=?,zone_code=?,location=?,description=?,tags_json=?,row_version=row_version+1,updated_at=CURRENT_TIMESTAMP,updated_by=? WHERE managed_server_id=? AND row_version=?",
                    c.serverName().trim(),c.displayName().trim(),emptyToNull(c.hostname()),emptyToNull(c.managementIdentity()),c.environment().trim(),emptyToNull(c.serverGroup()),emptyToNull(c.zone()),emptyToNull(c.location()),emptyToNull(c.description()),tags,c.operatorId(),c.managedServerId(),current);
            if(updated!=1) throw new CpfRuntimeVersionConflictException(current,current);
        }
        return getManagedServer(c.managedServerId());
    }

    public void disableManagedServer(String managedServerId,long expectedVersion,String reason,String operatorId) {
        requireText(managedServerId,"managedServerId"); requireText(reason,"reason"); requireText(operatorId,"operatorId");
        int updated=jdbc.update("UPDATE ops_managed_server SET enabled_yn='N',status='DISABLED',row_version=row_version+1,updated_at=CURRENT_TIMESTAMP,updated_by=? WHERE managed_server_id=? AND row_version=?",
                operatorId,managedServerId,expectedVersion);
        if(updated!=1) {
            java.util.List<java.util.Map<String,Object>> rows=jdbc.queryForList("SELECT row_version FROM ops_managed_server WHERE managed_server_id=?",managedServerId);
            if(rows.isEmpty()) throw new IllegalArgumentException("Managed Server를 찾을 수 없습니다: "+managedServerId);
            long current=((Number)rows.getFirst().get("row_version")).longValue();
            throw new CpfRuntimeVersionConflictException(expectedVersion,current);
        }
    }

    public CpfRuntimeInventoryPage findRuntimeInventory(String environment,String capability,String status,String keyword,int page,int size) {
        int safePage=Math.max(0,page);
        int safeSize=Math.max(1,Math.min(200,size<=0?50:size));
        long from=(long)safePage*safeSize;
        long to=from+safeSize;
        StringBuilder where=new StringBuilder(" WHERE 1=1");
        java.util.List<Object> args=new java.util.ArrayList<>();
        if(environment!=null&&!environment.isBlank()){where.append(" AND i.environment_code=?");args.add(environment.trim());}
        if(status!=null&&!status.isBlank()){where.append(" AND i.instance_status=?");args.add(status.trim().toUpperCase(java.util.Locale.ROOT));}
        if(capability!=null&&!capability.isBlank()){
            where.append(" AND LOWER(COALESCE(s.capabilities_json,'')) LIKE ?");
            args.add("%\""+capability.trim().toLowerCase(java.util.Locale.ROOT)+"\"%");
        }
        if(keyword!=null&&!keyword.isBlank()){
            where.append(" AND (LOWER(i.instance_id) LIKE ? OR LOWER(COALESCE(ms.server_name,'')) LIKE ? OR LOWER(COALESCE(i.runtime_hostname,'')) LIKE ? OR LOWER(COALESCE(i.system_code,'')) LIKE ?)");
            String q="%"+keyword.trim().toLowerCase(java.util.Locale.ROOT)+"%";args.add(q);args.add(q);args.add(q);args.add(q);
        }
        String joins=" FROM OPS_SERVICE_INSTANCE i LEFT JOIN ops_managed_server ms ON ms.managed_server_id=i.managed_server_id LEFT JOIN OPS_RUNTIME_INSTANCE_STATE s ON s.instance_id=i.instance_id";
        Long total=jdbc.queryForObject("SELECT COUNT(*)"+joins+where,Long.class,args.toArray());
        String sql="SELECT * FROM (SELECT i.instance_id,i.managed_server_id,ms.server_name,i.service_id,i.system_code,i.application_name,i.application_role,i.runtime_hostname,i.environment_code,i.zone_code,i.instance_status,i.started_at,i.last_heartbeat_at,"+
                "s.artifact_version,s.capabilities_json,i.cpf_version,i.java_version,ROW_NUMBER() OVER(ORDER BY i.environment_code,COALESCE(ms.server_name,''),i.instance_id) cpf_rn"+
                joins+where+") cpf_page WHERE cpf_rn>? AND cpf_rn<=? ORDER BY cpf_rn";
        java.util.List<Object> pageArgs=new java.util.ArrayList<>(args);pageArgs.add(from);pageArgs.add(to);
        java.util.List<CpfRuntimeInventorySnapshot> items=new java.util.ArrayList<>();
        for(java.util.Map<String,Object> row:jdbc.queryForList(sql,pageArgs.toArray())){
            java.util.Map<String,Object> raw=readMapOrEmpty(nullable(row.get("capabilities_json")));
            java.util.LinkedHashMap<String,String> caps=new java.util.LinkedHashMap<>();raw.forEach((k,v)->caps.put(k,String.valueOf(v)));
            items.add(new CpfRuntimeInventorySnapshot(nullable(row.get("instance_id")),nullable(row.get("managed_server_id")),nullable(row.get("server_name")),nullable(row.get("service_id")),nullable(row.get("system_code")),nullable(row.get("application_name")),nullable(row.get("application_role")),nullable(row.get("runtime_hostname")),nullable(row.get("environment_code")),nullable(row.get("zone_code")),nullable(row.get("instance_status")),nullable(row.get("artifact_version")),nullable(row.get("cpf_version")),nullable(row.get("java_version")),caps,toInstant(row.get("started_at")),toInstant(row.get("last_heartbeat_at"))));
        }
        return CpfRuntimeInventoryPage.of(items,safePage,safeSize,total==null?0L:total);
    }

    private CpfManagedServerSnapshot managedServer(java.util.Map<String,Object> row){
        return new CpfManagedServerSnapshot(nullable(row.get("managed_server_id")),nullable(row.get("server_name")),nullable(row.get("display_name")),nullable(row.get("hostname")),nullable(row.get("management_identity")),nullable(row.get("environment_code")),nullable(row.get("server_group")),nullable(row.get("zone_code")),nullable(row.get("location")),nullable(row.get("description")),nullable(row.get("status")),"Y".equalsIgnoreCase(nullable(row.get("enabled_yn"))),nullable(row.get("tags_json")),number(row.get("row_version")),number(row.get("runtime_count")),number(row.get("active_runtime_count")),toInstant(row.get("registered_at")),toInstant(row.get("updated_at")));
    }

    public com.cpf.platform.operations.runtimecontrol.CpfRuntimeAuditVerification verifyAudit(String changeId) {
        requireText(changeId, "changeId");
        List<Map<String,Object>> rows = jdbc.queryForList(
                "SELECT audit_id,event_type,actor_id,reason,evidence_hash,previous_hash,chain_hash,created_at " +
                        "FROM OPS_RUNTIME_CHANGE_AUDIT WHERE change_id=? ORDER BY audit_id", changeId);
        String previous = "GENESIS";
        long count = 0L;
        for (Map<String,Object> row : rows) {
            count++;
            String storedPrevious = String.valueOf(row.get("previous_hash"));
            Instant eventAt = toInstant(row.get("created_at"));
            String expected = CpfRuntimeCanonicalHash.sha256(Map.of(
                    "previous", previous,
                    "changeId", changeId,
                    "eventType", blank(nullable(row.get("event_type"))),
                    "actor", blank(nullable(row.get("actor_id"))),
                    "reason", blank(nullable(row.get("reason"))),
                    "evidenceHash", blank(nullable(row.get("evidence_hash"))),
                    "at", eventAt == null ? "" : eventAt.toString()));
            String actual = String.valueOf(row.get("chain_hash"));
            if (!previous.equals(storedPrevious) || !expected.equals(actual)) {
                return new com.cpf.platform.operations.runtimecontrol.CpfRuntimeAuditVerification(
                        changeId, false, count - 1L, ((Number) row.get("audit_id")).longValue(),
                        expected, actual, "Runtime Change audit hash-chain 변조 또는 불일치가 탐지되었습니다.");
            }
            previous = actual;
        }
        return new com.cpf.platform.operations.runtimecontrol.CpfRuntimeAuditVerification(
                changeId, true, count, null, null, null, "VALID");
    }

    private void appendAudit(String changeId,String eventType,String actor,String reason,String evidenceHash) {
        // 같은 Change의 audit append를 직렬화해 hash-chain fork를 방지합니다.
        jdbc.queryForList("SELECT change_id FROM OPS_RUNTIME_CHANGE WHERE change_id=? FOR UPDATE", changeId);
        List<Map<String,Object>> rows=jdbc.queryForList(
                "SELECT chain_hash FROM OPS_RUNTIME_CHANGE_AUDIT WHERE change_id=? ORDER BY audit_id",changeId);
        String previous=rows.isEmpty()?"GENESIS":String.valueOf(rows.getLast().get("chain_hash"));
        Instant eventAt=Instant.now();
        String current=CpfRuntimeCanonicalHash.sha256(Map.of(
                "previous",previous,"changeId",changeId,"eventType",blank(eventType),
                "actor",blank(actor),"reason",blank(reason),"evidenceHash",blank(evidenceHash),
                "at",eventAt.toString()));
        jdbc.update("INSERT INTO OPS_RUNTIME_CHANGE_AUDIT(change_id,event_type,actor_id,reason,evidence_hash," +
                        "previous_hash,chain_hash,created_by,created_at) VALUES (?,?,?,?,?,?,?,'CPF',?)",
                changeId,blank(eventType),blank(actor),blank(reason),blank(evidenceHash),previous,current,ts(eventAt));
    }

    private String write(Object value){try{return objectMapper.writeValueAsString(value);}catch(Exception ex){throw new IllegalArgumentException("Runtime JSON 직렬화 실패",ex);}}
    private Map<String,Object> readMap(String json){try{return objectMapper.readValue(json,new TypeReference<>(){});}catch(Exception ex){throw new IllegalStateException("Runtime payload JSON 역직렬화 실패",ex);}}
    private Map<String,Object> readMapOrEmpty(String json){if(json==null||json.isBlank())return Map.of();Map<String,Object> value=readMap(json);return value==null?Map.of():value;}
    public String json(Object value){return write(value);}
    private Map<String,Object> jsonMap(String value){return readMap(value);}
    private Timestamp ts(Instant value){return value==null?null:Timestamp.from(value);}
    private static Instant toInstant(Object value){if(value==null)return null;if(value instanceof Timestamp t)return t.toInstant();if(value instanceof java.util.Date d)return d.toInstant();try{return Instant
            .parse(String.valueOf(value));}catch(Exception ignored){return null;}}
    private long number(Object value){return value==null?0L:((Number)value).longValue();}
    private static String nullable(Object value){return value==null?null:String.valueOf(value);}
    private String baseChangeType(String value) {
        String type = blank(value).trim().toUpperCase();
        return type.startsWith("ROLLBACK:") ? type.substring("ROLLBACK:".length()) : type;
    }
    private static String blank(String value){return value==null?"":value;}
    private String emptyToNull(String value){return value==null||value.isBlank()?null:value.trim();}
    private String truncate(String value,int max){if(value==null)return null;return value.length()>max?value.substring(0,max):value;}
    private void requireText(String value,String name){if(value==null||value.isBlank())throw new IllegalArgumentException(name+"가 필요합니다.");}
}
