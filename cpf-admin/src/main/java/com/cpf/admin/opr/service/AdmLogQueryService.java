package com.cpf.admin.opr.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.cpf.integration.fixedlength.api.CpfFixedLengthLayoutRegistry;
import com.cpf.integration.fixedlength.api.CpfFixedLengthLogDecoder;
import com.cpf.integration.fixedlength.api.CpfFixedLengthLogView;
import com.cpf.integration.fixedlength.api.CpfFixedLengthParser;
import com.cpf.foundation.util.CpfStrings;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import com.cpf.foundation.annotation.CpfService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * CPF 거래 로그를 ADM 운영 화면에 맞게 조회하고 포맷합니다.
 *
 * <p>운영자는 JSON 원문과 고정길이 전문을 같은 화면에서 봐야 하므로, 상세 로그 조회 시 JSON pretty print,
 * 민감정보 마스킹, 고정길이 필드 분해 결과를 함께 반환합니다.</p>
 */
@CpfService
public class AdmLogQueryService extends com.cpf.admin.common.base.AdmBaseService {
    private final JdbcTemplate cpfJdbcTemplate;
    private final ObjectMapper objectMapper;
    private final CpfFixedLengthLogDecoder fixedLengthDecoder;

    /** 기존 단위테스트/Library 조립 호환 생성자입니다. Layout decoder 없이 raw-only로 동작합니다. */
    public AdmLogQueryService(@Qualifier("cpfJdbcTemplate") JdbcTemplate cpfJdbcTemplate, ObjectMapper objectMapper) {
        this(cpfJdbcTemplate, objectMapper, null);
    }

    private AdmLogQueryService(JdbcTemplate cpfJdbcTemplate, ObjectMapper objectMapper, CpfFixedLengthLogDecoder decoder) {
        this.cpfJdbcTemplate = cpfJdbcTemplate;
        this.objectMapper = objectMapper;
        this.fixedLengthDecoder = decoder;
    }

    @Autowired
    public AdmLogQueryService(
            @Qualifier("cpfJdbcTemplate") JdbcTemplate cpfJdbcTemplate,
            ObjectMapper objectMapper,
            ObjectProvider<CpfFixedLengthParser> parserProvider,
            ObjectProvider<CpfFixedLengthLayoutRegistry> registryProvider) {
        this(cpfJdbcTemplate, objectMapper, createDecoder(parserProvider.getIfAvailable(), registryProvider.getIfAvailable()));
    }

    private static CpfFixedLengthLogDecoder createDecoder(CpfFixedLengthParser parser, CpfFixedLengthLayoutRegistry registry) {
        return parser == null || registry == null ? null : new CpfFixedLengthLogDecoder(parser, registry);
    }

