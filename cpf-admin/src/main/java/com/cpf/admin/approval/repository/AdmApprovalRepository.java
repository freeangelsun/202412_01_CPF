package com.cpf.admin.approval.repository;

import com.cpf.admin.approval.api.AdmApprovalTargetType;
import com.cpf.admin.approval.spi.AdmApprovalDirectoryEntry;
import com.cpf.admin.approval.spi.AdmApprovalDirectoryPort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

/** ADM 위험조치 승인 정본 Repository 및 기본 DB Directory Adapter. */
@Repository
public class AdmApprovalRepository implements AdmApprovalDirectoryPort {
    private final JdbcTemplate jdbc;

    public AdmApprovalRepository(@Qualifier("admJdbcTemplate") JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Map<String,Object>> findPolicies(String actionType) {
        return jdbc.queryForList("""
                SELECT POLICY_CODE AS policyCode, POLICY_VERSION AS policyVersion,
                       POLICY_NAME AS policyName, ACTION_TYPE AS actionType,
                       EFFECTIVE_FROM AS effectiveFrom, EFFECTIVE_TO AS effectiveTo,
                       ENABLED_YN AS enabledYn, SELF_APPROVAL_ALLOWED_YN AS selfApprovalAllowedYn,
                       BREAK_GLASS_ALLOWED_YN AS breakGlassAllowedYn, DESCRIPTION AS description
                  FROM adm_approval_policy
                 WHERE (? IS NULL OR ACTION_TYPE=?)
                 ORDER BY POLICY_CODE, POLICY_VERSION DESC
                """, emptyToNull(actionType), emptyToNull(actionType));
    }

    public Optional<Map<String,Object>> findPolicy(String code, int version) {
        return jdbc.queryForList("""
                SELECT POLICY_CODE AS policyCode, POLICY_VERSION AS policyVersion,
                       POLICY_NAME AS policyName, ACTION_TYPE AS actionType,
                       EFFECTIVE_FROM AS effectiveFrom, EFFECTIVE_TO AS effectiveTo,
                       ENABLED_YN AS enabledYn, SELF_APPROVAL_ALLOWED_YN AS selfApprovalAllowedYn,
                       BREAK_GLASS_ALLOWED_YN AS breakGlassAllowedYn, DESCRIPTION AS description
                  FROM adm_approval_policy WHERE POLICY_CODE=? AND POLICY_VERSION=?
                """, code, version).stream().findFirst();
    }

    public Optional<Map<String,Object>> findActivePolicy(String actionType, Instant at) {
        return jdbc.queryForList("""
                SELECT POLICY_CODE AS policyCode, POLICY_VERSION AS policyVersion,
                       POLICY_NAME AS policyName, ACTION_TYPE AS actionType,
                       EFFECTIVE_FROM AS effectiveFrom, EFFECTIVE_TO AS effectiveTo,
                       ENABLED_YN AS enabledYn, SELF_APPROVAL_ALLOWED_YN AS selfApprovalAllowedYn,
                       BREAK_GLASS_ALLOWED_YN AS breakGlassAllowedYn, DESCRIPTION AS description
                  FROM adm_approval_policy
                 WHERE ACTION_TYPE=? AND ENABLED_YN='Y'
                   AND EFFECTIVE_FROM <= ?
                   AND (EFFECTIVE_TO IS NULL OR EFFECTIVE_TO > ?)
                 ORDER BY POLICY_VERSION DESC
                """, actionType, Timestamp.from(at), Timestamp.from(at)).stream().findFirst();
    }

    public List<Map<String,Object>> findPolicySteps(String code, int version) {
        return jdbc.queryForList("""
                SELECT POLICY_CODE AS policyCode, POLICY_VERSION AS policyVersion, STEP_NO AS stepNo,
                       STEP_TYPE AS stepType, TARGET_TYPE AS targetType, TARGET_CODE AS targetCode,
                       DECISION_RULE AS decisionRule, REQUIRED_COUNT AS requiredCount, REQUIRED_YN AS requiredYn
                  FROM adm_approval_policy_step
                 WHERE POLICY_CODE=? AND POLICY_VERSION=?
                 ORDER BY STEP_NO, TARGET_TYPE, TARGET_CODE
                """, code, version);
    }

    public void replacePolicy(Map<String,Object> p, List<Map<String,Object>> steps) {
        int changed = jdbc.update("""
                UPDATE adm_approval_policy SET POLICY_NAME=?, ACTION_TYPE=?, EFFECTIVE_FROM=?, EFFECTIVE_TO=?,
                       ENABLED_YN=?, SELF_APPROVAL_ALLOWED_YN=?, BREAK_GLASS_ALLOWED_YN=?,
                       DESCRIPTION=?, updated_by=?
                 WHERE POLICY_CODE=? AND POLICY_VERSION=?
                """, p.get("policyName"), p.get("actionType"), p.get("effectiveFrom"), p.get("effectiveTo"),
                p.get("enabledYn"), p.get("selfApprovalAllowedYn"), p.get("breakGlassAllowedYn"),
                p.get("description"), p.get("operatorId"), p.get("policyCode"), p.get("policyVersion"));
        if (changed == 0 && findPolicy(String.valueOf(p.get("policyCode")), ((Number)p.get("policyVersion")).intValue()).isEmpty()) {
            jdbc.update("""
                    INSERT INTO adm_approval_policy (
                        POLICY_CODE,POLICY_VERSION,POLICY_NAME,ACTION_TYPE,EFFECTIVE_FROM,EFFECTIVE_TO,
                        ENABLED_YN,SELF_APPROVAL_ALLOWED_YN,BREAK_GLASS_ALLOWED_YN,DESCRIPTION,created_by,updated_by
                    ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)
                    """, p.get("policyCode"),p.get("policyVersion"),p.get("policyName"),p.get("actionType"),
                    p.get("effectiveFrom"),p.get("effectiveTo"),p.get("enabledYn"),p.get("selfApprovalAllowedYn"),
                    p.get("breakGlassAllowedYn"),p.get("description"),p.get("operatorId"),p.get("operatorId"));
        }
        jdbc.update("DELETE FROM adm_approval_policy_step WHERE POLICY_CODE=? AND POLICY_VERSION=?",
                p.get("policyCode"), p.get("policyVersion"));
        for (Map<String,Object> s : steps) {
            jdbc.update("""
                    INSERT INTO adm_approval_policy_step (
                        POLICY_CODE,POLICY_VERSION,STEP_NO,STEP_TYPE,TARGET_TYPE,TARGET_CODE,
                        DECISION_RULE,REQUIRED_COUNT,REQUIRED_YN,created_by,updated_by
                    ) VALUES (?,?,?,?,?,?,?,?,?,?,?)
                    """, s.get("policyCode"),s.get("policyVersion"),s.get("stepNo"),s.get("stepType"),
                    s.get("targetType"),s.get("targetCode"),s.get("decisionRule"),s.get("requiredCount"),
                    s.get("requiredYn"),s.get("operatorId"),s.get("operatorId"));
        }
    }

    @Override
    public List<AdmApprovalDirectoryEntry> resolve(AdmApprovalTargetType type, String code, Instant at) {
        Timestamp ts=Timestamp.from(at);
        String sql=switch(type){
            case OPERATOR -> """
                SELECT o.OPERATOR_ID operatorId,p.ORGANIZATION_CODE organizationCode,
                       p.POSITION_CODE positionCode,p.JOB_TITLE_CODE jobTitleCode
                  FROM adm_operator o LEFT JOIN adm_operator_profile p ON p.OPERATOR_ID=o.OPERATOR_ID
                 WHERE o.OPERATOR_ID=? AND o.USE_YN='Y' AND o.LOCKED_YN='N'
                   AND (p.EFFECTIVE_FROM IS NULL OR p.EFFECTIVE_FROM<=?)
                   AND (p.EFFECTIVE_TO IS NULL OR p.EFFECTIVE_TO>?)
                """;
            case ROLE -> """
                SELECT o.OPERATOR_ID operatorId,p.ORGANIZATION_CODE organizationCode,
                       p.POSITION_CODE positionCode,p.JOB_TITLE_CODE jobTitleCode
                  FROM adm_operator_role r JOIN adm_operator o ON o.OPERATOR_ID=r.OPERATOR_ID
                  LEFT JOIN adm_operator_profile p ON p.OPERATOR_ID=o.OPERATOR_ID
                 WHERE r.ROLE_ID=? AND o.USE_YN='Y' AND o.LOCKED_YN='N'
                   AND (p.EFFECTIVE_FROM IS NULL OR p.EFFECTIVE_FROM<=?)
                   AND (p.EFFECTIVE_TO IS NULL OR p.EFFECTIVE_TO>?)
                """;
            case ORGANIZATION -> """
                SELECT o.OPERATOR_ID operatorId,p.ORGANIZATION_CODE organizationCode,
                       p.POSITION_CODE positionCode,p.JOB_TITLE_CODE jobTitleCode
                  FROM adm_operator_profile p JOIN adm_operator o ON o.OPERATOR_ID=p.OPERATOR_ID
                 WHERE p.ORGANIZATION_CODE=? AND o.USE_YN='Y' AND o.LOCKED_YN='N'
                   AND (p.EFFECTIVE_FROM IS NULL OR p.EFFECTIVE_FROM<=?)
                   AND (p.EFFECTIVE_TO IS NULL OR p.EFFECTIVE_TO>?)
                """;
            case ORG_MANAGER -> """
                SELECT o.OPERATOR_ID operatorId,p.ORGANIZATION_CODE organizationCode,
                       p.POSITION_CODE positionCode,p.JOB_TITLE_CODE jobTitleCode
                  FROM adm_organization g JOIN adm_operator o ON o.OPERATOR_ID=g.MANAGER_OPERATOR_ID
                  LEFT JOIN adm_operator_profile p ON p.OPERATOR_ID=o.OPERATOR_ID
                 WHERE g.ORGANIZATION_CODE=? AND g.USE_YN='Y' AND o.USE_YN='Y' AND o.LOCKED_YN='N'
                   AND (p.EFFECTIVE_FROM IS NULL OR p.EFFECTIVE_FROM<=?)
                   AND (p.EFFECTIVE_TO IS NULL OR p.EFFECTIVE_TO>?)
                """;
        };
        LinkedHashMap<String,AdmApprovalDirectoryEntry> unique=new LinkedHashMap<>();
        for(Map<String,Object> r:jdbc.queryForList(sql,code,ts,ts)){
            String id=Objects.toString(value(r,"operatorId"),"");
            if(!id.isBlank()) unique.putIfAbsent(id,new AdmApprovalDirectoryEntry(id,
                    nullable(value(r,"organizationCode")),nullable(value(r,"positionCode")),nullable(value(r,"jobTitleCode"))));
        }
        return List.copyOf(unique.values());
    }

    public Optional<Long> findRequestIdByKey(String requestKey){
        return findRequestByKey(requestKey).map(row -> ((Number)value(row,"approvalRequestId")).longValue());
    }

    public Optional<Map<String,Object>> findRequestByKey(String requestKey){
        return jdbc.queryForList("""
            SELECT APPROVAL_REQUEST_ID approvalRequestId,REQUEST_KEY requestKey,POLICY_CODE policyCode,
                   POLICY_VERSION policyVersion,ACTION_TYPE actionType,OWNER_MODULE ownerModule,
                   OWNER_COMMAND ownerCommand,TARGET_TYPE targetType,TARGET_ID targetId,REQUESTED_BY requestedBy,
                   REQUEST_REASON requestReason,COMMAND_PAYLOAD_HASH payloadHash,
                   COMMAND_PAYLOAD_SNAPSHOT payloadSnapshot,APPROVAL_STATUS approvalStatus,
                   CURRENT_STEP_NO currentStepNo,EXPIRE_AT expireAt,TRANSACTION_ID transactionId,
                   VERSION_NO versionNo
              FROM adm_approval_request WHERE REQUEST_KEY=?
            """,requestKey).stream().findFirst();
    }

    public long insertRequest(Map<String,Object> v){
        KeyHolder kh=new GeneratedKeyHolder();
        jdbc.update(c->{
            PreparedStatement ps=c.prepareStatement("""
                INSERT INTO adm_approval_request (
                    REQUEST_KEY,POLICY_CODE,POLICY_VERSION,ACTION_TYPE,OWNER_MODULE,OWNER_COMMAND,
                    TARGET_TYPE,TARGET_ID,REQUESTED_BY,REQUEST_REASON,COMMAND_PAYLOAD_HASH,
                    COMMAND_PAYLOAD_SNAPSHOT,APPROVAL_STATUS,CURRENT_STEP_NO,EXPIRE_AT,TRANSACTION_ID,
                    VERSION_NO,created_by,updated_by
                ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,'PENDING',?,?,?,?,?,?)
                """, Statement.RETURN_GENERATED_KEYS);
            int i=1;
            ps.setObject(i++,v.get("requestKey"));ps.setObject(i++,v.get("policyCode"));ps.setObject(i++,v.get("policyVersion"));
            ps.setObject(i++,v.get("actionType"));ps.setObject(i++,v.get("ownerModule"));ps.setObject(i++,v.get("ownerCommand"));
            ps.setObject(i++,v.get("targetType"));ps.setObject(i++,v.get("targetId"));ps.setObject(i++,v.get("requestedBy"));
            ps.setObject(i++,v.get("requestReason"));ps.setObject(i++,v.get("payloadHash"));ps.setObject(i++,v.get("payloadSnapshot"));
            ps.setObject(i++,v.get("currentStepNo"));ps.setObject(i++,v.get("expireAt"));ps.setObject(i++,v.get("transactionId"));
            ps.setObject(i++,0L);ps.setObject(i++,v.get("operatorId"));ps.setObject(i++,v.get("operatorId"));
            return ps;
        },kh);
        Number n=kh.getKey(); if(n==null) throw new IllegalStateException("ADM 승인 요청 키 생성 실패");
        return n.longValue();
    }

    public void insertParticipant(long requestId,int stepNo,AdmApprovalDirectoryEntry e,
                                  String targetType,String targetCode,String operatorId){
        jdbc.update("""
            INSERT INTO adm_approval_participant (
              APPROVAL_REQUEST_ID,STEP_NO,OPERATOR_ID,SOURCE_TARGET_TYPE,SOURCE_TARGET_CODE,
              ORGANIZATION_CODE_SNAPSHOT,POSITION_CODE_SNAPSHOT,JOB_TITLE_CODE_SNAPSHOT,
              DECISION_STATUS,created_by,updated_by
            ) VALUES (?,?,?,?,?,?,?,?,'WAITING',?,?)
            """,requestId,stepNo,e.operatorId(),targetType,targetCode,e.organizationCode(),
                e.positionCode(),e.jobTitleCode(),operatorId,operatorId);
    }

    public Optional<Map<String,Object>> findRequest(long id){
        return jdbc.queryForList("""
            SELECT APPROVAL_REQUEST_ID approvalRequestId,REQUEST_KEY requestKey,POLICY_CODE policyCode,
                   POLICY_VERSION policyVersion,ACTION_TYPE actionType,OWNER_MODULE ownerModule,
                   OWNER_COMMAND ownerCommand,TARGET_TYPE targetType,TARGET_ID targetId,REQUESTED_BY requestedBy,
                   REQUEST_REASON requestReason,COMMAND_PAYLOAD_HASH payloadHash,
                   COMMAND_PAYLOAD_SNAPSHOT payloadSnapshot,
                   APPROVAL_STATUS approvalStatus,CURRENT_STEP_NO currentStepNo,EXPIRE_AT expireAt,
                   TRANSACTION_ID transactionId,VERSION_NO versionNo
              FROM adm_approval_request WHERE APPROVAL_REQUEST_ID=?
            """,id).stream().findFirst();
    }

    public List<Map<String,Object>> findParticipants(long id){
        return jdbc.queryForList("""
            SELECT APPROVAL_PARTICIPANT_ID participantId,STEP_NO stepNo,OPERATOR_ID operatorId,
                   SOURCE_TARGET_TYPE sourceTargetType,SOURCE_TARGET_CODE sourceTargetCode,
                   ORGANIZATION_CODE_SNAPSHOT organizationCode,POSITION_CODE_SNAPSHOT positionCode,
                   JOB_TITLE_CODE_SNAPSHOT jobTitleCode,DECISION_STATUS decisionStatus,
                   DECISION_REASON decisionReason,DECIDED_AT decidedAt
              FROM adm_approval_participant WHERE APPROVAL_REQUEST_ID=?
             ORDER BY STEP_NO,APPROVAL_PARTICIPANT_ID
            """,id);
    }

    public Optional<Map<String,Object>> findWaitingParticipant(long id,int stepNo,String operatorId){
        return jdbc.queryForList("""
            SELECT APPROVAL_PARTICIPANT_ID participantId,STEP_NO stepNo,SOURCE_TARGET_TYPE sourceTargetType,
                   SOURCE_TARGET_CODE sourceTargetCode
              FROM adm_approval_participant
             WHERE APPROVAL_REQUEST_ID=? AND STEP_NO=? AND OPERATOR_ID=? AND DECISION_STATUS='WAITING'
            """,id,stepNo,operatorId).stream().findFirst();
    }

    public Optional<Map<String,Object>> findDecisionByKey(String key){
        return jdbc.queryForList("""
            SELECT APPROVAL_REQUEST_ID approvalRequestId,APPROVAL_PARTICIPANT_ID participantId,
                   OPERATOR_ID operatorId,DECISION_STATUS decisionStatus,DECISION_REASON decisionReason
              FROM adm_approval_participant WHERE IDEMPOTENCY_KEY=?
            """,key).stream().findFirst();
    }

    public boolean decisionKeyExists(String key){
        return findDecisionByKey(key).isPresent();
    }

    public int decideParticipant(long participantId,String status,String key,String reason,String operatorId){
        return jdbc.update("""
            UPDATE adm_approval_participant SET DECISION_STATUS=?,IDEMPOTENCY_KEY=?,DECISION_REASON=?,
                   DECIDED_AT=CURRENT_TIMESTAMP(3),updated_by=?
             WHERE APPROVAL_PARTICIPANT_ID=? AND DECISION_STATUS='WAITING'
            """,status,key,reason,operatorId,participantId);
    }

    public int updateRequest(long id,long version,String status,int step,String operatorId){
        return jdbc.update("""
            UPDATE adm_approval_request SET APPROVAL_STATUS=?,CURRENT_STEP_NO=?,VERSION_NO=VERSION_NO+1,updated_by=?
             WHERE APPROVAL_REQUEST_ID=? AND VERSION_NO=?
            """,status,step,operatorId,id,version);
    }

    public void history(long id,String event,String actor,String before,String after,String reason,String data,String tx){
        jdbc.update("""
            INSERT INTO adm_approval_history (
              APPROVAL_REQUEST_ID,EVENT_TYPE,ACTOR_ID,BEFORE_STATUS,AFTER_STATUS,REASON,EVENT_DATA,TRANSACTION_ID
            ) VALUES (?,?,?,?,?,?,?,?)
            """,id,event,actor,before,after,reason,data,tx);
    }

    public Optional<Map<String,Object>> findExecution(long id){
        return jdbc.queryForList("""
            SELECT APPROVAL_REQUEST_ID approvalRequestId,COMMAND_REQUEST_ID commandRequestId,
                   EXECUTION_STATUS executionStatus,OWNER_RESULT_CODE ownerResultCode,
                   OWNER_RESULT_MESSAGE ownerResultMessage,STARTED_AT startedAt,COMPLETED_AT completedAt,
                   RECOVERY_REQUIRED_YN recoveryRequiredYn
              FROM adm_approval_execution WHERE APPROVAL_REQUEST_ID=?
            """,id).stream().findFirst();
    }

    @Transactional(transactionManager = "admTransactionManager")
    public boolean reserveExecution(long id,long expectedVersion,String commandRequestId,String operatorId){
        int requestChanged=jdbc.update("""
            UPDATE adm_approval_request SET APPROVAL_STATUS='EXECUTING',VERSION_NO=VERSION_NO+1,updated_by=?
             WHERE APPROVAL_REQUEST_ID=? AND APPROVAL_STATUS='APPROVED' AND VERSION_NO=?
            """,operatorId,id,expectedVersion);
        if(requestChanged!=1)return false;
        jdbc.update("""
            INSERT INTO adm_approval_execution (
              APPROVAL_REQUEST_ID,COMMAND_REQUEST_ID,EXECUTION_STATUS,STARTED_AT,RECOVERY_REQUIRED_YN,created_by,updated_by
            ) VALUES (?,?,'RUNNING',CURRENT_TIMESTAMP,'N',?,?)
            """,id,commandRequestId,operatorId,operatorId);
        return true;
    }

    /** @deprecated use reserveExecution for atomic APPROVED -> EXECUTING reservation. */
    @Deprecated
    public void startExecution(long id,String commandRequestId,String operatorId){
        Map<String,Object> request=findRequest(id).orElseThrow();
        if(!reserveExecution(id,((Number)value(request,"versionNo")).longValue(),commandRequestId,operatorId))
            throw new IllegalStateException("approval execution reservation failed");
    }

    public void finishExecution(long id,String status,String code,String message,boolean recovery,String operatorId){
        int changed=jdbc.update("""
            UPDATE adm_approval_execution SET EXECUTION_STATUS=?,OWNER_RESULT_CODE=?,OWNER_RESULT_MESSAGE=?,
                   COMPLETED_AT=CURRENT_TIMESTAMP,RECOVERY_REQUIRED_YN=?,updated_by=?
             WHERE APPROVAL_REQUEST_ID=? AND EXECUTION_STATUS='RUNNING'
            """,status,code,message,recovery?"Y":"N",operatorId,id);
        if(changed!=1)throw new IllegalStateException("approval execution finalization failed");
    }

    @Transactional(transactionManager = "admTransactionManager")
    public void finishExecutionAndRequest(long id,long expectedRequestVersion,String executionStatus,
            String requestStatus,String code,String message,boolean recovery,String operatorId,
            String reason,String eventData,String transactionId){
        finishExecution(id,executionStatus,code,message,recovery,operatorId);
        int requestChanged=jdbc.update("""
            UPDATE adm_approval_request SET APPROVAL_STATUS=?,VERSION_NO=VERSION_NO+1,updated_by=?
             WHERE APPROVAL_REQUEST_ID=? AND APPROVAL_STATUS='EXECUTING' AND VERSION_NO=?
            """,requestStatus,operatorId,id,expectedRequestVersion);
        if(requestChanged!=1)throw new IllegalStateException("approval request finalization failed");
        history(id,"RESULT",operatorId,"EXECUTING",requestStatus,reason,eventData,transactionId);
    }

    @Transactional(transactionManager = "admTransactionManager")
    public void markExecutionUnknown(long id,String code,String message,String operatorId){
        jdbc.update("""
            UPDATE adm_approval_execution SET EXECUTION_STATUS='UNKNOWN',OWNER_RESULT_CODE=?,
                   OWNER_RESULT_MESSAGE=?,COMPLETED_AT=CURRENT_TIMESTAMP,RECOVERY_REQUIRED_YN='Y',updated_by=?
             WHERE APPROVAL_REQUEST_ID=? AND EXECUTION_STATUS='RUNNING'
            """,code,message,operatorId,id);
        jdbc.update("""
            UPDATE adm_approval_request SET APPROVAL_STATUS='UNKNOWN',VERSION_NO=VERSION_NO+1,updated_by=?
             WHERE APPROVAL_REQUEST_ID=? AND APPROVAL_STATUS='EXECUTING'
            """,operatorId,id);
    }

    public int updateCommandSnapshot(long id,long version,String payloadHash,String payloadSnapshot,String operatorId){
        return jdbc.update("""
            UPDATE adm_approval_request SET COMMAND_PAYLOAD_HASH=?,COMMAND_PAYLOAD_SNAPSHOT=?,
                   VERSION_NO=VERSION_NO+1,updated_by=?
             WHERE APPROVAL_REQUEST_ID=? AND APPROVAL_STATUS='PENDING' AND VERSION_NO=?
            """,payloadHash,payloadSnapshot,operatorId,id,version);
    }

    private static Object value(Map<String,Object> row,String key){
        Object v=row.get(key);if(v!=null)return v;v=row.get(key.toUpperCase(Locale.ROOT));if(v!=null)return v;
        String snake=key.replaceAll("([a-z])([A-Z])","$1_$2").toLowerCase(Locale.ROOT);
        v=row.get(snake);return v!=null?v:row.get(snake.toUpperCase(Locale.ROOT));
    }
    private static String emptyToNull(String v){return v==null||v.isBlank()?null:v.trim();}
    private static String nullable(Object v){return v==null?null:String.valueOf(v);}
}
