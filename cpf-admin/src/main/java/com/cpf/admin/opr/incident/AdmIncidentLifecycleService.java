package com.cpf.admin.opr.incident;

import com.cpf.data.persistence.api.annotation.CpfTransactional;
import com.cpf.admin.opr.service.AdmAuditLogService;
import com.cpf.core.api.error.CpfValidationException;
import com.cpf.core.api.context.CpfContexts;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import com.cpf.foundation.annotation.CpfService;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static com.cpf.admin.opr.incident.AdmIncidentContracts.*;

/**
 * 알림 규칙에서 Incident 생성·승인 조치·Escalation·Maintenance까지 하나의 원장으로 관리합니다.
 *
 * <p>활성 Incident는 {@code active_key} 고유키로 다중 인스턴스 중복 생성을 차단하고,
 * 모든 변경은 expectedVersion·idempotencyKey·사유·승인 ID를 검증한 뒤 immutable timeline과
 * ADM audit에 함께 기록합니다.</p>
 */
@CpfService
public class AdmIncidentLifecycleService {
    private final JdbcTemplate jdbc;
    private final AdmAuditLogService audit;
    private final Clock clock;

    public AdmIncidentLifecycleService(
            @Qualifier("cpfJdbcTemplate") JdbcTemplate jdbc,
            AdmAuditLogService audit,
            Clock clock) {
        this.jdbc = jdbc;
        this.audit = audit;
        this.clock = clock;
    }

    public Page<PolicyResponse> findPolicies(int page, int size) {
        int p = page(page); int s = size(size); long total = count("adm_incident_policy");
        List<PolicyResponse> rows = jdbc.query("""
                SELECT * FROM (
                  SELECT p.*, ROW_NUMBER() OVER (ORDER BY p.policy_id DESC) AS rn
                  FROM adm_incident_policy p
                ) x WHERE x.rn > ? AND x.rn <= ? ORDER BY x.rn
                """, (rs, n) -> new PolicyResponse(
                rs.getLong("policy_id"), rs.getString("policy_code"), rs.getString("event_type"),
                rs.getString("event_sub_type"), rs.getString("severity"), rs.getInt("threshold_count"),
                rs.getInt("window_seconds"), rs.getInt("escalation_minutes"), rs.getString("receiver_group"),
                rs.getString("use_yn"), rs.getLong("version"), rs.getString("created_by"),
                local(rs.getTimestamp("created_at")), rs.getString("updated_by"), local(rs.getTimestamp("updated_at"))),
                p * s, (p + 1) * s);
        return page(rows, p, s, total);
    }

