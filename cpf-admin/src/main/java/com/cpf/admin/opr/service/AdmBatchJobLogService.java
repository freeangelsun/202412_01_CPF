package com.cpf.admin.opr.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.cpf.batch.api.CpfBatchLogPaths;
import com.cpf.platform.operations.observability.api.logging.CpfLogPaths;
import com.cpf.core.api.error.CpfValidationException;
import org.springframework.core.env.Environment;
import com.cpf.foundation.annotation.CpfService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
@CpfService
public class AdmBatchJobLogService extends com.cpf.admin.common.base.AdmBaseService {
    private static final int MAX_RECORDS = 500;

    private final Path logRoot;
    private final Path jobsRoot;
    private final CpfLogPaths pathPolicy;
    private final ObjectMapper objectMapper;

    public AdmBatchJobLogService(Environment environment, ObjectMapper objectMapper) {
        this.pathPolicy = new CpfLogPaths(environment);
        this.logRoot = pathPolicy.logRoot();
        this.jobsRoot = pathPolicy.batchJobLogPath(Path.of("bat", "jobs"));
        this.objectMapper = objectMapper;
    }

    public List<Map<String, Object>> findLogs(
            String businessDate,
            String jobName,
            Long jobInstanceId,
            String serverInstanceId,
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
                    .filter(row -> matches(row, "serverInstanceId", serverInstanceId))
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
            String serverInstanceId,
            int maxRecords) {
        LocalDate parsedDate = parseBusinessDate(businessDate);
        Path relativePath;
        try {
            relativePath = CpfBatchLogPaths.relativePath(
                    jobName,
                    jobInstanceId,
                    parsedDate,
                    serverInstanceId);
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
        Path normalizedJobsRoot = jobsRoot.toAbsolutePath().normalize();
        Path normalizedPath = path.toAbsolutePath().normalize();
        if (!normalizedPath.startsWith(normalizedJobsRoot)) {
            return null;
        }
        Path jobsRelativePath = normalizedJobsRoot.relativize(normalizedPath);
        if (jobsRelativePath.getNameCount() != 4) {
            return null;
        }
        String businessDate = jobsRelativePath.getName(0).toString();
        String jobName = jobsRelativePath.getName(1).toString();
        String serverInstanceId = jobsRelativePath.getName(2).toString();
        String fileName = jobsRelativePath.getFileName().toString();
        try {
            LocalDate parsedDate = LocalDate.parse(businessDate, DateTimeFormatter.BASIC_ISO_DATE);
            if (!isCanonicalPathToken(jobName) || !isCanonicalPathToken(serverInstanceId)) {
                return null;
            }
            Pattern filePattern = Pattern.compile(
                    "^cpf-bat-"
                            + Pattern.quote(jobName)
                            + "-(\\d+)-"
                            + Pattern.quote(serverInstanceId)
                            + "-"
                            + Pattern.quote(businessDate)
                            + "\\.log$");
            Matcher matcher = filePattern.matcher(fileName);
            if (!matcher.matches()) {
                return null;
            }
            long jobInstanceId = Long.parseLong(matcher.group(1));
            Path canonicalRelativePath = CpfBatchLogPaths.relativePath(
                    jobName,
                    jobInstanceId,
                    parsedDate,
                    serverInstanceId);
            Path canonicalJobsRelativePath = canonicalRelativePath.subpath(
                    2,
                    canonicalRelativePath.getNameCount());
            if (!jobsRelativePath.equals(canonicalJobsRelativePath)) {
                return null;
            }
            Map<String, Object> metadata = new LinkedHashMap<>();
            metadata.put("businessDate", businessDate);
            metadata.put("jobName", jobName);
            metadata.put("jobInstanceId", jobInstanceId);
            metadata.put("serverInstanceId", serverInstanceId);
            metadata.put("relativePath", logRoot.relativize(normalizedPath).toString().replace('\\', '/'));
            metadata.put("sizeBytes", Files.size(path));
            metadata.put("lastModifiedAt", Files.getLastModifiedTime(path).toInstant());
            return metadata;
        } catch (IOException | IllegalArgumentException ex) {
            return null;
        }
    }

    private boolean isCanonicalPathToken(String value) {
        try {
            return CpfBatchLogPaths.sanitize(value).equals(value);
        } catch (IllegalArgumentException ex) {
            return false;
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
        try {
            return LocalDate.parse(value, DateTimeFormatter.BASIC_ISO_DATE);
        } catch (DateTimeParseException ex) {
            throw new CpfValidationException("businessDate는 유효한 yyyyMMdd 일자여야 합니다.");
        }
    }
}
