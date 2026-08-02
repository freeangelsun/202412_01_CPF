package com.cpf.reference.batch.file.csv;

import com.cpf.batch.spi.FileProcessHandler;
import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.HexFormat;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** EDU 전용 CSV FILE_PROCESS Sample. Header와 행을 검증하고 결정적 결과 Hash를 반환합니다. */
@Component
@Profile("cpf-edu")
public final class ReferenceCsvFileProcessHandler implements FileProcessHandler {
    @Override
    public String processorId() {
        return "REF_CSV_COUNT";
    }

    @Override
    public FileProcessResult process(FileProcessCommand command) throws Exception {
        long rows = 0;
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (BufferedReader reader = Files.newBufferedReader(command.claimedPath(), StandardCharsets.UTF_8)) {
            String header = reader.readLine();
            if (header == null || !header.matches("[A-Za-z0-9_,-]+")) {
                return new FileProcessResult(Status.FAILED, "invalid CSV header", "");
            }
            digest.update(header.getBytes(StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                if (line.indexOf('\0') >= 0) {
                    return new FileProcessResult(Status.FAILED, "NUL is not allowed", "");
                }
                rows++;
                digest.update((byte) '\n');
                digest.update(line.getBytes(StandardCharsets.UTF_8));
            }
        }
        return FileProcessResult.completed("rows=" + rows, HexFormat.of().formatHex(digest.digest()));
    }
}
