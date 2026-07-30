package com.cpf.batch.control.job;

import com.cpf.batch.api.BatchJobDefinition;
import com.cpf.batch.api.BatchParameterDefinition;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;

/** Versioned Batch Job Definition의 BAT Owner 서비스입니다. */
@Service
public class BatchJobDefinitionService {
    private final JdbcTemplate jdbc; private final ObjectMapper mapper;
    public BatchJobDefinitionService(JdbcTemplate jdbc,ObjectMapper mapper){this.jdbc=jdbc;this.mapper=mapper;}

    @Transactional(readOnly=true)
    public List<Map<String,Object>> list(String jobId,String state,int limit){
        StringBuilder sql=new StringBuilder("SELECT job_id,definition_version,job_name,executor_type,definition_state,owner_domain,trigger_type,trigger_expression,timezone_id,agent_pool,max_concurrency,restartable_yn,unknown_result_policy,checksum,row_version,effective_from,effective_until,updated_at FROM bat_job_definition_version WHERE 1=1");
        List<Object> args=new ArrayList<>();if(text(jobId)){sql.append(" AND job_id LIKE ?");args.add("%"+jobId.trim()+"%");}if(text(state)){sql.append(" AND definition_state=?");args.add(state.trim().toUpperCase(Locale.ROOT));}sql.append(" ORDER BY job_id,definition_version DESC");
        return jdbc.query(c->{var ps=c.prepareStatement(sql.toString());ps.setMaxRows(Math.max(1,Math.min(limit,1000)));for(int i=0;i<args.size();i++)ps.setObject(i+1,args.get(i));return ps;},(rs,n)->{Map<String,Object> r=new LinkedHashMap<>();var md=rs.getMetaData();for(int i=1;i<=md.getColumnCount();i++)r.put(md.getColumnLabel(i),rs.getObject(i));return r;});
    }

    public ValidationResult validate(BatchJobDefinition definition){
        List<String> errors=new ArrayList<>();List<String>warnings=new ArrayList<>();
        if(definition.state()== BatchJobDefinition.State.PUBLISHED && !text(definition.checksum()))errors.add("Published definition requires checksum");
        if(definition.trigger().type()== BatchJobDefinition.TriggerType.CRON && !definition.trigger().expression().matches("([^\\s]+\\s+){4,6}[^\\s]+"))errors.add("Cron expression format invalid");
        if(definition.resourcePolicy().maxConcurrency()>1000)warnings.add("High concurrency requires capacity approval");
        if(definition.recoveryPolicy().maxAttempts()>1 && definition.recoveryPolicy().unknownResultPolicy()== BatchJobDefinition.UnknownResultPolicy.FAIL_CLOSED)warnings.add("Retry with FAIL_CLOSED unknown-result policy may require manual reconciliation");
        for(BatchParameterDefinition p:definition.parameters())if(p.sensitive() && !"SECRET_REFERENCE".equals(p.type()))errors.add("Sensitive parameter must use SECRET_REFERENCE: "+p.name());
        return new ValidationResult(errors.isEmpty(),List.copyOf(errors),List.copyOf(warnings),preview(definition));
    }