    /**
     * 거래 로그 목록을 keyset cursor로 검색합니다.
     *
     * <p>LOG_IDX DESC 정렬과 beforeLogIdx cursor를 사용해 DB vendor별 OFFSET 문법에 의존하지 않으며,
     * 다음 페이지 존재 여부를 확인하기 위해 pageSize + 1건만 읽습니다. 전체 건수는 같은 필터의 COUNT로
     * 계산하므로 Frontend가 최초 limit 결과를 다시 slice하는 False Paging을 만들지 않습니다.</p>
     */
    public LogPage findLogPage(
            String transactionId,
            String traceId,
            String businessTransactionId,
            String memberNo,
            String customerNo,
            String uri,
            String responseCode,
            Integer httpStatus,
            String clientId,
            String originalSystemCode,
            String systemCode,
            String callerSystemCode,
            String targetSystemCode,
            String originalChannel,
            String currentChannel,
            String callerChannel,
            String targetChannel,
            String targetOperationId,
            String logType,
            String moduleId,
            String wasId,
            String instanceId,
            String hostName,
            String domainCode,
            String application,
            String starterId,
            String capabilityId,
            String provider,
            String capabilityOperation,
            Long beforeLogIdx,
            int pageSize) {

        int boundedPageSize = Math.max(1, Math.min(pageSize, 100));
        QueryFilter filter = buildFilter(
                transactionId, traceId, businessTransactionId, memberNo, customerNo,
                uri, responseCode, httpStatus, clientId,
                originalSystemCode, systemCode, callerSystemCode, targetSystemCode,
                originalChannel, currentChannel, callerChannel, targetChannel, targetOperationId,
                logType, moduleId, wasId, instanceId, hostName,
                domainCode, application, starterId, capabilityId, provider, capabilityOperation);

        StringBuilder sql = new StringBuilder("""
                SELECT
                    LOG_IDX,
                    TRANSACTION_ID,
                    TRACE_ID,
                    MODULE_ID,
                    WAS_ID,
                    INSTANCE_ID,
                    HOST_NAME,
                    HOST_IP,
                    PROCESS_ID,
                    THREAD_NAME,
                    BUSINESS_TRANSACTION_ID,
                    BUSINESS_TRANSACTION_NAME,
                    LOG_TYPE,
                    REQUEST_TYPE,
                    CLIENT_ID,
                    CLIENT_VERSION,
                    CALLER_INSTANCE_ID,
                    ORIGINAL_SYSTEM_CODE,
                    SYSTEM_CODE,
                    CALLER_SYSTEM_CODE,
                    TARGET_SYSTEM_CODE,
                    ORIGINAL_CHANNEL,
                    CURRENT_CHANNEL,
                    CALLER_CHANNEL,
                    TARGET_CHANNEL,
                    TARGET_OPERATION_ID,
                    MEMBER_NO,
                    CUSTOMER_NO,
                    HTTP_METHOD,
                    URI,
                    HTTP_STATUS,
                    RESPONSE_CODE,
                    ERROR_CODE,
                    EXEC_USER,
                    DEVICE_ID,
                    CLIENT_IP,
                    USER_AGENT,
                    LOCALE,
                    START_TIME,
                    END_TIME,
                    DURATION_MS
                FROM cpf_transaction_log l
                WHERE 1 = 1
                """);
        sql.append(filter.sql());
        List<Object> args = new ArrayList<>(filter.args());
        if (beforeLogIdx != null && beforeLogIdx > 0) {
            sql.append(" AND LOG_IDX < ?");
            args.add(beforeLogIdx);
        }
        sql.append(" ORDER BY LOG_IDX DESC");

        List<Map<String, Object>> fetched = AdmJdbcQueries.queryForList(
                        cpfJdbcTemplate,
                        sql.toString(),
                        args,
                        boundedPageSize + 1).stream()
                .map(AdmLogSanitizer::sanitizeMap)
                .toList();
        boolean hasMore = fetched.size() > boundedPageSize;
        List<Map<String, Object>> items = hasMore ? fetched.subList(0, boundedPageSize) : fetched;
        Long nextCursor = null;
        if (hasMore && !items.isEmpty()) {
            Object value = items.get(items.size() - 1).get("LOG_IDX");
            if (value instanceof Number number) nextCursor = number.longValue();
            else if (value != null) nextCursor = Long.valueOf(String.valueOf(value));
        }

        long total = queryTotal(filter);
        return new LogPage(items, total, boundedPageSize, beforeLogIdx, nextCursor, hasMore);
    }

    /**
     * 기존 내부 호출 호환용 목록 API입니다. 신규 ADM Consumer는 {@link #findLogPage}를 사용합니다.
     */
    public List<Map<String, Object>> findLogs(
            String transactionId,
            String traceId,
            String businessTransactionId,
            String memberNo,
            String customerNo,
            String uri,
            String responseCode,
            Integer httpStatus,
            String clientId,
            String originalSystemCode,
            String systemCode,
            String callerSystemCode,
            String targetSystemCode,
            String originalChannel,
            String currentChannel,
            String callerChannel,
            String targetChannel,
            String targetOperationId,
            String logType,
            String moduleId,
            String wasId,
            String instanceId,
            String hostName,
            String domainCode,
            String application,
            String starterId,
            String capabilityId,
            String provider,
            String capabilityOperation,
            int limit) {
        return findLogPage(
                transactionId, traceId, businessTransactionId, memberNo, customerNo,
                uri, responseCode, httpStatus, clientId,
                originalSystemCode, systemCode, callerSystemCode, targetSystemCode,
                originalChannel, currentChannel, callerChannel, targetChannel, targetOperationId,
                logType, moduleId, wasId, instanceId, hostName,
                domainCode, application, starterId, capabilityId, provider, capabilityOperation,
                null, limit).items();
    }