    @CpfTransactional
    public PolicyResponse savePolicy(Long policyId, PolicySaveRequest request, String operatorId, String clientIp) {
        requireMutation(request.reason(), request.approvalRequestId(), request.idempotencyKey());
        String requestHash = hash(request.toString());
        CommandReservation reservation = reserve("POLICY_SAVE", request.idempotencyKey(), requestHash, operatorId);
        if (reservation.replayed()) return findPolicy(Long.parseLong(reservation.resultRef()));
        long id;
        if (policyId == null) {
            KeyHolder holder = new GeneratedKeyHolder();
            int inserted = jdbc.update(c -> {
                PreparedStatement ps = c.prepareStatement("""
                        INSERT INTO adm_incident_policy
                        (policy_code,event_type,event_sub_type,severity,threshold_count,window_seconds,
                         escalation_minutes,receiver_group,use_yn,version,created_by,updated_by,created_at,updated_at)
                        VALUES (?,?,?,?,?,?,?,?,?,0,?,?,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)
                        """, new String[]{"policy_id"});
                int i=1; ps.setString(i++, required(request.policyCode(), "policyCode"));
                ps.setString(i++, required(request.eventType(), "eventType")); ps.setString(i++, blank(request.eventSubType()));
                ps.setString(i++, normalizeSeverity(request.severity())); ps.setInt(i++, positive(request.thresholdCount(), "thresholdCount"));
                ps.setInt(i++, positive(request.windowSeconds(), "windowSeconds")); ps.setInt(i++, positive(request.escalationMinutes(), "escalationMinutes"));
                ps.setString(i++, required(request.receiverGroup(), "receiverGroup")); ps.setString(i++, yn(request.useYn()));
                ps.setString(i++, operatorId); ps.setString(i, operatorId); return ps;
            }, holder);
            if (inserted != 1 || holder.getKey() == null) throw new IllegalStateException("Incident policy insert failed");
            id = holder.getKey().longValue();
        } else {
            int updated = jdbc.update("""
                    UPDATE adm_incident_policy SET policy_code=?, event_type=?, event_sub_type=?, severity=?,
                      threshold_count=?, window_seconds=?, escalation_minutes=?, receiver_group=?, use_yn=?,
                      version=version+1, updated_by=?, updated_at=CURRENT_TIMESTAMP
                    WHERE policy_id=? AND version=?
                    """, required(request.policyCode(), "policyCode"), required(request.eventType(), "eventType"),
                    blank(request.eventSubType()), normalizeSeverity(request.severity()), positive(request.thresholdCount(), "thresholdCount"),
                    positive(request.windowSeconds(), "windowSeconds"), positive(request.escalationMinutes(), "escalationMinutes"),
                    required(request.receiverGroup(), "receiverGroup"), yn(request.useYn()), operatorId, policyId, request.expectedVersion());
            if (updated != 1) throw new AdmIncidentConflictException(AdmIncidentConflictException.Type.VERSION_CONFLICT, "Incident policy version conflict");
            id = policyId;
        }
        completeCommand(reservation.commandId(), String.valueOf(id));
        audit.record(CpfContexts.transactionId(), operatorId, "INCIDENT_POLICY_SAVE", "adm_incident_policy",
                String.valueOf(id), request.reason(), null, String.valueOf(findPolicy(id)), request.approvalRequestId(), clientIp);
        return findPolicy(id);
    }

    public Page<IncidentResponse> findIncidents(String status, int page, int size) {
        int p=page(page), s=size(size); String normalized = blank(status);
        long total = normalized == null ? count("adm_incident_lifecycle") : jdbc.queryForObject(
                "SELECT COUNT(*) FROM adm_incident_lifecycle WHERE status=?", Long.class, normalized.toUpperCase(Locale.ROOT));
        String where = normalized == null ? "" : " WHERE i.status=?";
        Object[] args = normalized == null ? new Object[]{p*s,(p+1)*s} : new Object[]{normalized.toUpperCase(Locale.ROOT),p*s,(p+1)*s};
        List<IncidentResponse> rows = jdbc.query("""
                SELECT * FROM (
                  SELECT i.*, ROW_NUMBER() OVER (ORDER BY i.last_occurred_at DESC, i.incident_id DESC) rn
                  FROM adm_incident_lifecycle i%s
                ) x WHERE x.rn > ? AND x.rn <= ? ORDER BY x.rn
                """.formatted(where), (rs,n)->incident(rs), args);
        return page(rows,p,s,total);
    }

    public IncidentResponse findIncident(long incidentId) {
        try {
            return jdbc.queryForObject("SELECT * FROM adm_incident_lifecycle WHERE incident_id=?", (rs,n)->incident(rs), incidentId);
        } catch (EmptyResultDataAccessException e) {
            throw new AdmIncidentConflictException(AdmIncidentConflictException.Type.NOT_FOUND, "Incident not found: " + incidentId);
        }
    }

