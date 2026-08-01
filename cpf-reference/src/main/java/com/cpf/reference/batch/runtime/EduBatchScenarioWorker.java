package com.cpf.reference.batch.runtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Durable worker shared by the 30 optional cpf-reference Batch jobs.
 *
 * <p>The worker owns only REF data. It never reads or writes generated-domain tables.
 * Batch execution, checkpoint and target outcome are persisted in the removable
 * {@code CPF_REF_BAT_*} pack, while the final reference business state is stored in
 * {@code CPF_EDU_BUSINESS_RECORD}. The worker never calls EduExecutionService, so a
 * Spring Batch launch cannot recurse into itself.</p>
 */
public final class EduBatchScenarioWorker {
    private final JdbcTemplate jdbc;
    private final ObjectMapper json;

    public EduBatchScenarioWorker(JdbcTemplate jdbc, ObjectMapper json) {
        this.jdbc = Objects.requireNonNull(jdbc);
        this.json = Objects.requireNonNull(json);
    }

    public int execute(String requirementId,
                       String businessKey,
                       String dataScope,
                       String idempotencyKey,
                       long fencingToken,
                       Map<String, Object> payload) {
        String executionKey = stableKey(requirementId + "|" + idempotencyKey);
        String targetId = stableKey(executionKey + "|business-result");
        String jobName = requirementId + ".job";
        String stepName = requirementId + ".step";
        Instant now = Instant.now();
        try {
            String payloadJson = json.writeValueAsString(payload);
            createExecutionIfAbsent(executionKey, requirementId, businessKey, dataScope,
                    jobName, idempotencyKey, fencingToken, payloadJson, now);

            int claimed = jdbc.update(
                    "update CPF_REF_BAT_JOB_EXECUTION set STATE=?,FENCING_TOKEN=?," +
                            "ATTEMPT_COUNT=ATTEMPT_COUNT+1,PAYLOAD_JSON=?,UPDATED_AT=? " +
                            "where JOB_EXECUTION_KEY=? and FENCING_TOKEN<=?",
                    "RUNNING", fencingToken, payloadJson, Timestamp.from(now), executionKey, fencingToken);
            if (claimed != 1) {
                throw new IllegalStateException("Batch fencing token rejected: " + requirementId);
            }

            saveCheckpoint(executionKey, stepName, "main", "RUNNING", 1L,
                    json.writeValueAsString(Map.of("businessKey", businessKey, "fencingToken", fencingToken)), now);

            int businessWrites = writeReferenceBusinessRecord(
                    requirementId, businessKey, dataScope, fencingToken, payloadJson, now);

            Map<String, Object> result = Map.of(
                    "requirementId", requirementId,
                    "businessKey", businessKey,
                    "jobName", jobName,
                    "jobExecutionKey", executionKey,
                    "state", "SUCCEEDED",
                    "written", businessWrites);
            String resultJson = json.writeValueAsString(result);
            saveTargetResult(targetId, executionKey, businessKey, "SUCCEEDED", resultJson, now);
            saveCheckpoint(executionKey, stepName, "main", "COMPLETED", 2L, resultJson, now);
            jdbc.update(
                    "update CPF_REF_BAT_JOB_EXECUTION set STATE=?,RESULT_JSON=?,UPDATED_AT=?,COMPLETED_AT=? " +
                            "where JOB_EXECUTION_KEY=? and FENCING_TOKEN=?",
                    "SUCCEEDED", resultJson, Timestamp.from(now), Timestamp.from(now), executionKey, fencingToken);
            return Math.max(1, businessWrites);
        } catch (Exception e) {
            markFailed(executionKey, fencingToken, e);
            throw new IllegalStateException("REF Batch worker failed: " + requirementId, e);
        }
    }

