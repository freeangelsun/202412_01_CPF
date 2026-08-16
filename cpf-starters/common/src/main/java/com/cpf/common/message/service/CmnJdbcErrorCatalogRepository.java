package com.cpf.common.message.service;

import com.cpf.common.message.api.CpfMessageRecord;
import com.cpf.common.message.api.CpfResponseCodeRecord;
import com.cpf.common.message.dto.CommonMessageRequest;
import com.cpf.common.message.dto.CommonResponseCodeRequest;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * CMN_RESPONSE_CODE / CMN_MESSAGE 전용 JDBC repository입니다.
 *
 * <p>Common Product Service는 MyBatis provider를 요구하지 않습니다. 공식 Data JDBC foundation만으로
 * Oracle/PostgreSQL/MariaDB에서 동일 SQL surface를 사용하며, catalog row의 기술 SQL 오류를 외부로 노출하지 않습니다.</p>
 */
@Repository
final class CmnJdbcErrorCatalogRepository implements CmnErrorCatalogRepository {
    private static final String RESPONSE_COLUMNS = "response_code,message_code,result_type,module_id,response_group,sequence_no," +
            "category,retry_disposition,exposure,effective_from,effective_to,catalog_version,description,use_yn,updated_at";
    private static final String MESSAGE_COLUMNS = "message_id,message_code,locale,message_format_type,external_message,internal_message," +
            "parameter_count,parameter_sample,parameter_schema_json,escape_html_yn,mask_arguments_yn,effective_from,effective_to," +
            "catalog_version,description,use_yn,updated_at";

    private final JdbcTemplate jdbc;

    CmnJdbcErrorCatalogRepository(@Qualifier("cpfCommonJdbcTemplate") JdbcTemplate cpfCommonJdbcTemplate) {
        this.jdbc = cpfCommonJdbcTemplate;
    }

    public CpfResponseCodeRecord findResponseCode(String code) {
        List<CpfResponseCodeRecord> rows = jdbc.query(
                "SELECT " + RESPONSE_COLUMNS + " FROM CMN_RESPONSE_CODE WHERE response_code=?",
                RESPONSE_MAPPER, normalizeCode(code));
        return rows.isEmpty() ? null : rows.get(0);
    }

    public CpfMessageRecord findMessage(String messageCode, String locale) {
        List<CpfMessageRecord> rows = jdbc.query(
                "SELECT " + MESSAGE_COLUMNS + " FROM CMN_MESSAGE WHERE message_code=? AND locale=?",
                MESSAGE_MAPPER, normalizeCode(messageCode), normalizeLocale(locale));
        return rows.isEmpty() ? null : rows.get(0);
    }