    @Transactional
    public SavedDefinition saveDraft(BatchJobDefinition d){
        ValidationResult validation=validate(d);if(!validation.valid())throw new IllegalArgumentException(String.join("; ",validation.errors()));
        validateDependencyGraph(d);
        if(d.state()== BatchJobDefinition.State.PUBLISHED||d.state()== BatchJobDefinition.State.RETIRED)throw new IllegalArgumentException("Published/Retired definition is immutable");
        String json=json(d);List<Map<String,Object>> existing=jdbc.queryForList("SELECT definition_state,row_version FROM bat_job_definition_version WHERE job_id=? AND definition_version=?",d.jobId(),d.definitionVersion());
        long next;
        if(existing.isEmpty()){
            if(d.expectedRowVersion()!=0)throw new IllegalStateException("Definition version does not exist; expectedRowVersion must be 0");
            try{jdbc.update("""
                INSERT INTO bat_job_definition_version(job_id,definition_version,job_name,executor_type,definition_state,owner_domain,description,trigger_type,trigger_expression,timezone_id,misfire_policy,agent_pool,zone_id,max_concurrency,timeout_seconds,restartable_yn,max_attempts,initial_backoff_seconds,backoff_multiplier,max_backoff_seconds,skip_limit,unknown_result_policy,compensation_reference,alert_delay_seconds,sla_seconds,notify_failure_yn,notify_missed_yn,executor_reference,definition_json,checksum,effective_from,effective_until,row_version,created_by,created_at,updated_by,updated_at)
                VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,1,?,CURRENT_TIMESTAMP,?,CURRENT_TIMESTAMP)
                """,values(d,json));}catch(DuplicateKeyException e){throw new IllegalStateException("Concurrent definition creation",e);}next=1;
        }else{
            String state=String.valueOf(existing.getFirst().get("definition_state"));long current=((Number)existing.getFirst().get("row_version")).longValue();
            if(Set.of("PUBLISHED","RETIRED").contains(state))throw new IllegalStateException("Published/Retired definition is immutable");
            if(current!=d.expectedRowVersion())throw new IllegalStateException("Definition optimistic lock conflict");
            int updated=jdbc.update("""
                UPDATE bat_job_definition_version SET job_name=?,executor_type=?,definition_state=?,owner_domain=?,description=?,trigger_type=?,trigger_expression=?,timezone_id=?,misfire_policy=?,agent_pool=?,zone_id=?,max_concurrency=?,timeout_seconds=?,restartable_yn=?,max_attempts=?,initial_backoff_seconds=?,backoff_multiplier=?,max_backoff_seconds=?,skip_limit=?,unknown_result_policy=?,compensation_reference=?,alert_delay_seconds=?,sla_seconds=?,notify_failure_yn=?,notify_missed_yn=?,executor_reference=?,definition_json=?,checksum=?,effective_from=?,effective_until=?,row_version=row_version+1,updated_by=?,updated_at=CURRENT_TIMESTAMP WHERE job_id=? AND definition_version=? AND row_version=?
                """,updateValues(d,json));if(updated!=1)throw new IllegalStateException("Definition optimistic lock conflict");next=current+1;
        }
        replaceChildren(d);
        jdbc.update("INSERT INTO bat_job_definition_audit(job_id,definition_version,action_code,from_state,to_state,reason,operator_id,created_at) VALUES (?,?,?,?,?,?,?,CURRENT_TIMESTAMP)",
                d.jobId(),d.definitionVersion(),existing.isEmpty()?"DRAFT_CREATE":"DRAFT_UPDATE",
                existing.isEmpty()?null:String.valueOf(existing.getFirst().get("definition_state")),d.state().name(),d.reason(),d.requestedBy());
        return new SavedDefinition(d.jobId(),d.definitionVersion(),d.state().name(),next,validation.warnings(),OffsetDateTime.now());
    }

    @Transactional
    public SavedDefinition transition(String jobId,long version,long expectedRowVersion,String targetState,String operatorId,String reason){
        if(!text(reason)||reason.trim().length()<5)throw new IllegalArgumentException("reason must be at least 5 characters");String target=targetState.toUpperCase(Locale.ROOT);
        List<Map<String,Object>> rows=jdbc.queryForList("SELECT definition_state,checksum,row_version FROM bat_job_definition_version WHERE job_id=? AND definition_version=?",jobId,version);if(rows.isEmpty())throw new NoSuchElementException("Definition not found");
        String current=String.valueOf(rows.getFirst().get("definition_state"));String checksum=Objects.toString(rows.getFirst().get("checksum"),"");long rowVersion=((Number)rows.getFirst().get("row_version")).longValue();if(rowVersion!=expectedRowVersion)throw new IllegalStateException("Definition optimistic lock conflict");
        Map<String,Set<String>> allowed=Map.of("DRAFT",Set.of("VALIDATED"),"VALIDATED",Set.of("APPROVAL","DRAFT"),"APPROVAL",Set.of("PUBLISHED","DRAFT"),"PUBLISHED",Set.of("RETIRED"),"RETIRED",Set.of());if(!allowed.getOrDefault(current,Set.of()).contains(target))throw new IllegalStateException("Invalid state transition: "+current+" -> "+target);if("PUBLISHED".equals(target)&&!text(checksum))throw new IllegalStateException("Checksum required before publish");
        int updated=jdbc.update("UPDATE bat_job_definition_version SET definition_state=?,row_version=row_version+1,updated_by=?,updated_at=CURRENT_TIMESTAMP WHERE job_id=? AND definition_version=? AND row_version=?",target,operatorId,jobId,version,rowVersion);if(updated!=1)throw new IllegalStateException("Definition optimistic lock conflict");
        jdbc.update("INSERT INTO bat_job_definition_audit(job_id,definition_version,action_code,from_state,to_state,reason,operator_id,created_at) VALUES (?,?,?,?,?,?,?,CURRENT_TIMESTAMP)",jobId,version,"STATE_TRANSITION",current,target,reason,operatorId);
        return new SavedDefinition(jobId,version,target,rowVersion+1,List.of(),OffsetDateTime.now());
    }

