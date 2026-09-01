package com.cpf.gateway.ledger;

import com.cpf.gateway.api.CpfGatewayLedgerPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.cpf.data.persistence.api.CpfDataSourceRegistry;
import com.cpf.data.persistence.api.CpfDatabaseRole;

import java.io.IOException;
import java.io.FilterInputStream;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.UUID;

/**
 * Gateway 원장 Event를 파일 단위 WAL에 먼저 확정한 뒤 중앙 DB로 적재합니다.
 * DB 장애·Gateway Crash·Replay 중 Crash에서도 Event 파일은 성공 적재 전까지 삭제되지 않습니다.
 */
// @ConditionalOnBean 은 auto-configuration 에서만 신뢰할 수 있다. component scan 으로
// 등록되는 이 Adapter 는 auto-configuration 이 대상 Bean 을 정의하기 전에 조건이 평가되어
// 항상 false 가 된다. Gateway 는 CPF Platform DB 를 소유하는 DataSource Owner 이고 이
// Adapter 는 해당 Port 의 유일한 구현체이므로, 조건부가 아니라 항상 등록되어야 한다.
// 의존 Bean 이 없으면 조용히 사라지는 대신 기동이 명확한 원인으로 실패해야 한다.
@Component
public final class DurableCpfGatewayLedgerAdapter implements CpfGatewayLedgerPort {
    private static final Logger log = LoggerFactory.getLogger(DurableCpfGatewayLedgerAdapter.class);
    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;
    private final Path pending;
    private final long maxSpoolBytes;

    /** DurableCpfGatewayLedgerAdapter 작업을 CPF 표준 계약에 따라 수행한다. */
    public DurableCpfGatewayLedgerAdapter(
            CpfDataSourceRegistry dataSources,
            ObjectMapper mapper,
            @Value("${cpf.gateway.ledger.spool-directory:${java.io.tmpdir}/cpf-gateway-ledger}") String spoolDirectory,
            @Value("${cpf.gateway.ledger.max-spool-bytes:1073741824}") long maxSpoolBytes) {
        // 형제 Gateway Adapter 와 동일하게 canonical role DataSource 에서 직접 만든다.
        // Spring Boot 의 단일후보 JdbcTemplate autoconfiguration 에 의존하지 않는다.
        this.jdbc = new JdbcTemplate(dataSources.require(CpfDatabaseRole.CPF_PLATFORM_DB));
        this.mapper = mapper;
        this.pending = Path.of(spoolDirectory).toAbsolutePath().normalize().resolve("pending");
        if (maxSpoolBytes < 1_024L) {
            throw new IllegalArgumentException("Gateway ledger maxSpoolBytes must be at least 1024");
        }
        this.maxSpoolBytes = maxSpoolBytes;
        initializeDirectory();
    }

    @Override public void begin(TransactionStart event) { persist(new Envelope("BEGIN", event.gatewayTransactionId(), event)); }
    @Override public void recordAttempt(Attempt event) { persist(new Envelope("ATTEMPT", event.attemptId(), event)); }
    @Override public void recordCapture(CaptureSegment event) { persist(new Envelope("CAPTURE", event.gatewayTransactionId()+":"+event.segmentType(), event)); }
    @Override public void complete(TransactionCompletion event) { persist(new Envelope("COMPLETE", event.gatewayTransactionId(), event)); }

    @Scheduled(fixedDelayString = "${cpf.gateway.ledger.replay-millis:5000}")
    /** replay 작업을 CPF 표준 계약에 따라 수행한다. */
    public void replay() {
        try (var files = Files.list(pending)) {
            files.filter(path -> path.getFileName().toString().endsWith(".json"))
                    .sorted().limit(1_000).forEach(this::replayOne);
        } catch (IOException ex) {
            log.error("Gateway ledger spool scan failed", ex);
        }
    }

