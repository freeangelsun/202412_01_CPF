package com.cpf.batch.control.job;

import com.cpf.batch.api.BatchJobDefinition;
import com.cpf.batch.api.BatchJobDefinitionControlPort;
import com.cpf.batch.api.BatchParameterDefinition;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.*;

/** Versioned Batch Job Definition의 BAT Owner 서비스입니다. */
@Service
public class BatchJobDefinitionService implements BatchJobDefinitionControlPort {
    private final JdbcTemplate jdbc; private final ObjectMapper mapper;
    public BatchJobDefinitionService(JdbcTemplate jdbc,ObjectMapper mapper){this.jdbc=jdbc;this.mapper=mapper;}

    @Transactional(readOnly=true)
    public List<Map<String,Object>> list(String jobId,String state,int limit){
        StringBuilder sql=new StringBuilder("SELECT job_id,definition_version,job_name,executor_type,definition_state,owner_domain,trigger_type,trigger_expression,timezone_id,agent_pool,max_concurrency,restartable_yn,unknown_result_policy,checksum,row_version,effective_from,effective_until,updated_at FROM bat_job_definition_version WHERE 1=1");
        List<Object> args=new ArrayList<>();if(text(jobId)){sql.append(" AND job_id LIKE ?");args.add("%"+jobId.trim()+"%");}if(text(state)){sql.append(" AND definition_state=?");args.add(state.trim().toUpperCase(Locale.ROOT));}sql.append(" ORDER BY job_id,definition_version DESC");
        return jdbc.query(c->{var ps=c.prepareStatement(sql.toString());ps.setMaxRows(Math.max(1,Math.min(limit,1000)));for(int i=0;i<args.size();i++)ps.setObject(i+1,args.get(i));return ps;},(rs,n)->{Map<String,Object> r=new LinkedHashMap<>();var md=rs.getMetaData();for(int i=1;i<=md.getColumnCount();i++)r.put(md.getColumnLabel(i),rs.getObject(i));return r;});
    }

    @Override
    @Transactional(readOnly = true)
    public DefinitionState state(String jobId, long definitionVersion) {
        List<Map<String, Object>> rows = jdbc.queryForList("""
                SELECT job_id,definition_version,definition_state,row_version,checksum,created_by
                  FROM bat_job_definition_version
                 WHERE job_id=? AND definition_version=?
                """, required(jobId, "jobId"), definitionVersion);
        if (rows.isEmpty()) {
            throw new NoSuchElementException("Batch Job Definition not found: " + jobId + "@" + definitionVersion);
        }
        Map<String, Object> row = rows.getFirst();
        return new DefinitionState(
                Objects.toString(rowValue(row, "job_id", "JOB_ID"), ""),
                ((Number) Objects.requireNonNull(rowValue(row, "definition_version", "DEFINITION_VERSION"))).longValue(),
                Objects.toString(rowValue(row, "definition_state", "DEFINITION_STATE"), ""),
                ((Number) Objects.requireNonNull(rowValue(row, "row_version", "ROW_VERSION"))).longValue(),
                Objects.toString(rowValue(row, "checksum", "CHECKSUM"), ""),
                Objects.toString(rowValue(row, "created_by", "CREATED_BY"), ""));
    }

