package com.cpf.admin.opr.service;

import com.cpf.admin.opr.audit.AdmMandatoryAuditContext;
import com.cpf.core.api.logging.CpfTransactionContext;
import com.cpf.core.api.security.CpfSensitiveData;
import com.cpf.core.api.util.CpfStrings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/** ADM 감사 조회 facade. 기록은 durable delivery를 사용하고 DB 오류를 빈 결과로 위장하지 않습니다. */
@Service
public class AdmAuditLogService extends com.cpf.admin.common.base.AdmBaseService {
    private static final Logger log=LoggerFactory.getLogger(AdmAuditLogService.class);
    private final JdbcTemplate jdbc; private final AdmAuditDeliveryService delivery;
    public AdmAuditLogService(@Qualifier("admJdbcTemplate") JdbcTemplate jdbc,AdmAuditDeliveryService delivery){this.jdbc=jdbc;this.delivery=delivery;}

    public List<Map<String,Object>> findAuditLogs(String operatorId,String actionType,String targetType,String targetId,int limit){
        StringBuilder sql=new StringBuilder("""
          SELECT AUDIT_ID,TRANSACTION_ID,TRACE_ID,OPERATOR_ID,MENU_ID,BUTTON_ID,ACTION_TYPE,TARGET_TYPE,TARGET_ID,REASON,BEFORE_DATA,AFTER_DATA,DIFF_DATA,CLIENT_IP,RETENTION_UNTIL,IMMUTABLE_YN,CREATED_AT
          FROM adm_audit_log WHERE 1=1
          """); List<Object> args=new ArrayList<>();
        if(CpfStrings.hasText(operatorId)){sql.append(" AND OPERATOR_ID=?");args.add(operatorId.trim());}
        if(CpfStrings.hasText(actionType)){sql.append(" AND ACTION_TYPE=?");args.add(actionType.trim());}
        if(CpfStrings.hasText(targetType)){sql.append(" AND TARGET_TYPE=?");args.add(targetType.trim());}
        if(CpfStrings.hasText(targetId)){sql.append(" AND TARGET_ID=?");args.add(targetId.trim());}
        sql.append(" ORDER BY AUDIT_ID DESC"); int capped=Math.max(1,Math.min(limit,500));
        try{return jdbc.query(sql.toString(),ps->{for(int i=0;i<args.size();i++)ps.setObject(i+1,args.get(i));ps.setMaxRows(capped);},new ColumnMapRowMapper());}
        catch(DataAccessException ex){throw new IllegalStateException("ADM 감사 로그 조회 실패. 정상 0건과 DB 장애를 구분합니다.",ex);}
    }
    public List<Map<String,Object>> findDeliveries(String state,int limit){return delivery.findDeliveries(state,limit);}
    public Map<String,Object> retryDelivery(long id,String operator,String reason){return delivery.retry(id,operator,requireReason(reason));}

    public <T> T executeAudited(String tx,String operator,String action,String targetType,String targetId,String reason,String before,String ip,Supplier<T> op,Function<T,String> after){
        var c=command(tx,operator,action,targetType,targetId,reason,before,ip); Long mandatory=AdmMandatoryAuditContext.deliveryId();
        if(mandatory==null)return delivery.executeAudited(c,op,after);
        delivery.enrichReservation(mandatory,c);
        try{T r=op.get();delivery.completeOperation(mandatory,"SUCCEEDED",after==null?null:after.apply(r),null);AdmMandatoryAuditContext.markCompleted();return r;}
        catch(RuntimeException ex){delivery.completeOperation(mandatory,"FAILED",null,"OWNER_OPERATION_FAILED: "+ex.getClass().getSimpleName());AdmMandatoryAuditContext.markCompleted();throw ex;}
    }
    public void record(String tx,String operator,String action,String targetType,String targetId,String reason,String ip){record(tx,operator,action,targetType,targetId,reason,null,null,null,ip);}
    public void record(String tx,String operator,String action,String targetType,String targetId,String reason,String before,String after,String diff,String ip){
        Long mandatory=AdmMandatoryAuditContext.deliveryId();
        String resolvedReason=mandatory!=null&&!CpfStrings.hasText(reason)?"ADM mutation mandatory reservation":reason;
        var c=command(tx,operator,action,targetType,targetId,resolvedReason,before,ip);
        if(mandatory==null){delivery.record(c,after,diff);return;}
        try{delivery.enrichReservation(mandatory,c);delivery.completeOperation(mandatory,"SUCCEEDED",after,diff);AdmMandatoryAuditContext.markCompleted();}
        catch(RuntimeException ex){log.error("ADM audit 상세 완료 기록 실패. deliveryId={}, transactionId={}, reason={}",mandatory,CpfTransactionContext.transactionId(),ex.getClass().getSimpleName());throw ex;}
    }
    public String requireReason(String reason){return CpfSensitiveData.sanitizeAuditReason(reason);}
    private AdmAuditDeliveryService.AuditCommand command(String tx,String operator,String action,String targetType,String targetId,String reason,String before,String ip){return new AdmAuditDeliveryService.AuditCommand(CpfStrings.hasText(tx)?tx:CpfTransactionContext.transactionId(),CpfTransactionContext.traceId(),operator,action,targetType,targetId,requireReason(reason),before,ip);}
}