    private void persist(Envelope envelope) {
        Path eventFile = pending.resolve(System.currentTimeMillis() + "-" + UUID.randomUUID() + ".json");
        Path temporary = eventFile.resolveSibling(eventFile.getFileName() + ".tmp");
        try {
            byte[] payload = mapper.writeValueAsBytes(envelope);
            assertCapacity(payload.length);
            Files.write(temporary, payload, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
            try (FileChannel channel = FileChannel.open(temporary, StandardOpenOption.WRITE)) { channel.force(true); }
            Files.move(temporary, eventFile, StandardCopyOption.ATOMIC_MOVE);
            replayOne(eventFile);
        // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
        } catch (IOException ex) {
            throw new IllegalStateException("Gateway ledger durable spool write failed", ex);
        } finally {
            try { Files.deleteIfExists(temporary); } catch (IOException ignored) { }
        }
    }

    private void replayOne(Path file) {
        try {
            long size = Files.size(file);
            if (size < 1L || size > maxSpoolBytes) {
                throw new IOException("Gateway ledger spool event size is invalid: " + size);
            }
            Envelope envelope;
            try (InputStream input = new BoundedInputStream(
                    Files.newInputStream(file, StandardOpenOption.READ), maxSpoolBytes)) {
                envelope = mapper.readValue(input, Envelope.class);
            }
            apply(envelope);
            Files.deleteIfExists(file);
        // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
        } catch (DuplicateKeyException duplicate) {
            try { Files.deleteIfExists(file); } catch (IOException ex) { log.warn("Duplicate spool cleanup failed: {}", file, ex); }
        } catch (Exception ex) {
            log.warn("Gateway ledger event remains in durable spool: {}", file.getFileName(), ex);
        }
    }

    private void apply(Envelope envelope) {
        switch (envelope.type()) {
            case "BEGIN" -> insertBegin(mapper.convertValue(envelope.payload(), TransactionStart.class));
            case "ATTEMPT" -> insertAttempt(mapper.convertValue(envelope.payload(), Attempt.class));
            case "CAPTURE" -> insertCapture(mapper.convertValue(envelope.payload(), CaptureSegment.class));
            case "COMPLETE" -> updateComplete(mapper.convertValue(envelope.payload(), TransactionCompletion.class));
            default -> throw new IllegalArgumentException("Unsupported gateway ledger event type: " + envelope.type());
        }
    }

    private void insertBegin(TransactionStart e) {
        jdbc.update("""
                INSERT INTO GW_TRANSACTION(
                  gateway_transaction_id,transaction_id,trace_id,channel_id,source_ip,source_port,
                  gateway_instance_id,binding_id,route_id,route_version,server_group_id,result_status,
                  unknown_yn,total_duration_ms,request_size,response_size,created_at,binding_version,
                  config_checksum,request_method,request_path)
                VALUES (?,?,?,?,?,?,?,?,?,?,?,'RUNNING','N',0,?,0,?,?,?,?,?)
                """,
                e.gatewayTransactionId(), e.transactionId(), e.traceId(), e.channelId(), e.sourceIp(), e.sourcePort(),
                e.gatewayInstanceId(), e.bindingId(), e.routeId(), e.routeVersion(), e.serverGroupId(),
                e.requestSize(), e.startedAt(), e.bindingVersion(), e.configChecksum(), e.requestMethod(), e.requestPath());
    }

    private void insertAttempt(Attempt e) {
        jdbc.update("""
                INSERT INTO GW_ATTEMPT(
                  attempt_id,gateway_transaction_id,attempt_no,instance_id,target_host,target_port,target_protocol,
                  connect_duration_ms,response_duration_ms,attempt_status,protocol_status,failure_code,failure_message,
                  started_at,finished_at,gateway_instance_id,selection_reason,unknown_yn)
                VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                """,
                e.attemptId(), e.gatewayTransactionId(), e.attemptNo(), e.instanceId(), e.targetHost(), e.targetPort(),
                e.targetProtocol(), e.connectDurationMs(), e.responseDurationMs(), e.status(), e.protocolStatus(),
                e.failureCode(), e.failureMessage(), e.startedAt(), e.finishedAt(), e.gatewayInstanceId(),
                e.selectionReason(), e.unknown() ? "Y" : "N");
    }


    private void insertCapture(CaptureSegment e) {
        jdbc.update("""
                INSERT INTO GW_TRANSACTION_CAPTURE_SEGMENT(
                  gateway_transaction_id,segment_type,policy_schema_version,policy_checksum,captured_value,
                  truncated_yn,metadata_only_yn,observed_bytes,captured_at)
                VALUES (?,?,?,?,?,?,?,?,?)
                """,e.gatewayTransactionId(),e.segmentType(),e.policySchemaVersion(),e.policyChecksum(),e.capturedValue(),
                e.truncated()?"Y":"N",e.metadataOnly()?"Y":"N",e.observedBytes(),e.capturedAt());
    }

    private void updateComplete(TransactionCompletion e) {
        int changed = jdbc.update("""
                UPDATE GW_TRANSACTION
                   SET final_instance_id=?,result_status=?,protocol_status=?,business_code=?,failure_stage=?,
                       unknown_yn=?,total_duration_ms=?,response_size=?,completed_at=?
                 WHERE gateway_transaction_id=?
                """,
                e.finalInstanceId(), e.resultStatus(), e.protocolStatus(), e.businessCode(), e.failureStage(),
                e.unknown() ? "Y" : "N", e.totalDurationMs(), e.responseSize(), e.completedAt(), e.gatewayTransactionId());
        if (changed != 1) throw new IllegalStateException("Gateway transaction start is missing: " + e.gatewayTransactionId());
    }

    private void assertCapacity(long incomingBytes) {
        try (var files = Files.list(pending)) {
            long used = files.filter(Files::isRegularFile).mapToLong(path -> {
                // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
                try { return Files.size(path); } catch (IOException ex) { return 0L; }
            }).sum();
            if (incomingBytes < 1L || incomingBytes > maxSpoolBytes - used) {
                throw new IllegalStateException("Gateway ledger spool capacity exceeded");
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Gateway ledger spool capacity check failed", ex);
        }
    }

    private void initializeDirectory() {
        try {
            Files.createDirectories(pending);
            try {
                Files.setPosixFilePermissions(pending, PosixFilePermissions.fromString("rwx------"));
            // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
            } catch (UnsupportedOperationException ignored) { }
        } catch (IOException ex) {
            throw new IllegalStateException("Gateway ledger spool directory initialization failed", ex);
        }
    }

    public record Envelope(String type, String id, Object payload) { }

    /** File size 사전검사 뒤 교체 경쟁이 있어도 실제 Parser read 자체를 동일 상한으로 제한합니다. */
    private static final class BoundedInputStream extends FilterInputStream {
        private final long limit;
        private long observed;

        private BoundedInputStream(InputStream input, long limit) {
            super(input);
            this.limit = limit;
        }

        @Override
        public int read() throws IOException {
            int value = super.read();
            if (value >= 0) requireBudget(1L);
            return value;
        }

        @Override
        public int read(byte[] bytes, int offset, int length) throws IOException {
            int read = super.read(bytes, offset, length);
            if (read > 0) requireBudget(read);
            return read;
        }

        private void requireBudget(long increment) throws IOException {
            observed += increment;
            if (observed > limit) {
                throw new IOException("Gateway ledger spool event exceeds configured limit");
            }
        }
    }
}
