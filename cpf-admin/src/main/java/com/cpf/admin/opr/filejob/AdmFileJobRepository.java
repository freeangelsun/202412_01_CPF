package com.cpf.admin.opr.filejob;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import com.cpf.data.persistence.api.CpfRepository;
import java.sql.*;
import java.time.*;
import java.util.*;

/** 3개 공식 DB에서 같은 SQL 계약으로 동작하는 File Job Repository입니다. */
@CpfRepository
public class AdmFileJobRepository {
    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper;
    private final AdmFilePayloadProtector payloadProtector;
    public AdmFileJobRepository(@Qualifier("admJdbcTemplate") JdbcTemplate jdbc, ObjectMapper objectMapper,
                                AdmFilePayloadProtector payloadProtector){
        this.jdbc=jdbc;this.objectMapper=objectMapper;this.payloadProtector=payloadProtector;
    }

    public Optional<Job> findByOperation(String operationId){
        return jdbc.query("SELECT * FROM adm_file_job WHERE operation_id=?",
                ps->ps.setString(1,operationId),rs->rs.next()?Optional.of(mapJob(rs)):Optional.empty());
    }
    public Job get(String jobId){
        return jdbc.query("SELECT * FROM adm_file_job WHERE job_id=?",ps->ps.setString(1,jobId),
                rs->{if(!rs.next())throw new IllegalArgumentException("File Job을 찾을 수 없습니다.");return mapJob(rs);});
    }
    public List<Job> list(int limit){
        int bounded=Math.max(1,Math.min(limit,500));
        return jdbc.query(con->{
            PreparedStatement ps=con.prepareStatement("SELECT * FROM adm_file_job ORDER BY created_at DESC");
            ps.setMaxRows(bounded);return ps;},(rs,row)->mapJob(rs));
    }
    public Job insert(Job job){
        try{
            jdbc.update("""
                INSERT INTO adm_file_job
                (job_id,operation_id,request_hash,job_type,template_code,template_version,file_format,
                 job_state,dry_run,rollback_supported,source_path,result_path,source_sha256,result_sha256,
                 total_rows,success_rows,failed_rows,lease_owner,fencing_token,lease_until,retention_until,
                 requested_by,reason,client_ip,error_code,error_message,approval_id,applied_by,resolved_by,
                 control_by,control_reason,control_updated_at,created_at,updated_at)
                VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                """,job.jobId(),job.operationId(),job.requestHash(),job.jobType().name(),job.templateCode(),
                    job.templateVersion(),job.format(),job.state().name(),yn(job.dryRun()),yn(job.rollbackSupported()),
                    job.sourcePath(),job.resultPath(),job.sourceSha256(),job.resultSha256(),job.totalRows(),
                    job.successRows(),job.failedRows(),job.leaseOwner(),job.fencingToken(),ts(job.leaseUntil()),
                    ts(job.retentionUntil()),job.requestedBy(),job.reason(),job.clientIp(),job.errorCode(),
                    job.errorMessage(),job.approvalId(),job.appliedBy(),job.resolvedBy(),job.controlBy(),job.controlReason(),
                    ts(job.controlUpdatedAt()),ts(job.createdAt()),ts(job.updatedAt()));
            return job;
        }catch(DuplicateKeyException duplicate){
            Job existing=findByOperation(job.operationId()).orElseThrow();
            if(!existing.requestHash().equals(job.requestHash()))throw new IllegalStateException("동일 operationId의 request hash가 다릅니다.");
            return existing;
        }
    }
    public boolean transition(String jobId,AdmFileJobState expected,AdmFileJobState next,String errorCode,String errorMessage){
        return jdbc.update("""
            UPDATE adm_file_job SET job_state=?,error_code=?,error_message=?,updated_at=?
             WHERE job_id=? AND job_state=?
            """,next.name(),errorCode,errorMessage,ts(Instant.now()),jobId,expected.name())==1;
    }
    public boolean transitionControl(String jobId, AdmFileJobState expected, AdmFileJobState next,
                                     String operator, String reason, String approvalId, ControlActor actor,
                                     String errorCode, String errorMessage){
        Objects.requireNonNull(actor,"actor");
        String actorColumn=switch(actor){case APPLIED_BY->"applied_by";case RESOLVED_BY->"resolved_by";case NONE->null;};
        String sql="UPDATE adm_file_job SET job_state=?,error_code=?,error_message=?,approval_id=?,control_by=?,control_reason=?,control_updated_at=?,updated_at=?"
                +(actorColumn==null?"":","+actorColumn+"=?")+" WHERE job_id=? AND job_state=?";
        List<Object> args=new ArrayList<>(Arrays.asList(next.name(),errorCode,errorMessage,approvalId,operator,reason,ts(Instant.now()),ts(Instant.now())));
        if(actorColumn!=null)args.add(operator);
        args.add(jobId);args.add(expected.name());
        return jdbc.update(sql,args.toArray())==1;
    }
    public Optional<Job> claim(Set<AdmFileJobState> states,String owner,Duration lease){
        for(AdmFileJobState state:states){
            List<Job> candidates=jdbc.query(con->{
                PreparedStatement ps=con.prepareStatement("""
                    SELECT * FROM adm_file_job
                     WHERE job_state=? AND (lease_until IS NULL OR lease_until<?)
                     ORDER BY created_at
                    """);ps.setString(1,state.name());ps.setTimestamp(2,ts(Instant.now()));ps.setMaxRows(10);return ps;
            },(rs,row)->mapJob(rs));
            for(Job candidate:candidates){
                long fence=candidate.fencingToken()+1;
                int updated=jdbc.update("""
                    UPDATE adm_file_job SET lease_owner=?,fencing_token=?,lease_until=?,updated_at=?
                     WHERE job_id=? AND job_state=? AND fencing_token=?
                       AND (lease_until IS NULL OR lease_until<?)
                    """,owner,fence,ts(Instant.now().plus(lease)),ts(Instant.now()),candidate.jobId(),
                        state.name(),candidate.fencingToken(),ts(Instant.now()));
                if(updated==1)return Optional.of(get(candidate.jobId()));
            }
        }
        return Optional.empty();
    }
    public void heartbeat(String jobId,String owner,long fence,Duration lease){
        if(jdbc.update("""
            UPDATE adm_file_job SET lease_until=?,updated_at=?
             WHERE job_id=? AND lease_owner=? AND fencing_token=? AND lease_until>=?
            """,ts(Instant.now().plus(lease)),ts(Instant.now()),jobId,owner,fence,ts(Instant.now()))!=1)throw new IllegalStateException("File Job lease/fencing을 잃었습니다.");
    }
    public void complete(String jobId,String owner,long fence,AdmFileJobState state,long total,long success,long failed,
                         String sourceSha,String resultPath,String resultSha){
        complete(jobId,owner,fence,state,total,success,failed,sourceSha,resultPath,resultSha,null,null);
    }
    public void complete(String jobId,String owner,long fence,AdmFileJobState state,long total,long success,long failed,
                         String sourceSha,String resultPath,String resultSha,String errorCode,String errorMessage){
        if(jdbc.update("""
            UPDATE adm_file_job SET job_state=?,total_rows=?,success_rows=?,failed_rows=?,
                   source_sha256=?,result_path=?,result_sha256=?,error_code=?,error_message=?,
                   lease_owner=NULL,lease_until=NULL,updated_at=?
             WHERE job_id=? AND lease_owner=? AND fencing_token=? AND lease_until>=?
            """,state.name(),total,success,failed,sourceSha,resultPath,resultSha,errorCode,errorMessage,
                ts(Instant.now()),jobId,owner,fence,ts(Instant.now()))!=1)
            throw new IllegalStateException("File Job 완료 fencing 검증에 실패했습니다.");
    }
    public void deleteRowsFenced(String jobId,String owner,long fence){
        int active=jdbc.queryForObject("SELECT COUNT(*) FROM adm_file_job WHERE job_id=? AND lease_owner=? AND fencing_token=? AND lease_until>=?",
                Integer.class,jobId,owner,fence,ts(Instant.now()));
        if(active!=1)throw new IllegalStateException("File Job 행 초기화 fencing 검증에 실패했습니다.");
        jdbc.update("DELETE FROM adm_file_job_row WHERE job_id=?",jobId);
    }
    public void markRowDispatching(String jobId,long rowNumber,String owner,long fence,String expectedState,String dispatchState){
        Instant now=Instant.now();
        int updated=jdbc.update("""
            UPDATE adm_file_job_row SET row_state=?,error_code=NULL,error_message=NULL,updated_at=?
             WHERE job_id=? AND row_no=? AND row_state=?
               AND EXISTS (SELECT 1 FROM adm_file_job j WHERE j.job_id=? AND j.lease_owner=? AND j.fencing_token=? AND j.lease_until>=?)
            """,dispatchState,ts(now),jobId,rowNumber,expectedState,jobId,owner,fence,ts(now));
        if(updated!=1)throw new IllegalStateException("File Job 행 Dispatch lease/fencing/CAS 검증에 실패했습니다.");
    }
    public void updateRowFenced(String jobId,long rowNumber,String owner,long fence,String expectedState,String state,String businessKey,
                                String errorCode,String message,String rollbackToken){
        Instant now=Instant.now();
        int updated=jdbc.update("""
            UPDATE adm_file_job_row SET row_state=?,business_key=?,error_code=?,error_message=?,rollback_token=?,updated_at=?
             WHERE job_id=? AND row_no=? AND row_state=?
               AND EXISTS (SELECT 1 FROM adm_file_job j WHERE j.job_id=? AND j.lease_owner=? AND j.fencing_token=? AND j.lease_until>=?)
            """,state,businessKey,errorCode,message,rollbackToken,ts(now),jobId,rowNumber,expectedState,jobId,owner,fence,ts(now));
        if(updated!=1)throw new IllegalStateException("File Job 행 갱신 lease/fencing/CAS 검증에 실패했습니다.");
    }
    public void resolveUnknownJob(String jobId,long success,long failed,AdmFileJobState next,String errorCode,String errorMessage,
                                  String operator,String reason,String approvalId){
        int updated=jdbc.update("""
            UPDATE adm_file_job SET job_state=?,success_rows=?,failed_rows=?,error_code=?,error_message=?,
                   approval_id=?,resolved_by=?,control_by=?,control_reason=?,control_updated_at=?,lease_owner=NULL,lease_until=NULL,updated_at=?
             WHERE job_id=? AND job_state=?
            """,next.name(),success,failed,errorCode,errorMessage,approvalId,operator,operator,reason,ts(Instant.now()),
                ts(Instant.now()),jobId,AdmFileJobState.UNKNOWN_RESULT.name());
        if(updated!=1)throw new IllegalStateException("결과 불명 Job 상태가 변경되었습니다.");
    }
    public void addRow(String jobId,long rowNumber,String status,String businessKey,Map<String,String> payload,
                       String errorCode,String message,String rollbackToken){
        jdbc.update("""
            INSERT INTO adm_file_job_row
            (job_id,row_no,row_state,business_key,payload_json,error_code,error_message,rollback_token,created_at,updated_at)
            VALUES (?,?,?,?,?,?,?,?,?,?)
            """,jobId,rowNumber,status,businessKey,payloadProtector.protect(json(payload)),errorCode,message,rollbackToken,
                ts(Instant.now()),ts(Instant.now()));
    }
    public List<Row> rows(String jobId){
        return jdbc.query("""
            SELECT job_id,row_no,row_state,business_key,payload_json,error_code,error_message,rollback_token
              FROM adm_file_job_row WHERE job_id=? ORDER BY row_no
            """,(rs,row)->new Row(rs.getString(1),rs.getLong(2),rs.getString(3),rs.getString(4),
                map(payloadProtector.unprotect(rs.getString(5))),rs.getString(6),rs.getString(7),rs.getString(8)),jobId);
    }
    public void resolveUnknownRow(String jobId,long rowNumber,String expectedState,String nextState,String businessKey,
                                  String errorCode,String message,String rollbackToken){
        int updated=jdbc.update("""
            UPDATE adm_file_job_row SET row_state=?,business_key=?,error_code=?,error_message=?,rollback_token=?,updated_at=?
             WHERE job_id=? AND row_no=? AND row_state=?
               AND EXISTS (SELECT 1 FROM adm_file_job j WHERE j.job_id=? AND j.job_state=?)
            """,nextState,businessKey,errorCode,message,rollbackToken,ts(Instant.now()),jobId,rowNumber,expectedState,
                jobId,AdmFileJobState.UNKNOWN_RESULT.name());
        if(updated!=1)throw new IllegalStateException("결과 불명 행 상태가 이미 변경되었습니다.");
    }
    public List<Job> expired(int limit){
        int bounded=Math.max(1,Math.min(limit,500));
        return jdbc.query(con->{PreparedStatement ps=con.prepareStatement("""
            SELECT * FROM adm_file_job WHERE retention_until<?
             AND (job_state IN (?,?,?,?,?)
                  OR (job_state=? AND (source_path IS NOT NULL OR result_path IS NOT NULL)))
             ORDER BY retention_until
            """);
            ps.setTimestamp(1,ts(Instant.now()));
            ps.setString(2,AdmFileJobState.COMPLETED.name());ps.setString(3,AdmFileJobState.PARTIAL_FAILED.name());
            ps.setString(4,AdmFileJobState.FAILED.name());ps.setString(5,AdmFileJobState.CANCELLED.name());
            ps.setString(6,AdmFileJobState.ROLLED_BACK.name());ps.setString(7,AdmFileJobState.EXPIRED.name());
            ps.setMaxRows(bounded);return ps;},(rs,row)->mapJob(rs));
    }
    public boolean beginExpiry(String jobId){
        return jdbc.update("""
            UPDATE adm_file_job SET job_state=?,updated_at=?
             WHERE job_id=? AND retention_until<?
               AND job_state IN (?,?,?,?,?,?)
            """,AdmFileJobState.EXPIRED.name(),ts(Instant.now()),jobId,ts(Instant.now()),
                AdmFileJobState.COMPLETED.name(),AdmFileJobState.PARTIAL_FAILED.name(),AdmFileJobState.FAILED.name(),
                AdmFileJobState.CANCELLED.name(),AdmFileJobState.ROLLED_BACK.name(),AdmFileJobState.EXPIRED.name())==1;
    }
    public void finalizeExpiry(String jobId){
        int updated=jdbc.update("""
            UPDATE adm_file_job SET source_path=NULL,result_path=NULL,updated_at=?
             WHERE job_id=? AND job_state=?
            """,ts(Instant.now()),jobId,AdmFileJobState.EXPIRED.name());
        if(updated!=1)throw new IllegalStateException("만료 Job 상태가 변경되었습니다.");
        jdbc.update("DELETE FROM adm_file_job_row WHERE job_id=?",jobId);
    }