    public List<TimelineResponse> findTimeline(long incidentId) {
        return jdbc.query("""
                SELECT timeline_id,incident_id,action_type,before_status,after_status,reason,
                       approval_request_id,actor_id,created_at
                FROM adm_incident_timeline WHERE incident_id=? ORDER BY timeline_id
                """, (rs,n)->new TimelineResponse(rs.getLong(1),rs.getLong(2),rs.getString(3),rs.getString(4),
                rs.getString(5),rs.getString(6),rs.getString(7),rs.getString(8),local(rs.getTimestamp(9))), incidentId);
    }

    @CpfTransactional
    public SignalResult ingestSignal(SignalRequest request, String operatorId) {
        String idempotency = required(request.idempotencyKey(), "idempotencyKey");
        CommandReservation reservation = reserve("SIGNAL", idempotency, hash(request.toString()), operatorId);
        if (reservation.replayed()) {
            Long incidentId = "NONE".equals(reservation.resultRef()) ? null : Long.valueOf(reservation.resultRef());
            return new SignalResult(-1, incidentId, "REPLAY", false, 0, 0);
        }
        PolicyResponse policy = findPolicyByCode(required(request.policyCode(), "policyCode"));
        LocalDateTime occurredAt = request.occurredAt() == null ? LocalDateTime.now(clock) : request.occurredAt();
        boolean maintenance = isMaintenance(request.sourceType(), request.sourceId(), occurredAt);
        long signalId = insertSignal(policy.policyId(), request, occurredAt, maintenance, operatorId);
        if (maintenance) {
            completeCommand(reservation.commandId(), "NONE");
            return new SignalResult(signalId, null, "SUPPRESSED_MAINTENANCE", true, 0, policy.thresholdCount());
        }
        LocalDateTime from = occurredAt.minusSeconds(policy.windowSeconds());
        Integer observed = jdbc.queryForObject("""
                SELECT COUNT(*) FROM adm_incident_signal
                WHERE policy_id=? AND source_type=? AND source_id=? AND suppressed_yn='N' AND occurred_at BETWEEN ? AND ?
                """, Integer.class, policy.policyId(), required(request.sourceType(),"sourceType"), required(request.sourceId(),"sourceId"),
                Timestamp.valueOf(from), Timestamp.valueOf(occurredAt));
        if (observed == null || observed < policy.thresholdCount()) {
            completeCommand(reservation.commandId(), "NONE");
            return new SignalResult(signalId, null, "BELOW_THRESHOLD", false, observed == null ? 0 : observed, policy.thresholdCount());
        }
        long incidentId = upsertActiveIncident(policy, request, occurredAt, observed, operatorId);
        completeCommand(reservation.commandId(), String.valueOf(incidentId));
        return new SignalResult(signalId, incidentId, "INCIDENT_OPEN", false, observed, policy.thresholdCount());
    }

