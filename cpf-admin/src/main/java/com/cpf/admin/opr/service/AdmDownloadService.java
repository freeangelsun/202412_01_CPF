package com.cpf.admin.opr.service;

import com.cpf.data.persistence.api.annotation.CpfTransactional;
import com.cpf.admin.opr.dto.DownloadAuditLog;
import com.cpf.admin.opr.dto.DownloadPolicy;
import com.cpf.admin.opr.dto.DownloadRequest;
import com.cpf.admin.opr.dto.DownloadResult;
import com.cpf.batch.api.CpfBatchOperationsPort;
import com.cpf.core.api.error.CpfValidationException;
import com.cpf.security.api.CpfMasking;
import com.cpf.foundation.util.CpfStrings;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import com.cpf.foundation.annotation.CpfService;

import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * ADM 공통 다운로드 기능을 처리합니다.
 *
 * <p>다운로드는 운영 감사 대상이므로 조회 SQL, 민감정보 마스킹, 감사 사유, 감사 로그 저장을
 * 한 곳에서 관리합니다. 화면별 개별 다운로드 구현을 허용하면 권한과 마스킹 정책이 쉽게 갈라지므로
 * ADM 화면은 이 서비스를 통해 CSV 파일을 내려받습니다.</p>
 */
@CpfService
public class AdmDownloadService extends com.cpf.admin.common.base.AdmBaseService {
    private static final int DEFAULT_LIMIT = 1000;
    private static final int MAX_LIMIT = 10000;
    private static final DateTimeFormatter FILE_TIME = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final JdbcTemplate cpfJdbcTemplate;
    private final CpfBatchOperationsPort batchOperations;
    private final JdbcTemplate admJdbcTemplate;
    private final AdmAuditLogService auditLogService;

    public AdmDownloadService(
            @Qualifier("cpfJdbcTemplate") JdbcTemplate cpfJdbcTemplate,
            CpfBatchOperationsPort batchOperations,
            @Qualifier("admJdbcTemplate") JdbcTemplate admJdbcTemplate,
            AdmAuditLogService auditLogService) {
        this.cpfJdbcTemplate = cpfJdbcTemplate;
        this.batchOperations = batchOperations;
        this.admJdbcTemplate = admJdbcTemplate;
        this.auditLogService = auditLogService;
    }

    @CpfTransactional(transactionManager="admTransactionManager")
    public DownloadResult downloadCsv(DownloadRequest request, String operatorId, String clientIp, String userAgent) {
        DownloadPolicy policy = resolvePolicy(request.downloadType());
        String reason = auditLogService.requireReason(request.reason());
        String adminId = value(operatorId, value(request.requestUser(), "UNKNOWN"));
        String searchSummary = summarize(request);
        boolean includeSensitiveRequested = Boolean.TRUE.equals(request.includeSensitive());
        boolean includeSensitiveAllowed = policy.includeSensitiveAllowed() && includeSensitiveRequested;
        boolean mask = !includeSensitiveAllowed;

        Long downloadId = null;
        try {
            List<? extends Map<String, Object>> rows = queryRows(policy.downloadType(), request);
            String csv = toCsv(rows, mask);
            String fileName = fileName(policy.downloadType());
            downloadId = recordDownloadAudit(
                    adminId,
                    policy,
                    request,
                    searchSummary,
                    rows.size(),
                    mask,
                    includeSensitiveRequested,
                    reason,
                    clientIp,
                    userAgent,
                    "SUCCESS",
                    null,
                    fileName);
            auditLogService.record(
                    null,
                    adminId,
                    "DOWNLOAD_CSV",
                    policy.downloadType(),
                    String.valueOf(downloadId),
                    reason,
                    null,
                    "rowCount=" + rows.size() + ", masked=" + mask,
                    "DOWNLOAD_CSV",
                    clientIp);
            return new DownloadResult(
                    downloadId,
                    fileName,
                    "text/csv;charset=UTF-8",
                    csv.getBytes(StandardCharsets.UTF_8),
                    rows.size(),
                    mask ? "Y" : "N");
        } catch (RuntimeException ex) {
            recordDownloadAudit(
                    adminId,
                    policy,
                    request,
                    searchSummary,
                    0,
                    true,
                    includeSensitiveRequested,
                    reason,
                    clientIp,
                    userAgent,
                    "FAILED",
                    CpfMasking.mask(ex.getMessage(), 1000),
                    null);
            throw ex;
        }
    }