    private Job mapJob(ResultSet rs)throws SQLException{
        return new Job(rs.getString("job_id"),rs.getString("operation_id"),rs.getString("request_hash"),
                AdmFileJobType.valueOf(rs.getString("job_type")),rs.getString("template_code"),
                rs.getInt("template_version"),rs.getString("file_format"),
                AdmFileJobState.valueOf(rs.getString("job_state")),"Y".equals(rs.getString("dry_run")),
                "Y".equals(rs.getString("rollback_supported")),rs.getString("source_path"),rs.getString("result_path"),
                rs.getString("source_sha256"),rs.getString("result_sha256"),rs.getLong("total_rows"),
                rs.getLong("success_rows"),rs.getLong("failed_rows"),rs.getString("lease_owner"),
                rs.getLong("fencing_token"),instant(rs.getTimestamp("lease_until")),instant(rs.getTimestamp("retention_until")),
                rs.getString("requested_by"),rs.getString("reason"),rs.getString("client_ip"),
                rs.getString("error_code"),rs.getString("error_message"),rs.getString("approval_id"),
                rs.getString("applied_by"),rs.getString("resolved_by"),rs.getString("control_by"),rs.getString("control_reason"),
                instant(rs.getTimestamp("control_updated_at")),instant(rs.getTimestamp("created_at")),
                instant(rs.getTimestamp("updated_at")));
    }
    private String json(Map<String,String> value){try{return objectMapper.writeValueAsString(value);}catch(Exception e){throw new IllegalStateException(e);}}
    private Map<String,String> map(String value){try{return objectMapper.readValue(value,new TypeReference<>(){});}catch(Exception e){throw new IllegalStateException(e);}}
    private static String yn(boolean value) {
        return value ? "Y" : "N";
    }

