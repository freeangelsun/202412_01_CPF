package com.cpf.batch.operation;

import com.cpf.core.api.batch.CpfCenterCutOperationsExtension;
import com.cpf.core.api.batch.CpfCenterCutOperationsPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** BAT-owned Center-Cut 운영 조회 구현. 업무별 저장소는 Extension SPI에 위임합니다. */
@Service
public class BatCenterCutOperationsFacade implements CpfCenterCutOperationsPort {
    private static final Logger log = LoggerFactory.getLogger(BatCenterCutOperationsFacade.class);
    private static final int DEFAULT_LIMIT=100, MAX_LIMIT=500;
    private final JdbcTemplate jdbc;
    private final List<CpfCenterCutOperationsExtension> extensions;

    public BatCenterCutOperationsFacade(@Qualifier("batJdbcTemplate") JdbcTemplate jdbc,
                                        ObjectProvider<CpfCenterCutOperationsExtension> extensions) {
        this.jdbc=jdbc; this.extensions=extensions.orderedStream().toList();
    }
    public List<Map<String,Object>> findJobs(){return query("""
        SELECT c.center_cut_job_id AS centerCutJobId,c.batch_job_id AS batchJobId,
               c.center_cut_job_name AS centerCutJobName,c.provider_key AS providerKey,c.handler_key AS handlerKey,
               c.chunk_size AS chunkSize,c.retry_limit AS retryLimit,c.use_yn AS useYn,c.description AS description,
               c.created_at AS createdAt,c.updated_at AS updatedAt,j.job_name AS batchJobName,j.job_type AS batchJobType
          FROM bat_center_cut_job c LEFT JOIN bat_job j ON j.job_id=c.batch_job_id ORDER BY c.center_cut_job_id
        """);}
    public Map<String,Object> findJobDetail(String id){String v=req(id,"centerCutJobId");Map<String,Object> r=new LinkedHashMap<>();r.put("job",one("""
        SELECT c.center_cut_job_id AS centerCutJobId,c.batch_job_id AS batchJobId,c.center_cut_job_name AS centerCutJobName,
               c.provider_key AS providerKey,c.handler_key AS handlerKey,c.chunk_size AS chunkSize,c.retry_limit AS retryLimit,
               c.use_yn AS useYn,c.description AS description,c.created_at AS createdAt,c.updated_at AS updatedAt,
               j.job_name AS batchJobName,j.job_type AS batchJobType
          FROM bat_center_cut_job c LEFT JOIN bat_job j ON j.job_id=c.batch_job_id WHERE c.center_cut_job_id=?
        """,v));r.put("parameters",findParameters(v));r.put("summary",findSummary(v));r.put("targets",findTargets(v,null,DEFAULT_LIMIT));r.put("results",findResults(v,null,DEFAULT_LIMIT));return r;}
    public List<Map<String,Object>> findParameters(String id){return query("""
        SELECT parameter_id AS parameterId,center_cut_job_id AS centerCutJobId,parameter_key AS parameterKey,
               CASE WHEN encrypted_yn='Y' THEN '[MASKED]' ELSE parameter_value END AS parameterValue,
               encrypted_yn AS encryptedYn,use_yn AS useYn,created_at AS createdAt,updated_at AS updatedAt
          FROM bat_center_cut_parameter WHERE center_cut_job_id=? ORDER BY parameter_key
        """,req(id,"centerCutJobId"));}
    public Map<String,Object> findSummary(String id){String v=req(id,"centerCutJobId");CpfCenterCutOperationsExtension e=extension(v);if(e!=null)return e.findSummary(v);Map<String,Object> r=new LinkedHashMap<>();r.put("centerCutJobId",v);r.put("adapterType","BAT_STANDARD");r.putAll(one("""
        SELECT COUNT(*) AS totalCount,SUM(CASE WHEN item_status='READY' THEN 1 ELSE 0 END) AS readyCount,
               SUM(CASE WHEN item_status='RUNNING' THEN 1 ELSE 0 END) AS runningCount,SUM(CASE WHEN item_status='SUCCESS' THEN 1 ELSE 0 END) AS successCount,
               SUM(CASE WHEN item_status='FAILED' THEN 1 ELSE 0 END) AS failedCount,SUM(CASE WHEN item_status='SKIPPED' THEN 1 ELSE 0 END) AS skippedCount,
               SUM(CASE WHEN item_status='RETRY_REQUESTED' THEN 1 ELSE 0 END) AS retryRequestedCount,
               SUM(CASE WHEN item_status='STOP_REQUESTED' THEN 1 ELSE 0 END) AS stopRequestedCount,
               MAX(started_at) AS lastStartedAt,MAX(completed_at) AS lastCompletedAt
          FROM bat_center_cut_item WHERE center_cut_job_id=?
        """,v));Map<String,Object> rr=one("""
        SELECT COUNT(*) AS totalCount,SUM(CASE WHEN result_status='SUCCESS' THEN 1 ELSE 0 END) AS successCount,
               SUM(CASE WHEN result_status='FAILED' THEN 1 ELSE 0 END) AS failedCount,MAX(created_at) AS lastCreatedAt
          FROM bat_center_cut_result WHERE center_cut_job_id=?
        """,v);rr.forEach((k,val)->r.put("result"+Character.toUpperCase(k.charAt(0))+k.substring(1),val));return r;}
    public List<Map<String,Object>> findTargets(String id,String status,int limit){String v=req(id,"centerCutJobId");CpfCenterCutOperationsExtension e=extension(v);if(e!=null)return e.findTargets(v,status,safe(limit));List<Object>a=new ArrayList<>();a.add(v);String where="";if(text(status)){where=" AND item_status=?";a.add(status.trim());}a.add(safe(limit));return query("""
        SELECT center_cut_item_id AS targetId,center_cut_job_id AS centerCutJobId,business_key AS businessKey,business_date AS businessDate,
               item_status AS statusCode,retry_count AS retryCount,transaction_id AS transactionId,parent_segment_id AS parentSegmentId,
               transaction_segment_id AS transactionSegmentId,started_at AS startedAt,completed_at AS completedAt,last_error_message AS lastErrorMessage,
               CASE WHEN item_payload IS NULL THEN NULL ELSE CONCAT('[MASKED target payload length=',CHAR_LENGTH(item_payload),']') END AS targetPayloadMasked,
               CHAR_LENGTH(item_payload) AS targetPayloadLength,created_at AS createdAt,updated_at AS updatedAt
          FROM bat_center_cut_item WHERE center_cut_job_id=?
        """+where+" ORDER BY center_cut_item_id LIMIT ?",a.toArray());}
    public List<Map<String,Object>> findResults(String id,String status,int limit){String v=req(id,"centerCutJobId");CpfCenterCutOperationsExtension e=extension(v);if(e!=null)return e.findResults(v,status,safe(limit));List<Object>a=new ArrayList<>();a.add(v);String where="";if(text(status)){where=" AND r.result_status=?";a.add(status.trim());}a.add(safe(limit));return query("""
        SELECT r.center_cut_result_id AS resultId,r.center_cut_item_id AS targetId,r.center_cut_job_id AS centerCutJobId,i.business_key AS businessKey,
               r.result_status AS resultStatus,r.result_message AS resultMessage,COALESCE(r.transaction_id,i.transaction_id) AS transactionId,COALESCE(r.parent_segment_id,i.parent_segment_id) AS parentSegmentId,
               COALESCE(r.transaction_segment_id,i.transaction_segment_id) AS transactionSegmentId,CASE WHEN r.result_payload IS NULL THEN NULL ELSE CONCAT('[MASKED result payload length=',CHAR_LENGTH(r.result_payload),']') END AS resultPayloadMasked,
               CHAR_LENGTH(r.result_payload) AS resultPayloadLength,r.created_at AS createdAt,r.updated_at AS updatedAt
          FROM bat_center_cut_result r LEFT JOIN bat_center_cut_item i ON i.center_cut_item_id=r.center_cut_item_id WHERE r.center_cut_job_id=?
        """+where+" ORDER BY r.center_cut_result_id DESC LIMIT ?",a.toArray());}
    public Map<String,Object> findResultDetail(String resultId){for(CpfCenterCutOperationsExtension e:extensions){Map<String,Object> r=e.findResultDetail(resultId);if(r!=null&&!r.isEmpty())return r;}Long id=parse(resultId);if(id==null)return Map.of();return one("""
        SELECT r.center_cut_result_id AS resultId,r.center_cut_item_id AS targetId,r.center_cut_job_id AS centerCutJobId,i.business_key AS businessKey,
               r.result_status AS resultStatus,r.result_message AS resultMessage,COALESCE(r.transaction_id,i.transaction_id) AS transactionId,COALESCE(r.parent_segment_id,i.parent_segment_id) AS parentSegmentId,
               COALESCE(r.transaction_segment_id,i.transaction_segment_id) AS transactionSegmentId,CASE WHEN r.result_payload IS NULL THEN NULL ELSE CONCAT('[MASKED result payload length=',CHAR_LENGTH(r.result_payload),']') END AS resultPayloadMasked,
               CHAR_LENGTH(r.result_payload) AS resultPayloadLength,r.created_at AS createdAt,r.updated_at AS updatedAt
          FROM bat_center_cut_result r LEFT JOIN bat_center_cut_item i ON i.center_cut_item_id=r.center_cut_item_id WHERE r.center_cut_result_id=?
        """,id);}
    private CpfCenterCutOperationsExtension extension(String id){return extensions.stream().filter(e->e.supports(id)).findFirst().orElse(null);}
    private List<Map<String,Object>> query(String sql,Object...args){try{return jdbc.queryForList(sql,args);}catch(DataAccessException e){log.error("BAT Center-Cut query failed",e);throw new IllegalStateException("BAT Center-Cut query failed",e);}}
    private Map<String,Object> one(String sql,Object...args){List<Map<String,Object>> r=query(sql,args);return r.isEmpty()?new LinkedHashMap<>():new LinkedHashMap<>(r.get(0));}
    private static String req(String v,String n){if(v==null||v.isBlank())throw new IllegalArgumentException(n+" is required");return v.trim();}
    private static boolean text(String v){return v!=null&&!v.isBlank();}
    private static int safe(int n){return Math.min(n<=0?DEFAULT_LIMIT:n,MAX_LIMIT);}
    private static Long parse(String s){try{return s==null?null:Long.valueOf(s);}catch(NumberFormatException e){return null;}}
}