    private QueryFilter buildFilter(
            String transactionId,
            String traceId,
            String businessTransactionId,
            String memberNo,
            String customerNo,
            String uri,
            String responseCode,
            Integer httpStatus,
            String clientId,
            String originalSystemCode,
            String systemCode,
            String callerSystemCode,
            String targetSystemCode,
            String originalChannel,
            String currentChannel,
            String callerChannel,
            String targetChannel,
            String targetOperationId,
            String logType,
            String moduleId,
            String wasId,
            String instanceId,
            String hostName,
            String domainCode,
            String application,
            String starterId,
            String capabilityId,
            String provider,
            String capabilityOperation) {
        StringBuilder where = new StringBuilder();
        List<Object> args = new ArrayList<>();
        appendLike(where, args, "TRANSACTION_ID", transactionId);
        appendLike(where, args, "TRACE_ID", traceId);
        appendLike(where, args, "BUSINESS_TRANSACTION_ID", businessTransactionId);
        appendEquals(where, args, "MEMBER_NO", memberNo);
        appendEquals(where, args, "CUSTOMER_NO", customerNo);
        appendLike(where, args, "URI", uri);
        appendEquals(where, args, "RESPONSE_CODE", responseCode);
        if (httpStatus != null) {
            where.append(" AND HTTP_STATUS = ?");
            args.add(httpStatus);
        }
        appendLike(where, args, "CLIENT_ID", clientId);
        appendEquals(where, args, "ORIGINAL_SYSTEM_CODE", originalSystemCode);
        appendEquals(where, args, "SYSTEM_CODE", systemCode);
        appendEquals(where, args, "CALLER_SYSTEM_CODE", callerSystemCode);
        appendEquals(where, args, "TARGET_SYSTEM_CODE", targetSystemCode);
        appendEquals(where, args, "ORIGINAL_CHANNEL", originalChannel);
        appendEquals(where, args, "CURRENT_CHANNEL", currentChannel);
        appendEquals(where, args, "CALLER_CHANNEL", callerChannel);
        appendEquals(where, args, "TARGET_CHANNEL", targetChannel);
        appendLike(where, args, "TARGET_OPERATION_ID", targetOperationId);
        appendEquals(where, args, "LOG_TYPE", logType);
        appendEquals(where, args, "MODULE_ID", moduleId);
        appendEquals(where, args, "WAS_ID", wasId);
        appendEquals(where, args, "INSTANCE_ID", instanceId);
        appendEquals(where, args, "HOST_NAME", hostName);
        appendDetailLike(where, args, "runtime.domainCode", domainCode);
        appendDetailLike(where, args, "runtime.application", application);
        appendDetailLike(where, args, "capability.starters", starterId);
        appendDetailLike(where, args, "capability.ids", capabilityId);
        appendDetailLike(where, args, "capability.providers", provider);
        appendDetailLike(where, args, "capability.operations", capabilityOperation);
        return new QueryFilter(where.toString(), List.copyOf(args));
    }

    private long queryTotal(QueryFilter filter) {
        String sql = "SELECT COUNT(*) FROM cpf_transaction_log l WHERE 1 = 1" + filter.sql();
        Long total = cpfJdbcTemplate.queryForObject(sql, Long.class, filter.args().toArray());
        return total == null ? 0L : total;
    }

    public record LogPage(
            List<Map<String, Object>> items,
            long total,
            int pageSize,
            Long cursor,
            Long nextCursor,
            boolean hasMore) {
        public LogPage {
            items = items == null ? List.of() : List.copyOf(items);
        }
    }

    private record QueryFilter(String sql, List<Object> args) {
    }

