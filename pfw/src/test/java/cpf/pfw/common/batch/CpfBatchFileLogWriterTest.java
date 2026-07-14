package cpf.pfw.common.batch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import cpf.pfw.common.logging.TransactionIdGenerator;
import cpf.pfw.common.logging.file.CpfFileLogWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.ExitStatus;
import org.springframework.mock.env.MockEnvironment;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CpfBatchFileLogWriterTest {
    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @TempDir
    Path tempDir;

    @Test
    void separatesJobInstancesAndAppendsRestartToOriginalFile() throws Exception {
        CpfBatchFileLogWriter writer = writer(Instant.parse("2026-07-13T00:00:00Z"));
        JobExecution firstExecution = execution(42L, 100L, "20260713", null, "0");
        JobExecution restartExecution = execution(42L, 101L, "20260713", "100", "1");
        JobExecution anotherInstance = execution(43L, 102L, "20260713", null, "0");

        writer.writeBatch("BATCH_JOB_STARTED", firstExecution, null);
        writer.writeBatch("BATCH_JOB_FINISHED", firstExecution, null);
        writer.writeBatch("BATCH_JOB_RESTARTED", restartExecution, null);
        writer.writeBatch("BATCH_JOB_STARTED", anotherInstance, null);

        Path firstFile = logPath(42L, "20260713");
        Path secondFile = logPath(43L, "20260713");
        assertThat(firstFile).exists();
        assertThat(secondFile).exists();
        assertThat(Files.readAllLines(firstFile)).hasSize(3);
        assertThat(Files.readAllLines(secondFile)).hasSize(1);
        assertThat(Files.readString(firstFile))
                .contains("\"jobExecutionId\":100")
                .contains("\"jobExecutionId\":101")
                .contains("\"originalJobExecutionId\":\"100\"")
                .contains("\"restartAttempt\":1");
    }

    @Test
    void fixesBusinessDateAcrossMidnightAndWritesRequiredStepFields() throws Exception {
        CpfBatchFileLogWriter beforeMidnight = writer(Instant.parse("2026-07-13T14:59:59Z"));
        CpfBatchFileLogWriter afterMidnight = writer(Instant.parse("2026-07-13T15:00:01Z"));
        JobExecution execution = execution(55L, 200L, null, null, "0");
        StepExecution stepExecution = new StepExecution("sampleStep", execution);
        stepExecution.setId(300L);
        stepExecution.setStatus(BatchStatus.FAILED);
        stepExecution.setStartTime(LocalDateTime.of(2026, 7, 13, 23, 59, 59));
        stepExecution.setEndTime(LocalDateTime.of(2026, 7, 14, 0, 0, 1));
        stepExecution.setReadCount(10);
        stepExecution.setWriteCount(8);
        stepExecution.setFilterCount(1);
        stepExecution.setReadSkipCount(1);
        stepExecution.setCommitCount(2);
        stepExecution.setRollbackCount(1);
        stepExecution.setExitStatus(new ExitStatus("FAILED", "password=plain-secret"));

        beforeMidnight.writeBatch("BATCH_JOB_STARTED", execution, null);
        afterMidnight.writeBatch("BATCH_STEP_FINISHED", execution, stepExecution);

        Path fixedDateFile = logPath(55L, "20260713");
        assertThat(fixedDateFile).exists();
        assertThat(logPath(55L, "20260714")).doesNotExist();
        List<String> lines = Files.readAllLines(fixedDateFile);
        assertThat(lines).hasSize(2);
        for (String line : lines) {
            JsonNode json = OBJECT_MAPPER.readTree(line);
            assertThat(json.path("businessDate").asText()).isEqualTo("20260713");
            assertThat(json.path("jobName").asText()).isEqualTo("CPF_BAT_SMOKE_JOB");
            assertThat(json.path("jobInstanceId").asLong()).isEqualTo(55L);
            assertThat(json.path("transactionGlobalId").asText()).isNotBlank();
            assertThat(json.path("serverInstanceId").asText()).isNotBlank();
            assertThat(json.path("workerInstanceId").asText()).isNotBlank();
        }
        assertThat(lines.get(1))
                .contains("\"stepExecutionId\":300")
                .contains("\"readCount\":10")
                .contains("\"writeCount\":8")
                .contains("\"skipCount\":1")
                .doesNotContain("plain-secret");
    }

    @Test
    void separatesDifferentBusinessDates() throws Exception {
        CpfBatchFileLogWriter writer = writer(Instant.parse("2026-07-13T00:00:00Z"));

        writer.writeBatch("BATCH_JOB_STARTED", execution(60L, 301L, "20260713", null, "0"), null);
        writer.writeBatch("BATCH_JOB_STARTED", execution(61L, 302L, "20260714", null, "0"), null);

        assertThat(logPath(60L, "20260713")).exists();
        assertThat(logPath(61L, "20260714")).exists();
    }

    private CpfBatchFileLogWriter writer(Instant instant) {
        Clock clock = Clock.fixed(instant, SEOUL);
        MockEnvironment environment = new MockEnvironment()
                .withProperty("cpf.logging.file.base-path", tempDir.toString())
                .withProperty("cpf.logging.file.timezone", SEOUL.getId())
                .withProperty("cpf.framework.module-id", "BAT")
                .withProperty("cpf.framework.was-id", "batWK01");
        CpfFileLogWriter fileLogWriter = new CpfFileLogWriter(environment, clock);
        TransactionIdGenerator idGenerator = new TransactionIdGenerator("BAT", "batWK01", 7, clock);
        return new CpfBatchFileLogWriter(fileLogWriter, idGenerator, clock);
    }

    private JobExecution execution(
            long jobInstanceId,
            long jobExecutionId,
            String businessDate,
            String originalExecutionId,
            String restartAttempt) {
        JobParametersBuilder parameters = new JobParametersBuilder()
                .addString("transactionGlobalId", "20260713090000000BATbatWK010000001")
                .addString("runId", String.valueOf(jobExecutionId))
                .addString("rerunId", restartAttempt)
                .addString("workerInstanceId", "bat-worker-01");
        if (businessDate != null) {
            parameters.addString("businessDate", businessDate);
        }
        if (originalExecutionId != null) {
            parameters.addString("originalJobExecutionId", originalExecutionId);
        }
        parameters.addString("restartAttempt", restartAttempt);
        JobExecution execution = new JobExecution(
                new JobInstance(jobInstanceId, "CPF_BAT_SMOKE_JOB"),
                parameters.toJobParameters());
        execution.setId(jobExecutionId);
        execution.setStatus(BatchStatus.STARTED);
        execution.setStartTime(LocalDateTime.of(2026, 7, 13, 9, 0));
        return execution;
    }

    private Path logPath(long jobInstanceId, String businessDate) {
        return tempDir.resolve("local").resolve(CpfBatchJobLogPath.relativePath(
                "CPF_BAT_SMOKE_JOB",
                jobInstanceId,
                java.time.LocalDate.parse(businessDate, java.time.format.DateTimeFormatter.BASIC_ISO_DATE)));
    }
}