    @Override
    @Transactional
    public PublishResult publishApproved(PublishCommand command) {
        Objects.requireNonNull(command, "command");
        if (command.approvalRequestId() <= 0) {
            throw new IllegalArgumentException("approvalRequestId must be positive");
        }
        DefinitionState current = state(command.jobId(), command.definitionVersion());
        if (!current.checksum().equalsIgnoreCase(required(command.payloadHash(), "payloadHash"))) {
            throw new IllegalStateException("Approved payload hash does not match Batch Definition checksum");
        }
        if ("PUBLISHED".equals(current.state())) {
            Integer delivered = jdbc.queryForObject("""
                    SELECT COUNT(*) FROM bat_job_definition_audit
                     WHERE job_id=? AND definition_version=? AND to_state='PUBLISHED'
                       AND approval_request_id=?
                    """, Integer.class, command.jobId(), command.definitionVersion(),
                    Long.toString(command.approvalRequestId()));
            if (delivered == null || delivered == 0) {
                throw new IllegalStateException("Definition is published by a different approval request");
            }
            return new PublishResult(current.jobId(), current.definitionVersion(), current.state(),
                    current.rowVersion(), current.checksum(), command.operationId());
        }
        if (!"APPROVAL".equals(current.state())) {
            throw new IllegalStateException("Approved publish requires APPROVAL state: " + current.state());
        }
        SavedDefinition saved = transition(
                command.jobId(), command.definitionVersion(), command.expectedRowVersion(),
                "PUBLISHED", required(command.approvedBy(), "approvedBy"),
                required(command.reason(), "reason"),
                new AuditContext(required(command.requestedBy(), "requestedBy"),
                        Long.toString(command.approvalRequestId()), null, null));
        DefinitionState published = state(command.jobId(), command.definitionVersion());
        return new PublishResult(published.jobId(), published.definitionVersion(), saved.state(),
                saved.rowVersion(), published.checksum(), command.operationId());
    }

    public ValidationResult validate(BatchJobDefinition definition){
        List<String> errors=new ArrayList<>();List<String>warnings=new ArrayList<>();
        if(definition.state()== BatchJobDefinition.State.PUBLISHED && !text(definition.checksum()))errors.add("Published definition requires checksum");
        if(definition.trigger().type()== BatchJobDefinition.TriggerType.CRON && !definition.trigger().expression().matches("([^\\s]+\\s+){4,6}[^\\s]+"))errors.add("Cron expression format invalid");
        if(definition.trigger().type()== BatchJobDefinition.TriggerType.CRON && definition.trigger().expression().length()>100)errors.add("Cron expression exceeds bat_schedule limit (100)");
        if(definition.state()== BatchJobDefinition.State.PUBLISHED && definition.effectiveFrom()!=null && definition.effectiveFrom().isAfter(OffsetDateTime.now()))warnings.add("Published definition is not effective yet");
        if(definition.resourcePolicy().maxConcurrency()>1000)warnings.add("High concurrency requires capacity approval");
        if(definition.recoveryPolicy().maxAttempts()>1 && definition.recoveryPolicy().unknownResultPolicy()== BatchJobDefinition.UnknownResultPolicy.FAIL_CLOSED)warnings.add("Retry with FAIL_CLOSED unknown-result policy may require manual reconciliation");
        for(BatchParameterDefinition p:definition.parameters())if(p.sensitive() && !"SECRET_REFERENCE".equals(p.type()))errors.add("Sensitive parameter must use SECRET_REFERENCE: "+p.name());
        return new ValidationResult(errors.isEmpty(),List.copyOf(errors),List.copyOf(warnings),preview(definition));
    }

    @Transactional
    public SavedDefinition saveDraft(BatchJobDefinition definition, String verifiedOperatorId){
        BatchJobDefinition d = withServerChecksum(withVerifiedActor(definition, verifiedOperatorId));
        ValidationResult validation=validate(d);if(!validation.valid())throw new IllegalArgumentException(String.join("; ",validation.errors()));
        validateDependencyGraph(d);
        if(d.state()== BatchJobDefinition.State.PUBLISHED||d.state()== BatchJobDefinition.State.RETIRED)throw new IllegalArgumentException("Published/Retired definition is immutable");
        String json=json(d);List<Map<String,Object>> existing=jdbc.queryForList("SELECT definition_state,row_version,definition_json FROM bat_job_definition_version WHERE job_id=? AND definition_version=?",d.jobId(),d.definitionVersion());
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
        audit(
                d.jobId(), d.definitionVersion(), existing.isEmpty()?"DRAFT_CREATE":"DRAFT_UPDATE",
                existing.isEmpty()?null:String.valueOf(existing.getFirst().get("definition_state")),
                d.state().name(), d.reason(), d.requestedBy(),
                new AuditContext(d.requestedBy(), null, null, null),
                existing.isEmpty()?null:Objects.toString(rowValue(existing.getFirst(),"definition_json","DEFINITION_JSON"),null),
                json);
        return new SavedDefinition(d.jobId(),d.definitionVersion(),d.state().name(),next,validation.warnings(),OffsetDateTime.now());
    }

