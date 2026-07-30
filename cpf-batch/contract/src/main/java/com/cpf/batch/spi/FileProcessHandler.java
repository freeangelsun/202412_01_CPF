package com.cpf.batch.spi;

import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

/**
 * FILE_PROCESS Definition이 Claim한 파일을 실제 업무 처리기로 연결하는 Batch 확장 SPI입니다.
 * Worker는 Claim/Fencing을 보유한 동안에만 이 SPI를 호출하고 결과를 Attempt Ledger에 기록합니다.
 */
public interface FileProcessHandler {
    String processorId();

    FileProcessResult process(FileProcessCommand command) throws Exception;

    record FileProcessCommand(
            long executionId,
            long definitionVersion,
            String definitionChecksum,
            String transactionId,
            String segmentId,
            long fencingToken,
            Path claimedPath,
            long size,
            String sha256,
            Map<String, Object> parameters) {
        public FileProcessCommand {
            definitionChecksum = Objects.requireNonNull(definitionChecksum, "definitionChecksum");
            claimedPath = Objects.requireNonNull(claimedPath, "claimedPath");
            sha256 = Objects.requireNonNull(sha256, "sha256");
            parameters = parameters == null ? Map.of() : Map.copyOf(parameters);
        }
    }

    record FileProcessResult(Status status, String message, String outputHash) {
        public FileProcessResult {
            status = Objects.requireNonNull(status, "status");
            message = Objects.toString(message, "");
            outputHash = Objects.toString(outputHash, "");
        }

        public static FileProcessResult completed(String message, String outputHash) {
            return new FileProcessResult(Status.COMPLETED, message, outputHash);
        }
    }

    enum Status {
        COMPLETED, RETRYABLE_FAILURE, FAILED, UNKNOWN_RESULT
    }
}
