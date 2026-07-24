package com.cpf.common.sample;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * cmn_sample_item 한 개로 DB 연결과 표준 CRUD/Paging/Transaction을 검증합니다.
 */
@Service
public class CmnSampleItemService {
    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;

    private final ObjectProvider<JdbcTemplate> jdbcTemplateProvider;
    private final ObjectProvider<TransactionTemplate> transactionTemplateProvider;

    public CmnSampleItemService(
            @Qualifier("cmnSampleJdbcTemplate") ObjectProvider<JdbcTemplate> jdbcTemplateProvider,
            @Qualifier("cmnSampleTransactionTemplate") ObjectProvider<TransactionTemplate> transactionTemplateProvider) {
        this.jdbcTemplateProvider = jdbcTemplateProvider;
        this.transactionTemplateProvider = transactionTemplateProvider;
    }

    public boolean isEnabled() {
        return jdbcTemplateProvider.getIfAvailable() != null
                && transactionTemplateProvider.getIfAvailable() != null;
    }

    public CmnSampleItem create(CmnSampleItemRequest request) {
        ValidatedRequest validated = validate(request);
        JdbcTemplate jdbcTemplate = requireJdbcTemplate();
        KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement statement = connection.prepareStatement("""
                        INSERT INTO cmn_sample_item (
                            sample_key, item_name, category_code, status_code,
                            searchable_text, owner_reference, sort_order, version_no,
                            deleted_yn, created_by, updated_by
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, 0, 'N', ?, ?)
                        """, Statement.RETURN_GENERATED_KEYS);
                statement.setString(1, validated.sampleKey());
                statement.setString(2, validated.itemName());
                statement.setString(3, validated.categoryCode());
                statement.setString(4, validated.statusCode());
                statement.setString(5, validated.searchableText());
                statement.setString(6, validated.ownerReference());
                statement.setLong(7, validated.sortOrder());
                statement.setString(8, validated.requestUser());
                statement.setString(9, validated.requestUser());
                return statement;
            }, keyHolder);
        } catch (DuplicateKeyException ex) {
            throw new DuplicateKeyException("CMN sampleKey가 이미 존재합니다.", ex);
        }
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("CMN Sample 등록 ID를 확인할 수 없습니다.");
        }
        return require(key.longValue());
    }

    public Optional<CmnSampleItem> find(long sampleItemId) {
        List<CmnSampleItem> items = requireJdbcTemplate().query("""
                SELECT sample_item_id, sample_key, item_name, category_code, status_code,
                       searchable_text, owner_reference, sort_order, version_no,
                       created_at, updated_at
                FROM cmn_sample_item
                WHERE sample_item_id = ? AND deleted_yn = 'N'
                """, (rs, rowNum) -> map(rs), sampleItemId);
        return items.stream().findFirst();
    }

    public List<CmnSampleItem> offsetPage(
            String keyword, String statusCode, int offset, int limit) {
        int safeOffset = Math.max(offset, 0);
        int safeLimit = normalizeLimit(limit);
        return requireJdbcTemplate().query("""
                SELECT sample_item_id, sample_key, item_name, category_code, status_code,
                       searchable_text, owner_reference, sort_order, version_no,
                       created_at, updated_at
                FROM cmn_sample_item
                WHERE deleted_yn = 'N'
                  AND (? IS NULL OR status_code = ?)
                  AND (? IS NULL OR LOWER(CONCAT(item_name, ' ', category_code, ' ', COALESCE(searchable_text, '')))
                       LIKE CONCAT('%', LOWER(?), '%'))
                ORDER BY sort_order, sample_item_id
                LIMIT ? OFFSET ?
                """, (rs, rowNum) -> map(rs),
                blankToNull(statusCode), blankToNull(statusCode),
                blankToNull(keyword), blankToNull(keyword),
                safeLimit, safeOffset);
    }

    public CmnSampleSlice cursorPage(Long afterId, String statusCode, int limit) {
        int safeLimit = normalizeLimit(limit);
        List<CmnSampleItem> rows = requireJdbcTemplate().query("""
                SELECT sample_item_id, sample_key, item_name, category_code, status_code,
                       searchable_text, owner_reference, sort_order, version_no,
                       created_at, updated_at
                FROM cmn_sample_item
                WHERE deleted_yn = 'N'
                  AND sample_item_id > ?
                  AND (? IS NULL OR status_code = ?)
                ORDER BY sample_item_id
                LIMIT ?
                """, (rs, rowNum) -> map(rs),
                afterId == null ? 0L : Math.max(afterId, 0L),
                blankToNull(statusCode), blankToNull(statusCode),
                safeLimit + 1);
        boolean hasNext = rows.size() > safeLimit;
        List<CmnSampleItem> items = hasNext ? List.copyOf(rows.subList(0, safeLimit)) : List.copyOf(rows);
        Long nextCursor = hasNext && !items.isEmpty()
                ? items.getLast().sampleItemId()
                : null;
        return new CmnSampleSlice(items, hasNext, nextCursor);
    }

    public CmnSampleItem update(
            long sampleItemId, long expectedVersion, CmnSampleItemRequest request) {
        ValidatedRequest validated = validate(request);
        int updated = requireJdbcTemplate().update("""
                UPDATE cmn_sample_item
                SET sample_key = ?, item_name = ?, category_code = ?, status_code = ?,
                    searchable_text = ?, owner_reference = ?, sort_order = ?,
                    version_no = version_no + 1, updated_by = ?, updated_at = CURRENT_TIMESTAMP(3)
                WHERE sample_item_id = ? AND version_no = ? AND deleted_yn = 'N'
                """,
                validated.sampleKey(), validated.itemName(), validated.categoryCode(), validated.statusCode(),
                validated.searchableText(), validated.ownerReference(), validated.sortOrder(),
                validated.requestUser(), sampleItemId, expectedVersion);
        if (updated != 1) {
            throw new OptimisticLockingFailureException(
                    "CMN Sample이 없거나 version이 변경되었습니다. sampleItemId=" + sampleItemId);
        }
        return require(sampleItemId);
    }

    public void delete(long sampleItemId, long expectedVersion, String requestUser) {
        int updated = requireJdbcTemplate().update("""
                UPDATE cmn_sample_item
                SET deleted_yn = 'Y', status_code = 'INACTIVE',
                    version_no = version_no + 1, updated_by = ?, updated_at = CURRENT_TIMESTAMP(3)
                WHERE sample_item_id = ? AND version_no = ? AND deleted_yn = 'N'
                """, requireText(requestUser, "requestUser"), sampleItemId, expectedVersion);
        if (updated != 1) {
            throw new OptimisticLockingFailureException(
                    "CMN Sample이 없거나 version이 변경되었습니다. sampleItemId=" + sampleItemId);
        }
    }

    /**
     * rollbackOnly를 사용해 실제 insert가 남지 않는 Transaction rollback 검증을 수행합니다.
     */
    public boolean verifyRollback(CmnSampleItemRequest request) {
        TransactionTemplate transactionTemplate = requireTransactionTemplate();
        ValidatedRequest validated = validate(request);
        Integer rowCountBefore = requireJdbcTemplate().queryForObject(
                "SELECT COUNT(*) FROM cmn_sample_item WHERE sample_key = ?",
                Integer.class,
                validated.sampleKey());
        transactionTemplate.executeWithoutResult(status -> {
            requireJdbcTemplate().update("""
                    INSERT INTO cmn_sample_item (
                        sample_key, item_name, category_code, status_code,
                        searchable_text, owner_reference, sort_order, version_no,
                        deleted_yn, created_by, updated_by
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, 0, 'N', ?, ?)
                    """,
                    validated.sampleKey(), validated.itemName(), validated.categoryCode(), validated.statusCode(),
                    validated.searchableText(), validated.ownerReference(), validated.sortOrder(),
                    validated.requestUser(), validated.requestUser());
            status.setRollbackOnly();
        });
        Integer rowCountAfter = requireJdbcTemplate().queryForObject(
                "SELECT COUNT(*) FROM cmn_sample_item WHERE sample_key = ?",
                Integer.class,
                validated.sampleKey());
        return rowCountBefore != null && rowCountBefore.equals(rowCountAfter);
    }

    private CmnSampleItem require(long sampleItemId) {
        return find(sampleItemId)
                .orElseThrow(() -> new IllegalStateException("CMN Sample을 찾을 수 없습니다. sampleItemId=" + sampleItemId));
    }

    private CmnSampleItem map(java.sql.ResultSet resultSet) throws java.sql.SQLException {
        return new CmnSampleItem(
                resultSet.getLong("sample_item_id"),
                resultSet.getString("sample_key"),
                resultSet.getString("item_name"),
                resultSet.getString("category_code"),
                resultSet.getString("status_code"),
                resultSet.getString("searchable_text"),
                resultSet.getString("owner_reference"),
                resultSet.getLong("sort_order"),
                resultSet.getLong("version_no"),
                instant(resultSet.getTimestamp("created_at")),
                instant(resultSet.getTimestamp("updated_at")));
    }

    private Instant instant(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }

    private JdbcTemplate requireJdbcTemplate() {
        JdbcTemplate jdbcTemplate = jdbcTemplateProvider.getIfAvailable();
        if (jdbcTemplate == null) {
            throw new IllegalStateException(
                    "CMN Sample DB가 비활성화되어 있습니다. cpf.cmn.sample-db.enabled를 확인하세요.");
        }
        return jdbcTemplate;
    }

    private TransactionTemplate requireTransactionTemplate() {
        TransactionTemplate transactionTemplate = transactionTemplateProvider.getIfAvailable();
        if (transactionTemplate == null) {
            throw new IllegalStateException("CMN Sample Transaction 설정이 없습니다.");
        }
        return transactionTemplate;
    }

    private ValidatedRequest validate(CmnSampleItemRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request는 필수입니다.");
        }
        String statusCode = defaultText(request.statusCode(), "ACTIVE").toUpperCase(Locale.ROOT);
        if (!statusCode.equals("ACTIVE") && !statusCode.equals("INACTIVE")) {
            throw new IllegalArgumentException("statusCode는 ACTIVE 또는 INACTIVE여야 합니다.");
        }
        return new ValidatedRequest(
                requireText(request.sampleKey(), "sampleKey"),
                requireText(request.itemName(), "itemName"),
                defaultText(request.categoryCode(), "GENERAL").toUpperCase(Locale.ROOT),
                statusCode,
                blankToNull(request.searchableText()),
                blankToNull(request.ownerReference()),
                request.sortOrder(),
                defaultText(request.requestUser(), "CMN_SAMPLE"));
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private String requireText(String value, String field) {
        String result = blankToNull(value);
        if (result == null) {
            throw new IllegalArgumentException(field + "는 필수입니다.");
        }
        return result;
    }

    private String defaultText(String value, String defaultValue) {
        String result = blankToNull(value);
        return result == null ? defaultValue : result;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private record ValidatedRequest(
            String sampleKey,
            String itemName,
            String categoryCode,
            String statusCode,
            String searchableText,
            String ownerReference,
            long sortOrder,
            String requestUser) {
    }
}
