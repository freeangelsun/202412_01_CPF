package cpf.cmn.biz.sequence;

import cpf.cmn.utils.TextUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

/**
 * CMN 공통 채번 서비스입니다.
 *
 * <p>채번 기준 행을 {@code SELECT ... FOR UPDATE}로 잠근 뒤 현재 값을 증가시키므로
 * 이중화 WAS 환경에서도 같은 채번 기준 안에서는 중복 번호가 발급되지 않습니다.
 * 업무 처리 실패 후 이미 발급된 번호는 감사 추적을 위해 유지하며, 번호 gap은 허용하는 정책을 기본으로 둡니다.</p>
 */
@Service
public class CmnSequenceService {
    private static final String DEFAULT_REQUEST_USER = "CMN";
    private static final String DEFAULT_SEQUENCE_KIND = "DEFAULT";
    private static final String DEFAULT_CHANNEL_CODE = "ALL";
    private static final String DEFAULT_TIMEZONE = "Asia/Seoul";

    private final ObjectProvider<JdbcTemplate> jdbcTemplateProvider;
    private final ObjectProvider<TransactionTemplate> transactionTemplateProvider;
    private final Clock clock;

    @Autowired
    public CmnSequenceService(
            @Qualifier("cmnBusinessJdbcTemplate") ObjectProvider<JdbcTemplate> jdbcTemplateProvider,
            @Qualifier("cmnBusinessTransactionTemplate") ObjectProvider<TransactionTemplate> transactionTemplateProvider) {
        this(jdbcTemplateProvider, transactionTemplateProvider, Clock.systemDefaultZone());
    }

    CmnSequenceService(ObjectProvider<JdbcTemplate> jdbcTemplateProvider,
                       ObjectProvider<TransactionTemplate> transactionTemplateProvider,
                       Clock clock) {
        this.jdbcTemplateProvider = jdbcTemplateProvider;
        this.transactionTemplateProvider = transactionTemplateProvider;
        this.clock = clock;
    }

    /**
     * CMN 업무 공통 DB가 활성화되어 있는지 반환합니다.
     */
    public boolean isEnabled() {
        return jdbcTemplateProvider.getIfAvailable() != null
                && transactionTemplateProvider.getIfAvailable() != null;
    }

    /**
     * 채번 기준을 잠그고 신규 번호를 발급합니다.
     */
    public CmnSequenceIssueResult issue(CmnSequenceIssueRequest request) {
        JdbcTemplate jdbcTemplate = requireJdbcTemplate();
        TransactionTemplate transactionTemplate = requireTransactionTemplate();
        String requestUser = TextUtils.defaultIfBlank(request.requestUser(), DEFAULT_REQUEST_USER);

        CmnSequenceIssueResult result = transactionTemplate.execute(status -> {
            Map<String, Object> row = findSequenceForUpdate(jdbcTemplate, request);
            SequenceRule rule = SequenceRule.from(row);
            String resetKey = resetKey(rule);
            long baseValue = rule.currentValue();
            String nextResetKey = rule.lastResetKey();

            if (shouldReset(rule.resetCycle(), rule.lastResetKey(), resetKey)) {
                baseValue = rule.startValue() - rule.incrementBy();
                nextResetKey = resetKey;
            }

            long issuedValue = baseValue + rule.incrementBy();
            validateRange(rule, issuedValue);

            jdbcTemplate.update("""
                    UPDATE cmn_sequence
                    SET current_value = ?,
                        last_reset_key = ?,
                        updated_by = ?,
                        updated_at = CURRENT_TIMESTAMP
                    WHERE sequence_key = ?
                    """, issuedValue, nextResetKey, requestUser, rule.sequenceKey());

            String dateKey = dateKey(rule.datePattern(), rule.resetTimezone());
            String issuedNo = formatIssuedNo(rule.prefix(), dateKey, issuedValue, rule.numberLength());

            if ("Y".equalsIgnoreCase(rule.logEnabledYn())) {
                jdbcTemplate.update("""
                        INSERT INTO cmn_sequence_issue_log (
                            sequence_key, business_area, business_key, sequence_kind, channel_code,
                            issued_no, issued_value, prefix, date_key,
                            request_channel, request_user, transaction_id, trace_id,
                            success_yn, retention_until, created_by, updated_by
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'Y',
                                  DATE_ADD(CURDATE(), INTERVAL ? DAY), ?, ?)
                        """,
                        rule.sequenceKey(),
                        rule.businessArea(),
                        rule.businessKey(),
                        rule.sequenceKind(),
                        rule.channelCode(),
                        issuedNo,
                        issuedValue,
                        rule.prefix(),
                        dateKey,
                        request.requestChannel(),
                        requestUser,
                        request.transactionId(),
                        request.traceId(),
                        rule.retentionDays(),
                        requestUser,
                        requestUser);
            }

            return new CmnSequenceIssueResult(
                    rule.sequenceKey(),
                    rule.businessArea(),
                    rule.businessKey(),
                    rule.sequenceKind(),
                    rule.channelCode(),
                    issuedNo,
                    issuedValue,
                    dateKey);
        });
        if (result == null) {
            throw new IllegalStateException("CMN 채번 트랜잭션 결과가 없습니다.");
        }
        return result;
    }