    private void validateDependencyGraph(BatchJobDefinition candidate) {
        List<Map<String,Object>> rows = jdbc.queryForList("""
                SELECT d.job_id, d.related_job_id
                FROM bat_job_dependency d
                JOIN bat_job_definition_version v
                  ON v.job_id=d.job_id AND v.definition_version=d.definition_version
                WHERE v.definition_state IN ('DRAFT','VALIDATED','APPROVAL','PUBLISHED')
                """);
        Map<String,Set<String>> graph = new HashMap<>();
        for (Map<String,Object> row : rows) {
            String from = Objects.toString(rowValue(row,"job_id","JOB_ID"),"");
            String to = Objects.toString(rowValue(row,"related_job_id","RELATED_JOB_ID"),"");
            if (!from.isBlank() && !to.isBlank() && !from.equals(candidate.jobId())) {
                graph.computeIfAbsent(from, ignored -> new LinkedHashSet<>()).add(to);
            }
        }
        graph.put(candidate.jobId(), candidate.dependencies().stream()
                .map(BatchJobDefinition.Dependency::relatedJobId)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new)));
        Set<String> visiting = new HashSet<>();
        Set<String> visited = new HashSet<>();
        for (String node : graph.keySet()) {
            if (hasCycle(node,graph,visiting,visited)) {
                throw new IllegalArgumentException("Batch Job dependency cycle detected: " + node);
            }
        }
    }

    private boolean hasCycle(String node, Map<String,Set<String>> graph, Set<String> visiting, Set<String> visited) {
        if (visited.contains(node)) return false;
        if (!visiting.add(node)) return true;
        for (String next : graph.getOrDefault(node,Set.of())) {
            if (hasCycle(next,graph,visiting,visited)) return true;
        }
        visiting.remove(node);
        visited.add(node);
        return false;
    }

    private Object rowValue(Map<String,Object> row,String lower,String upper) {
        if (row.containsKey(lower)) return row.get(lower);
        if (row.containsKey(upper)) return row.get(upper);
        return row.entrySet().stream().filter(e -> e.getKey().equalsIgnoreCase(lower)).map(Map.Entry::getValue).findFirst().orElse(null);
    }

    private void replaceChildren(BatchJobDefinition d){jdbc.update("DELETE FROM bat_job_parameter_definition WHERE job_id=? AND definition_version=?",d.jobId(),d.definitionVersion());int order=0;for(var p:d.parameters())jdbc.update("INSERT INTO bat_job_parameter_definition(job_id,definition_version,parameter_name,parameter_type,label_text,description_text,required_yn,sensitive_yn,default_value,allowed_values,validation_pattern,min_value,max_value,min_length,max_length,reference_type,alias_required_yn,runtime_override_allowed_yn,sort_order) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",d.jobId(),d.definitionVersion(),p.name(),p.type(),p.label(),p.description(),yn(p.required()),yn(p.sensitive()),p.defaultValue(),String.join(",",p.allowedValues()),p.pattern(),emptyDecimal(p.minValue()),emptyDecimal(p.maxValue()),p.minLength(),p.maxLength(),p.referenceType(),yn(!p.alias().isBlank()),yn(p.overrideAllowed()),order++);
        jdbc.update("DELETE FROM bat_job_dependency WHERE job_id=? AND definition_version=?",d.jobId(),d.definitionVersion());order=0;for(var dep:d.dependencies())jdbc.update("INSERT INTO bat_job_dependency(job_id,definition_version,related_job_id,condition_code,timeout_seconds,required_yn,sort_order) VALUES (?,?,?,?,?,?,?)",d.jobId(),d.definitionVersion(),dep.relatedJobId(),dep.condition(),dep.timeoutSeconds(),yn(dep.required()),order++);}
    private Object[] values(BatchJobDefinition d,String json){return new Object[]{d.jobId(),d.definitionVersion(),d.jobName(),d.executorType().name(),d.state().name(),d.ownerDomain(),d.description(),d.trigger().type().name(),d.trigger().expression(),d.trigger().timezone(),d.trigger().misfirePolicy().name(),d.resourcePolicy().agentPool(),d.resourcePolicy().zone(),d.resourcePolicy().maxConcurrency(),d.resourcePolicy().timeoutSeconds(),yn(d.recoveryPolicy().restartable()),d.recoveryPolicy().maxAttempts(),d.recoveryPolicy().initialBackoffSeconds(),d.recoveryPolicy().multiplier(),d.recoveryPolicy().maxBackoffSeconds(),d.recoveryPolicy().skipLimit(),d.recoveryPolicy().unknownResultPolicy().name(),d.recoveryPolicy().compensationReference(),d.alertPolicy().delayThresholdSeconds(),d.alertPolicy().slaSeconds(),yn(d.alertPolicy().notifyOnFailure()),yn(d.alertPolicy().notifyOnMissed()),d.executorReference(),json,d.checksum(),d.effectiveFrom(),d.effectiveUntil(),d.requestedBy(),d.requestedBy()};}
    private Object[] updateValues(BatchJobDefinition d,String json){return new Object[]{
            d.jobName(),d.executorType().name(),d.state().name(),d.ownerDomain(),d.description(),
            d.trigger().type().name(),d.trigger().expression(),d.trigger().timezone(),d.trigger().misfirePolicy().name(),
            d.resourcePolicy().agentPool(),d.resourcePolicy().zone(),d.resourcePolicy().maxConcurrency(),d.resourcePolicy().timeoutSeconds(),
            yn(d.recoveryPolicy().restartable()),d.recoveryPolicy().maxAttempts(),d.recoveryPolicy().initialBackoffSeconds(),
            d.recoveryPolicy().multiplier(),d.recoveryPolicy().maxBackoffSeconds(),d.recoveryPolicy().skipLimit(),
            d.recoveryPolicy().unknownResultPolicy().name(),d.recoveryPolicy().compensationReference(),
            d.alertPolicy().delayThresholdSeconds(),d.alertPolicy().slaSeconds(),yn(d.alertPolicy().notifyOnFailure()),
            yn(d.alertPolicy().notifyOnMissed()),d.executorReference(),json,d.checksum(),d.effectiveFrom(),d.effectiveUntil(),
            d.requestedBy(),d.jobId(),d.definitionVersion(),d.expectedRowVersion()};}
    private Map<String,Object> preview(BatchJobDefinition d){return Map.of("jobId",d.jobId(),"version",d.definitionVersion(),"executor",d.executorType(),"trigger",d.trigger(),"parameterCount",d.parameters().size(),"dependencyCount",d.dependencies().size(),"agentPool",d.resourcePolicy().agentPool(),"maxConcurrency",d.resourcePolicy().maxConcurrency(),"retryAttempts",d.recoveryPolicy().maxAttempts(),"unknownResultPolicy",d.recoveryPolicy().unknownResultPolicy());}
    private String json(BatchJobDefinition d){try{return mapper.writeValueAsString(d);}catch(JsonProcessingException e){throw new IllegalArgumentException("Definition JSON serialization failed",e);}}
    private static Object emptyDecimal(String value){return value==null||value.isBlank()?null:new java.math.BigDecimal(value);}
    private static String yn(boolean v){return v?"Y":"N";}private static boolean text(String v){return v!=null&&!v.isBlank();}
    public record ValidationResult(boolean valid,List<String> errors,List<String>warnings,Map<String,Object> preview){}
    public record SavedDefinition(String jobId,long definitionVersion,String state,long rowVersion,List<String>warnings,OffsetDateTime updatedAt){}
}
