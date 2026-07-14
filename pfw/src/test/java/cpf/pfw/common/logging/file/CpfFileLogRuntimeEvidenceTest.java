package cpf.pfw.common.logging.file;

import com.fasterxml.jackson.databind.ObjectMapper;
import cpf.pfw.common.logging.TransactionContext;
import cpf.pfw.common.logging.TransactionLogRecord;
import cpf.pfw.common.logging.fallback.TransactionLogFallbackStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.mock.env.MockEnvironment;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 저장소 절대 로그 root에 신규 raw 로그를 만드는 명시적 검증용 테스트입니다.
 *
 * <p>일반 test 실행에서는 비활성화되며, 검증 스크립트가 {@code CPF_RUNTIME_LOG_PROBE=true}와
 * 절대 {@code CPF_LOG_ROOT}를 주입한 경우에만 실행합니다. 생성된 raw 파일은 별도 manifest와
 * 민감정보 검사를 마친 뒤 cleanup할 수 있습니다.</p>
 */
@EnabledIfEnvironmentVariable(named = "CPF_RUNTIME_LOG_PROBE", matches = "true")
class CpfFileLogRuntimeEvidenceTest {

    @AfterEach
    void clearContext() {
        TransactionContext.clear();
    }

    @Test
    void createsStandardLogsForTwoInstancesAndDurableFallback() throws Exception {
        Path logRoot = Path.of(requiredEnvironment("CPF_LOG_ROOT")).toAbsolutePath().normalize();
        assertThat(logRoot).isAbsolute();
        LocalDate businessDate = LocalDate.now(ZoneId.of("Asia/Seoul"));
        String date = businessDate.format(DateTimeFormatter.BASIC_ISO_DATE);

        CpfFileLogWriter firstWriter = writer(logRoot, "acc-runtime-probe-01");
        CpfFileLogWriter secondWriter = writer(logRoot, "acc-runtime-probe-02");
        String transactionId = "ACC01LOG0001";
        String firstGlobalId = date + "120000000ACCaccAP010000001";
        String secondGlobalId = date + "120100000ACCaccAP010000002";

        writeBaseEvent(firstWriter, "ACC", "application", "RUNTIME_PROBE_APPLICATION");
        writeBaseEvent(firstWriter, "PFW", "application", "RUNTIME_PROBE_FRAMEWORK");
        writeBaseEvent(firstWriter, "CMN", "application", "RUNTIME_PROBE_COMMON");
        writeBaseEvent(secondWriter, "ACC", "application", "RUNTIME_PROBE_SECOND_INSTANCE");

        firstWriter.writeTransaction(record(firstGlobalId, transactionId, businessDate, "SUCCESS"), Map.of(), null);
        firstWriter.writeTransaction(record(secondGlobalId, transactionId, businessDate, "FAILURE"), Map.of(), null);

        TransactionContext.initialize(firstGlobalId, "runtime-probe-trace", null, firstGlobalId);
        TransactionContext.putBusinessTransaction(transactionId, "파일 로그 런타임 검증");
        firstWriter.writeIntegration(
                "ACC",
                "EXS",
                "OUTBOUND",
                "GET",
                "/runtime-probe",
                200,
                "SUCCESS",
                15L,
                null,
                null,
                Map.of("eventType", "RUNTIME_PROBE_INTEGRATION"));

        MockEnvironment fallbackEnvironment = environment(logRoot, "acc-runtime-probe-01");
        TransactionLogFallbackStore fallbackStore = new TransactionLogFallbackStore(
                new ObjectMapper().findAndRegisterModules(),
                firstWriter,
                fallbackEnvironment);
        fallbackStore.enqueue(
                record(firstGlobalId, transactionId, businessDate, "FAILURE"),
                Map.of("Authorization", "Bearer runtime-probe-secret"),
                null,
                new IllegalStateException("runtime probe DB failure"));

        Path firstInstance = logRoot.resolve("local/acc/acc-runtime-probe-01");
        Path secondInstance = logRoot.resolve("local/acc/acc-runtime-probe-02");
        Path transactionFile = firstInstance.resolve(
                "transactions/" + date + '/' + transactionId + '_' + date + ".log");
        assertThat(firstInstance.resolve("application")).isDirectory();
        assertThat(firstInstance.resolve("framework/pfw")).isDirectory();
        assertThat(firstInstance.resolve("common/cmn")).isDirectory();
        assertThat(secondInstance.resolve("application")).isDirectory();
        assertThat(transactionFile).exists();
        assertThat(Files.readAllLines(transactionFile)).hasSize(3);
        assertThat(Files.readString(transactionFile))
                .contains(firstGlobalId)
                .contains(secondGlobalId)
                .contains(transactionId)
                .doesNotContain("NO_TRANSACTION");

        Path pending = fallbackStore.pendingFiles().getFirst();
        assertThat(pending).exists();
        assertThat(Files.readString(pending))
                .doesNotContain("runtime-probe-secret")
                .contains("***");
    }

    private CpfFileLogWriter writer(Path root, String instanceId) {
        return new CpfFileLogWriter(environment(root, instanceId));
    }

    private void writeBaseEvent(
            CpfFileLogWriter writer,
            String ownerModule,
            String logType,
            String eventType) {
        Map<String, Object> event = writer.newBaseEvent(ownerModule, logType);
        event.put("eventType", eventType);
        writer.writeEvent(ownerModule, logType, event);
    }

    private MockEnvironment environment(Path root, String instanceId) {
        return new MockEnvironment()
                .withProperty("cpf.logging.file.base-path", root.toString())
                .withProperty("cpf.environment", "local")
                .withProperty("cpf.framework.module-id", "ACC")
                .withProperty("cpf.framework.instance-id", instanceId)
                .withProperty("cpf.logging.file.archive-compress-enabled", "false");
    }

    private TransactionLogRecord record(
            String globalId,
            String transactionId,
            LocalDate businessDate,
            String status) {
        return TransactionLogRecord.builder()
                .transactionId(globalId)
                .traceId("runtime-probe-trace")
                .spanId("runtime-probe-span")
                .sequenceNo(1)
                .moduleId("ACC")
                .businessTransactionId(transactionId)
                .businessTransactionName("파일 로그 런타임 검증")
                .logType(status)
                .httpMethod("GET")
                .uri("/runtime-probe")
                .httpStatus("SUCCESS".equals(status) ? 200 : 500)
                .responseCode("SUCCESS".equals(status) ? "CPF-CMN-0000" : "CPF-PFW-5000")
                .errorCode("SUCCESS".equals(status) ? null : "RUNTIME_PROBE_FAILURE")
                .errorMessage("SUCCESS".equals(status) ? null : "password=masked-value")
                .startTime(LocalDateTime.of(businessDate, java.time.LocalTime.NOON))
                .endTime(LocalDateTime.of(businessDate, java.time.LocalTime.NOON).plusSeconds(1))
                .durationMs(1_000L)
                .execUser("RUNTIME_PROBE")
                .build();
    }

    private String requiredEnvironment(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " 환경변수는 필수입니다.");
        }
        return value;
    }
}
