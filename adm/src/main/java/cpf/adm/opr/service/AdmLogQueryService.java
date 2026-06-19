package cpf.adm.opr.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cpf.cmn.utils.TextUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * PFW 거래 로그를 ADM 운영 화면에 맞게 조회하고 포맷합니다.
 *
 * <p>운영자는 JSON 원문과 고정길이 전문을 같은 화면에서 봐야 하므로, 상세 로그 조회 시 JSON pretty print,
 * 민감정보 마스킹, 고정길이 필드 분해 결과를 함께 반환합니다.</p>
 */
@Service
public class AdmLogQueryService {
    private final JdbcTemplate pfwJdbcTemplate;
    private final ObjectMapper objectMapper;

    public AdmLogQueryService(@Qualifier("pfwJdbcTemplate") JdbcTemplate pfwJdbcTemplate, ObjectMapper objectMapper) {
        this.pfwJdbcTemplate = pfwJdbcTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 거래 로그 목록을 조건별로 검색합니다.
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
            String channelCode,
            String logType,
            int limit) {

        StringBuilder sql = new StringBuilder("""
                SELECT
                    LOG_IDX,
                    TRANSACTION_ID,
                    TRACE_ID,
                    MODULE_ID,
                    WAS_ID,
                    SERVER_INSTANCE_ID,
                    HOST_NAME,
                    PROCESS_ID,
                    THREAD_NAME,
                    BUSINESS_TRANSACTION_ID,
                    BUSINESS_TRANSACTION_NAME,
                    LOG_TYPE,
                    REQUEST_TYPE,
                    ORIGINAL_CHANNEL_CODE,
                    CHANNEL_CODE,
                    MEMBER_NO,
                    CUSTOMER_NO,
                    HTTP_METHOD,
                    URI,
                    HTTP_STATUS,
                    RESPONSE_CODE,
                    ERROR_CODE,
                    EXEC_USER,
                    START_TIME,
                    END_TIME,
                    DURATION_MS
                FROM pfw_transaction_log
                WHERE 1 = 1
                """);
        List<Object> args = new ArrayList<>();
        appendLike(sql, args, "TRANSACTION_ID", transactionId);
        appendLike(sql, args, "TRACE_ID", traceId);
        appendLike(sql, args, "BUSINESS_TRANSACTION_ID", businessTransactionId);
        appendEquals(sql, args, "MEMBER_NO", memberNo);
        appendEquals(sql, args, "CUSTOMER_NO", customerNo);
        appendLike(sql, args, "URI", uri);
        appendEquals(sql, args, "RESPONSE_CODE", responseCode);
        if (httpStatus != null) {
            sql.append(" AND HTTP_STATUS = ?");
            args.add(httpStatus);
        }
        appendEquals(sql, args, "CHANNEL_CODE", channelCode);
        appendEquals(sql, args, "LOG_TYPE", logType);
        sql.append(" ORDER BY LOG_IDX DESC LIMIT ?");
        args.add(Math.max(1, Math.min(limit, 500)));

        return pfwJdbcTemplate.queryForList(sql.toString(), args.toArray());
    }

    /**
     * 거래 로그 상세와 포맷된 상세 값을 조회합니다.
     */
    public Map<String, Object> getLogDetail(Long logIdx) {
        Map<String, Object> response = new LinkedHashMap<>();
        Map<String, Object> summary = pfwJdbcTemplate.queryForMap(
                "SELECT * FROM pfw_transaction_log WHERE LOG_IDX = ?",
                logIdx);
        List<Map<String, Object>> details = pfwJdbcTemplate.queryForList(
                "SELECT DETAIL_KEY, DETAIL_VALUE, CREATED_AT FROM pfw_transaction_log_detail WHERE LOG_IDX = ? ORDER BY DETAIL_KEY",
                logIdx);

        response.put("summary", summary);
        response.put("headers", formatValue("headers", value(findDetail(details, "headers"))));
        response.put("request", formatValue("request", value(summary.get("REQUEST_BODY"))));
        response.put("response", formatValue("response", value(summary.get("RESPONSE"))));
        response.put("error", formatValue("error", value(summary.get("ERROR_MESSAGE"))));
        response.put("details", details);
        response.put("formattedDetails", details.stream()
                .map(row -> formatValue(value(row.get("DETAIL_KEY")), value(row.get("DETAIL_VALUE"))))
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

    private Map<String, Object> formatValue(String key, String value) {
        Map<String, Object> formatted = new LinkedHashMap<>();
        formatted.put("detailKey", key);
        formatted.put("raw", mask(value));
        if (!TextUtils.hasText(value)) {
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
            formatted.put("formatType", "FIXED_LENGTH");
            formatted.put("fields", parseDefaultFixedLength(mask(value)));
            return formatted;
        }
        formatted.put("formatType", "TEXT");
        formatted.put("pretty", mask(value));
        return formatted;
    }

    private boolean isJson(String value) {
        return (value.startsWith("{") && value.endsWith("}")) || (value.startsWith("[") && value.endsWith("]"));
    }

    private String prettyJson(String value) {
        try {
            Object parsed = objectMapper.readValue(value, Object.class);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(parsed);
        } catch (JsonProcessingException ex) {
            return mask(value);
        }
    }

    private boolean isFixedLengthKey(String key) {
        String normalized = key == null ? "" : key.toLowerCase();
        return normalized.contains("fixed") || normalized.contains("telegram") || normalized.contains("전문");
    }

    private List<Map<String, Object>> parseDefaultFixedLength(String value) {
        List<FieldSpec> specs = List.of(
                new FieldSpec("memberNo", "회원번호", 10),
                new FieldSpec("memberName", "회원명", 20),
                new FieldSpec("amount", "금액", 12),
                new FieldSpec("activeYn", "활성 여부", 1),
                new FieldSpec("baseDate", "기준일자", 8));
        List<Map<String, Object>> fields = new ArrayList<>();
        int offset = 0;
        for (FieldSpec spec : specs) {
            int end = Math.min(value.length(), offset + spec.length());
            String raw = offset >= value.length() ? "" : value.substring(offset, end);
            fields.add(Map.of(
                    "name", spec.name(),
                    "koreanName", spec.koreanName(),
                    "start", offset + 1,
                    "length", spec.length(),
                    "value", raw.trim()));
            offset += spec.length();
        }
        if (value.length() > offset) {
            fields.add(Map.of(
                    "name", "__remaining",
                    "koreanName", "여분 데이터",
                    "start", offset + 1,
                    "length", value.length() - offset,
                    "value", value.substring(offset)));
        }
        return fields;
    }

    private void appendLike(StringBuilder sql, List<Object> args, String column, String value) {
        if (TextUtils.hasText(value)) {
            sql.append(" AND ").append(column).append(" LIKE CONCAT('%', ?, '%')");
            args.add(value.trim());
        }
    }

    private void appendEquals(StringBuilder sql, List<Object> args, String column, String value) {
        if (TextUtils.hasText(value)) {
            sql.append(" AND ").append(column).append(" = ?");
            args.add(value.trim());
        }
    }

    private String mask(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replaceAll("(?i)(password|passwd|pwd|secret|token)(\"?\\s*[:=]\\s*\"?)[^,\"}\\s]+", "$1$2****")
                .replaceAll("([0-9]{3})-?([0-9]{3,4})-?([0-9]{4})", "$1-****-$3")
                .replaceAll("([A-Za-z0-9._%+-]{2})[A-Za-z0-9._%+-]*(@[A-Za-z0-9.-]+)", "$1****$2");
    }

    private String value(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private record FieldSpec(String name, String koreanName, int length) {
    }
}
