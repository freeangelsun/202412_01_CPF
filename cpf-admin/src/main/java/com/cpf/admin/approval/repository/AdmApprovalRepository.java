package com.cpf.admin.approval.repository;

import com.cpf.admin.common.base.AdmBaseRepository;

import com.cpf.data.persistence.api.annotation.CpfTransactional;
import com.cpf.admin.approval.api.AdmApprovalTargetType;
import com.cpf.admin.approval.spi.AdmApprovalDirectoryEntry;
import com.cpf.admin.approval.spi.AdmApprovalDirectoryPort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import com.cpf.data.persistence.api.CpfRepository;
import org.springframework.transaction.annotation.Propagation;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

/** ADM 위험조치 승인 정본 Repository 및 기본 DB Directory Adapter. */
@CpfRepository
// 생성 키 컬럼을 지정하지 않으면 PostgreSQL Driver 는 삽입한 행 전체를 생성 키로 돌려주고
// KeyHolder.getKey() 가 "contains multiple keys" 로 실패한다. Oracle 은 ROWID 를 돌려준다.
// 세 Vendor 공통으로 안전한 방법은 키 컬럼을 명시하는 것이다.
public class AdmApprovalRepository extends AdmBaseRepository implements AdmApprovalDirectoryPort {
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
                  FROM ADM_APPROVAL_POLICY
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
                  FROM ADM_APPROVAL_POLICY WHERE POLICY_CODE=? AND POLICY_VERSION=?
                """, code, version).stream().findFirst();
    }

    public Optional<Map<String,Object>> findActivePolicy(String actionType, Instant at) {
        List<Map<String,Object>> active = jdbc.queryForList("""
                SELECT POLICY_CODE AS policyCode, POLICY_VERSION AS policyVersion,
                       POLICY_NAME AS policyName, ACTION_TYPE AS actionType,
                       EFFECTIVE_FROM AS effectiveFrom, EFFECTIVE_TO AS effectiveTo,
                       ENABLED_YN AS enabledYn, SELF_APPROVAL_ALLOWED_YN AS selfApprovalAllowedYn,
                       BREAK_GLASS_ALLOWED_YN AS breakGlassAllowedYn, DESCRIPTION AS description
                  FROM ADM_APPROVAL_POLICY
                 WHERE ACTION_TYPE=? AND ENABLED_YN='Y'
                   AND EFFECTIVE_FROM <= ?
                   AND (EFFECTIVE_TO IS NULL OR EFFECTIVE_TO > ?)
                 ORDER BY POLICY_VERSION DESC, POLICY_CODE
                """, actionType, Timestamp.from(at), Timestamp.from(at));
        if (active.size() > 1) {
            throw new org.springframework.dao.DataIntegrityViolationException(
                    "multiple active approval policies for actionType=" + actionType);
        }
        return active.stream().findFirst();
    }


    /** Locks one of 64 pre-seeded policy buckets for the current DB transaction. */
    public void lockPolicyActionType(String actionType) {
        int bucket = Math.floorMod(Objects.requireNonNull(actionType, "actionType").hashCode(), 64);
        Integer locked = jdbc.queryForObject(
                "SELECT LOCK_BUCKET FROM ADM_APPROVAL_POLICY_LOCK WHERE LOCK_BUCKET=? FOR UPDATE",
                Integer.class, bucket);
        if (locked == null || locked != bucket) {
            throw new IllegalStateException("approval policy lock bucket is not initialized: " + bucket);
        }
    }

    public boolean hasEnabledPolicyOverlap(String actionType, Instant effectiveFrom, Instant effectiveTo) {
        Timestamp from = Timestamp.from(Objects.requireNonNull(effectiveFrom, "effectiveFrom"));
        Timestamp to = effectiveTo == null ? null : Timestamp.from(effectiveTo);
        Integer count = jdbc.queryForObject("""
                SELECT COUNT(*) FROM ADM_APPROVAL_POLICY
                 WHERE ACTION_TYPE=? AND ENABLED_YN='Y'
                   AND (? IS NULL OR EFFECTIVE_FROM < ?)
                   AND (EFFECTIVE_TO IS NULL OR EFFECTIVE_TO > ?)
                """, Integer.class, actionType, to, to, from);
        return count != null && count > 0;
    }

    public List<Map<String,Object>> findPolicySteps(String code, int version) {
        return jdbc.queryForList("""
                SELECT POLICY_CODE AS policyCode, POLICY_VERSION AS policyVersion, STEP_NO AS stepNo,
                       STEP_TYPE AS stepType, TARGET_TYPE AS targetType, TARGET_CODE AS targetCode,
                       DECISION_RULE AS decisionRule, REQUIRED_COUNT AS requiredCount, REQUIRED_YN AS requiredYn
                  FROM ADM_APPROVAL_POLICY_STEP
                 WHERE POLICY_CODE=? AND POLICY_VERSION=?
                 ORDER BY STEP_NO, TARGET_TYPE, TARGET_CODE
                """, code, version);
    }

    /** Inserts a new immutable policy version. Existing policyCode/version rows are never updated. */
    public void insertPolicy(Map<String,Object> p, List<Map<String,Object>> steps) {
        String code = String.valueOf(p.get("policyCode"));
        int version = ((Number)p.get("policyVersion")).intValue();
        if (findPolicy(code, version).isPresent()) {
            throw new org.springframework.dao.DataIntegrityViolationException(
                    "approval policy version already exists: " + code + "/" + version);
        }
        jdbc.update("""
                INSERT INTO ADM_APPROVAL_POLICY (
                    POLICY_CODE,POLICY_VERSION,POLICY_NAME,ACTION_TYPE,EFFECTIVE_FROM,EFFECTIVE_TO,
                    ENABLED_YN,SELF_APPROVAL_ALLOWED_YN,BREAK_GLASS_ALLOWED_YN,DESCRIPTION,created_by,updated_by
                ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)
                """, p.get("policyCode"),p.get("policyVersion"),p.get("policyName"),p.get("actionType"),
                p.get("effectiveFrom"),p.get("effectiveTo"),p.get("enabledYn"),p.get("selfApprovalAllowedYn"),
                p.get("breakGlassAllowedYn"),p.get("description"),p.get("operatorId"),p.get("operatorId"));
        for (Map<String,Object> s : steps) {
            jdbc.update("""
                    INSERT INTO ADM_APPROVAL_POLICY_STEP (
                        POLICY_CODE,POLICY_VERSION,STEP_NO,STEP_TYPE,TARGET_TYPE,TARGET_CODE,
                        DECISION_RULE,REQUIRED_COUNT,REQUIRED_YN,created_by,updated_by
                    ) VALUES (?,?,?,?,?,?,?,?,?,?,?)
                    """, s.get("policyCode"),s.get("policyVersion"),s.get("stepNo"),s.get("stepType"),
                    s.get("targetType"),s.get("targetCode"),s.get("decisionRule"),s.get("requiredCount"),
                    s.get("requiredYn"),s.get("operatorId"),s.get("operatorId"));
        }
        jdbc.update("""
                INSERT INTO adm_approval_policy_history (
                    POLICY_CODE,POLICY_VERSION,CHANGE_TYPE,CHANGE_REASON,BEFORE_HASH,AFTER_HASH,OPERATOR_ID
                ) VALUES (?,?,'CREATE',?,NULL,?,?)
                """,p.get("policyCode"),p.get("policyVersion"),p.get("changeReason"),p.get("policyHash"),p.get("operatorId"));
    }

    /** @deprecated policy versions are immutable; this compatibility method now inserts only. */
    @Deprecated(forRemoval = true)
    public void replacePolicy(Map<String,Object> p, List<Map<String,Object>> steps) { insertPolicy(p, steps); }

    @Override
    public List<AdmApprovalDirectoryEntry> resolve(AdmApprovalTargetType type, String code, Instant at) {
        Timestamp ts=Timestamp.from(at);
        String sql=switch(type){
            case OPERATOR -> """
                SELECT o.OPERATOR_ID operatorId,p.ORGANIZATION_CODE organizationCode,
                       p.POSITION_CODE positionCode,p.JOB_TITLE_CODE jobTitleCode
                  FROM ADM_OPERATOR o LEFT JOIN ADM_OPERATOR_PROFILE p ON p.OPERATOR_ID=o.OPERATOR_ID
                 WHERE o.OPERATOR_ID=? AND o.USE_YN='Y' AND o.LOCKED_YN='N'
                   AND (p.EFFECTIVE_FROM IS NULL OR p.EFFECTIVE_FROM<=?)
                   AND (p.EFFECTIVE_TO IS NULL OR p.EFFECTIVE_TO>?)
                """;
            case ROLE -> """
                SELECT o.OPERATOR_ID operatorId,p.ORGANIZATION_CODE organizationCode,
                       p.POSITION_CODE positionCode,p.JOB_TITLE_CODE jobTitleCode
                  FROM ADM_OPERATOR_ROLE r JOIN ADM_OPERATOR o ON o.OPERATOR_ID=r.OPERATOR_ID
                  LEFT JOIN ADM_OPERATOR_PROFILE p ON p.OPERATOR_ID=o.OPERATOR_ID
                 WHERE r.ROLE_ID=? AND o.USE_YN='Y' AND o.LOCKED_YN='N'
                   AND (p.EFFECTIVE_FROM IS NULL OR p.EFFECTIVE_FROM<=?)
                   AND (p.EFFECTIVE_TO IS NULL OR p.EFFECTIVE_TO>?)
                """;
            case ORGANIZATION -> """
                SELECT o.OPERATOR_ID operatorId,p.ORGANIZATION_CODE organizationCode,
                       p.POSITION_CODE positionCode,p.JOB_TITLE_CODE jobTitleCode
                  FROM ADM_OPERATOR_PROFILE p JOIN ADM_OPERATOR o ON o.OPERATOR_ID=p.OPERATOR_ID
                 WHERE p.ORGANIZATION_CODE=? AND o.USE_YN='Y' AND o.LOCKED_YN='N'
                   AND (p.EFFECTIVE_FROM IS NULL OR p.EFFECTIVE_FROM<=?)
                   AND (p.EFFECTIVE_TO IS NULL OR p.EFFECTIVE_TO>?)
                """;
            case ORG_MANAGER -> """
                SELECT o.OPERATOR_ID operatorId,p.ORGANIZATION_CODE organizationCode,
                       p.POSITION_CODE positionCode,p.JOB_TITLE_CODE jobTitleCode
                  FROM ADM_ORGANIZATION g JOIN ADM_OPERATOR o ON o.OPERATOR_ID=g.MANAGER_OPERATOR_ID
                  LEFT JOIN ADM_OPERATOR_PROFILE p ON p.OPERATOR_ID=o.OPERATOR_ID
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
              FROM ADM_APPROVAL_REQUEST WHERE REQUEST_KEY=?
            """,requestKey).stream().findFirst();
    }

    public long insertRequest(Map<String,Object> v){
        KeyHolder kh=new GeneratedKeyHolder();
        jdbc.update(c->{
            PreparedStatement ps=c.prepareStatement("""
                INSERT INTO ADM_APPROVAL_REQUEST (
                    REQUEST_KEY,POLICY_CODE,POLICY_VERSION,ACTION_TYPE,OWNER_MODULE,OWNER_COMMAND,
                    TARGET_TYPE,TARGET_ID,REQUESTED_BY,REQUEST_REASON,COMMAND_PAYLOAD_HASH,
                    COMMAND_PAYLOAD_SNAPSHOT,APPROVAL_STATUS,CURRENT_STEP_NO,EXPIRE_AT,TRANSACTION_ID,
                    VERSION_NO,created_by,updated_by
                ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,'PENDING',?,?,?,?,?,?)
                """, new String[]{"approval_request_id"});
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
            INSERT INTO ADM_APPROVAL_PARTICIPANT (
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
              FROM ADM_APPROVAL_REQUEST WHERE APPROVAL_REQUEST_ID=?
            """,id).stream().findFirst();
    }

    public List<Map<String,Object>> findParticipants(long id){
        return jdbc.queryForList("""
            SELECT APPROVAL_PARTICIPANT_ID participantId,STEP_NO stepNo,OPERATOR_ID operatorId,
                   SOURCE_TARGET_TYPE sourceTargetType,SOURCE_TARGET_CODE sourceTargetCode,
                   ORGANIZATION_CODE_SNAPSHOT organizationCode,POSITION_CODE_SNAPSHOT positionCode,
                   JOB_TITLE_CODE_SNAPSHOT jobTitleCode,DECISION_STATUS decisionStatus,
                   DECISION_REASON decisionReason,DECIDED_AT decidedAt
              FROM ADM_APPROVAL_PARTICIPANT WHERE APPROVAL_REQUEST_ID=?
             ORDER BY STEP_NO,APPROVAL_PARTICIPANT_ID
            """,id);
    }

    public Optional<Map<String,Object>> findWaitingParticipant(long id,int stepNo,String operatorId){
        return jdbc.queryForList("""
            SELECT APPROVAL_PARTICIPANT_ID participantId,STEP_NO stepNo,SOURCE_TARGET_TYPE sourceTargetType,
                   SOURCE_TARGET_CODE sourceTargetCode
              FROM ADM_APPROVAL_PARTICIPANT
             WHERE APPROVAL_REQUEST_ID=? AND STEP_NO=? AND OPERATOR_ID=? AND DECISION_STATUS='WAITING'
            """,id,stepNo,operatorId).stream().findFirst();
    }

    public Optional<Map<String,Object>> findDecisionByKey(String key){
        return jdbc.queryForList("""
            SELECT APPROVAL_REQUEST_ID approvalRequestId,APPROVAL_PARTICIPANT_ID participantId,
                   OPERATOR_ID operatorId,DECISION_STATUS decisionStatus,DECISION_REASON decisionReason
              FROM ADM_APPROVAL_PARTICIPANT WHERE IDEMPOTENCY_KEY=?
            """,key).stream().findFirst();
    }

    public boolean decisionKeyExists(String key){
        return findDecisionByKey(key).isPresent();
    }

    public int decideParticipant(long participantId,String status,String key,String reason,String operatorId){
        return jdbc.update("""
            UPDATE ADM_APPROVAL_PARTICIPANT SET DECISION_STATUS=?,IDEMPOTENCY_KEY=?,DECISION_REASON=?,
                   DECIDED_AT=CURRENT_TIMESTAMP(3),updated_by=?
             WHERE APPROVAL_PARTICIPANT_ID=? AND DECISION_STATUS='WAITING'
            """,status,key,reason,operatorId,participantId);
    }

    public int updateRequest(long id,long version,String status,int step,String operatorId){
        return jdbc.update("""
            UPDATE ADM_APPROVAL_REQUEST SET APPROVAL_STATUS=?,CURRENT_STEP_NO=?,VERSION_NO=VERSION_NO+1,updated_by=?
             WHERE APPROVAL_REQUEST_ID=? AND VERSION_NO=?
            """,status,step,operatorId,id,version);
    }


    /**
     * Persists a fail-closed integrity incident independently from the caller transaction so that
     * the audit survives the service exception and the compromised approval cannot be retried.
     */
    @CpfTransactional(transactionManager="admTransactionManager", propagation=Propagation.REQUIRES_NEW)
    public void recordIntegrityFailure(long id,String operatorId,String beforeStatus,
                                       String reason,String eventData,String transactionId){
        int changed=jdbc.update("""
            UPDATE ADM_APPROVAL_REQUEST SET APPROVAL_STATUS='UNKNOWN',VERSION_NO=VERSION_NO+1,updated_by=?
             WHERE APPROVAL_REQUEST_ID=? AND APPROVAL_STATUS=?
            """,operatorId,id,beforeStatus);
        if(changed!=1)throw new IllegalStateException("approval integrity status transition failed");
        history(id,"SNAPSHOT_HASH_MISMATCH",operatorId,beforeStatus,"UNKNOWN",reason,eventData,transactionId);
    }

    public void history(long id,String event,String actor,String before,String after,String reason,String data,String tx){
        jdbc.update("""
            INSERT INTO ADM_APPROVAL_HISTORY (
              APPROVAL_REQUEST_ID,EVENT_TYPE,ACTOR_ID,BEFORE_STATUS,AFTER_STATUS,REASON,EVENT_DATA,TRANSACTION_ID
            ) VALUES (?,?,?,?,?,?,?,?)
            """,id,event,actor,before,after,reason,data,tx);
    }

    public Optional<Map<String,Object>> findExecution(long id){
        return jdbc.queryForList("""
            SELECT APPROVAL_REQUEST_ID approvalRequestId,COMMAND_REQUEST_ID commandRequestId,
                   EXECUTION_STATUS executionStatus,OWNER_RESULT_CODE ownerResultCode,
                   OWNER_RESULT_MESSAGE ownerResultMessage,STARTED_AT startedAt,COMPLETED_AT completedAt,
                   RECOVERY_REQUIRED_YN recoveryRequiredYn,LEASE_OWNER leaseOwner,LEASE_EXPIRES_AT leaseExpiresAt,FENCE_TOKEN fenceToken
              FROM ADM_APPROVAL_EXECUTION WHERE APPROVAL_REQUEST_ID=?
            """,id).stream().findFirst();
    }

    /**
     * Returns the exact server-reserved execution envelope immediately before Owner mutation.
     * Direct adapter calls without an APPROVED -> EXECUTING reservation fail closed.
     */
    public boolean isApprovedParticipant(long requestId,String operatorId){
        Integer count=jdbc.queryForObject("""
            SELECT COUNT(*) FROM ADM_APPROVAL_PARTICIPANT
             WHERE APPROVAL_REQUEST_ID=? AND OPERATOR_ID=? AND DECISION_STATUS='APPROVED'
            """,Integer.class,requestId,operatorId);
        return count!=null&&count>0;
    }

    public Optional<Map<String,Object>> findReservedExecutionCommand(long id,String commandRequestId){
        return jdbc.queryForList("""
            SELECT r.APPROVAL_REQUEST_ID approvalRequestId,r.REQUEST_KEY requestKey,
                   r.POLICY_CODE policyCode,r.POLICY_VERSION policyVersion,r.ACTION_TYPE actionType,
                   r.OWNER_MODULE ownerModule,r.OWNER_COMMAND ownerCommand,r.TARGET_TYPE targetType,
                   r.TARGET_ID targetId,r.REQUESTED_BY requestedBy,r.REQUEST_REASON requestReason,
                   r.COMMAND_PAYLOAD_HASH payloadHash,r.COMMAND_PAYLOAD_SNAPSHOT payloadSnapshot,
                   r.APPROVAL_STATUS approvalStatus,r.EXPIRE_AT expireAt,r.TRANSACTION_ID transactionId,
                   r.VERSION_NO versionNo,e.COMMAND_REQUEST_ID commandRequestId,e.EXECUTION_STATUS executionStatus,
                   e.LEASE_OWNER leaseOwner,e.LEASE_EXPIRES_AT leaseExpiresAt,e.FENCE_TOKEN fenceToken
              FROM ADM_APPROVAL_REQUEST r
              JOIN ADM_APPROVAL_EXECUTION e ON e.APPROVAL_REQUEST_ID=r.APPROVAL_REQUEST_ID
             WHERE r.APPROVAL_REQUEST_ID=? AND e.COMMAND_REQUEST_ID=?
               AND r.APPROVAL_STATUS='EXECUTING' AND e.EXECUTION_STATUS='RUNNING'
            """,id,commandRequestId).stream().findFirst();
    }

    @CpfTransactional(transactionManager="admTransactionManager")
    public boolean reserveExecution(long id,long expectedVersion,String commandRequestId,String operatorId){
        int requestChanged=jdbc.update("""
            UPDATE ADM_APPROVAL_REQUEST SET APPROVAL_STATUS='EXECUTING',VERSION_NO=VERSION_NO+1,updated_by=?
             WHERE APPROVAL_REQUEST_ID=? AND APPROVAL_STATUS='APPROVED' AND VERSION_NO=?
            """,operatorId,id,expectedVersion);
        if(requestChanged!=1)return false;
        Instant leaseExpiresAt=Instant.now().plus(EXECUTION_LEASE);
        jdbc.update("""
            INSERT INTO ADM_APPROVAL_EXECUTION (
              APPROVAL_REQUEST_ID,COMMAND_REQUEST_ID,EXECUTION_STATUS,STARTED_AT,RECOVERY_REQUIRED_YN,
              LEASE_OWNER,LEASE_EXPIRES_AT,FENCE_TOKEN,created_by,updated_by
            ) VALUES (?,?,'RUNNING',CURRENT_TIMESTAMP,'N',?,?,1,?,?)
            """,id,commandRequestId,executionLeaseOwner(),Timestamp.from(leaseExpiresAt),operatorId,operatorId);
        return true;
    }

    /** Atomically reserves UNKNOWN -> EXECUTING for observation-only reconciliation. */
    @CpfTransactional(transactionManager="admTransactionManager")
    public boolean reserveReconcile(long id,long expectedVersion,String operatorId){
        int executionChanged=jdbc.update("""
            UPDATE ADM_APPROVAL_EXECUTION SET EXECUTION_STATUS='RUNNING',STARTED_AT=CURRENT_TIMESTAMP,
                   COMPLETED_AT=NULL,LEASE_OWNER=?,LEASE_EXPIRES_AT=?,FENCE_TOKEN=FENCE_TOKEN+1,updated_by=?
             WHERE APPROVAL_REQUEST_ID=? AND EXECUTION_STATUS='UNKNOWN' AND RECOVERY_REQUIRED_YN='Y'
            """,executionLeaseOwner(),Timestamp.from(Instant.now().plus(EXECUTION_LEASE)),operatorId,id);
        if(executionChanged!=1)return false;
        int requestChanged=jdbc.update("""
            UPDATE ADM_APPROVAL_REQUEST SET APPROVAL_STATUS='EXECUTING',VERSION_NO=VERSION_NO+1,updated_by=?
             WHERE APPROVAL_REQUEST_ID=? AND APPROVAL_STATUS='UNKNOWN' AND VERSION_NO=?
            """,operatorId,id,expectedVersion);
        if(requestChanged!=1)throw new IllegalStateException("approval reconcile reservation failed");
        return true;
    }

    /** @deprecated use reserveExecution for atomic APPROVED -> EXECUTING reservation. */
    @Deprecated
    public void startExecution(long id,String commandRequestId,String operatorId){
        Map<String,Object> request=findRequest(id).orElseThrow();
        if(!reserveExecution(id,((Number)value(request,"versionNo")).longValue(),commandRequestId,operatorId))
            throw new IllegalStateException("approval execution reservation failed");
    }

    public void finishExecution(long id,String commandRequestId,String leaseOwner,long fenceToken,
            String status,String code,String message,boolean recovery,String operatorId){
        int changed=jdbc.update("""
            UPDATE ADM_APPROVAL_EXECUTION SET EXECUTION_STATUS=?,OWNER_RESULT_CODE=?,OWNER_RESULT_MESSAGE=?,
                   COMPLETED_AT=CURRENT_TIMESTAMP,RECOVERY_REQUIRED_YN=?,LEASE_OWNER=NULL,LEASE_EXPIRES_AT=NULL,updated_by=?
             WHERE APPROVAL_REQUEST_ID=? AND COMMAND_REQUEST_ID=? AND EXECUTION_STATUS='RUNNING'
               AND LEASE_OWNER=? AND FENCE_TOKEN=?
            """,status,code,message,recovery?"Y":"N",operatorId,id,commandRequestId,leaseOwner,fenceToken);
        if(changed!=1)throw new IllegalStateException("approval execution finalization rejected by fence");
    }

    @CpfTransactional(transactionManager="admTransactionManager")
    public void finishExecutionAndRequest(long id,long expectedRequestVersion,String commandRequestId,
            String leaseOwner,long fenceToken,String executionStatus,
            String requestStatus,String code,String message,boolean recovery,String operatorId,
            String reason,String eventData,String transactionId){
        finishExecution(id,commandRequestId,leaseOwner,fenceToken,executionStatus,code,message,recovery,operatorId);
        int requestChanged=jdbc.update("""
            UPDATE ADM_APPROVAL_REQUEST SET APPROVAL_STATUS=?,VERSION_NO=VERSION_NO+1,updated_by=?
             WHERE APPROVAL_REQUEST_ID=? AND APPROVAL_STATUS='EXECUTING' AND VERSION_NO=?
            """,requestStatus,operatorId,id,expectedRequestVersion);
        if(requestChanged!=1)throw new IllegalStateException("approval request finalization failed");
        history(id,"RESULT",operatorId,"EXECUTING",requestStatus,reason,eventData,transactionId);
    }


    /** Atomically preserves a post-reservation integrity failure as UNKNOWN with an audit event. */
    @CpfTransactional(transactionManager="admTransactionManager")
    public void recordExecutionIntegrityFailure(long id,String commandRequestId,String leaseOwner,long fenceToken,
                                                String code,String message,String operatorId,
                                                String reason,String eventData,String transactionId){
        int executionChanged=jdbc.update("""
            UPDATE ADM_APPROVAL_EXECUTION SET EXECUTION_STATUS='UNKNOWN',OWNER_RESULT_CODE=?,
                   OWNER_RESULT_MESSAGE=?,COMPLETED_AT=CURRENT_TIMESTAMP,RECOVERY_REQUIRED_YN='Y',LEASE_OWNER=NULL,LEASE_EXPIRES_AT=NULL,updated_by=?
             WHERE APPROVAL_REQUEST_ID=? AND COMMAND_REQUEST_ID=? AND EXECUTION_STATUS='RUNNING'
               AND LEASE_OWNER=? AND FENCE_TOKEN=?
            """,code,message,operatorId,id,commandRequestId,leaseOwner,fenceToken);
        int requestChanged=jdbc.update("""
            UPDATE ADM_APPROVAL_REQUEST SET APPROVAL_STATUS='UNKNOWN',VERSION_NO=VERSION_NO+1,updated_by=?
             WHERE APPROVAL_REQUEST_ID=? AND APPROVAL_STATUS='EXECUTING'
            """,operatorId,id);
        if(executionChanged!=1||requestChanged!=1)
            throw new IllegalStateException("approval execution integrity transition failed");
        history(id,"SNAPSHOT_HASH_MISMATCH",operatorId,"EXECUTING","UNKNOWN",reason,eventData,transactionId);
    }

    @CpfTransactional(transactionManager="admTransactionManager")
    public void markExecutionUnknown(long id,String commandRequestId,String leaseOwner,long fenceToken,
                                     String code,String message,String operatorId){
        int executionChanged=jdbc.update("""
            UPDATE ADM_APPROVAL_EXECUTION SET EXECUTION_STATUS='UNKNOWN',OWNER_RESULT_CODE=?,
                   OWNER_RESULT_MESSAGE=?,COMPLETED_AT=CURRENT_TIMESTAMP,RECOVERY_REQUIRED_YN='Y',LEASE_OWNER=NULL,LEASE_EXPIRES_AT=NULL,updated_by=?
             WHERE APPROVAL_REQUEST_ID=? AND COMMAND_REQUEST_ID=? AND EXECUTION_STATUS='RUNNING'
               AND LEASE_OWNER=? AND FENCE_TOKEN=?
            """,code,message,operatorId,id,commandRequestId,leaseOwner,fenceToken);
        int requestChanged=jdbc.update("""
            UPDATE ADM_APPROVAL_REQUEST SET APPROVAL_STATUS='UNKNOWN',VERSION_NO=VERSION_NO+1,updated_by=?
             WHERE APPROVAL_REQUEST_ID=? AND APPROVAL_STATUS='EXECUTING'
            """,operatorId,id);
        if(executionChanged!=1||requestChanged!=1)
            throw new IllegalStateException("approval UNKNOWN transition failed");
    }


    private static final Duration EXECUTION_LEASE=Duration.ofMinutes(5);
    private static final Duration LEGACY_RUNNING_GRACE=Duration.ofMinutes(10);

    /**
     * Converts stale RUNNING/EXECUTING reservations to durable UNKNOWN without replaying the mutation.
     * The transition is cluster-safe because the execution UPDATE is conditional on the current RUNNING
     * state and expired lease. A later operator/system reconcile only observes Owner state.
     */
    @CpfTransactional(transactionManager="admTransactionManager")
    public int sweepExpiredExecutions(Instant now,int maxRows,String operatorId){
        Objects.requireNonNull(now,"now");
        if(maxRows<1) return 0;
        Timestamp nowTs=Timestamp.from(now);
        Timestamp legacyCutoff=Timestamp.from(now.minus(LEGACY_RUNNING_GRACE));
        List<Map<String,Object>> candidates=jdbc.queryForList("""
            SELECT APPROVAL_REQUEST_ID approvalRequestId,COMMAND_REQUEST_ID commandRequestId,
                   LEASE_OWNER leaseOwner,FENCE_TOKEN fenceToken FROM ADM_APPROVAL_EXECUTION
             WHERE EXECUTION_STATUS='RUNNING'
               AND ((LEASE_EXPIRES_AT IS NOT NULL AND LEASE_EXPIRES_AT<=?)
                    OR (LEASE_EXPIRES_AT IS NULL AND STARTED_AT IS NOT NULL AND STARTED_AT<=?))
             ORDER BY STARTED_AT,APPROVAL_REQUEST_ID
            """,nowTs,legacyCutoff);
        int recovered=0;
        for(Map<String,Object> candidate:candidates.stream().limit(maxRows).toList()){
            long id=((Number)value(candidate,"approvalRequestId")).longValue();
            String commandRequestId=nullable(value(candidate,"commandRequestId"));
            String leaseOwner=nullable(value(candidate,"leaseOwner"));
            long fenceToken=((Number)value(candidate,"fenceToken")).longValue();
            int executionChanged=leaseOwner==null ? jdbc.update("""
                UPDATE ADM_APPROVAL_EXECUTION SET EXECUTION_STATUS='UNKNOWN',OWNER_RESULT_CODE='ADM-EXECUTION-LEASE-EXPIRED',
                       OWNER_RESULT_MESSAGE='실행 Lease 만료로 Owner 결과 확인이 필요합니다.',COMPLETED_AT=CURRENT_TIMESTAMP,
                       RECOVERY_REQUIRED_YN='Y',LEASE_OWNER=NULL,LEASE_EXPIRES_AT=NULL,updated_by=?
                 WHERE APPROVAL_REQUEST_ID=? AND COMMAND_REQUEST_ID=? AND EXECUTION_STATUS='RUNNING'
                   AND LEASE_OWNER IS NULL AND FENCE_TOKEN=?
                   AND ((LEASE_EXPIRES_AT IS NOT NULL AND LEASE_EXPIRES_AT<=?)
                        OR (LEASE_EXPIRES_AT IS NULL AND STARTED_AT IS NOT NULL AND STARTED_AT<=?))
                """,operatorId,id,commandRequestId,fenceToken,nowTs,legacyCutoff) : jdbc.update("""
                UPDATE ADM_APPROVAL_EXECUTION SET EXECUTION_STATUS='UNKNOWN',OWNER_RESULT_CODE='ADM-EXECUTION-LEASE-EXPIRED',
                       OWNER_RESULT_MESSAGE='실행 Lease 만료로 Owner 결과 확인이 필요합니다.',COMPLETED_AT=CURRENT_TIMESTAMP,
                       RECOVERY_REQUIRED_YN='Y',LEASE_OWNER=NULL,LEASE_EXPIRES_AT=NULL,updated_by=?
                 WHERE APPROVAL_REQUEST_ID=? AND COMMAND_REQUEST_ID=? AND EXECUTION_STATUS='RUNNING'
                   AND LEASE_OWNER=? AND FENCE_TOKEN=?
                   AND ((LEASE_EXPIRES_AT IS NOT NULL AND LEASE_EXPIRES_AT<=?)
                        OR (LEASE_EXPIRES_AT IS NULL AND STARTED_AT IS NOT NULL AND STARTED_AT<=?))
                """,operatorId,id,commandRequestId,leaseOwner,fenceToken,nowTs,legacyCutoff);
            if(executionChanged!=1) continue;
            int requestChanged=jdbc.update("""
                UPDATE ADM_APPROVAL_REQUEST SET APPROVAL_STATUS='UNKNOWN',VERSION_NO=VERSION_NO+1,updated_by=?
                 WHERE APPROVAL_REQUEST_ID=? AND APPROVAL_STATUS='EXECUTING'
                """,operatorId,id);
            if(requestChanged!=1) throw new IllegalStateException("stale approval request transition failed: "+id);
            history(id,"EXECUTION_LEASE_EXPIRED",operatorId,"EXECUTING","UNKNOWN",
                    "stale execution reservation recovered after process loss",
                    "{\"recovery\":\"RECONCILE_REQUIRED\"}",null);
            recovered++;
        }
        return recovered;
    }

    private static String executionLeaseOwner(){
        return com.cpf.foundation.runtime.CpfInstanceIdentity.current().instanceId();
    }

    public int updateCommandSnapshot(long id,long version,String payloadHash,String payloadSnapshot,String operatorId){
        return jdbc.update("""
            UPDATE ADM_APPROVAL_REQUEST SET COMMAND_PAYLOAD_HASH=?,COMMAND_PAYLOAD_SNAPSHOT=?,
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