    public CpfMessageRecord findMessage(long id) {
        List<CpfMessageRecord> rows = jdbc.query(
                "SELECT " + MESSAGE_COLUMNS + " FROM CMN_MESSAGE WHERE message_id=?",
                MESSAGE_MAPPER, id);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public List<CpfResponseCodeRecord> searchResponseCodes(String query) {
        String q = normalizeSearch(query);
        if (q == null) {
            return jdbc.query("SELECT " + RESPONSE_COLUMNS + " FROM CMN_RESPONSE_CODE ORDER BY response_code", RESPONSE_MAPPER);
        }
        return jdbc.query("SELECT " + RESPONSE_COLUMNS + " FROM CMN_RESPONSE_CODE " +
                        "WHERE UPPER(response_code) LIKE ? OR UPPER(message_code) LIKE ? OR UPPER(description) LIKE ? ORDER BY response_code",
                RESPONSE_MAPPER, q, q, q);
    }

    public List<CpfMessageRecord> searchMessages(String query, String locale) {
        String q = normalizeSearch(query);
        String l = normalizeNullableLocale(locale);
        StringBuilder sql = new StringBuilder("SELECT ").append(MESSAGE_COLUMNS).append(" FROM CMN_MESSAGE WHERE 1=1");
        List<Object> args = new ArrayList<>();
        if (q != null) {
            sql.append(" AND (UPPER(message_code) LIKE ? OR UPPER(external_message) LIKE ? OR UPPER(description) LIKE ?)");
            args.add(q); args.add(q); args.add(q);
        }
        if (l != null) {
            sql.append(" AND locale=?"); args.add(l);
        }
        sql.append(" ORDER BY message_code,locale");
        return jdbc.query(sql.toString(), MESSAGE_MAPPER, args.toArray());
    }

    public CpfResponseCodeRecord insertResponseCode(CommonResponseCodeRequest r, String actor) {
        jdbc.update("INSERT INTO CMN_RESPONSE_CODE " +
                        "(response_code,message_code,result_type,module_id,response_group,sequence_no,description,use_yn,created_by,updated_by," +
                        "category,retry_disposition,exposure,effective_from,effective_to,catalog_version) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                normalizeCode(r.getResponseCode()), normalizeCode(r.getMessageCode()), required(r.getResultType(), "resultType"),
                required(r.getModuleId(), "moduleId"), required(r.getResponseGroup(), "responseGroup"), required(r.getSequenceNo(), "sequenceNo"),
                trimToNull(r.getDescription()), yn(r.getUseYn(), "Y"), actor, actor,
                upper(r.getCategory(), "BUSINESS"), upper(r.getRetryDisposition(), "NEVER"), upper(r.getExposure(), "SAFE_MESSAGE_ONLY"),
                timestamp(r.getEffectiveFrom()), timestamp(r.getEffectiveTo()), positiveVersion(r.getCatalogVersion()));
        return findResponseCode(r.getResponseCode());
    }

    public CpfResponseCodeRecord updateResponseCode(String code, long expectedVersion, CommonResponseCodeRequest r, String actor) {
        int count = jdbc.update("UPDATE CMN_RESPONSE_CODE SET message_code=?,result_type=?,module_id=?,response_group=?,sequence_no=?," +
                        "description=?,use_yn=?,updated_by=?,updated_at=CURRENT_TIMESTAMP,category=?,retry_disposition=?,exposure=?," +
                        "effective_from=?,effective_to=?,catalog_version=catalog_version+1 WHERE response_code=? AND catalog_version=?",
                normalizeCode(r.getMessageCode()), required(r.getResultType(), "resultType"), required(r.getModuleId(), "moduleId"),
                required(r.getResponseGroup(), "responseGroup"), required(r.getSequenceNo(), "sequenceNo"), trimToNull(r.getDescription()),
                yn(r.getUseYn(), "Y"), actor, upper(r.getCategory(), "BUSINESS"), upper(r.getRetryDisposition(), "NEVER"),
                upper(r.getExposure(), "SAFE_MESSAGE_ONLY"), timestamp(r.getEffectiveFrom()), timestamp(r.getEffectiveTo()),
                normalizeCode(code), expectedVersion);
        if (count != 1) return null;
        return findResponseCode(code);
    }

    public boolean disableResponseCode(String code, long expectedVersion, String actor) {
        return jdbc.update("UPDATE CMN_RESPONSE_CODE SET use_yn='N',catalog_version=catalog_version+1,updated_by=?,updated_at=CURRENT_TIMESTAMP WHERE response_code=? AND catalog_version=?",
                actor, normalizeCode(code), expectedVersion) == 1;
    }

    public CpfMessageRecord insertMessage(CommonMessageRequest r, String actor) {
        String messageCode = normalizeCode(r.getEffectiveMessageCode());
        String locale = normalizeLocale(r.getLocale());
        jdbc.update("INSERT INTO CMN_MESSAGE " +
                        "(message_code,locale,message_format_type,external_message,internal_message,parameter_count,parameter_sample,description,use_yn," +
                        "created_by,updated_by,parameter_schema_json,escape_html_yn,mask_arguments_yn,effective_from,effective_to,catalog_version) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                messageCode, locale, upper(r.getMessageFormatType(), "FIXED"), required(r.getEffectiveExternalMessage(), "externalMessage"),
                required(r.getEffectiveInternalMessage(), "internalMessage"), nonNegative(r.getParameterCount()), trimToNull(r.getParameterSample()),
                trimToNull(r.getDescription()), yn(r.getUseYn(), "Y"), actor, actor, trimToNull(r.getParameterSchemaJson()),
                yn(r.getEscapeHtmlYn(), "Y"), yn(r.getMaskArgumentsYn(), "Y"), timestamp(r.getEffectiveFrom()), timestamp(r.getEffectiveTo()),
                positiveVersion(r.getCatalogVersion()));
        return findMessage(messageCode, locale);
    }

    public CpfMessageRecord updateMessage(long id, long expectedVersion, CommonMessageRequest r, String actor) {
        int count = jdbc.update("UPDATE CMN_MESSAGE SET message_code=?,locale=?,message_format_type=?,external_message=?,internal_message=?," +
                        "parameter_count=?,parameter_sample=?,description=?,use_yn=?,updated_by=?,updated_at=CURRENT_TIMESTAMP,parameter_schema_json=?," +
                        "escape_html_yn=?,mask_arguments_yn=?,effective_from=?,effective_to=?,catalog_version=catalog_version+1 " +
                        "WHERE message_id=? AND catalog_version=?",
                normalizeCode(r.getEffectiveMessageCode()), normalizeLocale(r.getLocale()), upper(r.getMessageFormatType(), "FIXED"),
                required(r.getEffectiveExternalMessage(), "externalMessage"), required(r.getEffectiveInternalMessage(), "internalMessage"),
                nonNegative(r.getParameterCount()), trimToNull(r.getParameterSample()), trimToNull(r.getDescription()), yn(r.getUseYn(), "Y"), actor,
                trimToNull(r.getParameterSchemaJson()), yn(r.getEscapeHtmlYn(), "Y"), yn(r.getMaskArgumentsYn(), "Y"),
                timestamp(r.getEffectiveFrom()), timestamp(r.getEffectiveTo()), id, expectedVersion);
        return count == 1 ? findMessage(id) : null;
    }

    public boolean disableMessage(long id, long expectedVersion, String actor) {
        return jdbc.update("UPDATE CMN_MESSAGE SET use_yn='N',catalog_version=catalog_version+1,updated_by=?,updated_at=CURRENT_TIMESTAMP WHERE message_id=? AND catalog_version=?", actor, id, expectedVersion) == 1;
    }

    public CatalogFence readFence() {
        FencePart responses = jdbc.queryForObject(
                "SELECT COUNT(*) row_count, COALESCE(SUM(catalog_version),0) version_sum, MAX(updated_at) max_updated FROM CMN_RESPONSE_CODE",
                (rs, rowNum) -> new FencePart(rs.getLong("row_count"), rs.getLong("version_sum"), instant(rs, "max_updated")));
        FencePart messages = jdbc.queryForObject(
                "SELECT COUNT(*) row_count, COALESCE(SUM(catalog_version),0) version_sum, MAX(updated_at) max_updated FROM CMN_MESSAGE",
                (rs, rowNum) -> new FencePart(rs.getLong("row_count"), rs.getLong("version_sum"), instant(rs, "max_updated")));
        return new CatalogFence(responses, messages);
    }

    private static final RowMapper<CpfResponseCodeRecord> RESPONSE_MAPPER = (rs, rowNum) -> new CpfResponseCodeRecord(
            rs.getString("response_code"), rs.getString("message_code"), rs.getString("result_type"), rs.getString("module_id"),
            rs.getString("response_group"), rs.getString("sequence_no"), rs.getString("category"), rs.getString("retry_disposition"),
            rs.getString("exposure"), instant(rs, "effective_from"), instant(rs, "effective_to"), rs.getLong("catalog_version"),
            rs.getString("description"), rs.getString("use_yn"), instant(rs, "updated_at"));

    private static final RowMapper<CpfMessageRecord> MESSAGE_MAPPER = (rs, rowNum) -> new CpfMessageRecord(
            rs.getLong("message_id"), rs.getString("message_code"), rs.getString("locale"), rs.getString("message_format_type"),
            rs.getString("external_message"), rs.getString("internal_message"), rs.getInt("parameter_count"), rs.getString("parameter_sample"),
            rs.getString("parameter_schema_json"), rs.getString("escape_html_yn"), rs.getString("mask_arguments_yn"),
            instant(rs, "effective_from"), instant(rs, "effective_to"), rs.getLong("catalog_version"), rs.getString("description"),
            rs.getString("use_yn"), instant(rs, "updated_at"));

    private static Instant instant(ResultSet rs, String column) throws SQLException {
        Timestamp value = rs.getTimestamp(column);
        return value == null ? null : value.toInstant();
    }
    private static Timestamp timestamp(Instant instant) { return instant == null ? null : Timestamp.from(instant); }
    private static String normalizeCode(String value) { return required(value, "code").toUpperCase(Locale.ROOT); }
    private static String normalizeLocale(String value) { return required(value, "locale").toLowerCase(Locale.ROOT); }
    private static String normalizeNullableLocale(String value) { return value == null || value.isBlank() ? null : normalizeLocale(value); }
    private static String normalizeSearch(String value) {
        if (value == null || value.isBlank()) return null;
        return "%" + value.trim().toUpperCase(Locale.ROOT).replace("%", "\\%").replace("_", "\\_") + "%";
    }
    private static String upper(String value, String fallback) { return (value == null || value.isBlank() ? fallback : value).trim().toUpperCase(Locale.ROOT); }
    private static String yn(String value, String fallback) {
        String v = upper(value, fallback);
        if (!v.equals("Y") && !v.equals("N")) throw new IllegalArgumentException("YN value must be Y or N");
        return v;
    }
    private static int nonNegative(Integer value) { int v = value == null ? 0 : value; if (v < 0) throw new IllegalArgumentException("value must be >= 0"); return v; }
    private static long positiveVersion(Long value) { long v = value == null ? 1 : value; if (v < 1) throw new IllegalArgumentException("catalogVersion must be >= 1"); return v; }
    private static String trimToNull(String value) { if (value == null) return null; String v=value.trim(); return v.isEmpty()?null:v; }
    private static String required(String value, String name) { if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " is required"); return value.trim(); }
}