    @Transactional
    public SavedDefinition transition(
            String jobId,
            long version,
            long expectedRowVersion,
            String targetState,
            String operatorId,
            String reason) {
        return transition(jobId, version, expectedRowVersion, targetState, operatorId, reason,
                new AuditContext(null, null, null, null));
    }

    @Transactional
    public SavedDefinition transition(
            String jobId,
            long version,
            long expectedRowVersion,
            String targetState,
            String operatorId,
            String reason,
            AuditContext auditContext) {
        if (!text(reason) || reason.trim().length() < 5) {
            throw new IllegalArgumentException("reason must be at least 5 characters");
        }
        String target = required(targetState, "targetState").toUpperCase(Locale.ROOT);
        String verifiedOperator = required(operatorId, "operatorId");
        AuditContext context = auditContext == null
                ? new AuditContext(null, null, null, null) : auditContext;
        List<Map<String,Object>> rows = jdbc.queryForList("""
                SELECT definition_state,checksum,row_version,job_name,executor_type,description,restartable_yn,
                       trigger_type,trigger_expression,timezone_id,misfire_policy,executor_reference,
                       effective_from,effective_until,created_by,definition_json
                  FROM bat_job_definition_version
                 WHERE job_id=? AND definition_version=?
                """, jobId, version);
        if (rows.isEmpty()) {
            throw new NoSuchElementException("Definition not found");
        }
        Map<String,Object> definitionRow = rows.getFirst();
        String current = Objects.toString(rowValue(definitionRow,"definition_state","DEFINITION_STATE"),"");
        String checksum = Objects.toString(rowValue(definitionRow,"checksum","CHECKSUM"),"");
        long rowVersion = ((Number) Objects.requireNonNull(
                rowValue(definitionRow,"row_version","ROW_VERSION"), "row_version")).longValue();
        if (rowVersion != expectedRowVersion) {
            throw new IllegalStateException("Definition optimistic lock conflict");
        }
        Map<String,Set<String>> allowed = Map.of(
                "DRAFT", Set.of("VALIDATED"),
                "VALIDATED", Set.of("APPROVAL", "DRAFT"),
                "APPROVAL", Set.of("PUBLISHED", "DRAFT"),
                "PUBLISHED", Set.of("RETIRED"),
                "RETIRED", Set.of());
        if (!allowed.getOrDefault(current, Set.of()).contains(target)) {
            throw new IllegalStateException("Invalid state transition: " + current + " -> " + target);
        }
        if ("PUBLISHED".equals(target) && !text(checksum)) {
            throw new IllegalStateException("Checksum required before publish");
        }
        boolean approvalRequired = Set.of("PUBLISHED", "RETIRED").contains(target);
        if (approvalRequired) {
            String maker = required(context.requestedBy(), "verified approval requester");
            required(context.approvalRequestId(), "verified approval request id");
            String creator = Objects.toString(
                    rowValue(definitionRow,"created_by","CREATED_BY"), "").trim();
            if (verifiedOperator.equals(maker) || verifiedOperator.equals(creator)) {
                throw new SecurityException("Maker and checker must be different for " + target);
            }
        }
        String beforeJson = Objects.toString(
                rowValue(definitionRow,"definition_json","DEFINITION_JSON"), null);
        String afterJson = definitionJsonWithState(beforeJson, target, rowVersion + 1);
        int updated = jdbc.update("UPDATE bat_job_definition_version SET definition_state=?,definition_json=?,row_version=row_version+1,updated_by=?,updated_at=CURRENT_TIMESTAMP WHERE job_id=? AND definition_version=? AND row_version=?",
                target, afterJson, verifiedOperator, jobId, version, rowVersion);
        if (updated != 1) {
            throw new IllegalStateException("Definition optimistic lock conflict");
        }
        audit(jobId, version, "STATE_TRANSITION", current, target, reason, verifiedOperator,
                context, beforeJson, afterJson);
        if ("PUBLISHED".equals(target)) {
            persistRuntimeProjection(jobId, version, checksum, definitionRow, afterJson, verifiedOperator);
            projectPublishedDefinition(jobId, version, checksum, definitionRow, verifiedOperator);
        } else if ("RETIRED".equals(target)) {
            retireRuntimeProjection(jobId, version, checksum, afterJson, verifiedOperator);
        }
        return new SavedDefinition(jobId, version, target, rowVersion + 1, List.of(), OffsetDateTime.now());
    }

