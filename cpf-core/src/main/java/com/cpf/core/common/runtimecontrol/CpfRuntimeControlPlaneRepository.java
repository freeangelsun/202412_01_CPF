package com.cpf.core.common.runtimecontrol;

import com.cpf.core.api.runtimecontrol.CpfRuntimeActualState;
import com.cpf.core.api.runtimecontrol.CpfRuntimeDelivery;
import com.cpf.core.api.runtimecontrol.CpfRuntimeInstanceLease;
import com.cpf.core.api.runtimecontrol.CpfRuntimeInstanceRegistration;
import com.cpf.core.api.runtimecontrol.CpfRuntimeTargetSelector;
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

    public Optional<Map<String, Object>> findOperation(String operationId) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT operation_id, command_type, request_hash, entity_id, result_state, result_json, expires_at " +
                        "FROM cpf_control_operation WHERE operation_id=?", operationId);
        return rows.stream().findFirst();
    }

    public void consumeRateLimit(String subjectId, int limitPerMinute) {
        requireText(subjectId, "subjectId");
        int limit = Math.max(1, Math.min(10_000, limitPerMinute));
        Instant now = Instant.now();
        String minute = java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmm")
                .withZone(java.time.ZoneOffset.UTC).format(now);
        String bucket = subjectId + ":" + minute;
        int updated = jdbc.update("UPDATE cpf_runtime_rate_bucket SET request_count=request_count+1," +
                        "updated_at=CURRENT_TIMESTAMP WHERE bucket_key=? AND request_count<?",
                bucket, limit);
        if (updated == 1) return;
        Integer existing = jdbc.queryForObject(
                "SELECT COUNT(*) FROM cpf_runtime_rate_bucket WHERE bucket_key=?", Integer.class, bucket);
        if (existing != null && existing > 0) throw new CpfRuntimeRateLimitException(limit);
        try {
            jdbc.update("INSERT INTO cpf_runtime_rate_bucket " +
                            "(bucket_key,subject_id,window_start,request_count,created_by,updated_by) " +
                            "VALUES (?,?,?,1,'CPF','CPF')",
                    bucket, subjectId, ts(now.truncatedTo(java.time.temporal.ChronoUnit.MINUTES)));
        } catch (DuplicateKeyException duplicate) {
            consumeRateLimit(subjectId, limitPerMinute);
        }
    }

    public boolean insertOperation(String operationId, String commandType, String requestHash, Instant expiresAt) {
        try {
            return jdbc.update("INSERT INTO cpf_control_operation " +
                            "(operation_id, command_type, request_hash, result_state, expires_at, created_by, updated_by) " +
                            "VALUES (?,?,?,?,?,?,?)",
                    operationId, commandType, requestHash, "PROCESSING", ts(expiresAt), "CPF", "CPF") == 1;
        } catch (DuplicateKeyException duplicate) {
            return false;
        }
    }

    public void completeOperation(String operationId, String entityId, String state, String resultJson) {
        int updated = jdbc.update("UPDATE cpf_control_operation SET entity_id=?, result_state=?, result_json=?, updated_at=CURRENT_TIMESTAMP " +
                        "WHERE operation_id=? AND request_hash IS NOT NULL",
                entityId, state, resultJson, operationId);
        if (updated != 1) throw new IllegalStateException("operation 완료 상태 갱신 실패: " + operationId);
    }

    public long lockAndNextVersion(Long expectedVersion) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT version_no FROM cpf_runtime_version WHERE version_key='GLOBAL' FOR UPDATE");
        if (rows.isEmpty()) {
            try {
                jdbc.update("INSERT INTO cpf_runtime_version (version_key, version_no, created_by, updated_by) VALUES ('GLOBAL',0,'CPF','CPF')");
            } catch (DuplicateKeyException ignored) {
                // 동시 insert는 재조회합니다.
            }
            rows = jdbc.queryForList("SELECT version_no FROM cpf_runtime_version WHERE version_key='GLOBAL' FOR UPDATE");
        }
        long current = ((Number) rows.getFirst().get("version_no")).longValue();
        if (expectedVersion != null && expectedVersion.longValue() != current) {
            throw new CpfRuntimeVersionConflictException(expectedVersion, current);
        }
        long next = current + 1L;
        if (jdbc.update("UPDATE cpf_runtime_version SET version_no=?, updated_at=CURRENT_TIMESTAMP, updated_by='CPF' " +
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
            StringBuilder sql = new StringBuilder("SELECT i.instance_id FROM cpf_service_instance i " +
                    "LEFT JOIN cpf_runtime_instance_state s ON s.instance_id=i.instance_id WHERE i.active_yn='Y'");
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
                        "FROM cpf_service_instance i JOIN cpf_runtime_instance_state s ON s.instance_id=i.instance_id " +
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
                "SELECT capabilities_json FROM cpf_runtime_instance_state WHERE instance_id=?", instanceId);
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
            jdbc.queryForList("SELECT instance_id FROM cpf_runtime_group_member WHERE group_id=? AND active_yn='Y' ORDER BY instance_id", groupId)
                    .forEach(row -> instances.add(String.valueOf(row.get("instance_id"))));
            jdbc.queryForList("SELECT group_id FROM cpf_runtime_instance_group WHERE parent_group_id=? AND active_yn='Y' ORDER BY group_id", groupId)
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
        jdbc.update("INSERT INTO cpf_runtime_change " +
                        "(change_id,operation_id,change_type,payload_schema_version,request_hash,payload_hash,payload_json,rollback_payload_json,target_snapshot_json,desired_version," +
                        "rollout_mode,wave_size,quorum_percent,change_state,scheduled_at,expires_at,reason,approval_id,break_glass_id,requested_by,created_by,updated_by) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                changeId, operationId, type, payloadSchemaVersion, requestHash, payloadHash, payloadJson, rollbackJson,
                targetSnapshotJson, version, rolloutMode, waveSize, quorumPercent, state, ts(scheduledAt), ts(expiresAt),
                reason, approvalId, breakGlassId, requestedBy, requestedBy, requestedBy);

        int sequence = 0;
        for (String instanceId : targets) {
            String deliveryId = UUID.randomUUID().toString();
            int inserted = jdbc.update("INSERT INTO cpf_runtime_delivery " +
                            "(delivery_id,change_id,instance_id,sequence_no,desired_version,delivery_state,attempt_no,next_attempt_at,created_by,updated_by) " +
                            "VALUES (?,?,?,?,?,'PENDING',0,CURRENT_TIMESTAMP,?,?)",
                    deliveryId, changeId, instanceId, ++sequence, version, requestedBy, requestedBy);
            if (inserted != 1) throw new IllegalStateException("Runtime delivery 생성 실패: " + instanceId);
            jdbc.update("UPDATE cpf_runtime_instance_state SET desired_version=?, desired_hash=?, drift_state='PENDING', updated_at=CURRENT_TIMESTAMP, updated_by=? WHERE instance_id=?",
                    version, payloadHash, requestedBy, instanceId);
            int featureUpdated = jdbc.update("UPDATE cpf_runtime_instance_feature_state SET desired_version=?,desired_hash=?,drift_state='PENDING'," +
                            "updated_by=?,updated_at=CURRENT_TIMESTAMP WHERE instance_id=? AND change_type=?",
                    version, payloadHash, requestedBy, instanceId, baseChangeType(type));
            if (featureUpdated == 0) {
                try {
                    jdbc.update("INSERT INTO cpf_runtime_instance_feature_state " +
                                    "(instance_id,change_type,desired_version,actual_version,desired_hash,actual_hash,drift_state,created_by,updated_by) " +
                                    "VALUES (?,?,?,0,?,NULL,'PENDING',?,?)",
                            instanceId, baseChangeType(type), version, payloadHash, requestedBy, requestedBy);
                } catch (DuplicateKeyException duplicate) {
                    jdbc.update("UPDATE cpf_runtime_instance_feature_state SET desired_version=?,desired_hash=?,drift_state='PENDING'," +
                                    "updated_by=?,updated_at=CURRENT_TIMESTAMP WHERE instance_id=? AND change_type=?",
                            version, payloadHash, requestedBy, instanceId, baseChangeType(type));
                }
            }
        }
        appendAudit(changeId, "CHANGE_CREATED", requestedBy, reason, requestHash);
    }

    public Optional<Map<String,Object>> findChange(String column, String value) {
        if (!"change_id".equals(column) && !"operation_id".equals(column)) throw new IllegalArgumentException("unsupported column");
        List<Map<String,Object>> rows=jdbc.queryForList("SELECT * FROM cpf_runtime_change WHERE "+column+"=?", value);
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
                        "FROM cpf_runtime_delivery d JOIN cpf_runtime_change c ON c.change_id=d.change_id " +
                        "WHERE d.instance_id=? AND d.delivery_state IN ('PENDING','FAILED') AND d.next_attempt_at<=CURRENT_TIMESTAMP " +
                        "AND c.change_state IN ('APPLYING','PARTIAL') " +
                        "AND (c.scheduled_at IS NULL OR c.scheduled_at<=CURRENT_TIMESTAMP) " +
                        "AND (c.expires_at IS NULL OR c.expires_at>CURRENT_TIMESTAMP) " +
                        "AND NOT EXISTS (" +
                        "  SELECT 1 FROM cpf_runtime_delivery older JOIN cpf_runtime_change older_change ON older_change.change_id=older.change_id " +
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
                    "UPDATE cpf_runtime_delivery SET delivery_state='CLAIMED',attempt_no=attempt_no+1," +
                            "fencing_token=?,claimed_at=CURRENT_TIMESTAMP,updated_at=CURRENT_TIMESTAMP " +
                            "WHERE delivery_id=? AND instance_id=? AND delivery_state IN ('PENDING','FAILED')",
                    fencingToken, deliveryId, instanceId);
            if (updated != 1) continue;
            Map<String,Object> payload = readMap(String.valueOf(row.get("payload_json")));
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
                "SELECT COUNT(*) FROM cpf_runtime_delivery WHERE change_id=? " +
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
                        "FROM cpf_runtime_delivery d JOIN cpf_runtime_change c ON c.change_id=d.change_id " +
                        "WHERE d.change_id=? AND d.sequence_no<? ORDER BY d.sequence_no",
                changeId, waveStart);
        if (prior.isEmpty()) return true;
        int healthy = 0;
        for (Map<String,Object> row : prior) {
            if (!"ACKED".equals(String.valueOf(row.get("delivery_state")))) return false;
            List<Map<String,Object>> health = jdbc.queryForList(
                    "SELECT f.drift_state,s.lease_until FROM cpf_runtime_instance_feature_state f " +
                            "JOIN cpf_runtime_instance_state s ON s.instance_id=f.instance_id " +
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
        jdbc.update("UPDATE cpf_runtime_change SET change_state='APPLYING',updated_at=CURRENT_TIMESTAMP " +
                "WHERE change_state='SCHEDULED' AND scheduled_at<=CURRENT_TIMESTAMP " +
                "AND (expires_at IS NULL OR expires_at>CURRENT_TIMESTAMP)");
        jdbc.update("UPDATE cpf_runtime_change SET change_state='EXPIRED',updated_at=CURRENT_TIMESTAMP " +
                "WHERE change_state IN ('SCHEDULED','APPLYING','PARTIAL') AND expires_at<=CURRENT_TIMESTAMP");
        jdbc.update("UPDATE cpf_runtime_delivery SET delivery_state='EXPIRED',updated_at=CURRENT_TIMESTAMP " +
                "WHERE delivery_state IN ('PENDING','FAILED','CLAIMED','RESTART_REQUIRED') " +
                "AND change_id IN (SELECT change_id FROM cpf_runtime_change WHERE change_state='EXPIRED')");
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
                "SELECT delivery_state,actual_hash,error_code FROM cpf_runtime_delivery " +
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
                    "SELECT attempt_no FROM cpf_runtime_delivery WHERE delivery_id=?", Integer.class, deliveryId);
            int attempt = attemptValue == null ? 1 : attemptValue;
            deliveryState = isPermanentFailure(errorCode) || attempt >= 8 ? "POISONED" : "FAILED";
        }

        int updated = jdbc.update(
                "UPDATE cpf_runtime_delivery SET delivery_state=?,actual_hash=?,error_code=?,error_message=?," +
                        "acknowledged_at=?,updated_at=CURRENT_TIMESTAMP " +
                        "WHERE delivery_id=? AND change_id=? AND instance_id=? AND fencing_token=? AND delivery_state='CLAIMED'",
                deliveryState, actualHash, errorCode, truncate(message, 900), ts(at),
                deliveryId, changeId, instanceId, fencingToken);
        if (updated != 1) {
            throw new CpfRuntimeFenceException("ACK가 오래되었거나 이미 처리된 delivery입니다. deliveryId=" + deliveryId);
        }

        if ("ACKED".equals(deliveryState)) {
            jdbc.update("UPDATE cpf_runtime_instance_state SET actual_version=?,actual_hash=?," +
                            "drift_state=CASE WHEN desired_version=? AND desired_hash=? THEN 'IN_SYNC' ELSE 'DRIFT' END," +
                            "last_ack_change_id=?,last_ack_at=?,updated_at=CURRENT_TIMESTAMP " +
                            "WHERE instance_id=? AND fencing_token=?",
                    appliedVersion, actualHash, appliedVersion, actualHash, changeId, ts(at), instanceId, fencingToken);
            jdbc.update("UPDATE cpf_runtime_instance_feature_state SET actual_version=?,actual_hash=?," +
                            "drift_state=CASE WHEN desired_version=? AND desired_hash=? THEN 'IN_SYNC' ELSE 'DRIFT' END," +
                            "source_delivery_id=?,updated_at=CURRENT_TIMESTAMP WHERE instance_id=? " +
                            "AND change_type=(SELECT change_type FROM cpf_runtime_change WHERE change_id=?)",
                    appliedVersion, actualHash, appliedVersion, actualHash, deliveryId, instanceId, changeId);
        } else if ("RESTART_REQUIRED".equals(deliveryState)) {
            jdbc.update("UPDATE cpf_runtime_instance_state SET drift_state='PENDING_RESTART'," +
                    "last_ack_change_id=?,last_ack_at=?,updated_at=CURRENT_TIMESTAMP WHERE instance_id=? AND fencing_token=?",
                    changeId, ts(at), instanceId, fencingToken);
            jdbc.update("UPDATE cpf_runtime_instance_feature_state SET drift_state='PENDING_RESTART'," +
                            "source_delivery_id=?,updated_at=CURRENT_TIMESTAMP WHERE instance_id=? " +
                            "AND change_type=(SELECT change_type FROM cpf_runtime_change WHERE change_id=?)",
                    deliveryId, instanceId, changeId);
            jdbc.update("UPDATE cpf_service_instance SET drain_yn='Y',instance_status='DRAINING'," +
                    "drain_deadline_at=?,updated_at=CURRENT_TIMESTAMP WHERE instance_id=?",
                    ts(Instant.now().plusSeconds(600)), instanceId);
        } else if ("UNKNOWN_RESULT".equals(deliveryState)) {
            jdbc.update("UPDATE cpf_runtime_instance_state SET drift_state='UNKNOWN_RESULT'," +
                    "last_ack_change_id=?,last_ack_at=?,updated_at=CURRENT_TIMESTAMP WHERE instance_id=? AND fencing_token=?",
                    changeId, ts(at), instanceId, fencingToken);
            jdbc.update("UPDATE cpf_runtime_instance_feature_state SET drift_state='UNKNOWN_RESULT'," +
                            "source_delivery_id=?,updated_at=CURRENT_TIMESTAMP WHERE instance_id=? " +
                            "AND change_type=(SELECT change_type FROM cpf_runtime_change WHERE change_id=?)",
                    deliveryId, instanceId, changeId);
        } else if ("FAILED".equals(deliveryState)) {
            Integer attemptValue = jdbc.queryForObject(
                    "SELECT attempt_no FROM cpf_runtime_delivery WHERE delivery_id=?", Integer.class, deliveryId);
            int attempt = attemptValue == null ? 1 : attemptValue;
            long base = Math.min(300L, Math.max(1L, 1L << Math.min(8, Math.max(0, attempt - 1))));
            long jitter = Math.floorMod(deliveryId.hashCode(), Math.max(1, (int) Math.min(30L, base)));
            jdbc.update("UPDATE cpf_runtime_delivery SET next_attempt_at=?,updated_at=CURRENT_TIMESTAMP WHERE delivery_id=?",
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
        int updated=jdbc.update("UPDATE cpf_runtime_change SET change_state='CANCELLED',updated_at=CURRENT_TIMESTAMP,updated_by=? " +
                "WHERE change_id=? AND change_state IN ('SCHEDULED','APPLYING','PARTIAL')",operatorId,changeId);
        if (updated!=1) throw new IllegalStateException("취소할 수 없는 Runtime Change입니다. changeId="+changeId);
        jdbc.update("UPDATE cpf_runtime_delivery SET delivery_state='CANCELLED',updated_at=CURRENT_TIMESTAMP WHERE change_id=? AND delivery_state IN ('PENDING','FAILED')",changeId);
        appendAudit(changeId,"CHANGE_CANCELLED",operatorId,reason,null);
    }

    public void markRollbackPending(String changeId, String operatorId, String reason) {
        int updated=jdbc.update("UPDATE cpf_runtime_change SET change_state='ROLLBACK_PENDING',updated_at=CURRENT_TIMESTAMP,updated_by=? " +
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
                "SELECT fencing_token,lease_until,registration_source FROM cpf_runtime_instance_state " +
                        "WHERE instance_id=? FOR UPDATE", r.instanceId());
        long fence = existing.isEmpty() ? 1L : ((Number) existing.getFirst().get("fencing_token")).longValue() + 1L;
        if (!existing.isEmpty()) {
            Map<String,Object> current = existing.getFirst();
            Instant currentLease = toInstant(current.get("lease_until"));
            String source = nullable(current.get("registration_source"));
            if (currentLease != null && currentLease.isAfter(Instant.now())
                    && source != null && !source.isBlank()
                    && !source.equals(r.registrationSource())) {
                throw new CpfRuntimeFenceException(
                        "살아 있는 동일 instanceId에 다른 registrationSource가 등록될 수 없습니다: " + r.instanceId());
            }
        }

        // identity/fencing 검증이 완료된 뒤에만 Service Registry를 갱신합니다.
        upsertServiceInstance(r);
        Instant lease = Instant.now().plusSeconds(r.leaseSeconds());
        String capabilities = write(r.capabilities());
        String labels = write(r.labels());

        if (existing.isEmpty()) {
            try {
                jdbc.update("INSERT INTO cpf_runtime_instance_state " +
                                "(instance_id,fencing_token,lease_until,desired_version,actual_version,drift_state," +
                                "capabilities_json,labels_json,artifact_version,artifact_commit,runtime_role,registration_source," +
                                "schema_version,config_hash,clock_skew_ms,heartbeat_at,created_by,updated_by) " +
                                "VALUES (?,?,?,0,0,'IN_SYNC',?,?,?,?,?,?,?,?,?,CURRENT_TIMESTAMP,'CPF','CPF')",
                        r.instanceId(), fence, ts(lease), capabilities, labels, blank(r.artifactVersion()),
                        blank(r.artifactCommit()), blank(r.runtimeRole()), blank(r.registrationSource()),
                        blank(r.schemaVersion()), blank(r.configHash()), skewMs);
            } catch (DuplicateKeyException duplicate) {
                return register(r);
            }
        } else {
            int updated = jdbc.update("UPDATE cpf_runtime_instance_state SET fencing_token=?,lease_until=?," +
                            "capabilities_json=?,labels_json=?,artifact_version=?,artifact_commit=?,runtime_role=?," +
                            "registration_source=?,schema_version=?,config_hash=?,clock_skew_ms=?,heartbeat_at=CURRENT_TIMESTAMP," +
                            "updated_at=CURRENT_TIMESTAMP WHERE instance_id=? AND fencing_token=?",
                    fence, ts(lease), capabilities, labels, blank(r.artifactVersion()), blank(r.artifactCommit()),
                    blank(r.runtimeRole()), blank(r.registrationSource()), blank(r.schemaVersion()), blank(r.configHash()),
                    skewMs, r.instanceId(), fence - 1);
            if (updated != 1) throw new CpfRuntimeFenceException("Runtime instance 재등록 fencing 충돌: " + r.instanceId());
            jdbc.update("UPDATE cpf_runtime_delivery SET delivery_state='PENDING',fencing_token=NULL," +
                            "next_attempt_at=CURRENT_TIMESTAMP,updated_at=CURRENT_TIMESTAMP " +
                            "WHERE instance_id=? AND delivery_state='RESTART_REQUIRED'",
                    r.instanceId());
        }
        return lease(r.instanceId());
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
        int updated = jdbc.update("UPDATE cpf_runtime_instance_state SET lease_until=?,heartbeat_at=CURRENT_TIMESTAMP," +
                        "actual_hash=?,actual_version=?,clock_skew_ms=?," +
                        "drift_state=CASE WHEN desired_version=? AND COALESCE(desired_hash,'')=COALESCE(?,'') " +
                        "THEN 'IN_SYNC' ELSE drift_state END,updated_at=CURRENT_TIMESTAMP " +
                        "WHERE instance_id=? AND fencing_token=?",
                ts(until), actualHash, actualVersion, skewMs, actualVersion, actualHash, instanceId, fencingToken);
        if (updated != 1) throw new CpfRuntimeFenceException("Runtime heartbeat fencing 충돌: " + instanceId);
        jdbc.update("UPDATE cpf_service_instance SET instance_status=CASE WHEN COALESCE(drain_yn,'N')='Y' " +
                        "THEN 'DRAINING' ELSE 'UP' END,last_heartbeat_at=CURRENT_TIMESTAMP,updated_at=CURRENT_TIMESTAMP " +
                        "WHERE instance_id=?", instanceId);
        return lease(instanceId);
    }

    /** 기존 호출 호환입니다. */
    public CpfRuntimeInstanceLease heartbeat(String instanceId, long fencingToken, String actualHash,
                                             long actualVersion, int leaseSeconds) {
        return heartbeat(instanceId, fencingToken, actualHash, actualVersion, leaseSeconds, Instant.now());
    }

    public void deregister(String instanceId, long fencingToken, String reason) {
        assertFence(instanceId, fencingToken);
        jdbc.update("UPDATE cpf_runtime_instance_state SET lease_until=CURRENT_TIMESTAMP," +
                        "heartbeat_at=CURRENT_TIMESTAMP,updated_at=CURRENT_TIMESTAMP " +
                        "WHERE instance_id=? AND fencing_token=?", instanceId, fencingToken);
        jdbc.update("UPDATE cpf_service_instance SET instance_status='DOWN',active_yn='N'," +
                "updated_at=CURRENT_TIMESTAMP WHERE instance_id=?", instanceId);
    }

    public void reconcileActualState(String instanceId, long fencingToken, List<CpfRuntimeActualState> states) {
        assertFence(instanceId, fencingToken);
        long maxVersion = 0L;
        String maxHash = null;
        for (CpfRuntimeActualState state : states == null ? List.<CpfRuntimeActualState>of() : states) {
            if (state == null || state.changeType() == null || state.changeType().isBlank()
                    || state.actualHash() == null || state.actualHash().isBlank()) continue;
            int updated = jdbc.update("UPDATE cpf_runtime_instance_feature_state SET actual_version=?,actual_hash=?," +
                            "drift_state=CASE WHEN desired_version=? AND desired_hash=? THEN 'IN_SYNC' ELSE 'DRIFT' END," +
                            "source_delivery_id=?,updated_at=CURRENT_TIMESTAMP " +
                            "WHERE instance_id=? AND change_type=?",
                    state.actualVersion(), state.actualHash(), state.actualVersion(), state.actualHash(),
                    state.sourceDeliveryId(), instanceId, state.changeType().trim().toUpperCase());
            if (updated == 0) {
                try {
                    jdbc.update("INSERT INTO cpf_runtime_instance_feature_state " +
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
            jdbc.update("UPDATE cpf_runtime_instance_state SET actual_version=?,actual_hash=?," +
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
                "SELECT holder_id,fencing_token,lease_until FROM cpf_runtime_controller_lease " +
                        "WHERE lease_key='RUNTIME_CONTROL' FOR UPDATE");
        if (rows.isEmpty()) {
            try {
                jdbc.update("INSERT INTO cpf_runtime_controller_lease " +
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
            int updated = jdbc.update("UPDATE cpf_runtime_controller_lease SET lease_until=?," +
                            "last_reconciled_at=CURRENT_TIMESTAMP,updated_at=CURRENT_TIMESTAMP " +
                            "WHERE lease_key='RUNTIME_CONTROL' AND holder_id=? AND fencing_token=?",
                    ts(until), holderId, currentFence);
            return updated == 1 ? currentFence : 0L;
        }
        if (currentUntil == null || !currentUntil.isAfter(now)) {
            long nextFence = currentFence + 1L;
            int updated = jdbc.update("UPDATE cpf_runtime_controller_lease SET holder_id=?,fencing_token=?," +
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
                "SELECT delivery_id,attempt_no FROM cpf_runtime_delivery " +
                        "WHERE delivery_state='CLAIMED' AND claimed_at<?", ts(timeout));
        for (Map<String,Object> row : timedOut) {
            String deliveryId = String.valueOf(row.get("delivery_id"));
            int attempt = ((Number) row.get("attempt_no")).intValue();
            String nextState = attempt >= 8 ? "POISONED" : "FAILED";
            jdbc.update("UPDATE cpf_runtime_delivery SET delivery_state=?,error_code='ACK_TIMEOUT'," +
                            "error_message='Runtime Agent ACK timeout',next_attempt_at=CURRENT_TIMESTAMP," +
                            "updated_at=CURRENT_TIMESTAMP WHERE delivery_id=? AND delivery_state='CLAIMED'",
                    nextState, deliveryId);
        }

        jdbc.update("UPDATE cpf_service_instance SET instance_status='DOWN',updated_at=CURRENT_TIMESTAMP " +
                "WHERE instance_id IN (SELECT instance_id FROM cpf_runtime_instance_state " +
                "WHERE lease_until<CURRENT_TIMESTAMP)");

        List<Map<String,Object>> blocked = jdbc.queryForList(
                "SELECT DISTINCT c.change_id,c.rollout_mode,d.delivery_state " +
                        "FROM cpf_runtime_change c JOIN cpf_runtime_delivery d ON d.change_id=c.change_id " +
                        "WHERE c.change_state IN ('APPLYING','PARTIAL') " +
                        "AND c.rollout_mode IN ('CANARY','WAVE') " +
                        "AND d.delivery_state IN ('POISONED','UNKNOWN_RESULT')");
        for (Map<String,Object> row : blocked) {
            String changeId = String.valueOf(row.get("change_id"));
            String deliveryState = String.valueOf(row.get("delivery_state"));
            String state = "UNKNOWN_RESULT".equals(deliveryState) ? "UNKNOWN_RESULT" : "FAILED";
            jdbc.update("UPDATE cpf_runtime_change SET change_state=?,updated_at=CURRENT_TIMESTAMP " +
                    "WHERE change_id=? AND change_state IN ('APPLYING','PARTIAL')", state, changeId);
            jdbc.update("UPDATE cpf_runtime_delivery SET delivery_state='CANCELLED',updated_at=CURRENT_TIMESTAMP " +
                    "WHERE change_id=? AND delivery_state IN ('PENDING','FAILED')", changeId);
            appendAudit(changeId, "ROLLOUT_AUTO_STOP", holderId,
                    "Canary/Wave health gate stopped rollout: " + deliveryState, null);
        }
        jdbc.update("DELETE FROM cpf_runtime_rate_bucket WHERE window_start<?",
                ts(Instant.now().minusSeconds(172800)));
        jdbc.update("UPDATE cpf_runtime_controller_lease SET last_reconciled_at=CURRENT_TIMESTAMP," +
                        "updated_at=CURRENT_TIMESTAMP WHERE lease_key='RUNTIME_CONTROL' " +
                        "AND holder_id=? AND fencing_token=?",
                holderId, fencingToken);
    }

    public List<String> acknowledgedTargets(String changeId) {
        return jdbc.queryForList(
                "SELECT instance_id FROM cpf_runtime_delivery WHERE change_id=? AND delivery_state='ACKED' ORDER BY sequence_no",
                String.class, changeId);
    }

    public List<Map<String,Object>> autoRollbackCandidates() {
        return jdbc.queryForList(
                "SELECT change_id,change_state FROM cpf_runtime_change " +
                        "WHERE change_state IN ('FAILED','EXPIRED') " +
                        "AND rollback_payload_json IS NOT NULL");
    }

    private void assertControllerFence(String holderId, long fencingToken) {
        List<Map<String,Object>> rows = jdbc.queryForList(
                "SELECT holder_id,fencing_token,lease_until FROM cpf_runtime_controller_lease " +
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
        Map<String,Object> row=jdbc.queryForMap("SELECT instance_id,fencing_token,desired_version,actual_version,desired_hash,actual_hash,drift_state,lease_until FROM cpf_runtime_instance_state WHERE instance_id=?",instanceId);
        return new CpfRuntimeInstanceLease(instanceId,number(row.get("fencing_token")),number(row.get("desired_version")),number(row.get("actual_version")),
                nullable(row.get("desired_hash")),nullable(row.get("actual_hash")),String.valueOf(row.get("drift_state")),toInstant(row.get("lease_until")));
    }

    public Map<String,Object> status(String environment,String serviceId) {
        StringBuilder sql=new StringBuilder("SELECT s.instance_id,i.service_id,i.environment_code,i.zone_code,i.cell_code," +
                "s.fencing_token,s.lease_until,s.desired_version,s.actual_version,s.desired_hash,s.actual_hash,s.drift_state," +
                "i.maintenance_yn,i.drain_yn,i.drain_deadline_at,s.heartbeat_at,s.artifact_version,s.artifact_commit," +
                "s.runtime_role,s.registration_source,s.clock_skew_ms " +
                "FROM cpf_runtime_instance_state s JOIN cpf_service_instance i ON i.instance_id=s.instance_id WHERE 1=1");
        ArrayList<Object> args=new ArrayList<>();
        if(environment!=null&&!environment.isBlank()){sql.append(" AND i.environment_code=?");args.add(environment);}
        if(serviceId!=null&&!serviceId.isBlank()){sql.append(" AND i.service_id=?");args.add(serviceId);}
        sql.append(" ORDER BY i.service_id,s.instance_id");
        List<Map<String,Object>> instances=jdbc.queryForList(sql.toString(),args.toArray());

        StringBuilder featureSql=new StringBuilder("SELECT f.instance_id,i.service_id,f.change_type,f.desired_version," +
                "f.actual_version,f.desired_hash,f.actual_hash,f.drift_state,f.source_delivery_id,f.updated_at " +
                "FROM cpf_runtime_instance_feature_state f JOIN cpf_service_instance i ON i.instance_id=f.instance_id WHERE 1=1");
        ArrayList<Object> featureArgs=new ArrayList<>();
        if(environment!=null&&!environment.isBlank()){featureSql.append(" AND i.environment_code=?");featureArgs.add(environment);}
        if(serviceId!=null&&!serviceId.isBlank()){featureSql.append(" AND i.service_id=?");featureArgs.add(serviceId);}
        featureSql.append(" ORDER BY i.service_id,f.instance_id,f.change_type");
        List<Map<String,Object>> featureStates=jdbc.queryForList(featureSql.toString(),featureArgs.toArray());

        long drift=featureStates.stream().filter(r->Set.of("DRIFT","UNKNOWN_RESULT","PENDING_RESTART")
                .contains(String.valueOf(r.get("drift_state")))).count();
        long expired=instances.stream().filter(r->{Instant v=toInstant(r.get("lease_until"));return v!=null&&v.isBefore(Instant.now());}).count();
        List<Map<String,Object>> controller=jdbc.queryForList(
                "SELECT holder_id,fencing_token,lease_until,last_reconciled_at FROM cpf_runtime_controller_lease " +
                        "WHERE lease_key='RUNTIME_CONTROL'");
        List<Map<String,Object>> deliveries=jdbc.queryForList(
                "SELECT delivery_state,COUNT(*) count_value FROM cpf_runtime_delivery GROUP BY delivery_state");
        LinkedHashMap<String,Object> result=new LinkedHashMap<>();
        result.put("instances",instances);
        result.put("featureStates",featureStates);
        result.put("controller",controller.isEmpty()?Map.of():controller.getFirst());
        result.put("deliveryCounts",deliveries);
        result.put("instanceCount",instances.size());
        result.put("driftCount",drift);
        result.put("expiredLeaseCount",expired);
        return Map.copyOf(result);
    }

    public com.cpf.core.api.runtimecontrol.CpfRuntimeControlHealth health(long lagSloSeconds) {
        Instant now=Instant.now();
        int instanceCount=(int)number(jdbc.queryForObject("SELECT COUNT(*) FROM cpf_runtime_instance_state",Long.class));
        int backlog=(int)number(jdbc.queryForObject("SELECT COUNT(*) FROM cpf_runtime_delivery " +
                "WHERE delivery_state IN ('PENDING','FAILED','CLAIMED','RESTART_REQUIRED')",Long.class));
        int poison=(int)number(jdbc.queryForObject("SELECT COUNT(*) FROM cpf_runtime_delivery WHERE delivery_state='POISONED'",Long.class));
        int unknown=(int)number(jdbc.queryForObject("SELECT COUNT(*) FROM cpf_runtime_delivery WHERE delivery_state='UNKNOWN_RESULT'",Long.class));
        int drift=(int)number(jdbc.queryForObject("SELECT COUNT(*) FROM cpf_runtime_instance_feature_state " +
                "WHERE drift_state IN ('DRIFT','UNKNOWN_RESULT','PENDING_RESTART')",Long.class));
        int expired=(int)number(jdbc.queryForObject("SELECT COUNT(*) FROM cpf_runtime_instance_state WHERE lease_until<CURRENT_TIMESTAMP",Long.class));
        List<Map<String,Object>> oldestRows=jdbc.queryForList(
                "SELECT created_at FROM cpf_runtime_delivery WHERE delivery_state IN ('PENDING','FAILED','CLAIMED','RESTART_REQUIRED') ORDER BY created_at");
        long lag=0L;
        if(!oldestRows.isEmpty()){
            Instant oldest=toInstant(oldestRows.getFirst().get("created_at"));
            if(oldest!=null)lag=Math.max(0L,java.time.Duration.between(oldest,now).getSeconds());
        }
        List<Map<String,Object>> controllerRows=jdbc.queryForList(
                "SELECT holder_id,fencing_token,lease_until FROM cpf_runtime_controller_lease WHERE lease_key='RUNTIME_CONTROL'");
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
        return new com.cpf.core.api.runtimecontrol.CpfRuntimeControlHealth(
                ready,healthStatus,controllerId,controllerFence,instanceCount,backlog,poison,unknown,drift,expired,
                lag,lag>lagSloSeconds,reasons,now);
    }

    public Map<String,Object> previewTargets(String changeType,int payloadSchemaVersion,CpfRuntimeTargetSelector selector){
        List<String> base=resolveTargets(selector);
        LinkedHashSet<String> excluded=new LinkedHashSet<>(selector.excludeInstanceIds());
        ArrayList<Map<String,Object>> rows=new ArrayList<>();
        int eligible=0;
        for(String instanceId:base){
            List<Map<String,Object>> meta=jdbc.queryForList(
                    "SELECT i.service_id,i.environment_code,i.zone_code,i.cell_code,i.maintenance_yn,i.drain_yn," +
                            "s.capabilities_json,s.artifact_version,s.artifact_commit,s.runtime_role,s.lease_until " +
                            "FROM cpf_service_instance i JOIN cpf_runtime_instance_state s ON s.instance_id=i.instance_id " +
                            "WHERE i.instance_id=?",instanceId);
            if(meta.isEmpty())continue;
            Map<String,Object> row=new LinkedHashMap<>(meta.getFirst());
            Map<String,Object> caps=readMapOrEmpty(nullable(row.get("capabilities_json")));
            Object encoded=caps.get(baseChangeType(changeType));
            boolean schemaSupported=supportsCapability(instanceId,changeType,payloadSchemaVersion);
            boolean manuallyExcluded=excluded.contains(instanceId);
            boolean isEligible=schemaSupported&&!manuallyExcluded;
            if(isEligible)eligible++;
            row.put("instanceId",instanceId);
            row.put("capability",encoded);
            row.put("schemaSupported",schemaSupported);
            row.put("excluded",manuallyExcluded);
            row.put("eligible",isEligible);
            row.remove("capabilities_json");
            rows.add(row);
        }
        boolean broad=blank(selector.environment()).isBlank()&&blank(selector.serviceId()).isBlank()
                &&blank(selector.groupId()).isBlank()&&selector.instanceIds().isEmpty()
                &&selector.labels().isEmpty()&&blank(selector.zone()).isBlank()&&blank(selector.cell()).isBlank();
        LinkedHashMap<String,Object> result=new LinkedHashMap<>();
        result.put("changeType",baseChangeType(changeType));
        result.put("payloadSchemaVersion",payloadSchemaVersion);
        result.put("overbroad",broad&&!selector.allowAll());
        result.put("candidateCount",base.size());
        result.put("eligibleCount",eligible);
        result.put("targets",List.copyOf(rows));
        return Map.copyOf(result);
    }

    public List<Map<String,Object>> featureStates(List<String> instanceIds,String changeType){
        ArrayList<Map<String,Object>> result=new ArrayList<>();
        for(String instanceId:instanceIds){
            List<Map<String,Object>> rows=jdbc.queryForList(
                    "SELECT instance_id,change_type,desired_version,actual_version,desired_hash,actual_hash," +
                            "drift_state,source_delivery_id,updated_at FROM cpf_runtime_instance_feature_state " +
                            "WHERE instance_id=? AND change_type=?",instanceId,baseChangeType(changeType));
            if(rows.isEmpty()){
                LinkedHashMap<String,Object> empty=new LinkedHashMap<>();
                empty.put("instance_id",instanceId);empty.put("change_type",baseChangeType(changeType));
                empty.put("desired_version",0L);empty.put("actual_version",0L);empty.put("desired_hash",null);
                empty.put("actual_hash",null);empty.put("drift_state","UNKNOWN");empty.put("source_delivery_id",null);
                result.add(empty);
            }else result.add(new LinkedHashMap<>(rows.getFirst()));
        }
        return List.copyOf(result);
    }

    public Map<String,Number> deliveryCounts(String changeId) {
        List<Map<String,Object>> rows=jdbc.queryForList("SELECT delivery_state,COUNT(*) cnt FROM cpf_runtime_delivery WHERE change_id=? GROUP BY delivery_state",changeId);
        LinkedHashMap<String,Number> result=new LinkedHashMap<>(); rows.forEach(r->result.put(String.valueOf(r.get("delivery_state")),(Number)r.get("cnt"))); return result;
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
        jdbc.update("UPDATE cpf_runtime_change SET change_state=?,updated_at=CURRENT_TIMESTAMP " +
                        "WHERE change_id=? AND change_state NOT IN ('CANCELLED','ROLLBACK_PENDING','ROLLED_BACK','EXPIRED','SUPERSEDED')",
                state, changeId);
    }

    private void assertFence(String instanceId,long fencingToken) {
        List<Map<String,Object>> rows=jdbc.queryForList("SELECT fencing_token,lease_until FROM cpf_runtime_instance_state WHERE instance_id=?",instanceId);
        if(rows.isEmpty()) throw new CpfRuntimeFenceException("등록되지 않은 Runtime instance입니다: "+instanceId);
        Map<String,Object> row=rows.getFirst(); Instant until=toInstant(row.get("lease_until"));
        if(((Number)row.get("fencing_token")).longValue()!=fencingToken || until==null || until.isBefore(Instant.now()))
            throw new CpfRuntimeFenceException("Runtime fencing token 또는 lease가 유효하지 않습니다: "+instanceId);
    }


    public Map<String,Object> saveGroup(String groupId,String groupName,String parentGroupId,String environment,String description,
                                        Long expectedVersion,boolean active,String operatorId) {
        requireText(groupId,"groupId"); requireText(groupName,"groupName");
        if (groupId.equals(parentGroupId)) throw new IllegalArgumentException("Runtime Group은 자기 자신을 parent로 지정할 수 없습니다.");
        if (parentGroupId!=null && !parentGroupId.isBlank()) assertNoGroupCycle(groupId,parentGroupId);
        List<Map<String,Object>> rows=jdbc.queryForList("SELECT row_version FROM cpf_runtime_instance_group WHERE group_id=? FOR UPDATE",groupId);
        if(rows.isEmpty()) {
            if(expectedVersion!=null && expectedVersion!=0L) throw new CpfRuntimeVersionConflictException(expectedVersion,0L);
            jdbc.update("INSERT INTO cpf_runtime_instance_group(group_id,group_name,parent_group_id,environment_code,description,active_yn,row_version,created_by,updated_by) VALUES (?,?,?,?,?,?,0,?,?)",
                    groupId,groupName,emptyToNull(parentGroupId),emptyToNull(environment),emptyToNull(description),active?"Y":"N",operatorId,operatorId);
        } else {
            long current=((Number)rows.getFirst().get("row_version")).longValue();
            if(expectedVersion==null || expectedVersion.longValue()!=current) throw new CpfRuntimeVersionConflictException(expectedVersion==null?-1L:expectedVersion,current);
            int updated=jdbc.update("UPDATE cpf_runtime_instance_group SET group_name=?,parent_group_id=?,environment_code=?,description=?,active_yn=?,row_version=row_version+1,updated_by=?,updated_at=CURRENT_TIMESTAMP WHERE group_id=? AND row_version=?",
                    groupName,emptyToNull(parentGroupId),emptyToNull(environment),emptyToNull(description),active?"Y":"N",operatorId,groupId,current);
            if(updated!=1) throw new CpfRuntimeVersionConflictException(current,current);
        }
        return findGroup(groupId).orElseThrow();
    }

    public Optional<Map<String,Object>> findGroup(String groupId) {
        List<Map<String,Object>> rows=jdbc.queryForList("SELECT group_id,group_name,parent_group_id,environment_code,description,active_yn,row_version,created_at,updated_at FROM cpf_runtime_instance_group WHERE group_id=?",groupId);
        if(rows.isEmpty()) return Optional.empty();
        Map<String,Object> result=new LinkedHashMap<>(rows.getFirst());
        List<String> members=jdbc.queryForList("SELECT instance_id FROM cpf_runtime_group_member WHERE group_id=? AND active_yn='Y' ORDER BY instance_id",String.class,groupId);
        result.put("instance_ids",members);
        return Optional.of(result);
    }

    public Map<String,Object> changeGroupMember(String groupId,String instanceId,boolean active,String operatorId) {
        requireText(groupId,"groupId"); requireText(instanceId,"instanceId");
        if(findGroup(groupId).isEmpty()) throw new IllegalArgumentException("Runtime Group을 찾을 수 없습니다: "+groupId);
        Integer instanceCount=jdbc.queryForObject("SELECT COUNT(*) FROM cpf_service_instance WHERE instance_id=?",Integer.class,instanceId);
        if(instanceCount==null || instanceCount==0) throw new IllegalArgumentException("Runtime Instance를 찾을 수 없습니다: "+instanceId);
        int updated=jdbc.update("UPDATE cpf_runtime_group_member SET active_yn=?,updated_by=?,updated_at=CURRENT_TIMESTAMP WHERE group_id=? AND instance_id=?",active?"Y":"N",operatorId,groupId,instanceId);
        if(updated==0) {
            try { jdbc.update("INSERT INTO cpf_runtime_group_member(group_id,instance_id,active_yn,created_by,updated_by) VALUES (?,?,?,?,?)",groupId,instanceId,active?"Y":"N",operatorId,operatorId); }
            catch(DuplicateKeyException duplicate){ return changeGroupMember(groupId,instanceId,active,operatorId); }
        }
        return findGroup(groupId).orElseThrow();
    }

    public void deleteGroup(String groupId,Long expectedVersion,String operatorId) {
        List<Map<String,Object>> rows=jdbc.queryForList("SELECT row_version FROM cpf_runtime_instance_group WHERE group_id=? FOR UPDATE",groupId);
        if(rows.isEmpty()) return;
        long current=((Number)rows.getFirst().get("row_version")).longValue();
        if(expectedVersion==null || expectedVersion.longValue()!=current) throw new CpfRuntimeVersionConflictException(expectedVersion==null?-1L:expectedVersion,current);
        Integer children=jdbc.queryForObject("SELECT COUNT(*) FROM cpf_runtime_instance_group WHERE parent_group_id=? AND active_yn='Y'",Integer.class,groupId);
        if(children!=null && children>0) throw new IllegalStateException("활성 child Runtime Group이 있어 삭제할 수 없습니다: "+groupId);
        jdbc.update("DELETE FROM cpf_runtime_group_member WHERE group_id=?",groupId);
        if(jdbc.update("DELETE FROM cpf_runtime_instance_group WHERE group_id=? AND row_version=?",groupId,current)!=1) throw new CpfRuntimeVersionConflictException(current,current);
    }

    private void assertNoGroupCycle(String groupId,String parentGroupId) {
        LinkedHashSet<String> seen=new LinkedHashSet<>(); String current=parentGroupId;
        while(current!=null && !current.isBlank()) {
            if(groupId.equals(current)) throw new IllegalArgumentException("Runtime Group parent cycle이 탐지되었습니다: "+groupId);
            if(!seen.add(current) || seen.size()>1000) throw new IllegalArgumentException("Runtime Group parent cycle/깊이 오류가 탐지되었습니다.");
            List<Map<String,Object>> rows=jdbc.queryForList("SELECT parent_group_id FROM cpf_runtime_instance_group WHERE group_id=?",current);
            current=rows.isEmpty()?null:nullable(rows.getFirst().get("parent_group_id"));
        }
    }

    private void ensureServiceAndEndpoint(CpfRuntimeInstanceRegistration r) {
        Integer serviceCount=jdbc.queryForObject("SELECT COUNT(*) FROM cpf_service WHERE service_id=? AND use_yn='Y'",Integer.class,r.serviceId());
        if(serviceCount==null || serviceCount!=1) throw new IllegalStateException("Runtime Agent service가 중앙 Registry에 등록되어 있지 않습니다: "+r.serviceId());
        Integer endpointCount=jdbc.queryForObject("SELECT COUNT(*) FROM cpf_service_endpoint WHERE endpoint_code=? AND service_id=? AND use_yn='Y'",Integer.class,r.endpointCode(),r.serviceId());
        if(endpointCount==null || endpointCount!=1) throw new IllegalStateException("Runtime Agent endpoint가 중앙 Registry에 등록되어 있지 않습니다: "+r.serviceId()+"/"+r.endpointCode());
    }

    private void upsertServiceInstance(CpfRuntimeInstanceRegistration r) {
        int updated=jdbc.update("UPDATE cpf_service_instance SET service_id=?,endpoint_code=?,instance_name=?,base_url=?,environment_code=?,zone_code=?,cell_code=?,instance_status='UP',active_yn='Y',last_heartbeat_at=CURRENT_TIMESTAMP,updated_at=CURRENT_TIMESTAMP " +
                "WHERE instance_id=?",r.serviceId(),r.endpointCode(),r.instanceId(),r.baseUrl(),blank(r.environment()),blank(r.zone()),blank(r.cell()),r.instanceId());
        if(updated==0) {
            try { jdbc.update("INSERT INTO cpf_service_instance(instance_id,service_id,endpoint_code,instance_name,base_url,environment_code,zone_code,cell_code,instance_status,weight,priority_no,active_yn,maintenance_yn,drain_yn,last_heartbeat_at,created_by,updated_by) " +
                            "VALUES (?,?,?,?,?,?,?,?,'UP',100,100,'Y','N','N',CURRENT_TIMESTAMP,'CPF','CPF')",r.instanceId(),r.serviceId(),r.endpointCode(),r.instanceId(),r.baseUrl(),blank(r.environment()),blank(r.zone()),blank(r.cell())); }
            catch(DuplicateKeyException duplicate){upsertServiceInstance(r);}
        }
    }

    public com.cpf.core.api.runtimecontrol.CpfRuntimeAuditVerification verifyAudit(String changeId) {
        requireText(changeId, "changeId");
        List<Map<String,Object>> rows = jdbc.queryForList(
                "SELECT audit_id,event_type,actor_id,reason,evidence_hash,previous_hash,chain_hash,created_at " +
                        "FROM cpf_runtime_change_audit WHERE change_id=? ORDER BY audit_id", changeId);
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
                return new com.cpf.core.api.runtimecontrol.CpfRuntimeAuditVerification(
                        changeId, false, count - 1L, ((Number) row.get("audit_id")).longValue(),
                        expected, actual, "Runtime Change audit hash-chain 변조 또는 불일치가 탐지되었습니다.");
            }
            previous = actual;
        }
        return new com.cpf.core.api.runtimecontrol.CpfRuntimeAuditVerification(
                changeId, true, count, null, null, null, "VALID");
    }

    private void appendAudit(String changeId,String eventType,String actor,String reason,String evidenceHash) {
        // 같은 Change의 audit append를 직렬화해 hash-chain fork를 방지합니다.
        jdbc.queryForList("SELECT change_id FROM cpf_runtime_change WHERE change_id=? FOR UPDATE", changeId);
        List<Map<String,Object>> rows=jdbc.queryForList(
                "SELECT chain_hash FROM cpf_runtime_change_audit WHERE change_id=? ORDER BY audit_id",changeId);
        String previous=rows.isEmpty()?"GENESIS":String.valueOf(rows.getLast().get("chain_hash"));
        Instant eventAt=Instant.now();
        String current=CpfRuntimeCanonicalHash.sha256(Map.of(
                "previous",previous,"changeId",changeId,"eventType",blank(eventType),
                "actor",blank(actor),"reason",blank(reason),"evidenceHash",blank(evidenceHash),
                "at",eventAt.toString()));
        jdbc.update("INSERT INTO cpf_runtime_change_audit(change_id,event_type,actor_id,reason,evidence_hash," +
                        "previous_hash,chain_hash,created_by,created_at) VALUES (?,?,?,?,?,?,?,'CPF',?)",
                changeId,blank(eventType),blank(actor),blank(reason),blank(evidenceHash),previous,current,ts(eventAt));
    }

    private String write(Object value){try{return objectMapper.writeValueAsString(value);}catch(Exception ex){throw new IllegalArgumentException("Runtime JSON 직렬화 실패",ex);}}
    private Map<String,Object> readMap(String json){try{return objectMapper.readValue(json,new TypeReference<>(){});}catch(Exception ex){throw new IllegalStateException("Runtime payload JSON 역직렬화 실패",ex);}}
    private Map<String,Object> readMapOrEmpty(String json){if(json==null||json.isBlank())return Map.of();Map<String,Object> value=readMap(json);return value==null?Map.of():value;}
    public String json(Object value){return write(value);}
    public Map<String,Object> jsonMap(String value){return readMap(value);}
    private Timestamp ts(Instant value){return value==null?null:Timestamp.from(value);}
    private Instant toInstant(Object value){if(value==null)return null;if(value instanceof Timestamp t)return t.toInstant();if(value instanceof java.util.Date d)return d.toInstant();try{return Instant.parse(String.valueOf(value));}catch(Exception ignored){return null;}}
    private long number(Object value){return value==null?0L:((Number)value).longValue();}
    private String nullable(Object value){return value==null?null:String.valueOf(value);}
    private String baseChangeType(String value) {
        String type = blank(value).trim().toUpperCase();
        return type.startsWith("ROLLBACK:") ? type.substring("ROLLBACK:".length()) : type;
    }
    private String blank(String value){return value==null?"":value;}
    private String emptyToNull(String value){return value==null||value.isBlank()?null:value.trim();}
    private String truncate(String value,int max){if(value==null)return null;return value.length()>max?value.substring(0,max):value;}
    private void requireText(String value,String name){if(value==null||value.isBlank())throw new IllegalArgumentException(name+"가 필요합니다.");}
}