    @CpfTransactional
    public IncidentResponse transition(long incidentId, String action, IncidentActionRequest request, String operatorId, String clientIp) {
        requireMutation(request.reason(), request.approvalRequestId(), request.idempotencyKey());
        CommandReservation reservation = reserve("INCIDENT_" + action, request.idempotencyKey(), hash(incidentId+":"+action+":"+request), operatorId);
        if (reservation.replayed()) return findIncident(incidentId);
        IncidentResponse before = findIncident(incidentId);
        requireTransition(before.status(), action);
        String after = switch (action) {
            case "ACKNOWLEDGE" -> "ACKNOWLEDGED";
            case "RESOLVE" -> "RESOLVED";
            case "REOPEN" -> "OPEN";
            case "ESCALATE" -> before.status();
            default -> throw new CpfValidationException("Unsupported incident action: " + action);
        };
        String activeKey = "RESOLVED".equals(after) ? null : activeKey(before.policyId(), before.sourceType(), before.sourceId());
        int updated;
        try {
            updated = jdbc.update("""
                    UPDATE adm_incident_lifecycle SET status=?, active_key=?, escalation_level=escalation_level+?,
                      acknowledged_at=CASE WHEN ?='ACKNOWLEDGED' THEN CURRENT_TIMESTAMP ELSE acknowledged_at END,
                      resolved_at=CASE WHEN ?='RESOLVED' THEN CURRENT_TIMESTAMP ELSE NULL END,
                      owner_id=?, version=version+1, updated_by=?, updated_at=CURRENT_TIMESTAMP
                    WHERE incident_id=? AND version=?
                    """, after, activeKey, "ESCALATE".equals(action)?1:0, after, after, operatorId, operatorId, incidentId, request.expectedVersion());
        } catch (DuplicateKeyException duplicate) {
            throw new AdmIncidentConflictException(AdmIncidentConflictException.Type.ACTIVE_CONFLICT,
                    "Another active incident already exists for the same policy and source");
        }
        if (updated != 1) throw new AdmIncidentConflictException(AdmIncidentConflictException.Type.VERSION_CONFLICT, "Incident version conflict");
        insertTimeline(incidentId, action, before.status(), after, request.reason(), request.approvalRequestId(), operatorId);
        completeCommand(reservation.commandId(), String.valueOf(incidentId));
        IncidentResponse result = findIncident(incidentId);
        audit.record(CpfContexts.transactionId(), operatorId, "INCIDENT_"+action, "adm_incident_lifecycle",
                String.valueOf(incidentId), request.reason(), String.valueOf(before), String.valueOf(result), request.approvalRequestId(), clientIp);
        return result;
    }

    /** 해결된 Incident의 원인·조치·재발방지 내용을 immutable timeline과 Audit에 남깁니다. */
    @CpfTransactional
    public IncidentResponse recordPostmortem(long incidentId, IncidentActionRequest request, String operatorId, String clientIp) {
        requireMutation(request.reason(), request.approvalRequestId(), request.idempotencyKey());
        CommandReservation reservation = reserve("INCIDENT_POSTMORTEM", request.idempotencyKey(), hash(incidentId+":POSTMORTEM:"+request), operatorId);
        if (reservation.replayed()) return findIncident(incidentId);
        IncidentResponse before = findIncident(incidentId);
        if (!"RESOLVED".equals(before.status())) {
            throw new AdmIncidentConflictException(AdmIncidentConflictException.Type.INVALID_TRANSITION,
                    "Postmortem is allowed only after incident resolution");
        }
        int updated = jdbc.update("UPDATE adm_incident_lifecycle SET version=version+1, updated_by=?, updated_at=CURRENT_TIMESTAMP WHERE incident_id=? AND version=?",
                operatorId, incidentId, request.expectedVersion());
        if (updated != 1) throw new AdmIncidentConflictException(AdmIncidentConflictException.Type.VERSION_CONFLICT, "Incident version conflict");
        insertTimeline(incidentId, "POSTMORTEM", before.status(), before.status(), request.reason(), request.approvalRequestId(), operatorId);
        completeCommand(reservation.commandId(), String.valueOf(incidentId));
        IncidentResponse result = findIncident(incidentId);
        audit.record(CpfContexts.transactionId(), operatorId, "INCIDENT_POSTMORTEM", "adm_incident_lifecycle", String.valueOf(incidentId),
                request.reason(), String.valueOf(before), String.valueOf(result), request.approvalRequestId(), clientIp);
        return result;
    }

    public Page<MaintenanceResponse> findMaintenance(int page, int size) {
        int p=page(page), s=size(size); long total=count("adm_maintenance_window");
        List<MaintenanceResponse> rows=jdbc.query("""
                SELECT * FROM (SELECT m.*,ROW_NUMBER() OVER(ORDER BY m.starts_at DESC,m.maintenance_id DESC) rn
                FROM adm_maintenance_window m) x WHERE x.rn>? AND x.rn<=? ORDER BY x.rn
                """,(rs,n)->maintenance(rs),p*s,(p+1)*s);
        return page(rows,p,s,total);
    }