    private void projectPublishedDefinition(
            String jobId,
            long definitionVersion,
            String checksum,
            Map<String,Object> row,
            String operatorId) {
        String jobName=Objects.toString(rowValue(row,"job_name","JOB_NAME"),jobId);
        String executorType=Objects.toString(rowValue(row,"executor_type","EXECUTOR_TYPE"),"");
        String description=Objects.toString(rowValue(row,"description","DESCRIPTION"),"");
        String restartable=Objects.toString(rowValue(row,"restartable_yn","RESTARTABLE_YN"),"N");
        String executorReference=Objects.toString(rowValue(row,"executor_reference","EXECUTOR_REFERENCE"),"");
        int updated=jdbc.update("""
                UPDATE bat_job
                   SET job_name=?,job_type=?,published_definition_version=?,published_definition_checksum=?,
                       executor_reference=?,definition_published_at=CURRENT_TIMESTAMP,description=?,
                       restartable_yn=?,use_yn='Y',updated_by=?,updated_at=CURRENT_TIMESTAMP
                 WHERE job_id=?
                """,jobName,executorType,definitionVersion,checksum,executorReference,description,restartable,
                operatorId,jobId);
        if(updated==0){
            try{
                jdbc.update("""
                        INSERT INTO bat_job
                        (job_id,job_name,job_type,published_definition_version,published_definition_checksum,
                         executor_reference,definition_published_at,description,restartable_yn,use_yn,
                         created_by,created_at,updated_by,updated_at)
                        VALUES (?,?,?,?,?,?,CURRENT_TIMESTAMP,?,?,'Y',?,CURRENT_TIMESTAMP,?,CURRENT_TIMESTAMP)
                        """,jobId,jobName,executorType,definitionVersion,checksum,executorReference,description,
                        restartable,operatorId,operatorId);
            }catch(DuplicateKeyException duplicate){
                int retried=jdbc.update("""
                        UPDATE bat_job
                           SET job_name=?,job_type=?,published_definition_version=?,published_definition_checksum=?,
                               executor_reference=?,definition_published_at=CURRENT_TIMESTAMP,description=?,
                               restartable_yn=?,use_yn='Y',updated_by=?,updated_at=CURRENT_TIMESTAMP
                         WHERE job_id=?
                        """,jobName,executorType,definitionVersion,checksum,executorReference,description,
                        restartable,operatorId,jobId);
                if(retried!=1)throw new IllegalStateException("Published Job projection conflict: "+jobId,duplicate);
            }
        }

        String triggerType=Objects.toString(rowValue(row,"trigger_type","TRIGGER_TYPE"),"");
        String generatedScheduleId=generatedScheduleId(jobId);
        if("CRON".equals(triggerType)){
            String cron=required(Objects.toString(rowValue(row,"trigger_expression","TRIGGER_EXPRESSION"),""),"triggerExpression");
            if(cron.length()>100)throw new IllegalStateException("Published Cron exceeds bat_schedule limit: "+jobId);
            String timezone=Objects.toString(rowValue(row,"timezone_id","TIMEZONE_ID"),"Asia/Seoul");
            int scheduleUpdated=jdbc.update("""
                    UPDATE bat_schedule
                       SET definition_version=?,definition_checksum=?,cron_expression=?,timezone=?,
                           enabled_yn='Y',updated_by=?,updated_at=CURRENT_TIMESTAMP
                     WHERE schedule_id=?
                    """,definitionVersion,checksum,cron,timezone,operatorId,generatedScheduleId);
            if(scheduleUpdated==0){
                try{
                    jdbc.update("""
                            INSERT INTO bat_schedule
                            (schedule_id,job_id,definition_version,definition_checksum,cron_expression,timezone,
                             enabled_yn,created_by,created_at,updated_by,updated_at)
                            VALUES (?,?,?,?,?,?,'Y',?,CURRENT_TIMESTAMP,?,CURRENT_TIMESTAMP)
                            """,generatedScheduleId,jobId,definitionVersion,checksum,cron,timezone,operatorId,operatorId);
                }catch(DuplicateKeyException duplicate){
                    throw new IllegalStateException("Published Schedule projection conflict: "+generatedScheduleId,duplicate);
                }
            }
        }else{
            jdbc.update("""
                    UPDATE bat_schedule
                       SET enabled_yn='N',updated_by=?,updated_at=CURRENT_TIMESTAMP
                     WHERE schedule_id=?
                    """,operatorId,generatedScheduleId);
        }
    }