    /**
     * 거래 로그 상세와 포맷된 상세 값을 조회합니다.
     */
    public Map<String, Object> getLogDetail(Long logIdx) {
        Map<String, Object> response = new LinkedHashMap<>();
        Map<String, Object> summary = cpfJdbcTemplate.queryForMap("""
                SELECT LOG_IDX, TRANSACTION_ID, TRACE_ID, MODULE_ID, WAS_ID, INSTANCE_ID, HOST_NAME, HOST_IP,
                       PROCESS_ID, THREAD_NAME, BUSINESS_TRANSACTION_ID, BUSINESS_TRANSACTION_NAME,
                       LOG_TYPE, REQUEST_TYPE, CLIENT_ID, CLIENT_VERSION, CALLER_INSTANCE_ID,
                       ORIGINAL_SYSTEM_CODE, SYSTEM_CODE, CALLER_SYSTEM_CODE, TARGET_SYSTEM_CODE,
                       ORIGINAL_CHANNEL, CURRENT_CHANNEL, CALLER_CHANNEL, TARGET_CHANNEL, TARGET_OPERATION_ID,
                       MEMBER_NO, CUSTOMER_NO, DEVICE_ID, CLIENT_IP, USER_AGENT, LOCALE,
                       HTTP_METHOD, URI, HTTP_STATUS, RESPONSE_CODE, ERROR_CODE, EXEC_USER,
                       START_TIME, END_TIME, DURATION_MS, REQUEST_BODY, RESPONSE, ERROR_MESSAGE
                  FROM cpf_transaction_log
                 WHERE LOG_IDX = ?
                """, logIdx);
        List<Map<String, Object>> details = cpfJdbcTemplate.queryForList(
                "SELECT DETAIL_KEY, DETAIL_VALUE, CREATED_AT FROM cpf_transaction_log_detail WHERE LOG_IDX = ? ORDER BY DETAIL_KEY",
                logIdx);

        Map<String, Object> safeSummary = new LinkedHashMap<>(summary);
        safeSummary.remove("REQUEST_BODY");
        safeSummary.remove("RESPONSE");
        safeSummary.remove("ERROR_MESSAGE");
        response.put("summary", AdmLogSanitizer.sanitizeMap(safeSummary));
        response.put("headers", formatValue("headers", value(findDetail(details, "headers")), details));
        response.put("inboundHeaders", formatValue("inboundHeaders", value(findDetail(details, "inboundHeaders")), details));
        response.put("resolvedHeaders", formatValue("resolvedHeaders", value(findDetail(details, "resolvedHeaders")), details));
        response.put("outboundHeaders", formatValue("outboundHeaders", value(findDetail(details, "outboundHeaders")), details));
        response.put("responseHeaders", formatValue("responseHeaders", value(findDetail(details, "responseHeaders")), details));
        response.put("request", formatValue("request", value(summary.get("REQUEST_BODY")), details));
        response.put("response", formatValue("response", value(summary.get("RESPONSE")), details));
        response.put("error", formatValue("error", value(summary.get("ERROR_MESSAGE")), details));
        Map<String, Object> managementContext = new LinkedHashMap<>();
        for (String key : List.of("runtime.systemCode", "runtime.domainCode", "runtime.application", "runtime.module",
                "runtime.instanceId", "runtime.instanceToken", "capability.starters", "capability.ids",
                "capability.providers", "capability.operations")) {
            String found = value(findDetail(details, key));
            if (CpfStrings.hasText(found)) managementContext.put(key, found);
        }
        response.put("managementContext", managementContext);
        response.put("formattedDetails", details.stream()
                .map(row -> formatValue(value(row.get("DETAIL_KEY")), value(row.get("DETAIL_VALUE")), details))
                .toList());
        return response;
    }

    private Object findDetail(List<Map<String, Object>> details, String detailKey) {
        return details.stream()
                .filter(row -> detailKey.equalsIgnoreCase(value(row.get("DETAIL_KEY"))))
                .map(row -> row.get("DETAIL_VALUE"))
                .findFirst()
                .orElse("");
    }