    @CpfTransactional
    public MaintenanceResponse saveMaintenance(Long id, MaintenanceSaveRequest request, String operatorId, String clientIp) {
        requireMutation(request.reason(),request.approvalRequestId(),request.idempotencyKey());
        if (request.startsAt()==null || request.endsAt()==null || !request.endsAt().isAfter(request.startsAt()))
            throw new CpfValidationException("Maintenance endsAt must be after startsAt");
        CommandReservation reservation=reserve("MAINTENANCE_SAVE",request.idempotencyKey(),hash(request.toString()),operatorId);
        if(reservation.replayed()) return findMaintenanceById(Long.parseLong(reservation.resultRef()));
        long target;
        if(id==null){
            KeyHolder h=new GeneratedKeyHolder();
            int inserted=jdbc.update(c->{PreparedStatement ps=c.prepareStatement("""
                    INSERT INTO adm_maintenance_window(maintenance_code,target_type,target_id,starts_at,ends_at,use_yn,version,
                    created_by,updated_by,created_at,updated_at) VALUES(?,?,?,?,?,?,0,?,?,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)
                    """,new String[]{"maintenance_id"});
                ps.setString(1,required(request.maintenanceCode(),"maintenanceCode")); ps.setString(2,required(request.targetType(),"targetType"));
                ps.setString(3,required(request.targetId(),"targetId")); ps.setTimestamp(4,Timestamp.valueOf(request.startsAt()));
                ps.setTimestamp(5,Timestamp.valueOf(request.endsAt())); ps.setString(6,yn(request.useYn()));
                ps.setString(7,operatorId);ps.setString(8,operatorId);return ps;},h);
            if(inserted!=1||h.getKey()==null)throw new IllegalStateException("Maintenance insert failed"); target=h.getKey().longValue();
        } else {
            int updated=jdbc.update("""
                    UPDATE adm_maintenance_window SET maintenance_code=?,target_type=?,target_id=?,starts_at=?,ends_at=?,use_yn=?,
                    version=version+1,updated_by=?,updated_at=CURRENT_TIMESTAMP WHERE maintenance_id=? AND version=?
                    """,required(request.maintenanceCode(),"maintenanceCode"),required(request.targetType(),"targetType"),
                    required(request.targetId(),"targetId"),Timestamp.valueOf(request.startsAt()),Timestamp.valueOf(request.endsAt()),
                    yn(request.useYn()),operatorId,id,request.expectedVersion());
            if(updated!=1)throw new AdmIncidentConflictException(AdmIncidentConflictException.Type.VERSION_CONFLICT,"Maintenance version conflict");target=id;
        }
        completeCommand(reservation.commandId(),String.valueOf(target));
        MaintenanceResponse result=findMaintenanceById(target);
        audit.record(CpfContexts.transactionId(),operatorId,"MAINTENANCE_SAVE","adm_maintenance_window",
                String.valueOf(target),request.reason(),null,String.valueOf(result),request.approvalRequestId(),clientIp);
        return result;
    }