    static String formatIssuedNo(String prefix, String dateKey, long issuedValue, int numberLength) {
        String number = String.format(Locale.ROOT, "%0" + Math.max(numberLength, 1) + "d", issuedValue);
        return TextUtils.defaultIfBlank(prefix, "") + TextUtils.defaultIfBlank(dateKey, "") + number;
    }

    private Map<String, Object> findSequenceForUpdate(JdbcTemplate jdbcTemplate, CmnSequenceIssueRequest request) {
        if (TextUtils.hasText(request.sequenceKey())) {
            return jdbcTemplate.queryForMap("""
                    SELECT sequence_key, business_area, business_key, sequence_kind, channel_code,
                           prefix, date_pattern, current_value, start_value, increment_by,
                           min_value, max_value, number_length, reset_cycle, reset_pattern,
                           reset_timezone, last_reset_key, log_enabled_yn, retention_days
                    FROM cmn_sequence
                    WHERE sequence_key = ?
                      AND use_yn = 'Y'
                    FOR UPDATE
                    """, request.sequenceKey().trim());
        }

        String businessArea = TextUtils.requireText(request.businessArea(), "businessArea");
        String businessKey = TextUtils.requireText(request.businessKey(), "businessKey");
        String sequenceKind = TextUtils.defaultIfBlank(request.sequenceKind(), DEFAULT_SEQUENCE_KIND);
        String channelCode = TextUtils.defaultIfBlank(request.channelCode(), DEFAULT_CHANNEL_CODE);

        return jdbcTemplate.queryForMap("""
                SELECT sequence_key, business_area, business_key, sequence_kind, channel_code,
                       prefix, date_pattern, current_value, start_value, increment_by,
                       min_value, max_value, number_length, reset_cycle, reset_pattern,
                       reset_timezone, last_reset_key, log_enabled_yn, retention_days
                FROM cmn_sequence
                WHERE business_area = ?
                  AND business_key = ?
                  AND sequence_kind = ?
                  AND channel_code IN (?, ?)
                  AND use_yn = 'Y'
                ORDER BY CASE WHEN channel_code = ? THEN 0 ELSE 1 END
                LIMIT 1
                FOR UPDATE
                """, businessArea, businessKey, sequenceKind, channelCode, DEFAULT_CHANNEL_CODE, channelCode);
    }

    private JdbcTemplate requireJdbcTemplate() {
        JdbcTemplate jdbcTemplate = jdbcTemplateProvider.getIfAvailable();
        if (jdbcTemplate == null) {
            throw new IllegalStateException("CMN 업무 공통 DB가 비활성화되어 있습니다. cpf.cmn.business-db.enabled=true 설정을 확인하세요.");
        }
        return jdbcTemplate;
    }

