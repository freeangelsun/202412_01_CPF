package com.cpf.reference.edu.counterparty.persistence;

import com.cpf.reference.edu.counterparty.model.ReferenceCounterpartyExchange;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

/** Oracle/PostgreSQL/MariaDB-neutral JDBC adapter for the REF DB counterparty ledger. */
public final class JdbcReferenceCounterpartyStore implements ReferenceCounterpartyStore {
    private final JdbcTemplate jdbc; private final ObjectMapper json;
    public JdbcReferenceCounterpartyStore(JdbcTemplate jdbc,ObjectMapper json){this.jdbc=Objects.requireNonNull(jdbc);this.json=Objects.requireNonNull(json);}
    @Override public Optional<ReferenceCounterpartyExchange> find(String requirementId,String idempotencyKey){
        var rows=jdbc.query("select COUNTERPARTY_REQUEST_ID,REQUIREMENT_ID,IDEMPOTENCY_KEY,REQUEST_HASH,BUSINESS_KEY,FAMILY_CODE,SCENARIO_CODE,STATE,RESPONSE_STATUS,RESPONSE_JSON,ATTEMPT_COUNT,TRACE_ID,CREATED_AT,UPDATED_AT,COMPLETED_AT from CPF_EDU_COUNTERPARTY_REQUEST where REQUIREMENT_ID=? and IDEMPOTENCY_KEY=?",
            (rs,n)->new ReferenceCounterpartyExchange(rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(5),rs.getString(6),rs.getString(7),rs.getString(8),rs.getInt(9),read(rs.getString(10)),rs.getInt(11),rs.getString(12),instant(rs.getTimestamp(13)),instant(rs.getTimestamp(14)),instant(rs.getTimestamp(15))),requirementId,idempotencyKey);
        return rows.stream().findFirst();
    }
    @Override public boolean insert(ReferenceCounterpartyExchange e){
        try{int n=jdbc.update("insert into CPF_EDU_COUNTERPARTY_REQUEST (COUNTERPARTY_REQUEST_ID,REQUIREMENT_ID,IDEMPOTENCY_KEY,REQUEST_HASH,BUSINESS_KEY,FAMILY_CODE,SCENARIO_CODE,STATE,RESPONSE_STATUS,RESPONSE_JSON,ATTEMPT_COUNT,TRACE_ID,CREATED_AT,UPDATED_AT,COMPLETED_AT) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",e.requestId(),e.requirementId(),e.idempotencyKey(),e.requestHash(),e.businessKey(),e.familyCode(),e.scenarioCode(),e.state(),e.responseStatus(),write(e.response()),e.attemptCount(),e.traceId(),ts(e.createdAt()),ts(e.updatedAt()),ts(e.completedAt()));return n==1;}catch(DuplicateKeyException duplicate){return false;}
    }
    @Override public void update(ReferenceCounterpartyExchange e){
        int n=jdbc.update("update CPF_EDU_COUNTERPARTY_REQUEST set STATE=?,RESPONSE_STATUS=?,RESPONSE_JSON=?,ATTEMPT_COUNT=?,TRACE_ID=?,UPDATED_AT=?,COMPLETED_AT=? where COUNTERPARTY_REQUEST_ID=? and REQUEST_HASH=?",e.state(),e.responseStatus(),write(e.response()),e.attemptCount(),e.traceId(),ts(e.updatedAt()),ts(e.completedAt()),e.requestId(),e.requestHash());
        if(n!=1)throw new IllegalStateException("counterparty exchange update conflict: "+e.requestId());
    }
    private String write(Object v){try{return json.writeValueAsString(v);}catch(Exception x){throw new IllegalArgumentException("counterparty response serialization failed",x);}}
    @SuppressWarnings("unchecked") private Map<String,Object> read(String v){try{return v==null||v.isBlank()?Map.of():json.readValue(v,Map.class);}catch(Exception x){return Map.of("unreadable",true);}}
    private static Timestamp ts(Instant v){return v==null?null:Timestamp.from(v);}
    private static Instant instant(Timestamp v){return v==null?null:v.toInstant();}
}