    private void createExecutionIfAbsent(String executionKey,
                                         String requirementId,
                                         String businessKey,
                                         String dataScope,
                                         String jobName,
                                         String idempotencyKey,
                                         long fencingToken,
                                         String payloadJson,
                                         Instant now) {
        try {
            jdbc.update(
                    "insert into CPF_REF_BAT_JOB_EXECUTION " +
                            "(JOB_EXECUTION_KEY,REQUIREMENT_ID,BUSINESS_KEY,DATA_SCOPE,JOB_NAME,JOB_INSTANCE_KEY," +
                            "IDEMPOTENCY_KEY,STATE,FENCING_TOKEN,ATTEMPT_COUNT,PAYLOAD_JSON,RESULT_JSON,CREATED_AT,UPDATED_AT,COMPLETED_AT) " +
                            "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                    executionKey, requirementId, businessKey, dataScope, jobName,
                    requirementId + "|" + businessKey, idempotencyKey, "REQUESTED", fencingToken, 0,
                    payloadJson, "{}", Timestamp.from(now), Timestamp.from(now), null);
        } catch (DuplicateKeyException ignored) {
            // Deterministic replay: the existing row is claimed with a fencing compare below.
        }
    }

    private int writeReferenceBusinessRecord(String requirementId,
                                             String businessKey,
                                             String dataScope,
                                             long fencingToken,
                                             String payloadJson,
                                             Instant now) {
        int updated = jdbc.update(
                "update CPF_EDU_BUSINESS_RECORD set BUSINESS_STATE=?,RECORD_VERSION=RECORD_VERSION+1," +
                        "FENCING_TOKEN=?,PAYLOAD_JSON=?,UPDATED_AT=? " +
                        "where REQUIREMENT_ID=? and BUSINESS_KEY=? and DATA_SCOPE=? and FENCING_TOKEN<=?",
                "BATCH_COMPLETED", fencingToken, payloadJson, Timestamp.from(now),
                requirementId, businessKey, dataScope, fencingToken);
        if (updated == 0) {
            try {
                jdbc.update(
                        "insert into CPF_EDU_BUSINESS_RECORD " +
                                "(REQUIREMENT_ID,BUSINESS_KEY,DATA_SCOPE,BUSINESS_STATE,RECORD_VERSION,FENCING_TOKEN,PAYLOAD_JSON,CREATED_AT,UPDATED_AT) " +
                                "values (?,?,?,?,?,?,?,?,?)",
                        requirementId, businessKey, dataScope, "BATCH_COMPLETED", 1L,
                        fencingToken, payloadJson, Timestamp.from(now), Timestamp.from(now));
                return 1;
            } catch (DuplicateKeyException concurrentInsert) {
                int retried = jdbc.update(
                        "update CPF_EDU_BUSINESS_RECORD set BUSINESS_STATE=?,RECORD_VERSION=RECORD_VERSION+1," +
                                "FENCING_TOKEN=?,PAYLOAD_JSON=?,UPDATED_AT=? " +
                                "where REQUIREMENT_ID=? and BUSINESS_KEY=? and DATA_SCOPE=? and FENCING_TOKEN<=?",
                        "BATCH_COMPLETED", fencingToken, payloadJson, Timestamp.from(now),
                        requirementId, businessKey, dataScope, fencingToken);
                if (retried != 1) {
                    throw new IllegalStateException("Reference business record fencing conflict");
                }
                return retried;
            }
        }
        return updated;
    }

