package com.cpf.education.common.sample;
import com.cpf.foundation.api.CpfBaseService;
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
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * DB Tool이 제공하는 REFERENCE_FIXTURE의 REF_CMN_SAMPLE_ITEM 한 개로 DB 연결과 표준 CRUD/Paging/Transaction을 검증합니다.
 */
@Service
public class CmnSampleItemService extends CpfBaseService {
    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;

    private final ObjectProvider<JdbcTemplate> jdbcTemplateProvider;
    private final ObjectProvider<TransactionTemplate> transactionTemplateProvider;
    private volatile CmnSampleSqlDialect sqlDialect;

    /** CmnSampleItemService 작업을 CPF 표준 계약에 따라 수행한다. */
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

    /** create 작업을 CPF 표준 계약에 따라 수행한다. */
    public CmnSampleItem create(CmnSampleItemRequest request) {
        ValidatedRequest validated = validate(request);
        JdbcTemplate jdbcTemplate = requireJdbcTemplate();
        KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement statement = connection.prepareStatement(CmnEducationSqlResourceLoader.load("sample/insert.sql"),
                        new String[] {"sample_item_id"});
                statement.setString(1, validated.sampleKey());
                statement.setString(2, validated.itemName());
                statement.setString(3, validated.categoryCode());
                statement.setString(4, validated.statusCode());
                statement.setString(5, validated.searchableText());
                statement.setString(6, validated.ownerEducation());
                statement.setLong(7, validated.sortOrder());
                statement.setString(8, validated.requestUser());
                statement.setString(9, validated.requestUser());
                return statement;
            }, keyHolder);
        // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
        } catch (DuplicateKeyException ex) {
            throw new DuplicateKeyException("CMN sampleKey가 이미 존재합니다.", ex);
        }
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("CMN Sample 등록 ID를 확인할 수 없습니다.");
        }
        return require(key.longValue());
    }

    /** find 작업을 CPF 표준 계약에 따라 수행한다. */
    public Optional<CmnSampleItem> find(long sampleItemId) {
        List<CmnSampleItem> items = requireJdbcTemplate().query(CmnEducationSqlResourceLoader.load("sample/find-by-id.sql"), (rs, rowNum) -> map(rs), sampleItemId);
        return items.stream().findFirst();
    }

    public List<CmnSampleItem> offsetPage(
            String keyword, String statusCode, int offset, int limit) {
        int safeOffset = Math.max(offset, 0);
        int safeLimit = normalizeLimit(limit);
        String normalizedStatus = blankToNull(statusCode);
        String normalizedKeyword = blankToNull(keyword);
        CmnSampleSqlDialect dialect = requireSqlDialect();
        return requireJdbcTemplate().query(
                dialect.offsetPageSql(),
                (rs, rowNum) -> map(rs),
                dialect.offsetPageParameters(
                        normalizedStatus,
                        normalizedKeyword,
                        safeOffset,
                        safeLimit));
    }

    /** cursorPage 작업을 CPF 표준 계약에 따라 수행한다. */
    public CmnSampleSlice cursorPage(Long afterId, String statusCode, int limit) {
        int safeLimit = normalizeLimit(limit);
        String normalizedStatus = blankToNull(statusCode);
        List<CmnSampleItem> rows = requireJdbcTemplate().query(
                requireSqlDialect().cursorPageSql(),
                (rs, rowNum) -> map(rs),
                afterId == null ? 0L : Math.max(afterId, 0L),
                normalizedStatus,
                normalizedStatus,
                safeLimit + 1);
        boolean hasNext = rows.size() > safeLimit;
        List<CmnSampleItem> items = hasNext ? List.copyOf(rows.subList(0, safeLimit)) : List.copyOf(rows);
        Long nextCursor = hasNext && !items.isEmpty()
                ? items.getLast().sampleItemId()
                : null;
        return new CmnSampleSlice(items, hasNext, nextCursor);
    }

    /** update 작업을 CPF 표준 계약에 따라 수행한다. */
    public CmnSampleItem update(
            long sampleItemId, long expectedVersion, CmnSampleItemRequest request) {
        ValidatedRequest validated = validate(request);
        int updated = requireJdbcTemplate().update(CmnEducationSqlResourceLoader.load("sample/update.sql"),
                validated.sampleKey(), validated.itemName(), validated.categoryCode(), validated.statusCode(),
                validated.searchableText(), validated.ownerEducation(), validated.sortOrder(),
                validated.requestUser(), sampleItemId, expectedVersion);
        if (updated != 1) {
            throw new OptimisticLockingFailureException(
                    "CMN Sample이 없거나 version이 변경되었습니다. sampleItemId=" + sampleItemId);
        }
        return require(sampleItemId);
    }

    /** delete 작업을 CPF 표준 계약에 따라 수행한다. */
    public void delete(long sampleItemId, long expectedVersion, String requestUser) {
        int updated = requireJdbcTemplate().update(CmnEducationSqlResourceLoader.load("sample/soft-delete.sql"), requireText(requestUser, "requestUser"), sampleItemId, expectedVersion);
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
                CmnEducationSqlResourceLoader.load("sample/count-by-key.sql"),
                Integer.class,
                validated.sampleKey());
        transactionTemplate.executeWithoutResult(status -> {
            requireJdbcTemplate().update(CmnEducationSqlResourceLoader.load("sample/insert.sql"),
                    validated.sampleKey(), validated.itemName(), validated.categoryCode(), validated.statusCode(),
                    validated.searchableText(), validated.ownerEducation(), validated.sortOrder(),
                    validated.requestUser(), validated.requestUser());
            // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
            status.setRollbackOnly();
        });
        Integer rowCountAfter = requireJdbcTemplate().queryForObject(
                CmnEducationSqlResourceLoader.load("sample/count-by-key.sql"),
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

    private CmnSampleSqlDialect requireSqlDialect() {
        CmnSampleSqlDialect current = sqlDialect;
        if (current != null) {
            return current;
        }
        // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
        synchronized (this) {
            current = sqlDialect;
            if (current == null) {
                JdbcTemplate jdbcTemplate = requireJdbcTemplate();
                current = CmnSampleSqlDialect.detect(jdbcTemplate);
                sqlDialect = current;
            }
            return current;
        }
    }

    private JdbcTemplate requireJdbcTemplate() {
        JdbcTemplate jdbcTemplate = jdbcTemplateProvider.getIfAvailable();
        if (jdbcTemplate == null) {
            throw new IllegalStateException(
                    "CMN Sample DB가 비활성화되어 있습니다. cpf.common.sample-db.enabled를 확인하세요.");
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
                blankToNull(request.ownerEducation()),
                request.sortOrder(),
                defaultText(request.requestUser(), "CMN_SAMPLE"));
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private String defaultText(String value, String defaultValue) {
        String result = blankToNull(value);
        return result == null ? defaultValue : result;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    /** ValidatedRequest 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
    private record ValidatedRequest(
            String sampleKey,
            String itemName,
            String categoryCode,
            String statusCode,
            String searchableText,
            String ownerEducation,
            long sortOrder,
            String requestUser) {
    }
}