    private Map<String, Object> formatValue(String key, String value, List<Map<String,Object>> details) {
        Map<String, Object> formatted = new LinkedHashMap<>();
        formatted.put("detailKey", key);
        formatted.put("raw", mask(value));
        if (!CpfStrings.hasText(value)) {
            formatted.put("formatType", "EMPTY");
            return formatted;
        }
        String trimmed = value.trim();
        if (isJson(trimmed)) {
            formatted.put("formatType", "JSON");
            formatted.put("pretty", prettyJson(trimmed));
            return formatted;
        }
        if (isFixedLengthKey(key)) {
            String layoutId = firstDetail(details, key + ".layoutId", "fixedLengthLayoutId", "telegramLayoutId");
            String layoutVersion = firstDetail(details, key + ".layoutVersion", "fixedLengthLayoutVersion", "telegramLayoutVersion");
            if (fixedLengthDecoder != null && CpfStrings.hasText(layoutId) && CpfStrings.hasText(layoutVersion)) {
                try {
                    CpfFixedLengthLogView view = fixedLengthDecoder.decode(value, layoutId, layoutVersion);
                    formatted.put("formatType", "FIXED_LENGTH");
                    formatted.put("layoutResolved", true);
                    formatted.put("layoutId", view.layoutId());
                    formatted.put("layoutVersion", view.version());
                    formatted.put("byteLength", view.byteLength());
                    formatted.put("fields", AdmLogSanitizer.sanitizeStructure(view.fields(), "fields"));
                    formatted.put("groups", AdmLogSanitizer.sanitizeStructure(view.groups(), "groups"));
                    return formatted;
                } catch (RuntimeException ex) {
                    formatted.put("layoutError", mask(ex.getMessage()));
                }
            }
            formatted.put("formatType", "FIXED_LENGTH_RAW");
            formatted.put("pretty", mask(value));
            formatted.put("layoutResolved", false);
            formatted.put("message", "등록된 전문 Layout ID/version이 없거나 해석에 실패하여 임의 분해하지 않았습니다.");
            return formatted;
        }
        formatted.put("formatType", "TEXT");
        formatted.put("pretty", mask(value));
        return formatted;
    }


    private String firstDetail(List<Map<String,Object>> details, String... keys) {
        for (String key : keys) {
            String found = value(findDetail(details, key));
            if (CpfStrings.hasText(found)) return found.trim();
        }
        return null;
    }

    private boolean isJson(String value) {
        return (value.startsWith("{") && value.endsWith("}")) || (value.startsWith("[") && value.endsWith("]"));
    }

    private String prettyJson(String value) {
        return AdmLogSanitizer.sanitizeJson(objectMapper, value);
    }

    private boolean isFixedLengthKey(String key) {
        String normalized = key == null ? "" : key.toLowerCase(java.util.Locale.ROOT);
        return normalized.contains("fixed") || normalized.contains("telegram") || normalized.contains("전문");
    }

    private void appendDetailLike(StringBuilder sql, List<Object> args, String detailKey, String value) {
        if (CpfStrings.hasText(value)) {
            sql.append(" AND EXISTS (SELECT 1 FROM cpf_transaction_log_detail d WHERE d.LOG_IDX = l.LOG_IDX AND d.DETAIL_KEY = ? AND d.DETAIL_VALUE LIKE ?)");
            args.add(detailKey);
            args.add("%" + value.trim() + "%");
        }
    }

    private void appendLike(StringBuilder sql, List<Object> args, String column, String value) {
        if (CpfStrings.hasText(value)) {
            sql.append(" AND ").append(column).append(" LIKE ?");
            args.add("%" + value.trim() + "%");
        }
    }

    private void appendEquals(StringBuilder sql, List<Object> args, String column, String value) {
        if (CpfStrings.hasText(value)) {
            sql.append(" AND ").append(column).append(" = ?");
            args.add(value.trim());
        }
    }

    private String mask(String value) {
        return AdmLogSanitizer.sanitizeText(value);
    }

    private String value(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

}
