package com.cpf.bizadmin.audit.service;

import com.cpf.bizadmin.common.base.BzaBaseService;
import com.cpf.core.api.logging.CpfTransactionContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

/**
 * BZA 업무 감사의 tamper-evident hash chain 구현.
 *
 * <p>감사 기록 writer는 단일 JVM lock이 아니라 DB lock row를 SELECT FOR UPDATE 하므로
 * 여러 BZA 인스턴스가 동시에 기록해도 이전 hash와 현재 hash의 순서를 하나의 체인으로 유지합니다.</p>
 */
@Service
public class BzaBusinessAuditService extends BzaBaseService {
    private static final long LOCK_ID = 1L;
    private static final String GENESIS = "GENESIS";
    private final ObjectProvider<NamedParameterJdbcTemplate> provider;
    private final ObjectMapper mapper;

    public BzaBusinessAuditService(
            @Qualifier("bzaJdbcTemplate") ObjectProvider<NamedParameterJdbcTemplate> provider,
            ObjectMapper mapper) {
        this.provider = provider;
        this.mapper = mapper;
    }

    @Transactional(transactionManager = "bzaTransactionManager")
    public Map<String,Object> record(String actor,String action,String targetType,String targetId,String reason,Object before,Object after) {
        require(actor,"actor"); require(action,"action"); require(targetType,"targetType"); require(targetId,"targetId"); require(reason,"reason");
        ensureLockRow();
        Map<String,Object> lock=jdbc().queryForMap("SELECT current_hash AS currentHash FROM bza_audit_chain_lock WHERE chain_id=:id FOR UPDATE",new MapSqlParameterSource("id",LOCK_ID));
        String previous=Objects.toString(lock.get("currentHash"),GENESIS);
        String transactionId=CpfTransactionContext.transactionId();
        String beforeJson=canonical(before), afterJson=canonical(after);
        String hash=sha256(String.join("|",previous,nullable(transactionId),actor,action,targetType,targetId,reason,nullable(beforeJson),nullable(afterJson)));
        MapSqlParameterSource p=new MapSqlParameterSource()
                .addValue("transactionId",transactionId).addValue("actor",actor).addValue("action",action)
                .addValue("targetType",targetType).addValue("targetId",targetId).addValue("reason",reason)
                .addValue("beforeData",beforeJson).addValue("afterData",afterJson).addValue("previous",previous).addValue("hash",hash);
        jdbc().update("""
            INSERT INTO bza_business_audit(transaction_id,actor_id,action_type,target_type,target_id,reason,before_data,after_data,previous_record_hash,record_hash,created_by,updated_by)
            VALUES(:transactionId,:actor,:action,:targetType,:targetId,:reason,:beforeData,:afterData,:previous,:hash,:actor,:actor)
            """,p);
        Long auditId=jdbc().queryForObject("SELECT LAST_INSERT_ID()",Map.of(),Long.class);
        jdbc().update("UPDATE bza_audit_chain_lock SET current_hash=:hash,last_audit_id=:auditId,version_no=version_no+1,updated_by=:actor,updated_at=CURRENT_TIMESTAMP(3) WHERE chain_id=:id",
                new MapSqlParameterSource().addValue("hash",hash).addValue("auditId",auditId).addValue("actor",actor).addValue("id",LOCK_ID));
        Map<String,Object> result=new LinkedHashMap<>(); result.put("recordHash",hash); result.put("previousRecordHash",previous); result.put("transactionId",transactionId); return result;
    }

    /** 전체 체인과 lock-head를 모두 검증한다. 과거 legacy row는 PARTIAL_LEGACY로 구분한다. */
    public Map<String,Object> verify() {
        ensureLockRow();
        List<Map<String,Object>> rows=jdbc().queryForList("""
            SELECT audit_id AS auditId,transaction_id AS transactionId,actor_id AS actorId,action_type AS actionType,
                   target_type AS targetType,target_id AS targetId,reason,before_data AS beforeData,after_data AS afterData,
                   previous_record_hash AS previousRecordHash,record_hash AS recordHash
              FROM bza_business_audit ORDER BY audit_id
            """,Map.of());
        String previous=GENESIS; boolean legacy=false; long verified=0;
        for(Map<String,Object> row:rows){
            String actual=Objects.toString(row.get("recordHash"),"");
            String prev=Objects.toString(row.get("previousRecordHash"),"");
            if(actual.isBlank()||prev.isBlank()){legacy=true;continue;}
            if(!Objects.equals(previous,prev)) return result("BROKEN",verified,row.get("auditId"),"previous hash mismatch",previous);
            String expected=sha256(String.join("|",previous,nullable(row.get("transactionId")),nullable(row.get("actorId")),nullable(row.get("actionType")),nullable(row.get("targetType")),nullable(row.get("targetId")),nullable(row.get("reason")),nullable(row.get("beforeData")),nullable(row.get("afterData"))));
            if(!expected.equalsIgnoreCase(actual)) return result("BROKEN",verified,row.get("auditId"),"record hash mismatch",previous);
            previous=actual;verified++;
        }
        String head=Objects.toString(jdbc().queryForObject("SELECT current_hash FROM bza_audit_chain_lock WHERE chain_id=:id",new MapSqlParameterSource("id",LOCK_ID),String.class),GENESIS);
        if(!Objects.equals(previous,head)) return result("BROKEN",verified,null,"chain head mismatch",previous);
        return result(legacy?"PARTIAL_LEGACY":"VALID",verified,null,legacy?"legacy rows without hash exist":"ok",previous);
    }

    private Map<String,Object> result(String status,long count,Object auditId,String message,String head){Map<String,Object>r=new LinkedHashMap<>();r.put("status",status);r.put("verifiedRows",count);r.put("brokenAuditId",auditId);r.put("message",message);r.put("computedHead",head);return r;}
    private void ensureLockRow(){jdbc().update("INSERT INTO bza_audit_chain_lock(chain_id,current_hash,last_audit_id,version_no,updated_by) VALUES(:id,:hash,NULL,0,'SYSTEM') ON DUPLICATE KEY UPDATE chain_id=chain_id",new MapSqlParameterSource().addValue("id",LOCK_ID).addValue("hash",GENESIS));}
    private String canonical(Object value){if(value==null)return null;try{return mapper.writeValueAsString(value);}catch(JsonProcessingException e){throw new IllegalArgumentException("감사 Snapshot JSON 직렬화 실패",e);}}
    private static String sha256(String text){try{byte[]b=MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));return java.util.HexFormat.of().formatHex(b);}catch(Exception e){throw new IllegalStateException("SHA-256 unavailable",e);}}
    private static String nullable(Object v){return v==null?"":String.valueOf(v);} private static void require(String v,String f){if(v==null||v.isBlank())throw new IllegalArgumentException(f+"는 필수입니다.");}
    private NamedParameterJdbcTemplate jdbc(){NamedParameterJdbcTemplate j=provider.getIfAvailable();if(j==null)throw new IllegalStateException("BZA datasource가 구성되지 않았습니다.");return j;}
}