    private static Timestamp ts(Instant instant) {
        return instant == null ? null : Timestamp.from(instant);
    }

    private static Instant instant(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }

    enum ControlActor {
        NONE,
        APPLIED_BY,
        RESOLVED_BY
    }
    record Row(String jobId,long rowNumber,String state,String businessKey,Map<String,String> payload,
               String errorCode,String message,String rollbackToken){}
    record Job(String jobId,String operationId,String requestHash,AdmFileJobType jobType,String templateCode,
                      int templateVersion,String format,AdmFileJobState state,boolean dryRun,boolean rollbackSupported,
                      String sourcePath,String resultPath,String sourceSha256,String resultSha256,long totalRows,
                      long successRows,long failedRows,String leaseOwner,long fencingToken,Instant leaseUntil,
                      Instant retentionUntil,String requestedBy,String reason,String clientIp,String errorCode,
                      String errorMessage,String approvalId,String appliedBy,String resolvedBy,String controlBy,String controlReason,
                      Instant controlUpdatedAt,Instant createdAt,Instant updatedAt){
        AdmFileJobResponse response(){return new AdmFileJobResponse(jobId,operationId,requestHash,jobType,templateCode,templateVersion,
                format,state,dryRun,rollbackSupported,totalRows,successRows,failedRows,sourceSha256,resultSha256,
                requestedBy,reason,approvalId,appliedBy,resolvedBy,controlBy,controlReason,controlUpdatedAt,errorCode,errorMessage,
                retentionUntil,createdAt,updatedAt);}
    }
}