    private long upsertActiveIncident(PolicyResponse policy, SignalRequest request, LocalDateTime occurredAt, int observed, String operatorId) {
        String key=activeKey(policy.policyId(),request.sourceType(),request.sourceId());
        try {
            KeyHolder holder=new GeneratedKeyHolder();
            jdbc.update(c->{PreparedStatement ps=c.prepareStatement("""
                    INSERT INTO adm_incident_lifecycle(policy_id,policy_code,severity,status,title,summary,source_type,source_id,
                    correlation_id,transaction_id,occurrence_count,escalation_level,first_occurred_at,last_occurred_at,
                    active_key,version,created_by,updated_by,created_at,updated_at)
                    VALUES(?,?,?,'OPEN',?,?,?,?,?,?,?,0,?,?,?,0,?,?,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)
                    """,new String[]{"incident_id"});
                int i=1;ps.setLong(i++,policy.policyId());ps.setString(i++,policy.policyCode());ps.setString(i++,policy.severity());
                ps.setString(i++,required(request.title(),"title"));ps.setString(i++,blank(request.summary()));
                ps.setString(i++,required(request.sourceType(),"sourceType"));ps.setString(i++,required(request.sourceId(),"sourceId"));
                ps.setString(i++,blank(request.correlationId()));ps.setString(i++,blank(request.transactionId()));ps.setInt(i++,observed);
                ps.setTimestamp(i++,Timestamp.valueOf(occurredAt));ps.setTimestamp(i++,Timestamp.valueOf(occurredAt));ps.setString(i++,key);
                ps.setString(i++,operatorId);ps.setString(i,operatorId);return ps;},holder);
            long id=Objects.requireNonNull(holder.getKey()).longValue();
            insertTimeline(id,"OPEN",null,"OPEN","threshold reached",null,operatorId);return id;
        } catch(DuplicateKeyException race){
            int updated=jdbc.update("""
                    UPDATE adm_incident_lifecycle SET occurrence_count=occurrence_count+1,last_occurred_at=?,summary=?,version=version+1,
                    updated_by=?,updated_at=CURRENT_TIMESTAMP WHERE active_key=?
                    """,Timestamp.valueOf(occurredAt),blank(request.summary()),operatorId,key);
            if(updated!=1)throw race;
            return jdbc.queryForObject("SELECT incident_id FROM adm_incident_lifecycle WHERE active_key=?",Long.class,key);
        }
    }

    private long insertSignal(long policyId, SignalRequest r, LocalDateTime occurredAt, boolean suppressed, String operatorId){
        KeyHolder h=new GeneratedKeyHolder();jdbc.update(c->{PreparedStatement ps=c.prepareStatement("""
                INSERT INTO adm_incident_signal(policy_id,source_type,source_id,correlation_id,transaction_id,title,summary,
                occurred_at,suppressed_yn,idempotency_key,created_by,created_at) VALUES(?,?,?,?,?,?,?,?,?,?,?,CURRENT_TIMESTAMP)
                """,new String[]{"signal_id"});int i=1;ps.setLong(i++,policyId);ps.setString(i++,required(r.sourceType(),"sourceType"));
            ps.setString(i++,required(r.sourceId(),"sourceId"));ps.setString(i++,blank(r.correlationId()));ps.setString(i++,blank(r.transactionId()));
            ps.setString(i++,required(r.title(),"title"));ps.setString(i++,blank(r.summary()));ps.setTimestamp(i++,Timestamp.valueOf(occurredAt));
            ps.setString(i++,suppressed?"Y":"N");ps.setString(i++,required(r.idempotencyKey(),"idempotencyKey"));ps.setString(i,operatorId);return ps;},h);
        if(h.getKey()==null)throw new IllegalStateException("Incident signal insert failed");return h.getKey().longValue();}

    private boolean isMaintenance(String targetType,String targetId,LocalDateTime at){
        Integer count=jdbc.queryForObject("""
                SELECT COUNT(*) FROM adm_maintenance_window WHERE use_yn='Y' AND starts_at<=? AND ends_at>?
                AND (target_type='ALL' OR (target_type=? AND target_id=?))
                """,Integer.class,Timestamp.valueOf(at),Timestamp.valueOf(at),required(targetType,"targetType"),required(targetId,"targetId"));
        return count!=null&&count>0;
    }

