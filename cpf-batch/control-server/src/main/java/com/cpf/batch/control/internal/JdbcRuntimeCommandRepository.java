package com.cpf.batch.control.internal;

import com.cpf.batch.api.*;
import com.cpf.batch.runtime.SensitiveTextSanitizer;
import com.cpf.core.api.database.CpfVendorSqlCatalog;
import com.cpf.core.api.database.CpfVendorSqlCatalogProvider;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.*;

@Repository
public class JdbcRuntimeCommandRepository {
    private final JdbcTemplate jdbc;
    private final CpfVendorSqlCatalog sql;

    public JdbcRuntimeCommandRepository(JdbcTemplate jdbc, CpfVendorSqlCatalogProvider sqlCatalogProvider) {
        this.jdbc = jdbc;
        this.sql = sqlCatalogProvider.forModule("bat");
    }

    @Transactional
    public Map<String,Object> create(RuntimeCommand c) {
        try {
            jdbc.update(sql.required("runtime-command-insert"),
              c.commandId(),c.idempotencyKey(),c.commandType(),c.targetType(),SensitiveTextSanitizer.sanitize(c.targetSnapshot()),
              c.targetSnapshotHash(),c.expectedVersion(),c.requestedBy(),c.reason(),c.approvalPolicyVersion(),c.approvalRequestId(),c.approvedBy(),
              c.executionState().name(),c.executionAttempt(),Timestamp.from(c.requestedAt()),c.expiresAt()==null?null:Timestamp.from(c.expiresAt()),
              SensitiveTextSanitizer.sanitize(c.result()),c.failureStage(),SensitiveTextSanitizer.sanitize(c.beforeState()),
              SensitiveTextSanitizer.sanitize(c.afterState()),c.transactionId(),c.evidenceRef());
        } catch(DuplicateKeyException duplicate){return find(c.idempotencyKey()).orElseThrow();}
        return find(c.idempotencyKey()).orElseThrow();
    }

    public Optional<Map<String,Object>> find(String idempotencyKey){
        return jdbc.queryForList(
                sql.required("runtime-command-find"),idempotencyKey).stream().findFirst();
    }

    /** Only one caller may move an approved/requested command into EXECUTING. */
    public boolean beginExecution(String commandId){
        return jdbc.update(sql.required("runtime-command-begin"),commandId)==1;
    }

    public void transition(String commandId,CommandState state,String failureStage,String result){
        jdbc.update(sql.required("runtime-command-transition"),
                state.name(),failureStage,SensitiveTextSanitizer.sanitize(result),commandId);
    }

    public void recordAttempt(String commandId,int attempt,String instanceId,String stage,CommandState state,String message){
        jdbc.update(sql.required("runtime-command-attempt-insert"),
                commandId,attempt,instanceId,stage,state.name(),SensitiveTextSanitizer.sanitize(message));
    }
}