    public List<DownloadAuditLog> findDownloadAuditLogs(String downloadType, String adminId, int limit) {
        StringBuilder sql = new StringBuilder("""
                SELECT DOWNLOAD_ID, ADMIN_ID, DOWNLOAD_TYPE, ROW_COUNT, MASKED_YN, INCLUDE_SENSITIVE_YN,
                       REASON, STATUS, REQUESTED_AT, COMPLETED_AT
                FROM ADM_DOWNLOAD_AUDIT_LOG
                WHERE 1 = 1
                """);
        List<Object> args = new ArrayList<>();
        if (CpfStrings.hasText(downloadType)) {
            sql.append(" AND DOWNLOAD_TYPE = ?");
            args.add(downloadType.trim());
        }
        if (CpfStrings.hasText(adminId)) {
            sql.append(" AND ADMIN_ID = ?");
            args.add(adminId.trim());
        }
        sql.append(" ORDER BY DOWNLOAD_ID DESC");

        return AdmJdbcQueries.query(
                admJdbcTemplate,
                sql.toString(),
                args,
                limit(limit),
                (rs, rowNum) -> new DownloadAuditLog(
                        rs.getLong("DOWNLOAD_ID"),
                        rs.getString("ADMIN_ID"),
                        rs.getString("DOWNLOAD_TYPE"),
                        rs.getInt("ROW_COUNT"),
                        rs.getString("MASKED_YN"),
                        rs.getString("INCLUDE_SENSITIVE_YN"),
                        rs.getString("REASON"),
                        rs.getString("STATUS"),
                        rs.getObject("REQUESTED_AT", LocalDateTime.class),
                        rs.getObject("COMPLETED_AT", LocalDateTime.class)));
    }

    public List<DownloadPolicy> findPolicies() {
        return List.of(
                new DownloadPolicy("TRANSACTION_LOGS", "LOG_LIST", "온라인 거래 로그", false),
                new DownloadPolicy("ERROR_LOGS", "LOG_LIST", "오류 거래 로그", false),
                new DownloadPolicy("BATCH_EXECUTIONS", "BATCH", "배치 실행 이력", false),
                new DownloadPolicy("NOTIFICATION_DELIVERY_LOGS", "NOTIFICATION", "알림 발송 이력", false)
        );
    }

    private DownloadPolicy resolvePolicy(String downloadType) {
        String normalized = value(downloadType, "").trim().toUpperCase(Locale.ROOT);
        return findPolicies().stream()
                .filter(policy -> policy.downloadType().equals(normalized))
                .findFirst()
                .orElseThrow(() -> new CpfValidationException("지원하지 않는 다운로드 유형입니다."));
    }

    private List<? extends Map<String, Object>> queryRows(String downloadType, DownloadRequest request) {
        if ("BATCH_EXECUTIONS".equals(downloadType)) {
            return batchOperations.findExecutions(
                    request.jobId(),
                    request.transactionId(),
                    null,
                    null,
                    null,
                    request.fromDate(),
                    request.toDate(),
                    limit(request.limit()));
        }
        QuerySpec querySpec = buildQuery(downloadType, request);
        return AdmJdbcQueries.queryForList(
                cpfJdbcTemplate,
                querySpec.sql(),
                querySpec.args(),
                limit(request.limit()));
    }

    private QuerySpec buildQuery(String downloadType, DownloadRequest request) {
        return switch (downloadType) {
            case "TRANSACTION_LOGS" -> transactionLogQuery(request, false);
            case "ERROR_LOGS" -> transactionLogQuery(request, true);
            case "NOTIFICATION_DELIVERY_LOGS" -> notificationDeliveryLogQuery(request);
            default -> throw new CpfValidationException("지원하지 않는 다운로드 유형입니다.");
        };
    }

    private QuerySpec transactionLogQuery(DownloadRequest request, boolean errorOnly) {
        StringBuilder sql = new StringBuilder("""
                SELECT LOG_IDX, TRANSACTION_ID, TRACE_ID, MODULE_ID, WAS_ID, INSTANCE_ID,
                       HOST_NAME, PROCESS_ID, THREAD_NAME, BUSINESS_TRANSACTION_ID,
                       LOG_TYPE, REQUEST_TYPE, ORIGINAL_CHANNEL, CURRENT_CHANNEL, CALLER_CHANNEL,
                       TARGET_CHANNEL, MEMBER_NO, CUSTOMER_NO, HTTP_METHOD,
                       URI, HTTP_STATUS, RESPONSE_CODE, ERROR_CODE, START_TIME, END_TIME, DURATION_MS
                FROM CPF_TRANSACTION_LOG
                WHERE 1 = 1
                """);
        List<Object> args = new ArrayList<>();
        appendLike(sql, args, "TRANSACTION_ID", request.transactionId());
        appendLike(sql, args, "TRACE_ID", request.traceId());
        appendDateRange(sql, args, "START_TIME", request.fromDate(), request.toDate());
        if (errorOnly) {
            sql.append(" AND (ERROR_CODE IS NOT NULL OR HTTP_STATUS >= 400 OR LOG_TYPE = 'ERROR')");
        }
        sql.append(" ORDER BY LOG_IDX DESC");
        return new QuerySpec(sql.toString(), args);
    }

