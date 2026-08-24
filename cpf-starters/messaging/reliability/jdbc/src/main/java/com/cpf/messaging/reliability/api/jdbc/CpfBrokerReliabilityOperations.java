package com.cpf.messaging.reliability.api.jdbc;

import com.cpf.platform.operations.api.reliability.CpfReliabilityOperationsPort;
import com.cpf.messaging.spi.broker.CpfBrokerReplayPort;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Broker·Idempotency·UNKNOWN·File Transfer의 운영 read-model과 승인된 변경을 제공하는 JDBC facade입니다.
 *
 * <p>조회는 CPF Platform DB 정본을 사용하며 DLQ replay는 ADM Approval Owner Command를 거친
 * 호출만 실제 상태전이로 연결합니다. 직접 저수준 replay SPI는 승인 우회를 막기 위해 그대로
 * fail-closed 상태를 유지합니다.</p>
 */
public class CpfBrokerReliabilityOperations implements CpfReliabilityOperationsPort {
    private final CpfBrokerReplayPort replay;
    private final JdbcTemplate jdbc;

    /** 하위호환 생성자입니다. 운영 조회/변경에는 JDBC 구성이 필요합니다. */
    public CpfBrokerReliabilityOperations(CpfBrokerReplayPort replay) {
        this(replay, null);
    }

    /** 실제 운영 Runtime용 생성자입니다. */
    public CpfBrokerReliabilityOperations(CpfBrokerReplayPort replay, JdbcTemplate jdbc) {
        this.replay = Objects.requireNonNull(replay, "replay must not be null");
        this.jdbc = jdbc;
    }

    @Override
    public List<Map<String,Object>> findIdempotency(String scope,String status,String key,int limit) {
        StringBuilder sql=new StringBuilder("SELECT * FROM cpf_idempotency_record WHERE 1=1");
        List<Object> args=new ArrayList<>();
        eq(sql,args,"scope",scope); eq(sql,args,"record_status",status); like(sql,args,"idempotency_key",key);
        sql.append(" ORDER BY updated_at DESC, idempotency_seq DESC");
        return query(sql.toString(),args,limit);
    }

    @Override
    public List<Map<String,Object>> findOutbox(String status,String transactionId,String topic,int limit) {
        StringBuilder sql=new StringBuilder("SELECT * FROM CPF_BROKER_OUTBOX WHERE 1=1");
        List<Object> args=new ArrayList<>();
        eq(sql,args,"outbox_status",status); eq(sql,args,"transaction_id",transactionId); eq(sql,args,"topic",topic);
        sql.append(" ORDER BY updated_at DESC, outbox_id DESC");
        return query(sql.toString(),args,limit);
    }

    @Override
    public List<Map<String,Object>> findInbox(String status,String key,int limit) {
        StringBuilder sql=new StringBuilder("SELECT * FROM CPF_BROKER_INBOX WHERE 1=1");
        List<Object> args=new ArrayList<>();
        eq(sql,args,"inbox_status",status);
        if(hasText(key)) { sql.append(" AND (message_id = ? OR idempotency_key = ? OR consumer_identity = ?)"); args.add(key.trim());args.add(key.trim());args.add(key.trim()); }
        sql.append(" ORDER BY updated_at DESC, inbox_id DESC");
        return query(sql.toString(),args,limit);
    }

    @Override
    public List<Map<String,Object>> findDlq(String status,String transactionId,String topic,int limit) {
        StringBuilder sql=new StringBuilder("SELECT * FROM CPF_BROKER_DLQ WHERE 1=1");
        List<Object> args=new ArrayList<>();
        eq(sql,args,"replay_status",status); eq(sql,args,"transaction_id",transactionId); eq(sql,args,"topic",topic);
        sql.append(" ORDER BY updated_at DESC, dlq_id DESC");
        return query(sql.toString(),args,limit);
    }

    @Override
    public List<Map<String,Object>> findFileTransfers(String status,String transactionId,String endpointCode,int limit) {
        StringBuilder sql=new StringBuilder("SELECT * FROM cpf_file_transfer_history WHERE 1=1");
        List<Object> args=new ArrayList<>();
        eq(sql,args,"transfer_status",status); eq(sql,args,"transaction_id",transactionId); eq(sql,args,"endpoint_code",endpointCode);
        sql.append(" ORDER BY updated_at DESC, history_id DESC");
        return query(sql.toString(),args,limit);
    }

    @Override
    public List<Map<String,Object>> findUnknownResults(String type,String status,String transactionId,int limit) {
        StringBuilder sql=new StringBuilder("SELECT * FROM cpf_unknown_result WHERE 1=1");
        List<Object> args=new ArrayList<>();
        eq(sql,args,"unknown_type",type); eq(sql,args,"unknown_status",status); eq(sql,args,"transaction_id",transactionId);
        sql.append(" ORDER BY updated_at DESC, unknown_seq DESC");
        return query(sql.toString(),args,limit);
    }