    private PolicyResponse findPolicy(long id){try{return jdbc.queryForObject("SELECT * FROM adm_incident_policy WHERE policy_id=?",(rs,n)->new PolicyResponse(
            rs.getLong("policy_id"),rs.getString("policy_code"),rs.getString("event_type"),rs.getString("event_sub_type"),
            rs.getString("severity"),rs.getInt("threshold_count"),rs.getInt("window_seconds"),rs.getInt("escalation_minutes"),
            rs.getString("receiver_group"),rs.getString("use_yn"),rs.getLong("version"),rs.getString("created_by"),
            local(rs.getTimestamp("created_at")),rs.getString("updated_by"),local(rs.getTimestamp("updated_at"))),id);}
        catch(EmptyResultDataAccessException e){throw new AdmIncidentConflictException(AdmIncidentConflictException.Type.NOT_FOUND,"Incident policy not found: "+id);}}
    private PolicyResponse findPolicyByCode(String code){return jdbc.queryForObject("SELECT * FROM adm_incident_policy WHERE policy_code=? AND use_yn='Y'",(rs,n)->new PolicyResponse(
            rs.getLong("policy_id"),rs.getString("policy_code"),rs.getString("event_type"),rs.getString("event_sub_type"),
            rs.getString("severity"),rs.getInt("threshold_count"),rs.getInt("window_seconds"),rs.getInt("escalation_minutes"),
            rs.getString("receiver_group"),rs.getString("use_yn"),rs.getLong("version"),rs.getString("created_by"),
            local(rs.getTimestamp("created_at")),rs.getString("updated_by"),local(rs.getTimestamp("updated_at"))),code);}
    private MaintenanceResponse findMaintenanceById(long id){try{return jdbc.queryForObject("SELECT * FROM adm_maintenance_window WHERE maintenance_id=?",(rs,n)->maintenance(rs),id);}
        catch(EmptyResultDataAccessException e){throw new AdmIncidentConflictException(AdmIncidentConflictException.Type.NOT_FOUND,"Maintenance window not found: "+id);}}
    private IncidentResponse incident(java.sql.ResultSet rs)throws java.sql.SQLException{return new IncidentResponse(rs.getLong("incident_id"),rs.getLong("policy_id"),
            rs.getString("policy_code"),rs.getString("severity"),rs.getString("status"),rs.getString("title"),rs.getString("summary"),rs.getString("source_type"),rs.getString("source_id"),
            rs.getString("correlation_id"),rs.getString("transaction_id"),rs.getInt("occurrence_count"),rs.getInt("escalation_level"),local(rs.getTimestamp("first_occurred_at")),
            local(rs.getTimestamp("last_occurred_at")),local(rs.getTimestamp("acknowledged_at")),local(rs.getTimestamp("resolved_at")),rs.getString("owner_id"),rs.getLong("version"),
            rs.getString("created_by"),local(rs.getTimestamp("created_at")),rs.getString("updated_by"),local(rs.getTimestamp("updated_at")));}
    private MaintenanceResponse maintenance(java.sql.ResultSet rs)throws java.sql.SQLException{return new MaintenanceResponse(rs.getLong("maintenance_id"),rs.getString("maintenance_code"),
            rs.getString("target_type"),rs.getString("target_id"),local(rs.getTimestamp("starts_at")),local(rs.getTimestamp("ends_at")),rs.getString("use_yn"),rs.getLong("version"),
            rs.getString("created_by"),local(rs.getTimestamp("created_at")),rs.getString("updated_by"),local(rs.getTimestamp("updated_at")));}
    private void insertTimeline(long incidentId,String action,String before,String after,String reason,String approval,String actor){jdbc.update("""
            INSERT INTO adm_incident_timeline(incident_id,action_type,before_status,after_status,reason,approval_request_id,actor_id,created_at)
            VALUES(?,?,?,?,?,?,?,CURRENT_TIMESTAMP)
            """,incidentId,action,before,after,reason,approval,actor);}

