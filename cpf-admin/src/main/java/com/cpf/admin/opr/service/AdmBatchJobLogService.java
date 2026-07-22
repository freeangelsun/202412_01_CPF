package com.cpf.admin.opr.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.cpf.core.common.batch.CpfBatchJobLogPath;
import com.cpf.core.common.exception.CpfValidationException;
import com.cpf.core.common.logging.file.CpfLogPathPolicy;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ADM에서 BAT JobInstance 로그를 원본 경로 노출 없이 조회하는 서비스입니다.
 */
@Service
public class AdmBatchJobLogService extends com.cpf.admin.common.base.AdmBaseService {
    private static final Pattern FILE_PATTERN = Pattern.compile("cpf-bat-(.+)-(\\d+)-(\\d{8})\\.log");
    private static final int MAX_RECORDS = 500;

    private final Path logRoot;
    private final Path jobsRoot;
    private final CpfLogPathPolicy pathPolicy;
    private final ObjectMapper objectMapper;

    public AdmBatchJobLogService(Environment environment, ObjectMapper objectMapper) {
        this.pathPolicy = new CpfLogPathPolicy(environment);
        this.logRoot = pathPolicy.logRoot();
        this.jobsRoot = pathPolicy.batchJobLogPath(Path.of("bat", "jobs"));
        this.objectMapper = objectMapper;
    }

    public List<Map<String, Object>> findLogs(
            String businessDate,
            String jobName,
            Long jobInstanceId,
            int limit) {
        if (!Files.isDirectory(jobsRoot, LinkOption.NOFOLLOW_LINKS)) {
            return List.of();
        }
        int safeLimit = Math.max(1, Math.min(limit, 500));
        try (var paths = Files.walk(jobsRoot)) {
            return paths
                    .filter(path -> Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS))
                    .filter(path -> !Files.isSymbolicLink(path))
                    .map(this::metadata)
                    .filter(row -> row != null)
                    .filter(row -> matches(row, "businessDate", businessDate))
                    .filter(row -> matches(row, "jobName", jobName))
                    .filter(row -> jobInstanceId == null
                            || jobInstanceId.equals(((Number) row.get("jobInstanceId")).longValue()))
                    .sorted(Comparator.comparing(
                            row -> (Instant) row.get("lastModifiedAt"),
                            Comparator.reverseOrder()))
                    .limit(safeLimit)
                    .toList();
        } catch (IOException ex) {
            throw new CpfValidationException("BAT JobInstance 로그 목록을 읽을 수 없습니다.");
        }
    }

    public Map<String, Object> findDetail(
            String businessDate,
            String jobName,
            long jobInstanceId,
            int maxRecords) {
        LocalDate parsedDate = parseBusinessDate(businessDate);
        Path relativePath;
        try {
            relativePath = CpfBatchJobLogPath.relativePath(jobName, jobInstanceId, parsedDate);
        } catch (IllegalArgumentException ex) {
            throw new CpfValidationException(ex.getMessage());
        }
        Path candidate = pathPolicy.batchJobLogPath(relativePath);
        Path safeFile = requireSafeRegularFile(candidate);
        int safeMaxRecords = Math.max(1, Math.min(maxRecords, MAX_RECORDS));
        try {
            List<String> allLines = Files.readAllLines(safeFile);
            int fromIndex = Math.max(0, allLines.size() - safeMaxRecords);
            List<Map<String, Object>> records = new ArrayList<>();
            for (int index = fromIndex; index < allLines.size(); index++) {
                records.add(parseJsonLine(allLines.get(index), index + 1));
            }
            Map<String, Object> detail = new LinkedHashMap<>(metadata(safeFile));
            detail.put("totalRecordCount", allLines.size());
            detail.put("returnedRecordCount", records.size());
            detail.put("records", records);
            return detail;
        } catch (IOException ex) {
            throw new CpfValidationException("BAT JobInstance 로그 상세를 읽을 수 없습니다.");
        }
    }

    private Map<String, Object> metadata(Path path) {
        Matcher matcher = FILE_PATTERN.matcher(path.getFileName().toString());
        if (!matcher.matches()) {
            return null;
        }
        try {
            Map<String, Object> metadata = new LinkedHashMap<>();
            metadata.put("businessDate", matcher.group(3));
            metadata.put("jobName", matcher.group(1));
            metadata.put("jobInstanceId", Long.parseLong(matcher.group(2)));
            metadata.put("relativePath", logRoot.relativize(path.toAbsolutePath().normalize()).toString().replace('\\', '/'));
            metadata.put("sizeBytes", Files.size(path));
            metadata.put("lastModifiedAt", Files.getLastModifiedTime(path).toInstant());
            return metadata;
        } catch (IOException | NumberFormatException ex) {
            return null;
        }
    }

    private Path requireSafeRegularFile(Path candidate) {
        try {
            Path realRoot = logRoot.toRealPath(LinkOption.NOFOLLOW_LINKS);
            Path realFile = candidate.toRealPath(LinkOption.NOFOLLOW_LINKS);
            if (!realFile.startsWith(realRoot)
                    || Files.isSymbolicLink(candidate)
                    || !Files.isRegularFile(realFile, LinkOption.NOFOLLOW_LINKS)) {
                throw new CpfValidationException("허용되지 않은 BAT 로그 경로입니다.");
            }
            return realFile;
        } catch (IOException ex) {
            throw new CpfValidationException("BAT JobInstance 로그를 찾을 수 없습니다.");
        }
    }

    private Map<String, Object> parseJsonLine(String line, int lineNumber) {
        try {
            return objectMapper.readValue(line, new TypeReference<>() {
            });
        } catch (JsonProcessingException ex) {
            throw new CpfValidationException("BAT 로그 JSON Lines 형식이 올바르지 않습니다. line=" + lineNumber);
        }
    }

    private boolean matches(Map<String, Object> row, String key, String expected) {
        return expected == null || expected.isBlank()
                || expected.trim().equalsIgnoreCase(String.valueOf(row.get(key)));
    }

    private LocalDate parseBusinessDate(String value) {
        if (value == null || !value.matches("\\d{8}")) {
            throw new CpfValidationException("businessDate는 yyyyMMdd 형식이어야 합니다.");
        }
        return LocalDate.parse(value, DateTimeFormatter.BASIC_ISO_DATE);
    }
}