    private void saveCheckpoint(String executionKey,
                                String stepName,
                                String partitionKey,
                                String state,
                                long version,
                                String checkpointJson,
                                Instant now) {
        int updated = jdbc.update(
                "update CPF_REF_BAT_CHECKPOINT set CHECKPOINT_VERSION=?,STATE=?,CHECKPOINT_JSON=?,UPDATED_AT=? " +
                        "where JOB_EXECUTION_KEY=? and STEP_NAME=? and PARTITION_KEY=? and CHECKPOINT_VERSION<=?",
                version, state, checkpointJson, Timestamp.from(now), executionKey, stepName, partitionKey, version);
        if (updated == 0) {
            try {
                jdbc.update(
                        "insert into CPF_REF_BAT_CHECKPOINT " +
                                "(JOB_EXECUTION_KEY,STEP_NAME,PARTITION_KEY,CHECKPOINT_VERSION,STATE,CHECKPOINT_JSON,UPDATED_AT) " +
                                "values (?,?,?,?,?,?,?)",
                        executionKey, stepName, partitionKey, version, state, checkpointJson, Timestamp.from(now));
            } catch (DuplicateKeyException concurrentInsert) {
                int retried = jdbc.update(
                        "update CPF_REF_BAT_CHECKPOINT set CHECKPOINT_VERSION=?,STATE=?,CHECKPOINT_JSON=?,UPDATED_AT=? " +
                                "where JOB_EXECUTION_KEY=? and STEP_NAME=? and PARTITION_KEY=? and CHECKPOINT_VERSION<=?",
                        version, state, checkpointJson, Timestamp.from(now), executionKey, stepName, partitionKey, version);
                if (retried != 1) throw new IllegalStateException("Batch checkpoint version conflict");
            }
        }
    }

    private void saveTargetResult(String targetId,
                                  String executionKey,
                                  String targetKey,
                                  String state,
                                  String resultJson,
                                  Instant now) {
        int updated = jdbc.update(
                "update CPF_REF_BAT_TARGET_RESULT set STATE=?,ATTEMPT_COUNT=ATTEMPT_COUNT+1," +
                        "RESULT_JSON=?,ERROR_CODE=?,ERROR_MESSAGE=?,UPDATED_AT=? " +
                        "where JOB_EXECUTION_KEY=? and TARGET_KEY=?",
                state, resultJson, "", "", Timestamp.from(now), executionKey, targetKey);
        if (updated == 0) {
            try {
                jdbc.update(
                        "insert into CPF_REF_BAT_TARGET_RESULT " +
                                "(TARGET_RESULT_ID,JOB_EXECUTION_KEY,TARGET_KEY,STATE,ATTEMPT_COUNT,RESULT_JSON,ERROR_CODE,ERROR_MESSAGE,UPDATED_AT) " +
                                "values (?,?,?,?,?,?,?,?,?)",
                        targetId, executionKey, targetKey, state, 1, resultJson, "", "", Timestamp.from(now));
            } catch (DuplicateKeyException concurrentInsert) {
                jdbc.update(
                        "update CPF_REF_BAT_TARGET_RESULT set STATE=?,ATTEMPT_COUNT=ATTEMPT_COUNT+1," +
                                "RESULT_JSON=?,ERROR_CODE=?,ERROR_MESSAGE=?,UPDATED_AT=? " +
                                "where JOB_EXECUTION_KEY=? and TARGET_KEY=?",
                        state, resultJson, "", "", Timestamp.from(now), executionKey, targetKey);
            }
        }
    }

    private void markFailed(String executionKey, long fencingToken, Exception error) {
        try {
            String result = json.writeValueAsString(Map.of(
                    "state", "FAILED",
                    "errorType", error.getClass().getSimpleName(),
                    "message", safeMessage(error)));
            jdbc.update(
                    "update CPF_REF_BAT_JOB_EXECUTION set STATE=?,RESULT_JSON=?,UPDATED_AT=?,COMPLETED_AT=? " +
                            "where JOB_EXECUTION_KEY=? and FENCING_TOKEN=?",
                    "FAILED", result, Timestamp.from(Instant.now()), Timestamp.from(Instant.now()),
                    executionKey, fencingToken);
        } catch (Exception ignored) {
            error.addSuppressed(ignored);
        }
    }

    private static String stableKey(String value) {
        return UUID.nameUUIDFromBytes(value.getBytes(StandardCharsets.UTF_8)).toString();
    }

    private static String safeMessage(Throwable error) {
        String message = error.getMessage();
        if (message == null || message.isBlank()) return error.getClass().getSimpleName();
        return message.length() <= 500 ? message : message.substring(0, 500);
    }
}
