package cpf.xyz.edu.repository;

import cpf.xyz.edu.dto.XyzQueryEducationCriteria;
import cpf.xyz.edu.dto.XyzQueryEducationItem;
import cpf.xyz.edu.mapper.XyzQueryEducationMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * 조회 EDU 샘플의 Repository입니다.
 *
 * <p>Controller 파라미터를 SQL에 직접 넘기지 않고, 검색어/상태/정렬/limit을 정규화한 뒤 Mapper에 전달합니다.
 * 이 계층을 두면 동적 SQL injection 위험을 줄이고 테스트에서 정렬 whitelist와 최대 조회 건수 제한을 분리 검증할 수 있습니다.</p>
 */
@Repository
public class XyzQueryEducationRepository {
    static final int MAX_PAGE_SIZE = 100;
    static final String SORT_ID_ASC = "ID_ASC";
    static final String SORT_NAME_ASC = "NAME_ASC";
    static final String SORT_CREATED_DESC = "CREATED_DESC";

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

    private String normalizeText(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String normalizeUpper(String value) {
        return value == null || value.isBlank() ? null : value.trim().toUpperCase(Locale.ROOT);
    }
}