    private TransactionTemplate requireTransactionTemplate() {
        TransactionTemplate transactionTemplate = transactionTemplateProvider.getIfAvailable();
        if (transactionTemplate == null) {
            throw new IllegalStateException("CMN 업무 공통 트랜잭션 설정이 없습니다.");
        }
        return transactionTemplate;
    }

    private String dateKey(String datePattern, String timezone) {
        if (!TextUtils.hasText(datePattern)) {
            return "";
        }
        return LocalDate.now(clock.withZone(zone(timezone))).format(DateTimeFormatter.ofPattern(datePattern));
    }

    private String resetKey(SequenceRule rule) {
        String cycle = TextUtils.defaultIfBlank(rule.resetCycle(), "NONE").toUpperCase(Locale.ROOT);
        String timezone = rule.resetTimezone();
        return switch (cycle) {
            case "DAY" -> LocalDate.now(clock.withZone(zone(timezone))).format(DateTimeFormatter.BASIC_ISO_DATE);
            case "MONTH" -> LocalDate.now(clock.withZone(zone(timezone))).format(DateTimeFormatter.ofPattern("yyyyMM"));
            case "YEAR" -> LocalDate.now(clock.withZone(zone(timezone))).format(DateTimeFormatter.ofPattern("yyyy"));
            case "PATTERN" -> dateKey(TextUtils.defaultIfBlank(rule.resetPattern(), rule.datePattern()), timezone);
            default -> "";
        };
    }

    private boolean shouldReset(String resetCycle, String lastResetKey, String resetKey) {
        return !"NONE".equalsIgnoreCase(TextUtils.defaultIfBlank(resetCycle, "NONE"))
                && TextUtils.hasText(resetKey)
                && !resetKey.equals(lastResetKey);
    }

    private ZoneId zone(String timezone) {
        try {
            return ZoneId.of(TextUtils.defaultIfBlank(timezone, DEFAULT_TIMEZONE));
        } catch (Exception ex) {
            return ZoneId.of(DEFAULT_TIMEZONE);
        }
    }

    private void validateRange(SequenceRule rule, long issuedValue) {
        if (issuedValue < rule.minValue() || issuedValue > rule.maxValue()) {
            throw new IllegalStateException("CMN 채번 허용 범위를 초과했습니다. sequenceKey=" + rule.sequenceKey());
        }
    }

    private static String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private static long longValue(Object value, long defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }

    private static int intValue(Object value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.parseInt(String.valueOf(value));
    }

    private record SequenceRule(
            String sequenceKey,
            String businessArea,
            String businessKey,
            String sequenceKind,
            String channelCode,
            String prefix,
            String datePattern,
            long currentValue,
            long startValue,
            int incrementBy,
            long minValue,
            long maxValue,
            int numberLength,
            String resetCycle,
            String resetPattern,
            String resetTimezone,
            String lastResetKey,
            String logEnabledYn,
            int retentionDays) {

        private static SequenceRule from(Map<String, Object> row) {
            long startValue = longValue(row.get("start_value"), 1L);
            return new SequenceRule(
                    stringValue(row.get("sequence_key")),
                    stringValue(row.get("business_area")),
                    stringValue(row.get("business_key")),
                    stringValue(row.get("sequence_kind")),
                    stringValue(row.get("channel_code")),
                    stringValue(row.get("prefix")),
                    stringValue(row.get("date_pattern")),
                    longValue(row.get("current_value"), startValue - 1L),
                    startValue,
                    intValue(row.get("increment_by"), 1),
                    longValue(row.get("min_value"), startValue),
                    longValue(row.get("max_value"), Long.MAX_VALUE),
                    intValue(row.get("number_length"), 8),
                    stringValue(row.get("reset_cycle")),
                    stringValue(row.get("reset_pattern")),
                    TextUtils.defaultIfBlank(stringValue(row.get("reset_timezone")), DEFAULT_TIMEZONE),
                    stringValue(row.get("last_reset_key")),
                    TextUtils.defaultIfBlank(stringValue(row.get("log_enabled_yn")), "Y"),
                    intValue(row.get("retention_days"), 365));
        }
    }
}