    private void persistRuntimeProjection(
            String jobId, long definitionVersion, String checksum, Map<String, Object> row,
            String projectionJson, String operatorId) {
        jdbc.update("""
                UPDATE bat_job_runtime_projection
                   SET projection_status='RETIRED',retired_at=CURRENT_TIMESTAMP,row_version=row_version+1
                 WHERE job_id=? AND definition_version<>? AND projection_status='ACTIVE'
                """, jobId, definitionVersion);
        String executorType = Objects.toString(rowValue(row,"executor_type","EXECUTOR_TYPE"), "");
        String executorReference = Objects.toString(
                rowValue(row,"executor_reference","EXECUTOR_REFERENCE"), "");
        String triggerType = Objects.toString(rowValue(row,"trigger_type","TRIGGER_TYPE"), "");
        String triggerExpression = Objects.toString(
                rowValue(row,"trigger_expression","TRIGGER_EXPRESSION"), null);
        String timezone = Objects.toString(rowValue(row,"timezone_id","TIMEZONE_ID"), "Asia/Seoul");
        Object effectiveFrom = rowValue(row,"effective_from","EFFECTIVE_FROM");
        Object effectiveUntil = rowValue(row,"effective_until","EFFECTIVE_UNTIL");
        int updated = jdbc.update("""
                UPDATE bat_job_runtime_projection
                   SET definition_checksum=?,projection_status='ACTIVE',executor_type=?,executor_reference=?,
                       trigger_type=?,trigger_expression=?,timezone_id=?,projection_json=?,projection_hash=?,
                       effective_from=?,effective_until=?,published_by=?,published_at=CURRENT_TIMESTAMP,
                       retired_at=NULL,row_version=row_version+1
                 WHERE job_id=? AND definition_version=?
                """, checksum, executorType, executorReference, triggerType, triggerExpression, timezone,
                projectionJson, checksum, effectiveFrom, effectiveUntil, operatorId, jobId, definitionVersion);
        if (updated == 0) {
            try {
                jdbc.update("""
                        INSERT INTO bat_job_runtime_projection
                        (job_id,definition_version,definition_checksum,projection_status,executor_type,
                         executor_reference,trigger_type,trigger_expression,timezone_id,projection_json,
                         projection_hash,effective_from,effective_until,published_by,published_at,row_version)
                        VALUES (?,?,?,'ACTIVE',?,?,?,?,?,?,?,?,?,?,CURRENT_TIMESTAMP,1)
                        """, jobId, definitionVersion, checksum, executorType, executorReference,
                        triggerType, triggerExpression, timezone, projectionJson, checksum,
                        effectiveFrom, effectiveUntil, operatorId);
            } catch (DuplicateKeyException concurrent) {
                throw new IllegalStateException("Batch Runtime Projection creation conflict: "
                        + jobId + "@" + definitionVersion, concurrent);
            }
        }
        enqueueProjectionEvent(jobId, definitionVersion, "JOB_DEFINITION_PUBLISHED",
                checksum, projectionJson);
    }