    @Override
    public Optional<Map<String,Object>> findUnknownResult(String unknownId) {
        requireJdbc();
        if(!hasText(unknownId)) return Optional.empty();
        List<Map<String,Object>> rows=query("SELECT * FROM cpf_unknown_result WHERE unknown_id = ?",List.of(unknownId.trim()),2);
        if(rows.size()>1) throw new IllegalStateException("duplicate CPF unknown result id: "+unknownId);
        return rows.stream().findFirst();
    }

    @Override
    // 결과 미확정 원장은 업무 재시도와 분리된 복구 기준점이므로 CPF 플랫폼 트랜잭션 안에서 원자적으로 기록합니다.
    @Transactional(transactionManager="cpfTransactionManager")
    public UnknownResultRecord recordUnknownResult(UnknownResultCommand command) {
        requireJdbc(); Objects.requireNonNull(command,"command");
        String id=hasText(command.unknownId())?command.unknownId().trim():"UNK-"+UUID.randomUUID();
        try {
            jdbc.update("""
                INSERT INTO cpf_unknown_result
                (unknown_id, unknown_type, unknown_status, transaction_id, segment_id, external_key,
                 failure_code, failure_message, next_action, created_by, updated_by)
                VALUES (?, ?, 'CHECK_PENDING', ?, ?, ?, ?, ?, ?, ?, ?)
                """,id,required(command.type(),"type"),blankToNull(command.transactionId()),blankToNull(command.segmentId()),
                blankToNull(command.externalKey()),blankToNull(command.failureCode()),bounded(command.failureMessage(),1000),
                blankToNull(command.nextAction()),defaultText(command.createdBy(),"CPF"),defaultText(command.createdBy(),"CPF"));
        // 동일 UNKNOWN ID 재기록은 멱등 재진입만 허용하고, 의미가 다른 충돌은 그대로 실패시켜 결과 왜곡을 막습니다.
        } catch (org.springframework.dao.DuplicateKeyException duplicate) {
            Map<String,Object> current=findUnknownResult(id).orElseThrow(()->duplicate);
            if(!required(command.type(),"type").equalsIgnoreCase(value(current,"unknown_type")) ||
               !Objects.equals(blankToNull(command.externalKey()),blankToNull(value(current,"external_key")))) throw duplicate;
        }
        return new UnknownResultRecord(id,value(findUnknownResult(id).orElseThrow(),"unknown_status"));
    }

    @Override
    // 승인 완료된 DLQ 재처리는 Outbox 재발행 예약과 DLQ 상태전이를 하나의 트랜잭션으로 묶어 부분 적용을 막습니다.
    @Transactional(transactionManager="cpfTransactionManager")
    public ChangeResult requestDlqReplay(String messageId,String operatorId,String reason) {
        requireJdbc(); String id=required(messageId,"messageId"); required(operatorId,"operatorId"); required(reason,"reason");
        Map<String,Object> before=findDlqByMessage(id).orElseThrow(()->new IllegalArgumentException("DLQ not found: "+id));
        String state=value(before,"replay_status").toUpperCase(java.util.Locale.ROOT);
        if(!java.util.Set.of("WAITING","FAILED").contains(state)) throw new IllegalStateException("DLQ replay state is not eligible: "+state);
        int outbox=jdbc.update("""
            UPDATE CPF_BROKER_OUTBOX SET outbox_status='PENDING', worker_id=NULL, claimed_at=NULL, lease_until=NULL,
                next_attempt_at=CURRENT_TIMESTAMP, failure_message=NULL, updated_by=?, updated_at=CURRENT_TIMESTAMP
            WHERE message_id=? AND outbox_status IN ('FAILED','UNKNOWN')
            """,operatorId.trim(),id);
        if(outbox!=1) throw new IllegalStateException("Replay source outbox is not in replayable state: "+id);
        int dlq=jdbc.update("""
            UPDATE CPF_BROKER_DLQ SET replay_status='REQUESTED', replay_count=replay_count+1,
                replay_requested_at=CURRENT_TIMESTAMP, replay_completed_at=NULL, updated_by=?, updated_at=CURRENT_TIMESTAMP
            WHERE message_id=? AND replay_status IN ('WAITING','FAILED')
            """,operatorId.trim(),id);
        if(dlq!=1) throw new IllegalStateException("DLQ replay CAS conflict: "+id);
        return new ChangeResult(before,findDlqByMessage(id).orElseThrow(),reason.trim());
    }

