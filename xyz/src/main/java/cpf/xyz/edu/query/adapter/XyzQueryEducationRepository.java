package cpf.xyz.edu.query.adapter;

import cpf.xyz.edu.query.dto.XyzQueryEducationCriteria;
import cpf.xyz.edu.query.dto.XyzQueryEducationItem;
import cpf.xyz.edu.query.adapter.XyzQueryEducationMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * 조회/CRUD EDU 샘플 Repository입니다.
 *
 * <p>Controller 입력값을 SQL에 바로 붙이지 않고 검색어, 상태, 정렬, paging 값을 정규화한 뒤 Mapper로 전달합니다.
 * 정렬은 whitelist 코드만 Mapper XML의 {@code choose} 분기로 전달해 SQL injection 위험을 줄입니다.</p>
 */
@Repository
public class XyzQueryEducationRepository {
    public static final int MAX_PAGE_SIZE = 100;
    public static final String SORT_ID_ASC = "ID_ASC";
    public static final String SORT_NAME_ASC = "NAME_ASC";
    public static final String SORT_CREATED_DESC = "CREATED_DESC";

    private final XyzQueryEducationMapper mapper;

    public XyzQueryEducationRepository(XyzQueryEducationMapper mapper) {
        this.mapper = mapper;
    }

    public Optional<XyzQueryEducationItem> findById(Long itemId) {
        return Optional.ofNullable(mapper.findById(itemId));
    }

    public List<XyzQueryEducationItem> findItems(String keyword, String statusCode, String sort, int limit) {
        return mapper.findItems(criteria(keyword, statusCode, sort, normalizeSize(limit), 0, null));
    }

    public List<XyzQueryEducationItem> findOffsetPageItems(
            String keyword,
            String statusCode,
            String sort,
            int page,
            int size) {
        int normalizedPage = normalizePage(page);
        int normalizedSize = normalizeSize(size);
        int offset = (normalizedPage - 1) * normalizedSize;
        return mapper.findOffsetPageItems(criteria(keyword, statusCode, sort, normalizedSize, offset, null));
    }

    public long countOffsetPageItems(String keyword, String statusCode) {
        return mapper.countOffsetPageItems(criteria(keyword, statusCode, SORT_ID_ASC, MAX_PAGE_SIZE, 0, null));
    }

    public List<XyzQueryEducationItem> findKeysetPageItems(Long cursorId, int size) {
        int limitPlusOne = normalizeSize(size) + 1;
        return mapper.findKeysetPageItems(criteria(null, null, SORT_ID_ASC, limitPlusOne, 0, cursorId));
    }

    public Long nextCrudItemId() {
        Long itemId = mapper.nextCrudItemId();
        return itemId == null ? 91000L : itemId;
    }

    public void insertCrudItem(
            Long itemId,
            String itemName,
            String categoryCode,
            String statusCode,
            String ownerMemberNo,
            String requestUser) {
        mapper.insertCrudItem(itemId, itemName, categoryCode, statusCode, ownerMemberNo, requestUser);
    }

    public int updateCrudItem(
            Long itemId,
            String itemName,
            String categoryCode,
            String ownerMemberNo,
            String requestUser) {
        return mapper.updateCrudItem(itemId, itemName, categoryCode, ownerMemberNo, requestUser);
    }

    public int updateCrudItemStatus(Long itemId, String statusCode, String requestUser) {
        return mapper.updateCrudItemStatus(itemId, normalizeUpper(statusCode), requestUser);
    }

    public int logicalDeleteCrudItem(Long itemId, String requestUser) {
        return mapper.logicalDeleteCrudItem(itemId, requestUser);
    }

    public int normalizePage(int page) {
        return Math.max(page, 1);
    }

    public int normalizeSize(int size) {
        return Math.max(1, Math.min(size, MAX_PAGE_SIZE));
    }

    XyzQueryEducationCriteria criteria(
            String keyword,
            String statusCode,
            String sort,
            int limit,
            int offset,
            Long cursorId) {
        return new XyzQueryEducationCriteria(
                normalizeText(keyword),
                normalizeUpper(statusCode),
                normalizeSortCode(sort),
                normalizeSize(limit),
                Math.max(offset, 0),
                cursorId);
    }

    String normalizeSortCode(String sort) {
        if ("nameAsc".equalsIgnoreCase(sort) || SORT_NAME_ASC.equalsIgnoreCase(sort)) {
            return SORT_NAME_ASC;
        }
        if ("createdDesc".equalsIgnoreCase(sort) || SORT_CREATED_DESC.equalsIgnoreCase(sort)) {
            return SORT_CREATED_DESC;
        }
        return SORT_ID_ASC;
    }

    public String normalizeCategoryCode(String value) {
        String normalized = normalizeUpper(value);
        return normalized == null ? "CRUD" : normalized;
    }

    public String normalizeRequestUser(String value) {
        String normalized = normalizeText(value);
        return normalized == null ? "XYZ_EDU" : normalized;
    }

    private String normalizeText(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String normalizeUpper(String value) {
        return value == null || value.isBlank() ? null : value.trim().toUpperCase(Locale.ROOT);
    }
}