    private void enqueueProjectionEvent(
            String jobId, long definitionVersion, String eventType, String payloadHash, String payload) {
        String outboxId = UUID.randomUUID().toString();
        int inserted = jdbc.update("""
                INSERT INTO bat_job_runtime_projection_outbox
                (outbox_id,job_id,definition_version,event_type,payload_hash,event_payload,
                 delivery_status,fencing_token,attempt_count,next_attempt_at,created_at)
                VALUES (?,?,?,?,?,?,'PENDING',0,0,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)
                """, outboxId, jobId, definitionVersion, eventType, payloadHash, payload);
        if (inserted != 1) {
            throw new IllegalStateException("Batch Runtime Projection Outbox insert failed");
        }
    }

    private void retireRuntimeProjection(
            String jobId, long definitionVersion, String checksum, String projectionJson, String operatorId) {
        int projection = jdbc.update("""
                UPDATE bat_job_runtime_projection
                   SET projection_status='RETIRED',retired_at=CURRENT_TIMESTAMP,row_version=row_version+1
                 WHERE job_id=? AND definition_version=? AND definition_checksum=?
                   AND projection_status='ACTIVE'
                """, jobId, definitionVersion, checksum);
        if (projection != 1) {
            throw new IllegalStateException("Active Batch Runtime Projection not found for retire: "
                    + jobId + "@" + definitionVersion);
        }
        enqueueProjectionEvent(jobId, definitionVersion, "JOB_DEFINITION_RETIRED",
                checksum, projectionJson);
        jdbc.update("""
                UPDATE bat_schedule
                   SET enabled_yn='N',updated_by=?,updated_at=CURRENT_TIMESTAMP
                 WHERE schedule_id=? AND definition_version=?
                """,operatorId,generatedScheduleId(jobId),definitionVersion);
        jdbc.update("""
                UPDATE bat_job
                   SET use_yn='N',updated_by=?,updated_at=CURRENT_TIMESTAMP
                 WHERE job_id=? AND published_definition_version=?
                """,operatorId,jobId,definitionVersion);
    }

    private static String generatedScheduleId(String jobId){
        String scheduleId="DEF_"+jobId;
        if(scheduleId.length()>100)throw new IllegalArgumentException("Generated scheduleId exceeds 100 characters");
        return scheduleId;
    }