    private CommandReservation reserve(String type,String key,String requestHash,String actor){
        try{KeyHolder h=new GeneratedKeyHolder();jdbc.update(c->{PreparedStatement ps=c.prepareStatement("""
                INSERT INTO adm_incident_command(command_type,idempotency_key,request_hash,status,created_by,created_at,updated_at)
                VALUES(?,?,?,'RUNNING',?,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)
                """,new String[]{"command_id"});ps.setString(1,type);ps.setString(2,required(key,"idempotencyKey"));ps.setString(3,requestHash);ps.setString(4,actor);return ps;},h);
            return new CommandReservation(Objects.requireNonNull(h.getKey()).longValue(),false,null);
        }catch(DuplicateKeyException duplicate){Map<String,Object> row=jdbc.queryForMap("SELECT command_id,request_hash,status,result_ref FROM adm_incident_command WHERE idempotency_key=?",key);
            if(!requestHash.equals(row.get("request_hash")))throw new AdmIncidentConflictException(AdmIncidentConflictException.Type.IDEMPOTENCY_CONFLICT,"Idempotency key reused with different payload");
            if(!"DONE".equals(row.get("status")))throw new AdmIncidentConflictException(AdmIncidentConflictException.Type.COMMAND_IN_PROGRESS,"Idempotent command is still running or failed");
            return new CommandReservation(((Number)row.get("command_id")).longValue(),true,String.valueOf(row.get("result_ref")));}}
    private void completeCommand(long commandId,String resultRef){int
            updated=jdbc.update("UPDATE adm_incident_command SET status='DONE',result_ref=?,updated_at=CURRENT_TIMESTAMP WHERE command_id=? AND status='RUNNING'",resultRef,commandId);
            if(updated!=1)throw new IllegalStateException("Incident command completion conflict");}
    private record CommandReservation(long commandId,boolean replayed,String resultRef){}

    private long count(String table){return jdbc.queryForObject("SELECT COUNT(*) FROM "+table,Long.class);}
    private static <T> Page<T> page(List<T> rows,int p,int s,long total){return new Page<>(rows,p,s,total,(int)((total+s-1)/s));}
    private static int page(int value){return Math.max(0,value);} private static int size(int value){return Math.max(1,Math.min(value,200));}
    private static int positive(int value,String name){if(value<=0)throw new CpfValidationException(name+" must be positive");return value;}
    private static String required(String value,String name){if(value==null||value.isBlank())throw new CpfValidationException(name+" is required");return value.trim();}
    private static String blank(String value){return value==null||value.isBlank()?null:value.trim();}
    private static String yn(String value){String v=value==null?"Y":value.trim().toUpperCase(Locale.ROOT);if(!v.equals("Y")&&!v.equals("N"))throw new CpfValidationException("useYn must be Y or N");return v;}
    private static String normalizeSeverity(String value){String v=required(value,"severity").toUpperCase(Locale.ROOT);if(!List.of("INFO","WARNING","CRITICAL").contains(v))throw new
            CpfValidationException("unsupported severity");return v;}
    private static void requireTransition(String before, String action) {
        boolean allowed = switch (action) {
            case "ACKNOWLEDGE" -> "OPEN".equals(before);
            case "RESOLVE" -> "OPEN".equals(before) || "ACKNOWLEDGED".equals(before);
            case "REOPEN" -> "RESOLVED".equals(before);
            case "ESCALATE" -> "OPEN".equals(before) || "ACKNOWLEDGED".equals(before);
            default -> false;
        };
        if (!allowed) {
            throw new AdmIncidentConflictException(AdmIncidentConflictException.Type.INVALID_TRANSITION,
                    "Incident transition is not allowed: " + before + " -> " + action);
        }
    }
    private static void requireMutation(String reason,String approval,String key){required(reason,"reason");required(approval,"approvalRequestId");required(key,"idempotencyKey");}
    private static String activeKey(long policyId,String sourceType,String sourceId){return policyId+"|"+required(sourceType,"sourceType")+"|"+required(sourceId,"sourceId");}
    private static LocalDateTime local(Timestamp value){return value==null?null:value.toLocalDateTime();}
    private static String hash(String value){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));}catch(Exception e){
            throw new IllegalStateException(e);}}
}
