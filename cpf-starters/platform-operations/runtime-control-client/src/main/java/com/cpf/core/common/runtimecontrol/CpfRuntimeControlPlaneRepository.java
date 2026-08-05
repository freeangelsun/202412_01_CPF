package com.cpf.core.common.runtimecontrol;

import com.cpf.core.api.runtimecontrol.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

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
    static final String INSERT_CHANGE_SQL = "INSERT INTO cpf_runtime_change " +
            "(change_id,operation_id,change_type,payload_schema_version,request_hash,payload_hash,payload_json," +
            "rollback_payload_json,target_snapshot_json,desired_version,rollout_mode,wave_size,quorum_percent," +
            "change_state,scheduled_at,expires_at,reason,approval_id,break_glass_id,requested_by,created_by,updated_by) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    static final String APPLY_INSTANCE_DESIRED_SQL =
            "UPDATE cpf_runtime_instance_state SET desired_version=?,desired_hash=?," +
            "drift_state=CASE WHEN actual_version=? AND COALESCE(actual_hash,'')=COALESCE(?,'') " +
            "THEN 'IN_SYNC' ELSE 'PENDING' END,updated_at=CURRENT_TIMESTAMP,updated_by=? " +
            "WHERE instance_id=? AND desired_version<=?";
    static final String APPLY_FEATURE_DESIRED_SQL =
            "UPDATE cpf_runtime_instance_feature_state SET desired_version=?,desired_hash=?," +
            "drift_state=CASE WHEN actual_version=? AND COALESCE(actual_hash,'')=COALESCE(?,'') " +
            "THEN 'IN_SYNC' ELSE 'PENDING' END,updated_by=?,updated_at=CURRENT_TIMESTAMP " +
            "WHERE instance_id=? AND change_type=? AND desired_version<=?";
    static final String HEALTH_OLDEST_BACKLOG_SQL =
            "SELECT MIN(created_at) FROM cpf_runtime_delivery WHERE delivery_state IN ('PENDING','FAILED','CLAIMED','RESTART_REQUIRED')";

    static final String CLAIM_DELIVERY_SQL =
            "UPDATE cpf_runtime_delivery SET delivery_state='CLAIMED',attempt_no=attempt_no+1," +
            "fencing_token=?,claimed_at=CURRENT_TIMESTAMP,error_code=NULL,error_message=NULL," +
            "updated_at=CURRENT_TIMESTAMP WHERE delivery_id=? AND instance_id=? " +
            "AND delivery_state IN ('PENDING','FAILED') AND EXISTS (" +
            "SELECT 1 FROM cpf_runtime_change c WHERE c.change_id=cpf_runtime_delivery.change_id " +
            "AND c.change_state IN ('APPLYING','PARTIAL') " +
            "AND (c.scheduled_at IS NULL OR c.scheduled_at<=CURRENT_TIMESTAMP) " +
            "AND (c.expires_at IS NULL OR c.expires_at>CURRENT_TIMESTAMP)) " +
            "AND EXISTS (SELECT 1 FROM cpf_runtime_instance_state s " +
            "WHERE s.instance_id=cpf_runtime_delivery.instance_id AND s.fencing_token=? " +
            "AND s.lease_until>CURRENT_TIMESTAMP)";
    static final String ACK_DELIVERY_SQL =
            "UPDATE cpf_runtime_delivery SET delivery_state=?,actual_hash=?,error_code=?,error_message=?," +
            "acknowledged_at=?,updated_at=CURRENT_TIMESTAMP " +
            "WHERE delivery_id=? AND change_id=? AND instance_id=? AND fencing_token=? " +
            "AND attempt_no=? AND delivery_state='CLAIMED' " +
            "AND EXISTS (SELECT 1 FROM cpf_runtime_instance_state s " +
            "WHERE s.instance_id=cpf_runtime_delivery.instance_id AND s.fencing_token=? " +
            "AND s.lease_until>CURRENT_TIMESTAMP)";
    static final String CLAIM_CANDIDATE_SQL =
            "SELECT d.delivery_id,d.change_id,d.instance_id,d.sequence_no,d.desired_version,d.attempt_no," +
            "c.change_type,c.payload_schema_version,c.request_hash,c.payload_hash,c.payload_json,c.expires_at," +
            "c.rollout_mode,c.wave_size,c.quorum_percent " +
            "FROM cpf_runtime_delivery d JOIN cpf_runtime_change c ON c.change_id=d.change_id " +
            "WHERE d.instance_id=? AND d.delivery_state IN ('PENDING','FAILED') " +
            "AND d.next_attempt_at<=CURRENT_TIMESTAMP AND c.change_state IN ('APPLYING','PARTIAL') " +
            "AND (c.scheduled_at IS NULL OR c.scheduled_at<=CURRENT_TIMESTAMP) " +
            "AND (c.expires_at IS NULL OR c.expires_at>CURRENT_TIMESTAMP) " +
            "AND NOT EXISTS (SELECT 1 FROM cpf_runtime_delivery older " +
            "WHERE older.instance_id=d.instance_id AND older.desired_version<d.desired_version " +
            "AND older.delivery_state NOT IN ('ACKED','CANCELLED','EXPIRED','SUPERSEDED')) " +
            "ORDER BY d.desired_version,d.sequence_no,d.created_at";

    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper;
    private final java.util.function.Supplier<Instant> serverClock;

    public CpfRuntimeControlPlaneRepository(
            @Qualifier("cpfJdbcTemplate") ObjectProvider<JdbcTemplate> jdbcProvider,
            ObjectMapper objectMapper) {
        this.jdbc = jdbcProvider.getIfAvailable();
        if (this.jdbc == null) throw new IllegalStateException("Runtime Control Plane에는 cpfJdbcTemplate이 필요합니다.");
        this.objectMapper = java.util.Objects.requireNonNull(objectMapper, "objectMapper");
        this.serverClock = this::queryDatabaseNow;
    }

    CpfRuntimeControlPlaneRepository(JdbcTemplate jdbc, ObjectMapper objectMapper) {
        this(jdbc, objectMapper, java.time.Clock.systemUTC()::instant);
    }

    CpfRuntimeControlPlaneRepository(
            JdbcTemplate jdbc,
            ObjectMapper objectMapper,
            java.util.function.Supplier<Instant> serverClock) {
        this.jdbc = java.util.Objects.requireNonNull(jdbc, "jdbc");
        this.objectMapper = java.util.Objects.requireNonNull(objectMapper, "objectMapper");
        this.serverClock = java.util.Objects.requireNonNull(serverClock, "serverClock");
    }

    Optional<Map<String, Object>> findOperation(String operationId) {
        expireControlOperation(operationId);
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT operation_id, command_type, request_hash, entity_id, result_state, result_json, expires_at " +
                        "FROM cpf_control_operation WHERE operation_id=?", operationId);
        return rows.stream().findFirst();
    }

    public void consumeRateLimit(String subjectId, int limitPerMinute) {
        requireText(subjectId, "subjectId");
        int limit = Math.max(1, Math.min(10_000, limitPerMinute));
        Instant now = serverNow();
        String minute = java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmm")
                .withZone(java.time.ZoneOffset.UTC).format(now);
        String bucket = subjectId + ":" + minute;
        int updated = jdbc.update("UPDATE cpf_runtime_rate_bucket SET request_count=request_count+1," +
                        "updated_at=CURRENT_TIMESTAMP WHERE bucket_key=? AND request_count<?",
                bucket, limit);
        if (updated == 1) return;
        if (tryInsertUnique("INSERT INTO cpf_runtime_rate_bucket " +
                        "(bucket_key,subject_id,window_start,request_count,created_by,updated_by) " +
                        "VALUES (?,?,?,1,'CPF','CPF')",
                bucket, subjectId, ts(now.truncatedTo(java.time.temporal.ChronoUnit.MINUTES)))) {
            return;
        }
        // 다른 노드가 같은 minute bucket을 먼저 생성한 경우 count 존재만으로 제한 초과로
        // 오판하지 않고, 동일한 request_count<limit 조건으로 한 번 더 원자 증가합니다.
        int racedUpdate = jdbc.update("UPDATE cpf_runtime_rate_bucket SET request_count=request_count+1," +
                        "updated_at=CURRENT_TIMESTAMP WHERE bucket_key=? AND request_count<?",
                bucket, limit);
        if (racedUpdate != 1) {
            throw new com.cpf.core.api.runtimecontrol.CpfRuntimeRateLimitException(limit);
        }
    }

    public boolean insertOperation(String operationId, String commandType, String requestHash, Instant expiresAt) {
        return tryInsertUnique("INSERT INTO cpf_control_operation " +
                        "(operation_id, command_type, request_hash, result_state, expires_at, created_by, updated_by) " +
                        "VALUES (?,?,?,?,?,?,?)",
                operationId, commandType, requestHash, "PROCESSING", ts(expiresAt), "CPF", "CPF");
    }

    public void completeOperation(String operationId, String entityId, String state, String resultJson) {
        String normalizedState = blank(state).trim().toUpperCase();
        if (!Set.of("PROCESSING", "SUCCESS", "FAILED", "UNKNOWN", "EXPIRED", "CANCELLED")
                .contains(normalizedState)) {
            throw new IllegalArgumentException("지원하지 않는 Control operation result_state: " + state);
        }
        int updated = jdbc.update("UPDATE cpf_control_operation SET entity_id=?, result_state=?, result_json=?, updated_at=CURRENT_TIMESTAMP " +
                        "WHERE operation_id=? AND request_hash IS NOT NULL AND result_state='PROCESSING'",
                entityId, normalizedState, resultJson, operationId);
        if (updated == 1) return;
        Map<String,Object> current = findOperation(operationId).orElseThrow(
                () -> new IllegalStateException("operation 완료 상태 갱신 대상을 찾을 수 없습니다: " + operationId));
        boolean idempotentTerminal = normalizedState.equals(String.valueOf(current.get("result_state")))
                && java.util.Objects.equals(entityId, nullable(current.get("entity_id")));
        if (!idempotentTerminal) {
            throw new IllegalStateException("operation terminal 상태를 덮어쓸 수 없습니다: " + operationId
                    + ", current=" + current.get("result_state") + ", requested=" + normalizedState);
        }
    }

    private void expireControlOperation(String operationId) {
        jdbc.update("UPDATE cpf_control_operation SET result_state='EXPIRED',updated_at=CURRENT_TIMESTAMP " +
                        "WHERE operation_id=? AND result_state='PROCESSING' AND expires_at IS NOT NULL " +
                        "AND expires_at<=CURRENT_TIMESTAMP",
                operationId);
    }

    private void expireControlOperations() {
        jdbc.update("UPDATE cpf_control_operation SET result_state='EXPIRED',updated_at=CURRENT_TIMESTAMP " +
                "WHERE result_state='PROCESSING' AND expires_at IS NOT NULL AND expires_at<=CURRENT_TIMESTAMP");
    }

    public long lockAndNextVersion(Long expectedVersion) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT version_no FROM cpf_runtime_version WHERE version_key='GLOBAL' FOR UPDATE");
        if (rows.isEmpty()) {
            tryInsertUnique("INSERT INTO cpf_runtime_version " +
                    "(version_key, version_no, created_by, updated_by) VALUES ('GLOBAL',0,'CPF','CPF')");
            rows = jdbc.queryForList("SELECT version_no FROM cpf_runtime_version WHERE version_key='GLOBAL' FOR UPDATE");
            if (rows.isEmpty()) {
                throw new IllegalStateException("Runtime global version row를 생성하거나 잠글 수 없습니다.");
            }
        }
        long current = ((Number) rows.getFirst().get("version_no")).longValue();
        if (expectedVersion != null && expectedVersion.longValue() != current) {
            throw new com.cpf.core.api.runtimecontrol.CpfRuntimeVersionConflictException(expectedVersion, current);
        }
        long next = nextMonotonic(current,"Runtime global version");
        if (jdbc.update("UPDATE cpf_runtime_version SET version_no=?, updated_at=CURRENT_TIMESTAMP, updated_by='CPF' " +
                "WHERE version_key='GLOBAL' AND version_no=?", next, current) != 1) {
            throw new com.cpf.core.api.runtimecontrol.CpfRuntimeVersionConflictException(current, current);
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
                    "JOIN cpf_runtime_instance_state s ON s.instance_id=i.instance_id " +
                    "WHERE i.active_yn='Y' AND s.lease_until>CURRENT_TIMESTAMP");
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
                        "WHERE i.instance_id=? AND i.active_yn='Y' AND s.lease_until>CURRENT_TIMESTAMP", instanceId);
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
        Integer activeRoot = jdbc.queryForObject(
                "SELECT COUNT(*) FROM cpf_runtime_instance_group WHERE group_id=? AND active_yn='Y'",
                Integer.class, rootGroupId);
        if (activeRoot == null || activeRoot == 0) {
            throw new IllegalArgumentException("활성 Runtime Group을 찾을 수 없습니다: " + rootGroupId);
        }
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
        String state = scheduledAt != null && scheduledAt.isAfter(serverNow()) ? "SCHEDULED" : "APPLYING";
        jdbc.update(INSERT_CHANGE_SQL,
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
            if ("APPLYING".equals(state)) {
                applyDesiredState(instanceId, type, version, payloadHash, requestedBy);
            }
        }
        appendAudit(changeId, "CHANGE_CREATED", requestedBy, reason, requestHash);
    }

    /** 예약 시각 전에는 future desired state가 현재 drift/health를 오염시키지 않도록 활성화 시점에 반영합니다. */
    private void applyDesiredState(String instanceId, String type, long version, String payloadHash, String actor) {
        int instanceUpdated = jdbc.update(
                APPLY_INSTANCE_DESIRED_SQL,
                version, payloadHash, version, payloadHash, actor, instanceId, version);
        if (instanceUpdated == 0) {
            List<Map<String,Object>> current = jdbc.queryForList(
                    "SELECT desired_version,desired_hash FROM cpf_runtime_instance_state WHERE instance_id=?",
                    instanceId);
            if (current.isEmpty()) {
                throw new IllegalStateException("Runtime desired state 대상 instance를 찾을 수 없습니다: " + instanceId);
            }
            long currentVersion = number(current.getFirst().get("desired_version"));
            String currentHash = nullable(current.getFirst().get("desired_hash"));
            if (currentVersion == version && java.util.Objects.equals(currentHash, payloadHash)) {
                // 일부 Driver는 동일 값 UPDATE를 0건으로 반환합니다. 동일 version/hash는 멱등 성공입니다.
            } else if (currentVersion <= version) {
                throw new IllegalStateException("Runtime instance desired state 조건부 갱신 실패: " + instanceId);
            }
            // 더 최신 desired가 이미 활성화됐으면 예약된 이전 version이 이를 역행시키지 않습니다.
        }

        String changeType = baseChangeType(type);
        int featureUpdated = jdbc.update(
                APPLY_FEATURE_DESIRED_SQL,
                version, payloadHash, version, payloadHash, actor, instanceId, changeType, version);
        if (featureUpdated == 1) return;

        List<Map<String,Object>> currentFeature = jdbc.queryForList(
                "SELECT desired_version,desired_hash FROM cpf_runtime_instance_feature_state " +
                        "WHERE instance_id=? AND change_type=?",
                instanceId, changeType);
        if (!currentFeature.isEmpty()) {
            long currentVersion = number(currentFeature.getFirst().get("desired_version"));
            String currentHash = nullable(currentFeature.getFirst().get("desired_hash"));
            if (currentVersion > version) return;
            if (currentVersion == version && java.util.Objects.equals(currentHash, payloadHash)) return;
            int racedUpdate = jdbc.update(
                    APPLY_FEATURE_DESIRED_SQL,
                    version, payloadHash, version, payloadHash, actor, instanceId, changeType, version);
            if (racedUpdate == 1) return;
            throw new IllegalStateException("Runtime feature desired state 조건부 갱신 실패: " + instanceId);
        }

        if (tryInsertUnique(
                "INSERT INTO cpf_runtime_instance_feature_state " +
                        "(instance_id,change_type,desired_version,actual_version,desired_hash,actual_hash,drift_state,created_by,updated_by) " +
                        "VALUES (?,?,?,0,?,NULL,'PENDING',?,?)",
                instanceId, changeType, version, payloadHash, actor, actor)) {
            return;
        }
        int racedFeatureUpdate = jdbc.update(
                APPLY_FEATURE_DESIRED_SQL,
                version, payloadHash, version, payloadHash, actor, instanceId, changeType, version);
        if (racedFeatureUpdate != 1) {
            List<Map<String,Object>> winner = jdbc.queryForList(
                    "SELECT desired_version,desired_hash FROM cpf_runtime_instance_feature_state " +
                            "WHERE instance_id=? AND change_type=?",
                    instanceId, changeType);
            if (winner.isEmpty()) {
                throw new IllegalStateException("Runtime feature desired state 동시 갱신 실패: " + instanceId);
            }
            long winnerVersion = number(winner.getFirst().get("desired_version"));
            String winnerHash = nullable(winner.getFirst().get("desired_hash"));
            if (winnerVersion < version
                    || (winnerVersion == version && !java.util.Objects.equals(winnerHash, payloadHash))) {
                throw new IllegalStateException("Runtime feature desired state 동시 갱신 실패: " + instanceId);
            }
        }
    }

    Optional<Map<String,Object>> findChange(String column, String value) {
        if (!"change_id".equals(column) && !"operation_id".equals(column)) throw new IllegalArgumentException("unsupported column");
        List<Map<String,Object>> rows=jdbc.queryForList("SELECT * FROM cpf_runtime_change WHERE "+column+"=?", value);
        return rows.stream().findFirst();
    }

    public List<CpfRuntimeDelivery> claim(String instanceId, long fencingToken, int requestedLimit) {
        assertFence(instanceId, fencingToken);
        reconcileTemporalChanges();
        int limit = Math.max(1, Math.min(100, requestedLimit));
        List<Map<String,Object>> rows = jdbc.queryForList(CLAIM_CANDIDATE_SQL, instanceId);
        ArrayList<CpfRuntimeDelivery> claimed = new ArrayList<>();
        for (Map<String,Object> row : rows) {
            if (claimed.size() >= limit) break;
            if (!isWaveOpen(row)) continue;
            String deliveryId = String.valueOf(row.get("delivery_id"));
            int updated = jdbc.update(CLAIM_DELIVERY_SQL, fencingToken, deliveryId, instanceId, fencingToken);
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
        Instant healthNow = serverNow();
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
                        && until != null && until.isAfter(healthNow)) healthy++;
            }
        }
        return healthy * 100 >= prior.size() * quorum;
    }

    private void reconcileTemporalChanges() {
        List<Map<String,Object>> due = jdbc.queryForList(
                "SELECT change_id,change_type,desired_version,payload_hash,requested_by " +
                        "FROM cpf_runtime_change WHERE change_state='SCHEDULED' " +
                        "AND scheduled_at<=CURRENT_TIMESTAMP " +
                        "AND (expires_at IS NULL OR expires_at>CURRENT_TIMESTAMP) " +
                        "ORDER BY desired_version,change_id FOR UPDATE");
        for (Map<String,Object> row : due) {
            String changeId = String.valueOf(row.get("change_id"));
            int activated = jdbc.update(
                    "UPDATE cpf_runtime_change SET change_state='APPLYING',updated_at=CURRENT_TIMESTAMP " +
                            "WHERE change_id=? AND change_state='SCHEDULED'",
                    changeId);
            if (activated != 1) continue;
            List<String> targets = jdbc.queryForList(
                    "SELECT instance_id FROM cpf_runtime_delivery WHERE change_id=? ORDER BY sequence_no",
                    String.class, changeId);
            for (String instanceId : targets) {
                applyDesiredState(instanceId, String.valueOf(row.get("change_type")),
                        number(row.get("desired_version")), String.valueOf(row.get("payload_hash")),
                        nullable(row.get("requested_by")) == null ? "CPF" : nullable(row.get("requested_by")));
            }
            appendAudit(changeId, "SCHEDULED_CHANGE_ACTIVATED", "CPF_CONTROLLER",
                    "Scheduled Runtime Change reached activation time", null);
        }

        List<Map<String,Object>> expired = jdbc.queryForList(
                "SELECT change_id FROM cpf_runtime_change " +
                        "WHERE change_state IN ('SCHEDULED','APPLYING','PARTIAL') " +
                        "AND expires_at<=CURRENT_TIMESTAMP ORDER BY change_id FOR UPDATE");
        for (Map<String,Object> row : expired) {
            String changeId = String.valueOf(row.get("change_id"));
            int acknowledged = countDeliveries(changeId, "ACKED");
            int claimed = countDeliveries(changeId, "CLAIMED");
            int restartRequired = countDeliveries(changeId, "RESTART_REQUIRED");
            String terminal = deriveExpiredChangeState(acknowledged, claimed, restartRequired);
            int changed = jdbc.update(
                    "UPDATE cpf_runtime_change SET change_state=?,updated_by='CPF_CONTROLLER'," +
                            "updated_at=CURRENT_TIMESTAMP WHERE change_id=? " +
                            "AND change_state IN ('SCHEDULED','APPLYING','PARTIAL')",
                    terminal, changeId);
            if (changed != 1) continue;
            jdbc.update(
                    "UPDATE cpf_runtime_delivery SET delivery_state='UNKNOWN_RESULT'," +
                            "error_code='CHANGE_EXPIRED_IN_FLIGHT'," +
                            "error_message='Change expired while delivery side effect was in flight'," +
                            "updated_at=CURRENT_TIMESTAMP WHERE change_id=? AND delivery_state='CLAIMED'",
                    changeId);
            jdbc.update(
                    "UPDATE cpf_runtime_delivery SET delivery_state='EXPIRED',updated_at=CURRENT_TIMESTAMP " +
                            "WHERE change_id=? AND delivery_state IN ('PENDING','FAILED')",
                    changeId);
            String eventType = "UNKNOWN_RESULT".equals(terminal)
                    ? "CHANGE_EXPIRED_WITH_UNCERTAIN_SIDE_EFFECT"
                    : "CHANGE_EXPIRED";
            appendAudit(changeId, eventType, "CPF_CONTROLLER",
                    "Runtime Change expiry reconciled; acknowledged=" + acknowledged
                            + ", claimed=" + claimed + ", restartRequired=" + restartRequired, null);
            reconcileLinkedRollback(changeId, terminal);
        }
    }

    static String deriveExpiredChangeState(int acknowledged, int claimed, int restartRequired) {
        // ACKED는 side effect와 actual hash가 확정된 결과입니다. 남은 delivery가 실행되지 않은 채
        // 만료된 known-partial 상태는 EXPIRED로 두어 acknowledged target rollback을 허용합니다.
        // 실행 중이거나 restart stage가 남은 경우에만 side effect 결과를 UNKNOWN으로 보존합니다.
        return claimed > 0 || restartRequired > 0 ? "UNKNOWN_RESULT" : "EXPIRED";
    }

    /** 기존 모델 Test 호환입니다. */
    static String deriveExpiredChangeState(int claimed, int restartRequired) {
        return deriveExpiredChangeState(0, claimed, restartRequired);
    }

    private int countDeliveries(String changeId, String deliveryState) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM cpf_runtime_delivery WHERE change_id=? AND delivery_state=?",
                Integer.class, changeId, deliveryState);
        return count == null ? 0 : count;
    }

    public void acknowledge(String deliveryId, String changeId, String instanceId, long fencingToken,
                            int acknowledgedAttempt, long appliedVersion, String actualHash, String state, String errorCode,
                            String message, Instant at) {
        assertFence(instanceId, fencingToken);
        String normalized = blank(state).trim().toUpperCase(java.util.Locale.ROOT);
        errorCode = normalizeErrorCode(errorCode);
        message = sanitizeRuntimeMessage(message, 900);
        actualHash = actualHash == null ? null : actualHash.trim();
        if (actualHash != null && actualHash.length() > 64) {
            normalized = "UNKNOWN_RESULT";
            errorCode = "ACTUAL_HASH_INVALID";
            message = "Runtime ACK actualHash가 64자 제한을 초과해 결과불명으로 전환되었습니다.";
            actualHash = null;
        }
        if (!Set.of("SUCCESS", "ACKED", "FAILED", "UNKNOWN_RESULT", "RESTART_REQUIRED").contains(normalized)) {
            throw new IllegalArgumentException("지원하지 않는 Runtime ACK state: " + state);
        }

        List<Map<String,Object>> currentRows = jdbc.queryForList(
                "SELECT delivery_state,attempt_no,desired_version,actual_hash,error_code FROM cpf_runtime_delivery " +
                        "WHERE delivery_id=? AND change_id=? AND instance_id=?", deliveryId, changeId, instanceId);
        if (currentRows.isEmpty()) throw new com.cpf.core.api.runtimecontrol.CpfRuntimeFenceException("Runtime delivery를 찾을 수 없습니다: " + deliveryId);
        Map<String,Object> current = currentRows.getFirst();
        String currentState = String.valueOf(current.get("delivery_state"));
        int currentAttempt = ((Number) current.getOrDefault("attempt_no", 0)).intValue();
        validateAckAttempt(currentAttempt, acknowledgedAttempt, deliveryId);
        int expectedAttempt = currentAttempt;
        long desiredVersion = number(current.get("desired_version"));
        validateAckVersion(desiredVersion, appliedVersion, normalized, deliveryId);
        if (isIdempotentTerminalAck(
                currentState, normalized, nullable(current.get("actual_hash")), actualHash,
                nullable(current.get("error_code")), errorCode)) return;
        if (!"CLAIMED".equals(currentState)) {
            throw new com.cpf.core.api.runtimecontrol.CpfRuntimeFenceException(
                    "이미 처리되었거나 claim 상태가 아닌 delivery에 다른 ACK가 수신되었습니다: " + deliveryId);
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
            deliveryState = isPermanentFailure(errorCode) || currentAttempt >= 8 ? "POISONED" : "FAILED";
        }

        Instant serverAcknowledgedAt = serverNow();
        int updated = jdbc.update(ACK_DELIVERY_SQL,
                deliveryState, actualHash, errorCode, message, ts(serverAcknowledgedAt),
                deliveryId, changeId, instanceId, fencingToken, expectedAttempt, fencingToken);
        if (updated != 1) {
            throw new com.cpf.core.api.runtimecontrol.CpfRuntimeFenceException("ACK가 오래되었거나 이미 처리된 delivery입니다. deliveryId=" + deliveryId);
        }

        if ("ACKED".equals(deliveryState)) {
            jdbc.update("UPDATE cpf_runtime_instance_state SET actual_version=?,actual_hash=?," +
                            "drift_state=CASE WHEN desired_version=? AND desired_hash=? THEN 'IN_SYNC' ELSE 'DRIFT' END," +
                            "last_ack_change_id=?,last_ack_at=?,updated_at=CURRENT_TIMESTAMP " +
                            "WHERE instance_id=? AND fencing_token=?",
                    appliedVersion, actualHash, appliedVersion, actualHash, changeId, ts(serverAcknowledgedAt), instanceId, fencingToken);
            jdbc.update("UPDATE cpf_runtime_instance_feature_state SET actual_version=?,actual_hash=?," +
                            "drift_state=CASE WHEN desired_version=? AND desired_hash=? THEN 'IN_SYNC' ELSE 'DRIFT' END," +
                            "source_delivery_id=?,updated_at=CURRENT_TIMESTAMP WHERE instance_id=? " +
                            "AND change_type=(SELECT change_type FROM cpf_runtime_change WHERE change_id=?)",
                    appliedVersion, actualHash, appliedVersion, actualHash, deliveryId, instanceId, changeId);
        } else if ("RESTART_REQUIRED".equals(deliveryState)) {
            jdbc.update("UPDATE cpf_runtime_instance_state SET drift_state='PENDING_RESTART'," +
                    "last_ack_change_id=?,last_ack_at=?,updated_at=CURRENT_TIMESTAMP WHERE instance_id=? AND fencing_token=?",
                    changeId, ts(serverAcknowledgedAt), instanceId, fencingToken);
            jdbc.update("UPDATE cpf_runtime_instance_feature_state SET drift_state='PENDING_RESTART'," +
                            "source_delivery_id=?,updated_at=CURRENT_TIMESTAMP WHERE instance_id=? " +
                            "AND change_type=(SELECT change_type FROM cpf_runtime_change WHERE change_id=?)",
                    deliveryId, instanceId, changeId);
            jdbc.update("UPDATE cpf_service_instance SET drain_yn='Y',instance_status='DRAINING'," +
                    "drain_deadline_at=?,updated_by='CPF_RUNTIME_CONTROL',updated_at=CURRENT_TIMESTAMP " +
                    "WHERE instance_id=?",
                    ts(serverAcknowledgedAt.plusSeconds(600)), instanceId);
        } else if ("UNKNOWN_RESULT".equals(deliveryState)) {
            jdbc.update("UPDATE cpf_runtime_instance_state SET drift_state='UNKNOWN_RESULT'," +
                    "last_ack_change_id=?,last_ack_at=?,updated_at=CURRENT_TIMESTAMP WHERE instance_id=? AND fencing_token=?",
                    changeId, ts(serverAcknowledgedAt), instanceId, fencingToken);
            jdbc.update("UPDATE cpf_runtime_instance_feature_state SET drift_state='UNKNOWN_RESULT'," +
                            "source_delivery_id=?,updated_at=CURRENT_TIMESTAMP WHERE instance_id=? " +
                            "AND change_type=(SELECT change_type FROM cpf_runtime_change WHERE change_id=?)",
                    deliveryId, instanceId, changeId);
        } else if ("POISONED".equals(deliveryState)) {
            jdbc.update("UPDATE cpf_runtime_instance_state SET drift_state='DRIFT'," +
                            "updated_at=CURRENT_TIMESTAMP WHERE instance_id=? AND fencing_token=?",
                    instanceId, fencingToken);
            jdbc.update("UPDATE cpf_runtime_instance_feature_state SET drift_state='DRIFT'," +
                            "source_delivery_id=?,updated_at=CURRENT_TIMESTAMP WHERE instance_id=? " +
                            "AND change_type=(SELECT change_type FROM cpf_runtime_change WHERE change_id=?)",
                    deliveryId, instanceId, changeId);
        } else if ("FAILED".equals(deliveryState)) {
            long base = Math.min(300L, Math.max(1L, 1L << Math.min(8, Math.max(0, currentAttempt - 1))));
            long jitter = Math.floorMod(deliveryId.hashCode(), Math.max(1, (int) Math.min(30L, base)));
            jdbc.update("UPDATE cpf_runtime_delivery SET next_attempt_at=?,updated_at=CURRENT_TIMESTAMP WHERE delivery_id=?",
                    ts(serverAcknowledgedAt.plusSeconds(base + jitter)), deliveryId);
        }

        appendAudit(changeId, "ACKED".equals(deliveryState) ? "DELIVERY_ACK" : "DELIVERY_" + deliveryState,
                instanceId, truncate(message, 500), actualHash);
        reconcileChangeState(changeId);
    }

    /** 구버전 직접 호출 호환입니다. retry ACK에는 명시 attempt가 필요합니다. */
    public void acknowledge(String deliveryId, String changeId, String instanceId, long fencingToken,
                            long appliedVersion, String actualHash, String state, String errorCode,
                            String message, Instant at) {
        acknowledge(deliveryId, changeId, instanceId, fencingToken, 0, appliedVersion, actualHash,
                state, errorCode, message, at);
    }


    static boolean isIdempotentTerminalAck(
            String currentState, String requestedState, String currentHash, String requestedHash,
            String currentErrorCode, String requestedErrorCode) {
        String current = blankStatic(currentState).trim().toUpperCase(java.util.Locale.ROOT);
        String requested = blankStatic(requestedState).trim().toUpperCase(java.util.Locale.ROOT);
        boolean hashMatches = java.util.Objects.equals(currentHash, requestedHash);
        if ("ACKED".equals(current)) {
            return ("SUCCESS".equals(requested) || "ACKED".equals(requested)) && hashMatches;
        }
        if (Set.of("FAILED", "POISONED").contains(current)) {
            return "FAILED".equals(requested) && hashMatches
                    && java.util.Objects.equals(currentErrorCode, requestedErrorCode);
        }
        return Set.of("UNKNOWN_RESULT", "RESTART_REQUIRED").contains(current)
                && current.equals(requested) && hashMatches
                && java.util.Objects.equals(currentErrorCode, requestedErrorCode);
    }

    private static String blankStatic(String value) {
        return value == null ? "" : value;
    }

    static void validateAckAttempt(int currentAttempt, int acknowledgedAttempt, String deliveryId) {
        boolean legacyFirstAttempt = acknowledgedAttempt == 0 && currentAttempt == 1;
        if (currentAttempt <= 0 || (!legacyFirstAttempt && acknowledgedAttempt != currentAttempt)) {
            throw new com.cpf.core.api.runtimecontrol.CpfRuntimeFenceException(
                    "Runtime ACK attempt가 현재 claim과 일치하지 않습니다. deliveryId=" + deliveryId
                            + ", expected=" + currentAttempt + ", received=" + acknowledgedAttempt);
        }
    }

    static void validateAckVersion(long desiredVersion, long appliedVersion, String state, String deliveryId) {
        String normalized = state == null ? "" : state.trim().toUpperCase(java.util.Locale.ROOT);
        if (("SUCCESS".equals(normalized) || "ACKED".equals(normalized))
                && desiredVersion != appliedVersion) {
            throw new com.cpf.core.api.runtimecontrol.CpfRuntimeFenceException(
                    "Runtime 성공 ACK appliedVersion이 delivery desiredVersion과 일치하지 않습니다. deliveryId="
                            + deliveryId + ", desired=" + desiredVersion + ", applied=" + appliedVersion);
        }
    }

    static String normalizeErrorCode(String value) {
        String normalized=value==null?null:value.trim().toUpperCase(java.util.Locale.ROOT);
        if(normalized==null||normalized.isBlank()) return null;
        return normalized.length()>80?normalized.substring(0,80):normalized;
    }

    static String sanitizeRuntimeMessage(String value,int max) {
        if(value==null) return null;
        int boundedMax=Math.max(1,max);
        String masked=value
                .replaceAll("(?i)(password|passwd|pwd|token|secret|api[-_ ]?key|authorization)\\s*[:=]\\s*([^,;\\s]+)","$1=***")
                .replaceAll("(?i)\\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}\\b","***@***")
                .replaceAll("(?<!\\d)\\d{6}[- ]?[1-4]\\d{6}(?!\\d)","******-*******")
                .replaceAll("(?<!\\d)(?:\\+?82[- ]?)?0(?:10|11|16|17|18|19)[- ]?\\d{3,4}[- ]?\\d{4}(?!\\d)","***-****-****");
        return masked.length()>boundedMax?masked.substring(0,boundedMax):masked;
    }

    static boolean isPermanentFailure(String errorCode) {
        String code = errorCode == null ? "" : errorCode.trim().toUpperCase(java.util.Locale.ROOT);
        return Set.of(
                "APPLIER_NOT_FOUND",
                "PAYLOAD_SCHEMA_UNSUPPORTED",
                "PAYLOAD_HASH_MISMATCH",
                "VERSION_GAP_REQUIRES_SNAPSHOT",
                "ACTUAL_HASH_MISSING",
                "RECONCILIATION_INVALID",
                "DELIVERY_EXPIRED").contains(code);
    }

    public void cancel(String changeId, String operatorId, String reason) {
        List<Map<String,Object>> changeRows=jdbc.queryForList(
                "SELECT change_state FROM cpf_runtime_change WHERE change_id=? FOR UPDATE",changeId);
        if(changeRows.isEmpty()) throw new IllegalStateException("Runtime Change를 찾을 수 없습니다: "+changeId);
        String currentState=String.valueOf(changeRows.getFirst().get("change_state"));
        if(!Set.of("SCHEDULED","APPLYING","PARTIAL").contains(currentState))
            throw new IllegalStateException("취소할 수 없는 Runtime Change입니다. changeId="+changeId+", state="+currentState);

        List<Map<String,Object>> deliveries=jdbc.queryForList(
                "SELECT delivery_state FROM cpf_runtime_delivery WHERE change_id=? FOR UPDATE",changeId);
        int acknowledged=0;int uncertain=0;
        for(Map<String,Object> row:deliveries){
            String deliveryState=String.valueOf(row.get("delivery_state"));
            if("ACKED".equals(deliveryState)) acknowledged++;
            if(Set.of("CLAIMED","UNKNOWN_RESULT","RESTART_REQUIRED").contains(deliveryState)) uncertain++;
        }
        String terminal=deriveCancellationState(acknowledged,uncertain);
        jdbc.update("UPDATE cpf_runtime_delivery SET delivery_state='UNKNOWN_RESULT',"+
                        "error_code='CHANGE_CANCELLED_IN_FLIGHT',"+
                        "error_message='Change cancellation occurred while delivery side effect was in flight',"+
                        "updated_at=CURRENT_TIMESTAMP WHERE change_id=? AND delivery_state='CLAIMED'",changeId);
        jdbc.update("UPDATE cpf_runtime_delivery SET delivery_state='CANCELLED',updated_at=CURRENT_TIMESTAMP " +
                "WHERE change_id=? AND delivery_state IN ('PENDING','FAILED','POISONED')",changeId);
        int updated=jdbc.update("UPDATE cpf_runtime_change SET change_state=?,updated_at=CURRENT_TIMESTAMP,updated_by=? " +
                "WHERE change_id=? AND change_state=?",terminal,operatorId,changeId,currentState);
        if(updated!=1) throw new IllegalStateException("Runtime Change 취소 상태가 동시 변경되었습니다: "+changeId);
        String eventType="CANCELLED".equals(terminal)?"CHANGE_CANCELLED":
                ("PARTIAL".equals(terminal)?"CHANGE_CANCELLED_AFTER_PARTIAL_APPLY":"CHANGE_CANCELLED_WITH_UNCERTAIN_SIDE_EFFECT");
        appendAudit(changeId,eventType,operatorId,reason,null);
        reconcileLinkedRollback(changeId, terminal);
    }

    static String deriveCancellationState(int acknowledged,int uncertain) {
        if(uncertain>0) return "UNKNOWN_RESULT";
        if(acknowledged>0) return "PARTIAL";
        return "CANCELLED";
    }

    public void markRollbackPending(String changeId, String operatorId, String reason) {
        int updated=jdbc.update("UPDATE cpf_runtime_change SET change_state='ROLLBACK_PENDING',updated_at=CURRENT_TIMESTAMP,updated_by=? " +
                "WHERE change_id=? AND change_state IN ('SUCCESS','PARTIAL','FAILED','EXPIRED')",operatorId,changeId);
        if (updated == 1) {
            appendAudit(changeId,"ROLLBACK_REQUESTED",operatorId,reason,null);
            return;
        }
        List<Map<String,Object>> current = jdbc.queryForList(
                "SELECT change_state FROM cpf_runtime_change WHERE change_id=?", changeId);
        if (!current.isEmpty() && "ROLLBACK_PENDING".equals(String.valueOf(current.getFirst().get("change_state")))) {
            return;
        }
        throw new IllegalStateException("Rollback할 수 없는 Runtime Change입니다. changeId="+changeId);
    }

    /** Rollback Change와 원본 Change의 durable 관계를 immutable audit chain에 양방향으로 기록합니다. */
    public void linkRollbackChange(String originalChangeId, String rollbackChangeId, String operatorId, String reason) {
        requireText(originalChangeId, "originalChangeId");
        requireText(rollbackChangeId, "rollbackChangeId");
        if (originalChangeId.equals(rollbackChangeId)) {
            throw new IllegalArgumentException("Rollback Change는 원본 Change와 같을 수 없습니다.");
        }
        if (!hasAuditLink(rollbackChangeId, "ROLLBACK_OF_CHANGE", originalChangeId)) {
            appendAudit(rollbackChangeId, "ROLLBACK_OF_CHANGE", operatorId, reason, originalChangeId);
        }
        if (!hasAuditLink(originalChangeId, "ROLLBACK_CHANGE_CREATED", rollbackChangeId)) {
            appendAudit(originalChangeId, "ROLLBACK_CHANGE_CREATED", operatorId, reason, rollbackChangeId);
        }
    }

    private boolean hasAuditLink(String changeId, String eventType, String linkedChangeId) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM cpf_runtime_change_audit " +
                        "WHERE change_id=? AND event_type=? AND evidence_hash=?",
                Integer.class, changeId, eventType, linkedChangeId);
        return count != null && count > 0;
    }

    void reconcileLinkedRollback(String rollbackChangeId, String rollbackState) {
        List<Map<String,Object>> links = jdbc.queryForList(
                "SELECT evidence_hash FROM cpf_runtime_change_audit " +
                        "WHERE change_id=? AND event_type='ROLLBACK_OF_CHANGE' ORDER BY audit_id",
                rollbackChangeId);
        if (links.isEmpty()) return;
        String originalChangeId = nullable(links.getLast().get("evidence_hash"));
        if (originalChangeId == null || originalChangeId.isBlank()) return;

        if ("SUCCESS".equals(rollbackState)) {
            int updated = jdbc.update(
                    "UPDATE cpf_runtime_change SET change_state='ROLLED_BACK',updated_by='CPF_CONTROLLER'," +
                            "updated_at=CURRENT_TIMESTAMP WHERE change_id=? AND change_state='ROLLBACK_PENDING'",
                    originalChangeId);
            if (updated == 1 && !hasAuditLink(originalChangeId, "ROLLBACK_COMPLETED", rollbackChangeId)) {
                appendAudit(originalChangeId, "ROLLBACK_COMPLETED", "CPF_CONTROLLER",
                        "Rollback change completed", rollbackChangeId);
            }
        } else if (Set.of("UNKNOWN_RESULT", "FAILED", "EXPIRED", "CANCELLED").contains(rollbackState)) {
            int updated = jdbc.update(
                    "UPDATE cpf_runtime_change SET change_state='UNKNOWN_RESULT',updated_by='CPF_CONTROLLER'," +
                            "updated_at=CURRENT_TIMESTAMP WHERE change_id=? AND change_state='ROLLBACK_PENDING'",
                    originalChangeId);
            String eventType = "UNKNOWN_RESULT".equals(rollbackState)
                    ? "ROLLBACK_UNKNOWN"
                    : "ROLLBACK_TERMINATED_" + rollbackState;
            if (updated == 1 && !hasAuditLink(originalChangeId, eventType, rollbackChangeId)) {
                appendAudit(originalChangeId, eventType, "CPF_CONTROLLER",
                        "Rollback did not complete successfully and requires reconciliation", rollbackChangeId);
            }
        }
    }

    public CpfRuntimeInstanceLease register(CpfRuntimeInstanceRegistration r) {
        requireText(r.instanceId(), "instanceId");
        requireText(r.serviceId(), "serviceId");
        requireText(r.endpointCode(), "endpointCode");
        requireText(r.registrationSource(), "registrationSource");
        Instant serverNow = serverNow();
        long skewMs = validatedClockSkewMillis(r.agentTime(), serverNow, false, r.instanceId());
        ensureServiceAndEndpoint(r);

        Instant lease = serverNow.plusSeconds(r.leaseSeconds());
        String capabilities = write(r.capabilities());
        String labels = write(r.labels());
        List<Map<String,Object>> existing = jdbc.queryForList(
                "SELECT fencing_token,lease_until,registration_source FROM cpf_runtime_instance_state " +
                        "WHERE instance_id=? FOR UPDATE", r.instanceId());
        long fence;
        boolean reRegistered = false;
        if (existing.isEmpty()) {
            boolean inserted = tryInsertUnique("INSERT INTO cpf_runtime_instance_state " +
                            "(instance_id,fencing_token,lease_until,desired_version,actual_version,drift_state," +
                            "capabilities_json,labels_json,artifact_version,artifact_commit,runtime_role,registration_source," +
                            "schema_version,config_hash,clock_skew_ms,heartbeat_at,created_by,updated_by) " +
                            "VALUES (?,?,?,0,0,'IN_SYNC',?,?,?,?,?,?,?,?,?,CURRENT_TIMESTAMP,'CPF','CPF')",
                    r.instanceId(), 1L, ts(lease), capabilities, labels, blank(r.artifactVersion()),
                    blank(r.artifactCommit()), blank(r.runtimeRole()), blank(r.registrationSource()),
                    blank(r.schemaVersion()), blank(r.configHash()), skewMs);
            if (inserted) {
                fence = 1L;
            } else {
                existing = jdbc.queryForList(
                        "SELECT fencing_token,lease_until,registration_source FROM cpf_runtime_instance_state " +
                                "WHERE instance_id=? FOR UPDATE", r.instanceId());
                if (existing.isEmpty()) {
                    throw new com.cpf.core.api.runtimecontrol.CpfRuntimeFenceException(
                            "Runtime instance 동시 등록 결과를 확인할 수 없습니다: " + r.instanceId());
                }
                assertRegistrationSourceAvailable(r, existing.getFirst(), serverNow);
                long currentFence = ((Number) existing.getFirst().get("fencing_token")).longValue();
                fence = nextMonotonic(currentFence,"Runtime instance fencing token");
                updateRegistration(r, lease, capabilities, labels, skewMs, fence, currentFence);
                reRegistered = true;
            }
        } else {
            assertRegistrationSourceAvailable(r, existing.getFirst(), serverNow);
            long currentFence = ((Number) existing.getFirst().get("fencing_token")).longValue();
            fence = nextMonotonic(currentFence,"Runtime instance fencing token");
            updateRegistration(r, lease, capabilities, labels, skewMs, fence, currentFence);
            reRegistered = true;
        }

        // instance identity/fencing이 확정된 뒤에만 Service Registry를 갱신합니다.
        upsertServiceInstance(r);
        if (reRegistered) {
            jdbc.update("UPDATE cpf_runtime_delivery SET delivery_state='PENDING',fencing_token=NULL," +
                            "error_code='RESTART_RECONCILE_PENDING'," +
                            "error_message='Restarted agent must reconcile durable APPLIED inbox before reclaim'," +
                            "next_attempt_at=CURRENT_TIMESTAMP,updated_at=CURRENT_TIMESTAMP " +
                            "WHERE instance_id=? AND delivery_state='RESTART_REQUIRED'",
                    r.instanceId());
        }
        return lease(r.instanceId());
    }

    private void assertRegistrationSourceAvailable(
            CpfRuntimeInstanceRegistration registration,
            Map<String,Object> current,
            Instant serverNow) {
        validateRegistrationTakeover(
                toInstant(current.get("lease_until")),
                nullable(current.get("registration_source")),
                registration.registrationSource(),
                registration.instanceId(),
                serverNow);
    }

    static void validateRegistrationTakeover(
            Instant currentLease,String currentSource,String requestedSource,String instanceId,Instant now) {
        if(currentLease!=null&&currentLease.isAfter(now)) {
            String owner=currentSource==null||currentSource.isBlank()?"UNKNOWN":currentSource;
            String requester=requestedSource==null||requestedSource.isBlank()?"UNKNOWN":requestedSource;
            throw new com.cpf.core.api.runtimecontrol.CpfRuntimeFenceException(
                    "살아 있는 Runtime instance lease를 재등록으로 탈취할 수 없습니다: "
                            +instanceId+", currentSource="+owner+", requestedSource="+requester);
        }
    }

    private void updateRegistration(
            CpfRuntimeInstanceRegistration r,
            Instant lease,
            String capabilities,
            String labels,
            long skewMs,
            long nextFence,
            long currentFence) {
        int updated = jdbc.update("UPDATE cpf_runtime_instance_state SET fencing_token=?,lease_until=?," +
                        "capabilities_json=?,labels_json=?,artifact_version=?,artifact_commit=?,runtime_role=?," +
                        "registration_source=?,schema_version=?,config_hash=?,clock_skew_ms=?,heartbeat_at=CURRENT_TIMESTAMP," +
                        "updated_at=CURRENT_TIMESTAMP WHERE instance_id=? AND fencing_token=?",
                nextFence, ts(lease), capabilities, labels, blank(r.artifactVersion()), blank(r.artifactCommit()),
                blank(r.runtimeRole()), blank(r.registrationSource()), blank(r.schemaVersion()), blank(r.configHash()),
                skewMs, r.instanceId(), currentFence);
        if (updated != 1) {
            throw new com.cpf.core.api.runtimecontrol.CpfRuntimeFenceException(
                    "Runtime instance 재등록 fencing 충돌: " + r.instanceId());
        }
    }

    public CpfRuntimeInstanceLease heartbeat(String instanceId, long fencingToken, String actualHash,
                                             long actualVersion, int leaseSeconds, Instant agentTime) {
        Instant now=serverNow();
        long skewMs = validatedClockSkewMillis(
                agentTime == null ? now : agentTime, now, true, instanceId);
        List<Map<String,Object>> currentRows=jdbc.queryForList(
                "SELECT fencing_token,lease_until,heartbeat_at FROM cpf_runtime_instance_state WHERE instance_id=?",
                instanceId);
        if(currentRows.isEmpty())
            throw new com.cpf.core.api.runtimecontrol.CpfRuntimeFenceException("등록되지 않은 Runtime instance입니다: "+instanceId);
        Map<String,Object> current=currentRows.getFirst();
        Instant currentLease=toInstant(current.get("lease_until"));
        long currentFence=number(current.get("fencing_token"));
        if(currentFence!=fencingToken||currentLease==null||!currentLease.isAfter(now))
            throw new com.cpf.core.api.runtimecontrol.CpfRuntimeFenceException(
                    "만료되었거나 fencing token이 다른 Runtime heartbeat입니다: "+instanceId);
        int effectiveLeaseSeconds=resolveHeartbeatLeaseSeconds(
                toInstant(current.get("heartbeat_at")),currentLease,leaseSeconds);
        Instant until = now.plusSeconds(effectiveLeaseSeconds);
        int updated = jdbc.update("UPDATE cpf_runtime_instance_state SET lease_until=?,heartbeat_at=CURRENT_TIMESTAMP," +
                        "actual_hash=?,actual_version=?,clock_skew_ms=?," +
                        "drift_state=CASE WHEN desired_version=? AND COALESCE(desired_hash,'')=COALESCE(?,'') " +
                        "THEN 'IN_SYNC' WHEN drift_state IN ('UNKNOWN_RESULT','PENDING_RESTART') " +
                        "THEN drift_state ELSE 'DRIFT' END,updated_at=CURRENT_TIMESTAMP " +
                        "WHERE instance_id=? AND fencing_token=? AND lease_until>CURRENT_TIMESTAMP",
                ts(until), actualHash, actualVersion, skewMs, actualVersion, actualHash, instanceId, fencingToken);
        if (updated != 1) throw new com.cpf.core.api.runtimecontrol.CpfRuntimeFenceException("Runtime heartbeat fencing/lease 충돌: " + instanceId);
        jdbc.update("UPDATE cpf_service_instance SET instance_status=CASE WHEN COALESCE(drain_yn,'N')='Y' " +
                        "THEN 'DRAINING' ELSE 'UP' END,last_heartbeat_at=CURRENT_TIMESTAMP,updated_at=CURRENT_TIMESTAMP " +
                        "WHERE instance_id=?", instanceId);
        return lease(instanceId);
    }

    static long validatedClockSkewMillis(
            Instant agentTime, Instant controllerTime, boolean fencingFailure, String instanceId) {
        Instant safeController = controllerTime == null ? Instant.now() : controllerTime;
        Instant safeAgent = agentTime == null ? safeController : agentTime;
        java.time.Duration skew;
        try {
            skew = java.time.Duration.between(safeAgent, safeController).abs();
        } catch (ArithmeticException overflow) {
            skew = java.time.Duration.ofDays(365_000_000L);
        }
        if (skew.compareTo(java.time.Duration.ofMinutes(5)) > 0) {
            String message = "Runtime Agent clock skew가 허용 범위를 초과했습니다. instanceId="
                    + instanceId + ", maxMs=300000";
            if (fencingFailure) {
                throw new com.cpf.core.api.runtimecontrol.CpfRuntimeFenceException(message);
            }
            throw new IllegalArgumentException(message);
        }
        return skew.toMillis();
    }

    static int resolveHeartbeatLeaseSeconds(Instant heartbeatAt,Instant leaseUntil,int fallback) {
        long configured=heartbeatAt==null||leaseUntil==null
                ? fallback : java.time.Duration.between(heartbeatAt,leaseUntil).getSeconds();
        if(configured<10L||configured>3600L) configured=fallback;
        return Math.max(10,Math.min(3600,(int)configured));
    }

    /** 기존 호출 호환입니다. */
    public CpfRuntimeInstanceLease heartbeat(String instanceId, long fencingToken, String actualHash,
                                             long actualVersion, int leaseSeconds) {
        return heartbeat(instanceId, fencingToken, actualHash, actualVersion, leaseSeconds, serverNow());
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
        LinkedHashSet<String> recoveredChangeIds = new LinkedHashSet<>();
        boolean restartDrainRecovered = false;
        for (CpfRuntimeActualState state : states == null ? List.<CpfRuntimeActualState>of() : states) {
            if (state == null || state.changeType() == null || state.changeType().isBlank()
                    || state.actualHash() == null || state.actualHash().isBlank()
                    || state.sourceDeliveryId() == null || state.sourceDeliveryId().isBlank()) {
                continue;
            }
            String changeType = state.changeType().trim().toUpperCase();
            List<Map<String,Object>> proofs = jdbc.queryForList(
                    "SELECT d.delivery_id,d.change_id,d.instance_id,d.desired_version,d.delivery_state,d.actual_hash," +
                            "d.error_code,c.change_type FROM cpf_runtime_delivery d " +
                            "JOIN cpf_runtime_change c ON c.change_id=d.change_id " +
                            "WHERE d.delivery_id=? AND d.instance_id=?",
                    state.sourceDeliveryId(), instanceId);
            if (proofs.isEmpty()) {
                throw new com.cpf.core.api.runtimecontrol.CpfRuntimeFenceException(
                        "Runtime actual state source delivery를 찾을 수 없습니다: " + state.sourceDeliveryId());
            }
            Map<String,Object> proof = proofs.getFirst();
            validateActualStateProof(proof, state, instanceId);
            String deliveryState = String.valueOf(proof.get("delivery_state"));
            String proofErrorCode = nullable(proof.get("error_code"));
            boolean restartRecoveryProof = "RESTART_REQUIRED".equals(deliveryState)
                    || ("PENDING".equals(deliveryState)
                    && "RESTART_RECONCILE_PENDING".equals(proofErrorCode));
            String changeId = String.valueOf(proof.get("change_id"));
            if (!"ACKED".equals(deliveryState)) {
                int recovered = jdbc.update(
                        "UPDATE cpf_runtime_delivery SET delivery_state='ACKED',actual_hash=?,error_code=NULL," +
                                "error_message=NULL,acknowledged_at=CURRENT_TIMESTAMP,fencing_token=?," +
                                "updated_at=CURRENT_TIMESTAMP WHERE delivery_id=? AND instance_id=? " +
                                "AND desired_version=? AND (delivery_state IN ('CLAIMED','UNKNOWN_RESULT','RESTART_REQUIRED') OR (delivery_state='PENDING' AND error_code='RESTART_RECONCILE_PENDING'))",
                        state.actualHash(), fencingToken, state.sourceDeliveryId(), instanceId, state.actualVersion());
                if (recovered != 1) {
                    throw new com.cpf.core.api.runtimecontrol.CpfRuntimeFenceException(
                            "Runtime actual state delivery 복구가 다른 처리와 충돌했습니다: "
                                    + state.sourceDeliveryId());
                }
                appendAudit(changeId, "DELIVERY_RECOVERED_FROM_INBOX", instanceId,
                        "Durable APPLIED inbox proved side effect after ACK loss/restart",
                        state.sourceDeliveryId() + ":version=" + state.actualVersion());
                recoveredChangeIds.add(changeId);
                restartDrainRecovered = restartDrainRecovered || restartRecoveryProof;
            }

            int updated = jdbc.update("UPDATE cpf_runtime_instance_feature_state SET actual_version=?,actual_hash=?," +
                            "drift_state=CASE WHEN desired_version=? AND desired_hash=? THEN 'IN_SYNC' ELSE 'DRIFT' END," +
                            "source_delivery_id=?,updated_at=CURRENT_TIMESTAMP " +
                            "WHERE instance_id=? AND change_type=?",
                    state.actualVersion(), state.actualHash(), state.actualVersion(), state.actualHash(),
                    state.sourceDeliveryId(), instanceId, changeType);
            if (updated == 0 && !tryInsertUnique(
                    "INSERT INTO cpf_runtime_instance_feature_state " +
                            "(instance_id,change_type,desired_version,actual_version,desired_hash,actual_hash," +
                            "drift_state,source_delivery_id,created_by,updated_by) " +
                            "VALUES (?,?,0,?,?,?,'DRIFT',?,'CPF','CPF')",
                    instanceId, changeType, state.actualVersion(), null, state.actualHash(), state.sourceDeliveryId())) {
                int racedActualUpdate = jdbc.update(
                        "UPDATE cpf_runtime_instance_feature_state SET actual_version=?,actual_hash=?," +
                                "drift_state=CASE WHEN desired_version=? AND desired_hash=? THEN 'IN_SYNC' ELSE 'DRIFT' END," +
                                "source_delivery_id=?,updated_at=CURRENT_TIMESTAMP WHERE instance_id=? AND change_type=?",
                        state.actualVersion(), state.actualHash(), state.actualVersion(), state.actualHash(),
                        state.sourceDeliveryId(), instanceId, changeType);
                if (racedActualUpdate != 1) {
                    throw new IllegalStateException("Runtime actual state 동시 갱신 실패: " + instanceId);
                }
            }
            if (state.actualVersion() >= maxVersion) {
                maxVersion = state.actualVersion();
                maxHash = state.actualHash();
            }
        }
        if (maxVersion > 0L) {
            jdbc.update("UPDATE cpf_runtime_instance_state SET actual_version=?,actual_hash=?," +
                            "drift_state=CASE WHEN desired_version=? AND COALESCE(desired_hash,'')=COALESCE(?,'') " +
                            "THEN 'IN_SYNC' ELSE 'DRIFT' END,updated_at=CURRENT_TIMESTAMP " +
                            "WHERE instance_id=? AND fencing_token=?",
                    maxVersion, maxHash, maxVersion, maxHash, instanceId, fencingToken);
        }
        if (restartDrainRecovered) {
            releaseRuntimeRestartDrain(instanceId);
        }
        recoveredChangeIds.forEach(this::reconcileChangeState);
    }

    private void releaseRuntimeRestartDrain(String instanceId) {
        jdbc.update("UPDATE cpf_service_instance SET drain_yn='N',drain_deadline_at=NULL," +
                        "instance_status=CASE WHEN COALESCE(maintenance_yn,'N')='Y' " +
                        "THEN 'MAINTENANCE' ELSE 'UP' END,updated_by='CPF_RUNTIME_CONTROL'," +
                        "updated_at=CURRENT_TIMESTAMP WHERE instance_id=? AND drain_yn='Y' " +
                        "AND updated_by='CPF_RUNTIME_CONTROL' " +
                        "AND NOT EXISTS (SELECT 1 FROM cpf_runtime_delivery pending_restart " +
                        "WHERE pending_restart.instance_id=? " +
                        "AND (pending_restart.delivery_state='RESTART_REQUIRED' " +
                        "OR (pending_restart.delivery_state='PENDING' " +
                        "AND pending_restart.error_code='RESTART_RECONCILE_PENDING')))",
                instanceId, instanceId);
    }

    static boolean isRuntimeRestartDrainOwned(String drainYn, String updatedBy, int pendingRestartCount) {
        return "Y".equalsIgnoreCase(drainYn)
                && "CPF_RUNTIME_CONTROL".equals(updatedBy)
                && pendingRestartCount == 0;
    }

    static void validateActualStateProof(
            Map<String,Object> proof, CpfRuntimeActualState state, String instanceId) {
        String proofInstance = String.valueOf(proof.get("instance_id"));
        String proofType = baseChangeTypeStatic(String.valueOf(proof.get("change_type")));
        long proofVersion = ((Number) proof.getOrDefault("desired_version", -1L)).longValue();
        String deliveryState = String.valueOf(proof.get("delivery_state"));
        String currentHash = proof.get("actual_hash") == null ? null : String.valueOf(proof.get("actual_hash"));
        String errorCode = proof.get("error_code") == null ? null : String.valueOf(proof.get("error_code"));
        String requestedType = baseChangeTypeStatic(state.changeType());
        if (!instanceId.equals(proofInstance)
                || !requestedType.equals(proofType)
                || proofVersion != state.actualVersion()) {
            throw new com.cpf.core.api.runtimecontrol.CpfRuntimeFenceException(
                    "Runtime actual state proof identity/version이 delivery와 일치하지 않습니다: "
                            + state.sourceDeliveryId());
        }
        boolean restartReconcilePending = "PENDING".equals(deliveryState)
                && "RESTART_RECONCILE_PENDING".equals(errorCode);
        if (!restartReconcilePending
                && !Set.of("CLAIMED", "UNKNOWN_RESULT", "RESTART_REQUIRED", "ACKED").contains(deliveryState)) {
            throw new com.cpf.core.api.runtimecontrol.CpfRuntimeFenceException(
                    "복구할 수 없는 Runtime delivery 상태입니다: " + deliveryState);
        }
        if ("ACKED".equals(deliveryState) && !java.util.Objects.equals(currentHash, state.actualHash())) {
            throw new com.cpf.core.api.runtimecontrol.CpfRuntimeFenceException(
                    "이미 ACKED된 Runtime delivery의 actualHash와 재보고 값이 다릅니다: "
                            + state.sourceDeliveryId());
        }
    }

    private static String baseChangeTypeStatic(String value) {
        String normalized = value == null ? "" : value.trim().toUpperCase(java.util.Locale.ROOT);
        return normalized.startsWith("ROLLBACK:")
                ? normalized.substring("ROLLBACK:".length())
                : normalized;
    }

    @Transactional(transactionManager = "cpfTransactionManager")
    public long acquireControllerLease(String holderId, int leaseSeconds) {
        requireText(holderId, "holderId");
        int seconds = Math.max(10, Math.min(300, leaseSeconds));
        Instant now = serverNow();
        Instant until = now.plusSeconds(seconds);
        List<Map<String,Object>> rows = jdbc.queryForList(
                "SELECT holder_id,fencing_token,lease_until FROM cpf_runtime_controller_lease " +
                        "WHERE lease_key='RUNTIME_CONTROL' FOR UPDATE");
        if (rows.isEmpty()) {
            boolean inserted = tryInsertUnique("INSERT INTO cpf_runtime_controller_lease " +
                            "(lease_key,holder_id,fencing_token,lease_until,created_by,updated_by) " +
                            "VALUES ('RUNTIME_CONTROL',?,1,?,'CPF','CPF')",
                    holderId, ts(until));
            // 경쟁 insert가 이겼다면 현재 cycle은 fail-closed하고 다음 reconcile에서 재시도합니다.
            return inserted ? 1L : 0L;
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
            long nextFence = nextMonotonic(currentFence,"Runtime controller fencing token");
            int updated = jdbc.update("UPDATE cpf_runtime_controller_lease SET holder_id=?,fencing_token=?," +
                            "lease_until=?,last_reconciled_at=CURRENT_TIMESTAMP,updated_at=CURRENT_TIMESTAMP " +
                            "WHERE lease_key='RUNTIME_CONTROL' AND fencing_token=?",
                    holderId, nextFence, ts(until), currentFence);
            return updated == 1 ? nextFence : 0L;
        }
        return 0L;
    }

    @Transactional(transactionManager = "cpfTransactionManager")
    public void reconcileController(String holderId, long fencingToken, int ackTimeoutSeconds) {
        assertControllerFence(holderId, fencingToken);
        reconcileTemporalChanges();
        expireControlOperations();
        Instant timeout = serverNow().minusSeconds(Math.max(10, ackTimeoutSeconds));
        List<Map<String,Object>> timedOut = jdbc.queryForList(
                "SELECT delivery_id,change_id,instance_id,fencing_token,attempt_no FROM cpf_runtime_delivery " +
                        "WHERE delivery_state='CLAIMED' AND claimed_at<?", ts(timeout));
        for (Map<String,Object> row : timedOut) {
            markAckTimeoutUnknown(row, holderId);
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
            reconcileLinkedRollback(changeId, state);
        }
        jdbc.update("DELETE FROM cpf_runtime_rate_bucket WHERE window_start<?",
                ts(serverNow().minusSeconds(172800)));
        jdbc.update("UPDATE cpf_runtime_controller_lease SET last_reconciled_at=CURRENT_TIMESTAMP," +
                        "updated_at=CURRENT_TIMESTAMP WHERE lease_key='RUNTIME_CONTROL' " +
                        "AND holder_id=? AND fencing_token=?",
                holderId, fencingToken);
    }

    /**
     * ACK timeout은 Agent가 side effect를 완료한 뒤 응답만 유실된 경우를 배제할 수 없습니다.
     * 따라서 blind retry/POISON 전환을 하지 않고 UNKNOWN_RESULT로 보존한 뒤 대사 대상으로 남깁니다.
     */
    private void markAckTimeoutUnknown(Map<String, Object> row, String controllerId) {
        String deliveryId = String.valueOf(row.get("delivery_id"));
        String changeId = String.valueOf(row.get("change_id"));
        String instanceId = String.valueOf(row.get("instance_id"));
        long claimFence = ((Number) row.getOrDefault("fencing_token", 0L)).longValue();
        int attempt = ((Number) row.get("attempt_no")).intValue();
        int updated = jdbc.update(
                "UPDATE cpf_runtime_delivery SET delivery_state='UNKNOWN_RESULT'," +
                        "error_code='ACK_TIMEOUT_UNKNOWN'," +
                        "error_message='Runtime Agent ACK timeout; side effect requires reconciliation'," +
                        "updated_at=CURRENT_TIMESTAMP WHERE delivery_id=? AND attempt_no=? " +
                        "AND delivery_state='CLAIMED'",
                deliveryId, attempt);
        if (updated != 1) return;
        jdbc.update("UPDATE cpf_runtime_instance_state SET drift_state='UNKNOWN_RESULT'," +
                        "updated_at=CURRENT_TIMESTAMP WHERE instance_id=? AND fencing_token=?",
                instanceId, claimFence);
        jdbc.update("UPDATE cpf_runtime_instance_feature_state SET drift_state='UNKNOWN_RESULT'," +
                        "source_delivery_id=?,updated_at=CURRENT_TIMESTAMP WHERE instance_id=? " +
                        "AND change_type=(SELECT change_type FROM cpf_runtime_change WHERE change_id=?) " +
                        "AND EXISTS (SELECT 1 FROM cpf_runtime_instance_state s WHERE s.instance_id=? AND s.fencing_token=?)",
                deliveryId, instanceId, changeId, instanceId, claimFence);
        appendAudit(changeId, "DELIVERY_UNKNOWN_RESULT", controllerId,
                "Runtime Agent ACK timeout; blind retry blocked", deliveryId + ":attempt=" + attempt);
        reconcileChangeState(changeId);
    }

    public List<String> acknowledgedTargets(String changeId) {
        return jdbc.queryForList(
                "SELECT instance_id FROM cpf_runtime_delivery WHERE change_id=? AND delivery_state='ACKED' ORDER BY sequence_no",
                String.class, changeId);
    }

    List<Map<String,Object>> autoRollbackCandidates() {
        return jdbc.queryForList(
                "SELECT change_id,change_state,change_type,approval_id,break_glass_id " +
                        "FROM cpf_runtime_change " +
                        "WHERE change_state IN ('FAILED','EXPIRED') " +
                        "AND rollback_payload_json IS NOT NULL ORDER BY created_at,change_id");
    }

    int autoRollbackEventCount(String changeId, String eventType) {
        Integer value = jdbc.queryForObject(
                "SELECT COUNT(*) FROM cpf_runtime_change_audit " +
                        "WHERE change_id=? AND event_type=?",
                Integer.class,
                changeId,
                eventType);
        return value == null ? 0 : value;
    }

    int recentAutoRollbackFailureCount(String changeType, Instant since) {
        Integer value = jdbc.queryForObject(
                "SELECT COUNT(*) FROM cpf_runtime_change_audit audit " +
                        "JOIN cpf_runtime_change change_row ON change_row.change_id=audit.change_id " +
                        "WHERE change_row.change_type=? AND audit.event_type='AUTO_ROLLBACK_FAILED' " +
                        "AND audit.created_at>=?",
                Integer.class,
                changeType,
                ts(since));
        return value == null ? 0 : value;
    }

    @Transactional(transactionManager = "cpfTransactionManager")
    public void appendAutoRollbackAudit(
            String changeId,
            String eventType,
            String actor,
            String reason,
            String evidenceHash) {
        appendAudit(changeId, eventType, actor, reason, evidenceHash);
    }

    private void assertControllerFence(String holderId, long fencingToken) {
        List<Map<String,Object>> rows = jdbc.queryForList(
                "SELECT holder_id,fencing_token,lease_until FROM cpf_runtime_controller_lease " +
                        "WHERE lease_key='RUNTIME_CONTROL' FOR UPDATE");
        if (rows.isEmpty()) throw new com.cpf.core.api.runtimecontrol.CpfRuntimeFenceException("Runtime Controller lease가 없습니다.");
        Map<String,Object> row = rows.getFirst();
        Instant until = toInstant(row.get("lease_until"));
        if (!holderId.equals(String.valueOf(row.get("holder_id")))
                || ((Number) row.get("fencing_token")).longValue() != fencingToken
                || until == null || !until.isAfter(serverNow())) {
            throw new com.cpf.core.api.runtimecontrol.CpfRuntimeFenceException("Runtime Controller fencing token 또는 lease가 유효하지 않습니다.");
        }
    }

    public CpfRuntimeInstanceLease lease(String instanceId) {
        Map<String,Object> row=jdbc.queryForMap("SELECT instance_id,fencing_token,desired_version,actual_version,desired_hash,actual_hash,drift_state,lease_until FROM cpf_runtime_instance_state WHERE instance_id=?",
                instanceId);
        return new CpfRuntimeInstanceLease(instanceId,number(row.get("fencing_token")),number(row.get("desired_version")),number(row.get("actual_version")),
                nullable(row.get("desired_hash")),nullable(row.get("actual_hash")),String.valueOf(row.get("drift_state")),toInstant(row.get("lease_until")));
    }

    public CpfRuntimeStatus status(String environment,String serviceId) {
        StringBuilder sql=new StringBuilder("SELECT s.instance_id,i.service_id,i.environment_code,i.zone_code,i.cell_code," +
                "s.fencing_token,s.lease_until,s.desired_version,s.actual_version,s.desired_hash,s.actual_hash,s.drift_state," +
                "i.maintenance_yn,i.drain_yn,i.drain_deadline_at,s.heartbeat_at,s.artifact_version,s.artifact_commit," +
                "s.runtime_role,s.registration_source,s.clock_skew_ms " +
                "FROM cpf_runtime_instance_state s JOIN cpf_service_instance i ON i.instance_id=s.instance_id WHERE 1=1");
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
                "FROM cpf_runtime_instance_feature_state f JOIN cpf_service_instance i ON i.instance_id=f.instance_id WHERE 1=1");
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
        Instant statusNow = serverNow();
        long expired=instances.stream().filter(r->r.leaseUntil()!=null&&!r.leaseUntil().isAfter(statusNow)).count();
        List<CpfRuntimeControllerStatus> controllerRows=jdbc.query(
                "SELECT holder_id,fencing_token,lease_until,last_reconciled_at FROM cpf_runtime_controller_lease WHERE lease_key='RUNTIME_CONTROL'",
                (rs,rowNum)->new CpfRuntimeControllerStatus(rs.getString("holder_id"),rs.getLong("fencing_token"),
                        toInstant(rs.getTimestamp("lease_until")),toInstant(rs.getTimestamp("last_reconciled_at"))));
        List<CpfRuntimeDeliveryCount> deliveries=jdbc.query(
                "SELECT delivery_state,COUNT(*) count_value FROM cpf_runtime_delivery GROUP BY delivery_state",
                (rs,rowNum)->new CpfRuntimeDeliveryCount(rs.getString("delivery_state"),rs.getLong("count_value")));
        return new CpfRuntimeStatus(instances,featureStates,controllerRows.isEmpty()?null:controllerRows.getFirst(),
                deliveries,instances.size(),drift,expired);
    }

    public com.cpf.core.api.runtimecontrol.CpfRuntimeControlHealth health(long lagSloSeconds) {
        long effectiveLagSloSeconds = Math.max(1L, lagSloSeconds);
        Instant now=serverNow();
        long instanceCountValue=nonNegativeCount(jdbc.queryForObject("SELECT COUNT(*) FROM cpf_runtime_instance_state",Long.class),"instanceCount");
        long backlogValue=nonNegativeCount(jdbc.queryForObject("SELECT COUNT(*) FROM cpf_runtime_delivery " +
                "WHERE delivery_state IN ('PENDING','FAILED','CLAIMED','RESTART_REQUIRED')",Long.class),"backlogCount");
        long poisonValue=nonNegativeCount(jdbc.queryForObject("SELECT COUNT(*) FROM cpf_runtime_delivery WHERE delivery_state='POISONED'",Long.class),"poisonedCount");
        long unknownValue=nonNegativeCount(jdbc.queryForObject("SELECT COUNT(*) FROM cpf_runtime_delivery WHERE delivery_state='UNKNOWN_RESULT'",Long.class),"unknownResultCount");
        long driftValue=nonNegativeCount(jdbc.queryForObject("SELECT COUNT(*) FROM cpf_runtime_instance_feature_state " +
                "WHERE drift_state IN ('DRIFT','UNKNOWN_RESULT','PENDING_RESTART')",Long.class),"driftCount");
        long expiredValue=nonNegativeCount(jdbc.queryForObject("SELECT COUNT(*) FROM cpf_runtime_instance_state WHERE lease_until<CURRENT_TIMESTAMP",Long.class),"expiredLeaseCount");
        int instanceCount=saturatingCount(instanceCountValue);
        int backlog=saturatingCount(backlogValue);
        int poison=saturatingCount(poisonValue);
        int unknown=saturatingCount(unknownValue);
        int drift=saturatingCount(driftValue);
        int expired=saturatingCount(expiredValue);
        boolean countSaturated=instanceCountValue>Integer.MAX_VALUE||backlogValue>Integer.MAX_VALUE
                ||poisonValue>Integer.MAX_VALUE||unknownValue>Integer.MAX_VALUE
                ||driftValue>Integer.MAX_VALUE||expiredValue>Integer.MAX_VALUE;
        Instant oldest=toInstant(jdbc.queryForObject(HEALTH_OLDEST_BACKLOG_SQL,Object.class));
        long lag=oldest==null?0L:Math.max(0L,java.time.Duration.between(oldest,now).getSeconds());
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
        if(lag>effectiveLagSloSeconds)reasons.add("DELIVERY_LAG_SLO_EXCEEDED");
        if(countSaturated)reasons.add("COUNT_SATURATED");
        boolean ready=leaderHealthy&&unknown==0;
        String healthStatus=ready?(reasons.isEmpty()?"UP":"DEGRADED"):"DOWN";
        return new com.cpf.core.api.runtimecontrol.CpfRuntimeControlHealth(
                ready,healthStatus,controllerId,controllerFence,instanceCount,backlog,poison,unknown,drift,expired,
                lag,lag>effectiveLagSloSeconds,reasons,now);
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
                            "FROM cpf_service_instance i JOIN cpf_runtime_instance_state s ON s.instance_id=i.instance_id " +
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
                            "drift_state,source_delivery_id,updated_at FROM cpf_runtime_instance_feature_state " +
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
        List<Map<String,Object>> rows=jdbc.queryForList("SELECT delivery_state,COUNT(*) cnt FROM cpf_runtime_delivery WHERE change_id=? GROUP BY delivery_state",changeId);
        LinkedHashMap<String,Number> result=new LinkedHashMap<>(); rows.forEach(r->result.put(String.valueOf(r.get("delivery_state")),(Number)r.get("cnt"))); return result;
    }

    /** 해당 변경의 현재 desired version을 기준으로 불일치가 남은 instance 수를 계산합니다. */
    public int driftCount(String changeId) {
        List<Map<String,Object>> changeRows = jdbc.queryForList(
                "SELECT change_type,desired_version FROM cpf_runtime_change WHERE change_id=?", changeId);
        if (changeRows.isEmpty()) return 0;
        String changeType = baseChangeType(nullable(changeRows.getFirst().get("change_type")));
        long desiredVersion = number(changeRows.getFirst().get("desired_version"));
        Integer value = jdbc.queryForObject(
                "SELECT COUNT(*) FROM cpf_runtime_delivery d " +
                        "JOIN cpf_runtime_instance_feature_state f ON f.instance_id=d.instance_id " +
                        "AND f.change_type=? AND f.desired_version=? " +
                        "WHERE d.change_id=? AND f.drift_state IN " +
                        "('PENDING','DRIFT','UNKNOWN_RESULT','PENDING_RESTART')",
                Integer.class,
                changeType, desiredVersion, changeId);
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
        String state = deriveChangeState(total, ack, failed, poison, unknown, restart);
        jdbc.update("UPDATE cpf_runtime_change SET change_state=?,updated_at=CURRENT_TIMESTAMP " +
                        "WHERE change_id=? AND change_state NOT IN ('CANCELLED','ROLLBACK_PENDING','ROLLED_BACK','EXPIRED','SUPERSEDED')",
                state, changeId);
        reconcileLinkedRollback(changeId, state);
    }


    static String deriveChangeState(int total, int acknowledged, int failed, int poisoned,
                                    int unknown, int restartRequired) {
        if (unknown > 0) return "UNKNOWN_RESULT";
        if (acknowledged == total && total > 0) return "SUCCESS";
        if (poisoned > 0) return "FAILED";
        if (failed > 0 || restartRequired > 0) return "PARTIAL";
        return "APPLYING";
    }

    private void assertFence(String instanceId,long fencingToken) {
        List<Map<String,Object>> rows=jdbc.queryForList(
                "SELECT fencing_token,lease_until FROM cpf_runtime_instance_state WHERE instance_id=? FOR UPDATE",
                instanceId);
        if(rows.isEmpty()) throw new com.cpf.core.api.runtimecontrol.CpfRuntimeFenceException("등록되지 않은 Runtime instance입니다: "+instanceId);
        Map<String,Object> row=rows.getFirst(); Instant until=toInstant(row.get("lease_until"));
        if(((Number)row.get("fencing_token")).longValue()!=fencingToken || until==null || !until.isAfter(serverNow()))
            throw new com.cpf.core.api.runtimecontrol.CpfRuntimeFenceException("Runtime fencing token 또는 lease가 유효하지 않습니다: "+instanceId);
    }


    Map<String,Object> saveGroup(String groupId,String groupName,String parentGroupId,String environment,String description,
                                        Long expectedVersion,boolean active,String operatorId) {
        requireText(groupId,"groupId"); requireText(groupName,"groupName");
        lockGroupCatalog();
        if (groupId.equals(parentGroupId)) throw new IllegalArgumentException("Runtime Group은 자기 자신을 parent로 지정할 수 없습니다.");
        if (parentGroupId!=null && !parentGroupId.isBlank()) {
            Integer parentCount=jdbc.queryForObject(
                    "SELECT COUNT(*) FROM cpf_runtime_instance_group WHERE group_id=?",
                    Integer.class,parentGroupId);
            if(parentCount==null||parentCount!=1)
                throw new IllegalArgumentException("Runtime parent Group을 찾을 수 없습니다: "+parentGroupId);
            assertNoGroupCycle(groupId,parentGroupId);
        }
        List<Map<String,Object>> rows=jdbc.queryForList("SELECT row_version FROM cpf_runtime_instance_group WHERE group_id=? FOR UPDATE",groupId);
        if(rows.isEmpty()) {
            if(expectedVersion!=null && expectedVersion!=0L) throw new com.cpf.core.api.runtimecontrol.CpfRuntimeVersionConflictException(expectedVersion,0L);
            boolean inserted = tryInsertUnique(
                    "INSERT INTO cpf_runtime_instance_group(group_id,group_name,parent_group_id,environment_code,description,active_yn,row_version,created_by,updated_by) VALUES (?,?,?,?,?,?,0,?,?)",
                    groupId,groupName,emptyToNull(parentGroupId),emptyToNull(environment),emptyToNull(description),active?"Y":"N",operatorId,operatorId);
            if (!inserted) {
                List<Map<String,Object>> winner = jdbc.queryForList(
                        "SELECT row_version FROM cpf_runtime_instance_group WHERE group_id=? FOR UPDATE", groupId);
                long actual = winner.isEmpty() ? -1L : ((Number) winner.getFirst().get("row_version")).longValue();
                throw new com.cpf.core.api.runtimecontrol.CpfRuntimeVersionConflictException(0L, actual);
            }
        } else {
            long current=((Number)rows.getFirst().get("row_version")).longValue();
            if(expectedVersion==null || expectedVersion.longValue()!=current) throw new com.cpf.core.api.runtimecontrol.CpfRuntimeVersionConflictException(expectedVersion==null?-1L:expectedVersion,current);
            nextMonotonic(current,"Runtime Group rowVersion");
            int updated = jdbc.update(
                    "UPDATE cpf_runtime_instance_group "
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
            if(updated!=1) throw new com.cpf.core.api.runtimecontrol.CpfRuntimeVersionConflictException(current,current);
        }
        return findGroup(groupId).orElseThrow();
    }

    Optional<Map<String,Object>> findGroup(String groupId) {
        List<Map<String,Object>> rows=jdbc
                .queryForList("SELECT group_id,group_name,parent_group_id,environment_code,description,active_yn,row_version,created_at,updated_at FROM cpf_runtime_instance_group WHERE group_id=?",groupId);
        if(rows.isEmpty()) return Optional.empty();
        Map<String,Object> result=new LinkedHashMap<>(rows.getFirst());
        List<String> members=jdbc.queryForList("SELECT instance_id FROM cpf_runtime_group_member WHERE group_id=? AND active_yn='Y' ORDER BY instance_id",String.class,groupId);
        result.put("instance_ids",members);
        return Optional.of(result);
    }

    Map<String,Object> changeGroupMember(String groupId,String instanceId,boolean active,String operatorId) {
        requireText(groupId,"groupId"); requireText(instanceId,"instanceId");
        lockGroupCatalog();
        List<Map<String,Object>> groupRows=jdbc.queryForList(
                "SELECT row_version FROM cpf_runtime_instance_group WHERE group_id=? FOR UPDATE",groupId);
        if(groupRows.isEmpty()) throw new IllegalArgumentException("Runtime Group을 찾을 수 없습니다: "+groupId);
        long groupVersion=number(groupRows.getFirst().get("row_version"));
        nextMonotonic(groupVersion,"Runtime Group rowVersion");
        Integer instanceCount=jdbc.queryForObject("SELECT COUNT(*) FROM cpf_service_instance WHERE instance_id=?",Integer.class,instanceId);
        if(instanceCount==null || instanceCount==0) throw new IllegalArgumentException("Runtime Instance를 찾을 수 없습니다: "+instanceId);

        String requestedState=active?"Y":"N";
        List<Map<String,Object>> current=jdbc.queryForList(
                "SELECT active_yn FROM cpf_runtime_group_member WHERE group_id=? AND instance_id=? FOR UPDATE",
                groupId,instanceId);
        boolean changed;
        if(current.isEmpty()) {
            changed=tryInsertUnique(
                    "INSERT INTO cpf_runtime_group_member(group_id,instance_id,active_yn,created_by,updated_by) VALUES (?,?,?,?,?)",
                    groupId,instanceId,requestedState,operatorId,operatorId);
            if(!changed) {
                List<Map<String,Object>> winner=jdbc.queryForList(
                        "SELECT active_yn FROM cpf_runtime_group_member WHERE group_id=? AND instance_id=? FOR UPDATE",
                        groupId,instanceId);
                if(winner.isEmpty()) throw new IllegalStateException(
                        "Runtime Group member 동시 등록 결과를 확인할 수 없습니다: "+groupId+"/"+instanceId);
                String winnerState=String.valueOf(winner.getFirst().get("active_yn"));
                if(requestedState.equalsIgnoreCase(winnerState)) return findGroup(groupId).orElseThrow();
                changed=jdbc.update(
                        "UPDATE cpf_runtime_group_member SET active_yn=?,updated_by=?,updated_at=CURRENT_TIMESTAMP " +
                                "WHERE group_id=? AND instance_id=? AND active_yn=?",
                        requestedState,operatorId,groupId,instanceId,winnerState)==1;
            }
        } else {
            String currentState=String.valueOf(current.getFirst().get("active_yn"));
            if(requestedState.equalsIgnoreCase(currentState)) return findGroup(groupId).orElseThrow();
            changed=jdbc.update(
                    "UPDATE cpf_runtime_group_member SET active_yn=?,updated_by=?,updated_at=CURRENT_TIMESTAMP " +
                            "WHERE group_id=? AND instance_id=? AND active_yn=?",
                    requestedState,operatorId,groupId,instanceId,currentState)==1;
        }
        if(!changed) throw new IllegalStateException("Runtime Group member 동시 갱신 실패: "+groupId+"/"+instanceId);
        int versioned=jdbc.update(
                "UPDATE cpf_runtime_instance_group SET row_version=row_version+1,updated_by=?," +
                        "updated_at=CURRENT_TIMESTAMP WHERE group_id=? AND row_version=?",
                operatorId,groupId,groupVersion);
        if(versioned!=1) throw new IllegalStateException("Runtime Group member version 갱신 실패: "+groupId);
        return findGroup(groupId).orElseThrow();
    }

    public void deleteGroup(String groupId,Long expectedVersion,String operatorId) {
        lockGroupCatalog();
        List<Map<String,Object>> rows=jdbc.queryForList("SELECT row_version FROM cpf_runtime_instance_group WHERE group_id=? FOR UPDATE",groupId);
        if(rows.isEmpty()) return;
        long current=((Number)rows.getFirst().get("row_version")).longValue();
        if(expectedVersion==null || expectedVersion.longValue()!=current) throw new com.cpf.core.api.runtimecontrol.CpfRuntimeVersionConflictException(expectedVersion==null?-1L:expectedVersion,current);
        Integer children=jdbc.queryForObject("SELECT COUNT(*) FROM cpf_runtime_instance_group WHERE parent_group_id=?",Integer.class,groupId);
        if(children!=null && children>0) throw new IllegalStateException("child Runtime Group이 있어 삭제할 수 없습니다: "+groupId);
        jdbc.update("DELETE FROM cpf_runtime_group_member WHERE group_id=?",groupId);
        if(jdbc.update("DELETE FROM cpf_runtime_instance_group WHERE group_id=? AND row_version=?",groupId,current)!=1) throw new com.cpf.core.api.runtimecontrol.CpfRuntimeVersionConflictException(current,current);
    }

    private void lockGroupCatalog() {
        // 빈 catalog에서는 SELECT ... FOR UPDATE가 어떤 row도 잠그지 못하므로 서로 다른
        // 최초 group이 동시에 A->B, B->A로 생성되는 write-skew를 막을 수 없습니다.
        // cpf_runtime_version의 고정 sentinel을 모든 topology mutation이 먼저 잠그도록 합니다.
        ensureRuntimeVersionRow("GROUP_CATALOG");
        List<Map<String, Object>> lock = jdbc.queryForList(
                "SELECT version_no FROM cpf_runtime_version WHERE version_key='GROUP_CATALOG' FOR UPDATE");
        if (lock.size() != 1) {
            throw new IllegalStateException("Runtime Group catalog sentinel을 잠글 수 없습니다.");
        }
    }

    private Instant serverNow() {
        Instant value = serverClock.get();
        if (value == null) {
            throw new IllegalStateException("Runtime server clock을 조회할 수 없습니다.");
        }
        return value;
    }

    private Instant queryDatabaseNow() {
        ensureRuntimeVersionRow("DB_CLOCK");
        java.sql.Timestamp value = jdbc.queryForObject(
                "SELECT CURRENT_TIMESTAMP FROM cpf_runtime_version WHERE version_key='DB_CLOCK'",
                java.sql.Timestamp.class);
        if (value == null) {
            throw new IllegalStateException("Runtime DB clock을 조회할 수 없습니다.");
        }
        return value.toInstant();
    }

    private void ensureRuntimeVersionRow(String versionKey) {
        requireText(versionKey, "versionKey");
        if (versionKey.length() > 40) {
            throw new IllegalArgumentException("Runtime versionKey는 40자를 초과할 수 없습니다.");
        }
        Integer existing = jdbc.queryForObject(
                "SELECT COUNT(*) FROM cpf_runtime_version WHERE version_key=?",
                Integer.class,
                versionKey);
        if (existing != null && existing == 1) {
            return;
        }
        if (existing != null && existing > 1) {
            throw new IllegalStateException("Runtime version sentinel이 중복되었습니다: " + versionKey);
        }
        if (!tryInsertUnique(
                "INSERT INTO cpf_runtime_version "
                        + "(version_key,version_no,created_by,updated_by) VALUES (?,0,'CPF','CPF')",
                versionKey)) {
            Integer raced = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM cpf_runtime_version WHERE version_key=?",
                    Integer.class,
                    versionKey);
            if (raced == null || raced != 1) {
                throw new IllegalStateException("Runtime version sentinel을 확인할 수 없습니다: " + versionKey);
            }
        }
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
        int updated = jdbc.update(
                "UPDATE cpf_service_instance "
                        + "SET service_id=?, endpoint_code=?, instance_name=?, base_url=?, "
                        + "environment_code=?, zone_code=?, cell_code=?, instance_status='UP', "
                        + "active_yn='Y', last_heartbeat_at=CURRENT_TIMESTAMP, "
                        + "updated_at=CURRENT_TIMESTAMP WHERE instance_id=?",
                r.serviceId(),
                r.endpointCode(),
                r.instanceId(),
                r.baseUrl(),
                blank(r.environment()),
                blank(r.zone()),
                blank(r.cell()),
                r.instanceId());
        if(updated==0 && !tryInsertUnique(
                "INSERT INTO cpf_service_instance("
                        + "instance_id, service_id, endpoint_code, instance_name, base_url, "
                        + "environment_code, zone_code, cell_code, instance_status, weight, "
                        + "priority_no, active_yn, maintenance_yn, drain_yn, "
                        + "last_heartbeat_at, created_by, updated_by) "
                        + "VALUES (?,?,?,?,?,?,?,?,'UP',100,100,'Y','N','N',"
                        + "CURRENT_TIMESTAMP,'CPF','CPF')",
                r.instanceId(),
                r.serviceId(),
                r.endpointCode(),
                r.instanceId(),
                r.baseUrl(),
                blank(r.environment()),
                blank(r.zone()),
                blank(r.cell()))) {
            int racedUpdate = jdbc.update(
                    "UPDATE cpf_service_instance SET service_id=?,endpoint_code=?,base_url=?," +
                            "environment_code=?,zone_code=?,cell_code=?,instance_status='UP',active_yn='Y'," +
                            "last_heartbeat_at=CURRENT_TIMESTAMP,updated_at=CURRENT_TIMESTAMP " +
                            "WHERE instance_id=?",
                    r.serviceId(), r.endpointCode(), r.baseUrl(), blank(r.environment()), blank(r.zone()),
                    blank(r.cell()), r.instanceId());
            if (racedUpdate != 1) {
                throw new IllegalStateException("Service instance 동시 upsert 실패: " + r.instanceId());
            }
        }
    }

    /**
     * 공식 DB 3종에서 Transaction을 abort시키지 않고 unique-key race를 판정합니다.
     * PostgreSQL은 duplicate insert 뒤 동일 Transaction 사용이 불가능하므로 JDBC Savepoint로
     * 해당 statement만 rollback합니다. FK/CHECK 등 다른 무결성 위반은 숨기지 않습니다.
     */
    private boolean tryInsertUnique(String sql, Object... args) {
        return Boolean.TRUE.equals(jdbc.execute((org.springframework.jdbc.core.ConnectionCallback<Boolean>) connection -> {
            java.sql.Savepoint savepoint = connection.getAutoCommit() ? null : connection.setSavepoint();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                for (int index = 0; index < args.length; index++) {
                    statement.setObject(index + 1, args[index]);
                }
                boolean inserted = statement.executeUpdate() == 1;
                releaseSavepoint(connection, savepoint);
                return inserted;
            } catch (java.sql.SQLException failure) {
                if (savepoint != null) {
                    connection.rollback(savepoint);
                }
                releaseSavepoint(connection, savepoint);
                if (isDuplicateKey(failure)) {
                    return false;
                }
                throw failure;
            }
        }));
    }

    private boolean isDuplicateKey(java.sql.SQLException failure) {
        for (java.sql.SQLException current = failure; current != null; current = current.getNextException()) {
            String state = current.getSQLState();
            int vendorCode = current.getErrorCode();
            if ("23505".equals(state)
                    || vendorCode == 1062
                    || (vendorCode == 1 && "23000".equals(state))) {
                return true;
            }
        }
        return false;
    }

    private void releaseSavepoint(java.sql.Connection connection, java.sql.Savepoint savepoint) {
        if (savepoint == null) return;
        try {
            connection.releaseSavepoint(savepoint);
        } catch (java.sql.SQLException ignored) {
            // 일부 Driver는 rollback 직후 releaseSavepoint를 지원하지 않습니다.
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
            Instant eventAt = canonicalAuditInstant(toInstant(row.get("created_at")));
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

    static Instant canonicalAuditInstant(Instant value) {
        return value == null ? null : value.truncatedTo(java.time.temporal.ChronoUnit.SECONDS);
    }

    private void appendAudit(String changeId,String eventType,String actor,String reason,String evidenceHash) {
        // 같은 Change의 audit append를 직렬화해 hash-chain fork를 방지합니다.
        jdbc.queryForList("SELECT change_id FROM cpf_runtime_change WHERE change_id=? FOR UPDATE", changeId);
        List<Map<String,Object>> rows=jdbc.queryForList(
                "SELECT chain_hash FROM cpf_runtime_change_audit WHERE change_id=? ORDER BY audit_id",changeId);
        String previous=rows.isEmpty()?"GENESIS":String.valueOf(rows.getLast().get("chain_hash"));
        Instant eventAt=canonicalAuditInstant(serverNow());
        String normalizedEvent=truncate(blank(eventType),60);
        String normalizedActor=truncate(blank(actor),100);
        String normalizedReason=sanitizeRuntimeMessage(blank(reason),500);
        String normalizedEvidence=canonicalAuditEvidence(normalizedEvent,evidenceHash);
        String current=CpfRuntimeCanonicalHash.sha256(Map.of(
                "previous",previous,"changeId",changeId,"eventType",normalizedEvent,
                "actor",normalizedActor,"reason",normalizedReason,
                "evidenceHash",blank(normalizedEvidence),"at",eventAt.toString()));
        jdbc.update("INSERT INTO cpf_runtime_change_audit(change_id,event_type,actor_id,reason,evidence_hash," +
                        "previous_hash,chain_hash,created_by,created_at) VALUES (?,?,?,?,?,?,?,'CPF',?)",
                changeId,normalizedEvent,normalizedActor,normalizedReason,normalizedEvidence,previous,current,ts(eventAt));
    }

    static String canonicalAuditEvidence(String eventType,String evidence) {
        if(evidence==null||evidence.isBlank()) return null;
        String value=evidence.trim();
        String event=eventType==null?"":eventType.trim().toUpperCase(java.util.Locale.ROOT);
        // Rollback 관계는 현재 Canonical Schema에 별도 FK 컬럼이 없어 UUID를 직접 보존합니다.
        if(event.startsWith("ROLLBACK_")) {
            if(value.length()>64) throw new IllegalArgumentException("Rollback audit link는 64자를 초과할 수 없습니다.");
            return value;
        }
        if(value.matches("(?i)[0-9a-f]{64}")) return value.toLowerCase(java.util.Locale.ROOT);
        return CpfRuntimeCanonicalHash.sha256Hex(Map.of("eventType",event,"evidence",value));
    }

    private String write(Object value){try{return objectMapper.writeValueAsString(value);}catch(Exception ex){throw new IllegalArgumentException("Runtime JSON 직렬화 실패",ex);}}
    private Map<String,Object> readMap(String json){try{return objectMapper.readValue(json,new TypeReference<>(){});}catch(Exception ex){throw new IllegalStateException("Runtime payload JSON 역직렬화 실패",ex);}}
    private Map<String,Object> readMapOrEmpty(String json){if(json==null||json.isBlank())return Map.of();Map<String,Object> value=readMap(json);return value==null?Map.of():value;}
    public String json(Object value){return write(value);}
    <T> T readJson(String value,Class<T> type){
        try{return objectMapper.readValue(value,type);}catch(Exception ex){throw new IllegalStateException("Runtime JSON 결과 역직렬화 실패",ex);}
    }
    private Map<String,Object> jsonMap(String value){return readMap(value);}
    private Timestamp ts(Instant value){return value==null?null:Timestamp.from(value);}
    private Instant toInstant(Object value) {
        if (value == null) return null;
        if (value instanceof Instant instant) return instant;
        if (value instanceof Timestamp timestamp) return timestamp.toInstant();
        if (value instanceof java.util.Date date) return date.toInstant();
        if (value instanceof CharSequence text) {
            try {
                return Instant.parse(text.toString().trim());
            } catch (java.time.format.DateTimeParseException ex) {
                throw new IllegalStateException("Runtime DB 시간 값을 해석할 수 없습니다: " + text, ex);
            }
        }
        throw new IllegalStateException(
                "Runtime DB 시간 값의 타입이 올바르지 않습니다: " + value.getClass().getName());
    }

    private long number(Object value) {
        if (!(value instanceof Number number)) {
            throw new IllegalStateException("Runtime DB 필수 숫자 값이 누락되었거나 타입이 올바르지 않습니다.");
        }
        return number.longValue();
    }
    static long nextMonotonic(long current,String name){
        if(current<0L||current==Long.MAX_VALUE){
            throw new IllegalStateException(name+" 증가 한계를 초과했습니다: "+current);
        }
        return current+1L;
    }
    static long nonNegativeCount(Object value,String name){
        long count=value==null?0L:((Number)value).longValue();
        if(count<0L)throw new IllegalStateException(name+" 집계가 음수입니다: "+count);
        return count;
    }
    static int saturatingCount(long value){return value>Integer.MAX_VALUE?Integer.MAX_VALUE:(int)value;}
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