    private static BatchJobDefinition withVerifiedActor(BatchJobDefinition d,String operatorId){
        String verified=required(operatorId,"operatorId");
        return new BatchJobDefinition(
                d.jobId(),d.definitionVersion(),d.jobName(),d.executorType(),d.state(),d.ownerDomain(),
                d.description(),d.trigger(),d.parameters(),d.dependencies(),d.resourcePolicy(),
                d.recoveryPolicy(),d.alertPolicy(),d.executorReference(),d.checksum(),verified,d.reason(),
                d.effectiveFrom(),d.effectiveUntil(),d.expectedRowVersion());
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

    private BatchJobDefinition withServerChecksum(BatchJobDefinition d) {
        String checksum = semanticChecksum(d);
        return new BatchJobDefinition(
                d.jobId(), d.definitionVersion(), d.jobName(), d.executorType(), d.state(), d.ownerDomain(),
                d.description(), d.trigger(), d.parameters(), d.dependencies(), d.resourcePolicy(),
                d.recoveryPolicy(), d.alertPolicy(), d.executorReference(), checksum, d.requestedBy(), d.reason(),
                d.effectiveFrom(), d.effectiveUntil(), d.expectedRowVersion());
    }

    private String semanticChecksum(BatchJobDefinition d) {
        LinkedHashMap<String,Object> semantic = new LinkedHashMap<>();
        semantic.put("jobId", d.jobId());
        semantic.put("definitionVersion", d.definitionVersion());
        semantic.put("jobName", d.jobName());
        semantic.put("executorType", d.executorType());
        semantic.put("ownerDomain", d.ownerDomain());
        semantic.put("description", d.description());
        semantic.put("trigger", d.trigger());
        semantic.put("parameters", d.parameters());
        semantic.put("dependencies", d.dependencies());
        semantic.put("resourcePolicy", d.resourcePolicy());
        semantic.put("recoveryPolicy", d.recoveryPolicy());
        semantic.put("alertPolicy", d.alertPolicy());
        semantic.put("executorReference", d.executorReference());
        semantic.put("effectiveFrom", d.effectiveFrom());
        semantic.put("effectiveUntil", d.effectiveUntil());
        try {
            byte[] canonical = mapper.writeValueAsString(semantic).getBytes(StandardCharsets.UTF_8);
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(canonical));
        } catch (JsonProcessingException | NoSuchAlgorithmException failure) {
            throw new IllegalStateException("Definition checksum generation failed", failure);
        }
    }

    private void audit(
            String jobId,
            long definitionVersion,
            String actionCode,
            String fromState,
            String toState,
            String reason,
            String operatorId,
            AuditContext context,
            String beforeJson,
            String afterJson) {
        AuditContext safe = context == null ? new AuditContext(null, null, null, null) : context;
        int inserted = jdbc.update("""
                INSERT INTO bat_job_definition_audit
                (job_id,definition_version,action_code,from_state,to_state,reason,operator_id,
                 requested_by,approval_request_id,transaction_id,trace_id,before_json,after_json,created_at)
                VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,CURRENT_TIMESTAMP)
                """, jobId, definitionVersion, actionCode, fromState, toState, reason, operatorId,
                safe.requestedBy(), safe.approvalRequestId(), safe.transactionId(), safe.traceId(),
                beforeJson, afterJson);
        if (inserted != 1) {
            throw new IllegalStateException("Batch Job Definition audit insert failed");
        }
    }

    private String definitionJsonWithState(String definitionJson, String targetState, long nextRowVersion) {
        try {
            BatchJobDefinition source = mapper.readValue(definitionJson, BatchJobDefinition.class);
            BatchJobDefinition changed = new BatchJobDefinition(
                    source.jobId(), source.definitionVersion(), source.jobName(), source.executorType(),
                    BatchJobDefinition.State.valueOf(targetState), source.ownerDomain(), source.description(),
                    source.trigger(), source.parameters(), source.dependencies(), source.resourcePolicy(),
                    source.recoveryPolicy(), source.alertPolicy(), source.executorReference(), source.checksum(),
                    source.requestedBy(), source.reason(), source.effectiveFrom(), source.effectiveUntil(),
                    nextRowVersion);
            return mapper.writeValueAsString(changed);
        } catch (JsonProcessingException | IllegalArgumentException failure) {
            throw new IllegalStateException("Definition state snapshot failed", failure);
        }
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
    private static String required(String value,String name){if(!text(value))throw new IllegalArgumentException(name+" required");return value.trim();}
    private static String yn(boolean v){return v?"Y":"N";}private static boolean text(String v){return v!=null&&!v.isBlank();}
    public record AuditContext(String requestedBy,String approvalRequestId,String transactionId,String traceId){}
    public record ValidationResult(boolean valid,List<String> errors,List<String>warnings,Map<String,Object> preview){}
    public record SavedDefinition(String jobId,long definitionVersion,String state,long rowVersion,List<String>warnings,OffsetDateTime updatedAt){}
}
