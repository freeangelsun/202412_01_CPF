package com.cpf.admin.opr.service;

import com.cpf.core.api.logging.CpfTransactionContext;
import com.cpf.core.api.util.CpfStrings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * ADM 필수 감사의 durable reservation/relay를 담당합니다.
 * Owner 작업 전에 ADM DB reservation을 별도 transaction으로 확정하여 XA 없이도 감사 유실을 막습니다.
 */
@Service
public class AdmAuditDeliveryService {
    private static final Logger log = LoggerFactory.getLogger(AdmAuditDeliveryService.class);
    private static final int DEFAULT_MAX_ATTEMPTS = 10;
    private static final int RELAY_BATCH_SIZE = 100;
    private final JdbcTemplate jdbc;
    private final TransactionTemplate requiresNew;
    private final int requestedStaleSeconds;

    public AdmAuditDeliveryService(
            @Qualifier("admJdbcTemplate") JdbcTemplate jdbc,
            @Qualifier("admTransactionManager") PlatformTransactionManager transactionManager,
            @Value("${cpf.admin.audit.requested-stale-seconds:900}") int requestedStaleSeconds) {
        this.jdbc = jdbc;
        this.requestedStaleSeconds = Math.max(60, requestedStaleSeconds);
        this.requiresNew = new TransactionTemplate(transactionManager);
        this.requiresNew.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    /** 위험 작업 전에 reservation을 확정합니다. 실패하면 Owner 작업을 시작하면 안 됩니다. */
    public long reserve(AuditCommand command) {
        AuditCommand c=command.normalized();
        Long id=requiresNew.execute(status -> {
            KeyHolder kh=new GeneratedKeyHolder();
            jdbc.update(con -> {
                PreparedStatement ps=con.prepareStatement("""
                    INSERT INTO adm_audit_delivery(
                      TRANSACTION_ID,TRACE_ID,OPERATOR_ID,ACTION_TYPE,TARGET_TYPE,TARGET_ID,REASON,BEFORE_DATA,CLIENT_IP,
                      OPERATION_STATUS,DELIVERY_STATUS,ATTEMPT_COUNT,MAX_ATTEMPTS,NEXT_ATTEMPT_AT,CREATED_BY,UPDATED_BY)
                    VALUES(?,?,?,?,?,?,?,?,?,'REQUESTED','PENDING',0,?,CURRENT_TIMESTAMP(3),?,?)
                    """, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1,c.transactionId()); ps.setString(2,c.traceId()); ps.setString(3,c.operatorId());
                ps.setString(4,c.actionType()); ps.setString(5,c.targetType()); ps.setString(6,c.targetId());
                ps.setString(7,c.reason()); ps.setString(8,sanitize(c.beforeData())); ps.setString(9,c.clientIp());
                ps.setInt(10,DEFAULT_MAX_ATTEMPTS); ps.setString(11,c.operatorId()); ps.setString(12,c.operatorId());
                return ps;
            },kh);
            Number key=kh.getKey();
            if(key==null) throw new IllegalStateException("ADM 감사 reservation ID를 발급받지 못했습니다.");
            return key.longValue();
        });
        if(id==null) throw new IllegalStateException("ADM 감사 reservation을 저장하지 못했습니다.");
        return id;
    }

    public void enrichReservation(long id, AuditCommand command) {
        AuditCommand c=command.normalized();
        requiresNew.executeWithoutResult(status -> {
            int n=jdbc.update("""
                UPDATE adm_audit_delivery SET TRANSACTION_ID=?,TRACE_ID=?,OPERATOR_ID=?,ACTION_TYPE=?,TARGET_TYPE=?,TARGET_ID=?,
                 REASON=?,BEFORE_DATA=?,CLIENT_IP=?,UPDATED_BY=?,UPDATED_AT=CURRENT_TIMESTAMP(3)
                WHERE DELIVERY_ID=? AND DELIVERY_STATUS<>'DELIVERED'
                """,c.transactionId(),c.traceId(),c.operatorId(),c.actionType(),c.targetType(),c.targetId(),c.reason(),sanitize(c.beforeData()),c.clientIp(),c.operatorId(),id);
            if(n!=1) throw new IllegalStateException("ADM 감사 reservation 보강 충돌입니다. deliveryId="+id);
        });
    }

    public <T> T executeAudited(AuditCommand c, Supplier<T> operation, Function<T,String> afterMapper) {
        long id=reserve(c);
        try {
            T result=operation.get();
            completeOperation(id,"SUCCEEDED",afterMapper==null?null:afterMapper.apply(result),null);
            return result;
        } catch(RuntimeException ex) {
            completeOperation(id,"FAILED",null,"OWNER_OPERATION_FAILED: "+ex.getClass().getSimpleName());
            throw ex;
        }
    }

    public void record(AuditCommand c,String after,String diff) {
        long id=reserve(c); completeOperation(id,"SUCCEEDED",after,diff);
    }

    /** 결과 기록 실패 시 reservation은 REQUESTED로 남고 stale recovery가 UNKNOWN으로 승격합니다. */
    public void completeOperation(long id,String operationStatus,String after,String diff) {
        try {
            requiresNew.executeWithoutResult(status -> {
                int n=jdbc.update("""
                  UPDATE adm_audit_delivery SET OPERATION_STATUS=?,AFTER_DATA=?,DIFF_DATA=?,UPDATED_BY=OPERATOR_ID,UPDATED_AT=CURRENT_TIMESTAMP(3)
                  WHERE DELIVERY_ID=? AND DELIVERY_STATUS<>'DELIVERED'
                  """,operationStatus,sanitize(after),sanitize(diff),id);
                if(n!=1) throw new IllegalStateException("ADM 감사 reservation 상태 갱신 충돌입니다. deliveryId="+id);
            });
        } catch(RuntimeException ex) {
            log.error("ADM 감사 결과 기록 실패. deliveryId={}, transactionId={}, reason={}",id,safeTransactionId(),safeMessage(ex));
            return;
        }
        deliverNow(id,false);
    }

    public List<Map<String,Object>> findDeliveries(String state,int limit) {
        int safe=Math.max(1,Math.min(limit,500));
        if(CpfStrings.hasText(state)) return jdbc.queryForList("""
          SELECT DELIVERY_ID,TRANSACTION_ID,TRACE_ID,OPERATOR_ID,ACTION_TYPE,TARGET_TYPE,TARGET_ID,REASON,
                 OPERATION_STATUS,DELIVERY_STATUS,ATTEMPT_COUNT,MAX_ATTEMPTS,NEXT_ATTEMPT_AT,LAST_ERROR,AUDIT_ID,REQUESTED_AT,DELIVERED_AT,UPDATED_AT
          FROM adm_audit_delivery WHERE DELIVERY_STATUS=? ORDER BY DELIVERY_ID DESC LIMIT ?
          """,state.trim().toUpperCase(),safe);
        return jdbc.queryForList("""
          SELECT DELIVERY_ID,TRANSACTION_ID,TRACE_ID,OPERATOR_ID,ACTION_TYPE,TARGET_TYPE,TARGET_ID,REASON,
                 OPERATION_STATUS,DELIVERY_STATUS,ATTEMPT_COUNT,MAX_ATTEMPTS,NEXT_ATTEMPT_AT,LAST_ERROR,AUDIT_ID,REQUESTED_AT,DELIVERED_AT,UPDATED_AT
          FROM adm_audit_delivery ORDER BY DELIVERY_ID DESC LIMIT ?
          """,safe);
    }

    public Map<String,Object> findDelivery(long id) {
        return jdbc.queryForMap("SELECT * FROM adm_audit_delivery WHERE DELIVERY_ID=?",id);
    }

    public Map<String,Object> retry(long id,String operatorId,String reason) {
        String actor=require(operatorId,"operatorId"); String why=require(reason,"reason");
        requiresNew.executeWithoutResult(status -> {
            int n=jdbc.update("""
             UPDATE adm_audit_delivery SET DELIVERY_STATUS='RETRY',NEXT_ATTEMPT_AT=CURRENT_TIMESTAMP(3),
              LAST_ERROR=CONCAT('manual retry: ',?),UPDATED_BY=?,UPDATED_AT=CURRENT_TIMESTAMP(3)
             WHERE DELIVERY_ID=? AND DELIVERY_STATUS IN('PENDING','RETRY','FAILED')
             """,why,actor,id);
            if(n!=1) throw new IllegalStateException("재처리 가능한 감사 전달 건이 아닙니다. deliveryId="+id);
        });
        deliverNow(id,true); return findDelivery(id);
    }

    @Scheduled(fixedDelayString="${cpf.admin.audit.relay-delay-ms:5000}")
    public void relayPending() {
        recoverStaleRequested();
        List<Long> ids;
        try {
            ids=jdbc.queryForList("""
              SELECT DELIVERY_ID FROM adm_audit_delivery
              WHERE DELIVERY_STATUS IN('PENDING','RETRY') AND OPERATION_STATUS IN('SUCCEEDED','FAILED','UNKNOWN')
               AND (NEXT_ATTEMPT_AT IS NULL OR NEXT_ATTEMPT_AT<=CURRENT_TIMESTAMP(3))
              ORDER BY DELIVERY_ID LIMIT ?
              """,Long.class,RELAY_BATCH_SIZE);
        } catch(DataAccessException ex) {
            log.error("ADM 감사 relay 대상 조회 실패. transactionId={}, reason={}",safeTransactionId(),safeMessage(ex)); return;
        }
        ids.forEach(id -> deliverNow(id,false));
    }

    private void recoverStaleRequested() {
        try {
            requiresNew.executeWithoutResult(status -> jdbc.update("""
              UPDATE adm_audit_delivery SET OPERATION_STATUS='UNKNOWN',DELIVERY_STATUS='RETRY',NEXT_ATTEMPT_AT=CURRENT_TIMESTAMP(3),
               LAST_ERROR=COALESCE(LAST_ERROR,'stale REQUESTED recovered as UNKNOWN'),UPDATED_AT=CURRENT_TIMESTAMP(3)
              WHERE OPERATION_STATUS='REQUESTED' AND DELIVERY_STATUS IN('PENDING','RETRY')
               AND REQUESTED_AT<=TIMESTAMPADD(SECOND,?,CURRENT_TIMESTAMP(3))
              """,-requestedStaleSeconds));
        } catch(RuntimeException ex) {
            log.error("ADM stale audit reservation 복구 실패. transactionId={}, reason={}",safeTransactionId(),safeMessage(ex));
        }
    }

    /** FOR UPDATE를 사용해 다중 ADM 인스턴스 relay의 중복 전달을 직렬화합니다. */
    private void deliverNow(long id,boolean manual) {
        try {
            requiresNew.executeWithoutResult(status -> {
                Map<String,Object> row=jdbc.queryForMap("""
                 SELECT DELIVERY_ID,TRANSACTION_ID,TRACE_ID,OPERATOR_ID,ACTION_TYPE,TARGET_TYPE,TARGET_ID,REASON,BEFORE_DATA,AFTER_DATA,DIFF_DATA,
                        CLIENT_IP,OPERATION_STATUS,DELIVERY_STATUS,ATTEMPT_COUNT,MAX_ATTEMPTS
                 FROM adm_audit_delivery WHERE DELIVERY_ID=? FOR UPDATE
                 """,id);
                String delivery=text(row.get("DELIVERY_STATUS"));
                if("DELIVERED".equals(delivery)) return;
                String operation=text(row.get("OPERATION_STATUS"));
                if("REQUESTED".equals(operation)) return;
                int attempts=number(row.get("ATTEMPT_COUNT")); int max=Math.max(1,number(row.get("MAX_ATTEMPTS")));
                if(attempts>=max && !manual) {
                    jdbc.update("UPDATE adm_audit_delivery SET DELIVERY_STATUS='FAILED',UPDATED_AT=CURRENT_TIMESTAMP(3) WHERE DELIVERY_ID=?",id); return;
                }
                KeyHolder kh=new GeneratedKeyHolder();
                jdbc.update(con -> {
                    PreparedStatement ps=con.prepareStatement("""
                      INSERT INTO adm_audit_log(TRANSACTION_ID,TRACE_ID,OPERATOR_ID,ACTION_TYPE,TARGET_TYPE,TARGET_ID,REASON,BEFORE_DATA,AFTER_DATA,DIFF_DATA,CLIENT_IP,
                       RETENTION_UNTIL,IMMUTABLE_YN,CREATED_BY,UPDATED_BY)
                      VALUES(?,?,?,?,?,?,?,?,?,?,?,DATE_ADD(CURDATE(),INTERVAL 5 YEAR),'Y',?,?)
                      """,Statement.RETURN_GENERATED_KEYS);
                    String op=text(row.get("OPERATOR_ID"));
                    ps.setString(1,text(row.get("TRANSACTION_ID"))); ps.setString(2,text(row.get("TRACE_ID"))); ps.setString(3,op);
                    ps.setString(4,text(row.get("ACTION_TYPE"))); ps.setString(5,text(row.get("TARGET_TYPE"))); ps.setString(6,text(row.get("TARGET_ID")));
                    ps.setString(7,text(row.get("REASON"))); ps.setString(8,text(row.get("BEFORE_DATA"))); ps.setString(9,text(row.get("AFTER_DATA")));
                    String diff=text(row.get("DIFF_DATA")); String suffix="operationStatus="+operation+";deliveryId="+id;
                    ps.setString(10,CpfStrings.hasText(diff)?diff+"\n"+suffix:suffix); ps.setString(11,text(row.get("CLIENT_IP"))); ps.setString(12,op); ps.setString(13,op);
                    return ps;
                },kh);
                Number auditId=kh.getKey(); if(auditId==null) throw new IllegalStateException("immutable audit ID 발급 실패. deliveryId="+id);
                jdbc.update("""
                 UPDATE adm_audit_delivery SET DELIVERY_STATUS='DELIVERED',ATTEMPT_COUNT=ATTEMPT_COUNT+1,AUDIT_ID=?,
                  DELIVERED_AT=CURRENT_TIMESTAMP(3),LAST_ERROR=NULL,UPDATED_AT=CURRENT_TIMESTAMP(3) WHERE DELIVERY_ID=?
                 """,auditId.longValue(),id);
            });
        } catch(RuntimeException ex) { markRetry(id,ex); }
    }

    private void markRetry(long id,RuntimeException cause) {
        try {
            requiresNew.executeWithoutResult(status -> jdbc.update("""
              UPDATE adm_audit_delivery SET ATTEMPT_COUNT=ATTEMPT_COUNT+1,
               DELIVERY_STATUS=CASE WHEN ATTEMPT_COUNT+1>=MAX_ATTEMPTS THEN 'FAILED' ELSE 'RETRY' END,
               NEXT_ATTEMPT_AT=TIMESTAMPADD(SECOND,LEAST(300,POW(2,LEAST(ATTEMPT_COUNT+1,8))),CURRENT_TIMESTAMP(3)),
               LAST_ERROR=?,UPDATED_AT=CURRENT_TIMESTAMP(3)
              WHERE DELIVERY_ID=? AND DELIVERY_STATUS<>'DELIVERED'
              """,truncate(safeMessage(cause),1000),id));
        } catch(RuntimeException ex) {
            log.error("ADM 감사 retry 상태 기록 실패. deliveryId={}, transactionId={}, reason={}",id,safeTransactionId(),safeMessage(ex));
        }
    }

    private String safeTransactionId(){try{return CpfTransactionContext.transactionId();}catch(RuntimeException ex){return "UNAVAILABLE";}}
    private static String sanitize(String v){if(v==null)return null;return truncate(v.replaceAll("(?i)(password|passwd|token|secret|authorization)(\\s*[=:]\\s*)[^,}\\s]+","$1$2***").replaceAll("(?i)(Bearer\\s+)[A-Za-z0-9._\\-+/=]+","$1***"),16000);}
    private static String safeMessage(Throwable ex){return ex==null?"unknown":(ex.getMessage()==null||ex.getMessage().isBlank()?ex.getClass().getSimpleName():ex.getMessage());}
    private static String truncate(String v,int max){return v==null||v.length()<=max?v:v.substring(0,max);}
    private static String require(String v,String field){if(!CpfStrings.hasText(v))throw new IllegalArgumentException(field+"은(는) 필수입니다.");return v.trim();}
    private static int number(Object v){return v instanceof Number n?n.intValue():(v==null?0:Integer.parseInt(String.valueOf(v)));}
    private static String text(Object v){return v==null?null:String.valueOf(v);}

    public record AuditCommand(String transactionId,String traceId,String operatorId,String actionType,String targetType,String targetId,String reason,String beforeData,String clientIp){
        AuditCommand normalized(){return new AuditCommand(
          CpfStrings.hasText(transactionId)?transactionId.trim():CpfTransactionContext.transactionId(),
          CpfStrings.hasText(traceId)?traceId.trim():CpfTransactionContext.traceId(),require(operatorId,"operatorId"),require(actionType,"actionType"),
          targetType==null?null:targetType.trim(),targetId==null?null:targetId.trim(),require(reason,"reason"),beforeData,clientIp==null?null:clientIp.trim());}
    }
}