    @Override
    public ChangeResult resolveUnknown(String unknownId,String targetStatus,String operatorId,String reason) {
        Map<String,Object> row=findUnknownResult(unknownId).orElseThrow(()->new IllegalArgumentException("unknown result not found: "+unknownId));
        return resolveUnknown(unknownId,targetStatus,longValue(row.get("row_version")),operatorId,reason);
    }

    @Override
    // UNKNOWN 확정은 expectedVersion CAS로 단 한 운영자만 최종 상태를 확정하게 하며 감사 사유를 같은 원자 경계에 보존합니다.
    @Transactional(transactionManager="cpfTransactionManager")
    public ChangeResult resolveUnknown(String unknownId,String targetStatus,long expectedVersion,String operatorId,String reason) {
        requireJdbc(); String id=required(unknownId,"unknownId"); String status=required(targetStatus,"targetStatus").toUpperCase(java.util.Locale.ROOT);
        String actor=required(operatorId,"operatorId"); String audit=required(reason,"reason");
        if(!java.util.Set.of("RESOLVED","FAILED","CONFIRMED_SUCCESS","CONFIRMED_FAILED","RETRY_PENDING","CHECK_PENDING","MANUAL_REVIEW","UNKNOWN").contains(status))
            throw new IllegalArgumentException("unsupported UNKNOWN target status: "+status);
        Map<String,Object> before=findUnknownResult(id).orElseThrow(()->new IllegalArgumentException("unknown result not found: "+id));
        int changed=jdbc.update("""
            UPDATE cpf_unknown_result SET unknown_status=?, resolved_at=?, resolved_by=?, audit_reason=?,
                row_version=row_version+1, updated_by=?, updated_at=CURRENT_TIMESTAMP
            WHERE unknown_id=? AND row_version=?
            """,status,isFinal(status)?Timestamp.from(Instant.now()):null,isFinal(status)?actor:null,audit,actor,id,expectedVersion);
        if(changed!=1) throw new org.springframework.dao.OptimisticLockingFailureException("UNKNOWN result version conflict: "+id);
        return new ChangeResult(before,findUnknownResult(id).orElseThrow(),audit);
    }

    /** 승인 없는 저수준 replay SPI는 의도적으로 사용하지 않습니다. */
    @Deprecated(forRemoval=false)
    public void verifyLowLevelReplayIsClosed(String messageId) { replay.replay(required(messageId,"messageId")); }

    private Optional<Map<String,Object>> findDlqByMessage(String id) {
        List<Map<String,Object>> rows=query("SELECT * FROM CPF_BROKER_DLQ WHERE message_id = ?",List.of(id),2);
        if(rows.size()>1) throw new IllegalStateException("duplicate broker DLQ message: "+id);
        return rows.stream().findFirst();
    }
    private List<Map<String,Object>> query(String sql,List<?> args,int limit) {
        requireJdbc(); int max=Math.max(1,Math.min(limit<=0?100:limit,1000));
        return jdbc.query(con->{ PreparedStatement ps=con.prepareStatement(sql); for(int i=0;i<args.size();i++) ps.setObject(i+1,args.get(i)); ps.setMaxRows(max); return ps;},new ColumnMapRowMapper());
    }
    private void requireJdbc(){ if(jdbc==null) throw new IllegalStateException("CPF reliability JDBC operations require JdbcTemplate configuration"); }
    private static void eq(StringBuilder s,List<Object>a,String c,String v){ if(hasText(v)){s.append(" AND ").append(c).append(" = ?");a.add(v.trim());}}
    private static void like(StringBuilder s,List<Object>a,String c,String v){ if(hasText(v)){s.append(" AND ").append(c).append(" LIKE ?");a.add("%"+v.trim()+"%");}}
    private static boolean hasText(String v){return v!=null&&!v.isBlank();}
    private static String required(String v,String n){if(!hasText(v))throw new IllegalArgumentException(n+" is required");return v.trim();}
    private static String blankToNull(String v){return hasText(v)?v.trim():null;}
    private static String defaultText(String v,String d){return hasText(v)?v.trim():d;}
    private static String bounded(String v,int max){ if(v==null)return null;String x=v.trim();return x.length()<=max?x:x.substring(0,max);}
    private static String value(Map<String,?> r,String key){String n=key.replace("_","").toLowerCase(java.util.Locale.ROOT);for(var e:r.entrySet())if(e.getKey().replace("_","").toLowerCase(java.util.Locale.ROOT).equals(n))return e.getValue()==null?"":String.valueOf(e.getValue()).trim();return "";}
    private static long longValue(Object o){return o instanceof Number n?n.longValue():Long.parseLong(String.valueOf(o));}
    private static boolean isFinal(String s){return java.util.Set.of("RESOLVED","FAILED","CONFIRMED_SUCCESS","CONFIRMED_FAILED").contains(s);}
}
