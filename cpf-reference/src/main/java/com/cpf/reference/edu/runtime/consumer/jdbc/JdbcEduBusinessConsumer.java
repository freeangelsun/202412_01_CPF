package com.cpf.reference.edu.runtime.consumer.jdbc;
import com.cpf.reference.edu.runtime.application.*;
import com.cpf.reference.edu.runtime.consumer.*;
import com.cpf.reference.edu.runtime.model.EduExecutionCommand;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
/** Real JDBC consumer backed by the central three-vendor CPF_EDU_BUSINESS_RECORD contract. */
public final class JdbcEduBusinessConsumer implements EduBusinessConsumer {
    private final JdbcTemplate jdbc; private final ObjectMapper json;
    public JdbcEduBusinessConsumer(JdbcTemplate jdbc,ObjectMapper json){this.jdbc=Objects.requireNonNull(jdbc);this.json=Objects.requireNonNull(json);}
    @Override public EduConsumerType type(){return EduConsumerType.JDBC_COMMAND;}
    public EduBusinessConsumerResult invoke(EduConsumerBinding b,EduExecutionCommand c,long fence){return execute(b,c,fence,false);}
    public EduBusinessConsumerResult query(EduConsumerBinding b,EduExecutionCommand c,long fence){return execute(b,c,fence,true);}
    private EduBusinessConsumerResult execute(EduConsumerBinding b,EduExecutionCommand c,long fence,boolean forceQuery){
        boolean query=forceQuery||b.type()==EduConsumerType.JDBC_QUERY;
        if(query){
            int limit=Math.max(1,Math.min(1000,parseInt(c.payload().get("pageSize"),100)));
            List<Map<String,Object>> rows=jdbc.query("select BUSINESS_KEY,BUSINESS_STATE,RECORD_VERSION,PAYLOAD_JSON,UPDATED_AT from CPF_EDU_BUSINESS_RECORD where REQUIREMENT_ID=? and DATA_SCOPE=? and (?='*' or BUSINESS_KEY=?) order by UPDATED_AT desc",(rs,n)->Map.of("businessKey",rs.getString(1),"businessState",rs.getString(2),"recordVersion",rs.getLong(3),"payload",fromJson(rs.getString(4)),"updatedAt",rs.getTimestamp(5).toInstant().toString()),b.requirementId(),c.dataScope(),c.businessKey(),c.businessKey());
            if(rows.size()>limit)rows=rows.subList(0,limit);
            return EduBusinessConsumerResult.completed("JDBC_QUERY",Map.of("rows",List.copyOf(rows),"rowCount",rows.size(),"consumer",b.entryPoint()));
        }
        List<Long> versions=jdbc.query("select RECORD_VERSION from CPF_EDU_BUSINESS_RECORD where REQUIREMENT_ID=? and BUSINESS_KEY=? and DATA_SCOPE=?",(rs,n)->rs.getLong(1),b.requirementId(),c.businessKey(),c.dataScope());
        Instant now=Instant.now();String payload=toJson(c.payload());long nextVersion;
        if(versions.isEmpty()){
            if(c.expectedVersion()!=0)throw new EduConflictException("record does not exist for expectedVersion="+c.expectedVersion());
            try{jdbc.update("insert into CPF_EDU_BUSINESS_RECORD (REQUIREMENT_ID,BUSINESS_KEY,DATA_SCOPE,BUSINESS_STATE,RECORD_VERSION,FENCING_TOKEN,PAYLOAD_JSON,CREATED_AT,UPDATED_AT) values (?,?,?,?,?,?,?,?,?)",b.requirementId(),c.businessKey(),c.dataScope(),b.operation(),1L,fence,payload,Timestamp.from(now),Timestamp.from(now));nextVersion=1L;}catch(DuplicateKeyException e){throw new EduConflictException("concurrent create conflict: "+c.businessKey());}
        }else{
            long current=versions.get(0);if(current!=c.expectedVersion())throw new EduConflictException("expectedVersion="+c.expectedVersion()+" actual="+current);
            int n=jdbc.update("update CPF_EDU_BUSINESS_RECORD set BUSINESS_STATE=?,RECORD_VERSION=?,FENCING_TOKEN=?,PAYLOAD_JSON=?,UPDATED_AT=? where REQUIREMENT_ID=? and BUSINESS_KEY=? and DATA_SCOPE=? and RECORD_VERSION=?",b.operation(),current+1,fence,payload,Timestamp.from(now),b.requirementId(),c.businessKey(),c.dataScope(),current);
            if(n!=1)throw new EduConflictException("concurrent update conflict: "+c.businessKey());nextVersion=current+1;
        }
        return EduBusinessConsumerResult.completed("JDBC_COMMAND",Map.of("businessKey",c.businessKey(),"businessState",b.operation(),"recordVersion",nextVersion,"consumer",b.entryPoint()));
    }
    private String toJson(Object v){try{return json.writeValueAsString(v);}catch(JsonProcessingException e){throw new EduValidationException("payload JSON serialization failed");}}
    @SuppressWarnings("unchecked") private Map<String,Object> fromJson(String v){try{return json.readValue(v,Map.class);}catch(Exception e){return Map.of("unreadable",true);}}
    private static int parseInt(Object v,int d){try{return v==null?d:Integer.parseInt(String.valueOf(v));}catch(NumberFormatException e){return d;}}
}