    private QuerySpec notificationDeliveryLogQuery(DownloadRequest request) {
        StringBuilder sql = new StringBuilder("""
                SELECT DELIVERY_ID, RULE_ID, EVENT_TYPE, TARGET_TYPE, TARGET_ID, RECEIVER,
                       DELIVERY_STATUS, DELIVERY_MESSAGE, REQUESTED_AT, DELIVERED_AT, CREATED_AT
                FROM CPF_NOTIFICATION_DELIVERY_LOG
                WHERE 1 = 1
                """);
        List<Object> args = new ArrayList<>();
        appendDateRange(sql, args, "CREATED_AT", request.fromDate(), request.toDate());
        sql.append(" ORDER BY DELIVERY_ID DESC");
        return new QuerySpec(sql.toString(), args);
    }

    private Long recordDownloadAudit(
            String adminId,
            DownloadPolicy policy,
            DownloadRequest request,
            String searchSummary,
            int rowCount,
            boolean mask,
            boolean includeSensitiveRequested,
            String reason,
            String clientIp,
            String userAgent,
            String status,
            String failureReason,
            String fileName) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        admJdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO ADM_DOWNLOAD_AUDIT_LOG (
                        ADMIN_ID, MENU_ID, SCREEN_ID, DOWNLOAD_TYPE, TARGET_TYPE,
                        SEARCH_CONDITION_SUMMARY, ROW_COUNT, MASKED_YN, INCLUDE_SENSITIVE_YN,
                        REASON, CLIENT_IP, USER_AGENT, CSV_POLICY_VERSION, REQUESTED_AT, COMPLETED_AT, STATUS,
                        FAILURE_REASON, FILE_NAME, CREATED_BY, UPDATED_BY
                    )
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, ?, ?, ?, ?, ?)
                    """, new String[] {"download_id"});
            ps.setString(1, adminId);
            ps.setString(2, policy.menuId());
            ps.setString(3, value(request.targetType(), policy.downloadType()));
            ps.setString(4, policy.downloadType());
            ps.setString(5, value(request.targetType(), policy.downloadType()));
            ps.setString(6, searchSummary);
            ps.setInt(7, rowCount);
            ps.setString(8, mask ? "Y" : "N");
            ps.setString(9, includeSensitiveRequested ? "Y" : "N");
            ps.setString(10, reason);
            ps.setString(11, clientIp);
            ps.setString(12, CpfMasking.truncate(value(userAgent, ""), 500));
            ps.setString(13, AdmCsvSanitizer.POLICY_VERSION);
            ps.setString(14, status);
            ps.setString(15, failureReason);
            ps.setString(16, fileName);
            ps.setString(17, adminId);
            ps.setString(18, adminId);
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key == null ? null : key.longValue();
    }

    private String toCsv(List<? extends Map<String, Object>> rows, boolean mask) {
        if (rows.isEmpty()) {
            return "\uFEFF";
        }
        List<String> headers = new ArrayList<>(rows.get(0).keySet());
        StringBuilder csv = new StringBuilder("\uFEFF");
        csv.append(headers.stream().map(AdmCsvSanitizer::header).reduce((a, b) -> a + "," + b).orElse("")).append("\r\n");
        for (Map<String, Object> row : rows) {
            List<String> values = new ArrayList<>();
            for (String header : headers) {
                values.add(AdmCsvSanitizer.cell(row.get(header), mask));
            }
            csv.append(String.join(",", values)).append("\r\n");
        }
        return csv.toString();
    }


    private String fileName(String downloadType) {
        return "cpf_" + downloadType.toLowerCase(Locale.ROOT) + "_" + FILE_TIME.format(LocalDateTime.now()) + ".csv";
    }

    private String summarize(DownloadRequest request) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("downloadType", request.downloadType());
        summary.put("targetType", request.targetType());
        summary.put("fromDate", request.fromDate());
        summary.put("toDate", request.toDate());
        summary.put("transactionId", request.transactionId());
        summary.put("traceId", request.traceId());
        summary.put("jobId", request.jobId());
        summary.put("limit", limit(request.limit()));
        return summary.toString();
    }

    private void appendLike(StringBuilder sql, List<Object> args, String column, String value) {
        if (CpfStrings.hasText(value)) {
            sql.append(" AND ").append(column).append(" LIKE ?");
            args.add("%" + value.trim() + "%");
        }
    }

    private void appendDateRange(StringBuilder sql, List<Object> args, String column, String fromDate, String toDate) {
        if (CpfStrings.hasText(fromDate)) {
            sql.append(" AND ").append(column).append(" >= ?");
            args.add(fromDate.trim());
        }
        if (CpfStrings.hasText(toDate)) {
            sql.append(" AND ").append(column).append(" <= ?");
            args.add(toDate.trim());
        }
    }

    private int limit(Integer limit) {
        if (limit == null) {
            return DEFAULT_LIMIT;
        }
        return Math.max(1, Math.min(limit, MAX_LIMIT));
    }

    private String value(String value, String fallback) {
        return CpfStrings.hasText(value) ? value.trim() : fallback;
    }

    private record QuerySpec(String sql, List<Object> args) {
    }
}
