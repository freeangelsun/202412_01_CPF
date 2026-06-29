package cpf.xyz.edu.service;

import cpf.pfw.common.exception.CpfNotFoundException;
import cpf.xyz.edu.dto.XyzQueryEducationItem;
import cpf.xyz.edu.dto.XyzQueryKeysetResponse;
import cpf.xyz.edu.dto.XyzQueryPageResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

/**
 * 온라인 조회 API 작성법을 설명하는 EDU 서비스입니다.
 *
 * <p>실제 업무 모듈에서는 이 메모리 목록 부분을 MyBatis Mapper나 Repository 호출로 바꾸면 됩니다.
 * 검색 조건 정규화, 정렬 whitelist, offset/keyset 페이징 응답 형태, readOnly 트랜잭션 경계를
 * 한 곳에서 학습할 수 있도록 의도적으로 단순하게 구성했습니다.</p>
 */
@Service
public class XyzQueryEducationService {
    private final List<XyzQueryEducationItem> seedItems = List.of(
            new XyzQueryEducationItem(1L, "표준 헤더 단건 조회", "HEADER", "ACTIVE", "MBR-001", "2026-06-01T09:00:00"),
            new XyzQueryEducationItem(2L, "거래 로그 목록 조회", "LOG", "ACTIVE", "MBR-002", "2026-06-02T09:00:00"),
            new XyzQueryEducationItem(3L, "offset 페이징 조회", "QUERY", "ACTIVE", "MBR-003", "2026-06-03T09:00:00"),
            new XyzQueryEducationItem(4L, "keyset 페이징 조회", "QUERY", "ACTIVE", "MBR-004", "2026-06-04T09:00:00"),
            new XyzQueryEducationItem(5L, "검색 조건 정규화", "QUERY", "INACTIVE", "MBR-005", "2026-06-05T09:00:00"),
            new XyzQueryEducationItem(6L, "정렬 whitelist", "QUERY", "ACTIVE", "MBR-006", "2026-06-06T09:00:00"),
            new XyzQueryEducationItem(7L, "하위 호출 헤더 전파", "HEADER", "ACTIVE", "MBR-007", "2026-06-07T09:00:00"),
            new XyzQueryEducationItem(8L, "Swagger 조회 예시", "DOC", "ACTIVE", "MBR-008", "2026-06-08T09:00:00")
    );

    /**
     * 단건 조회는 키 검증, 미존재 예외, 응답 DTO 반환 흐름을 보여줍니다.
     */
    @Transactional(transactionManager = "cmnTransactionManager", readOnly = true)
    public XyzQueryEducationItem getItem(Long itemId) {
        return seedItems.stream()
                .filter(item -> item.itemId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new CpfNotFoundException("조회 EDU 항목을 찾을 수 없습니다. itemId=" + itemId));
    }

    /**
     * 목록 조회는 검색 조건과 정렬 기준을 whitelist 방식으로 제한합니다.
     */
    @Transactional(transactionManager = "cmnTransactionManager", readOnly = true)
    public List<XyzQueryEducationItem> findItems(String keyword, String statusCode, String sort, int limit) {
        return filtered(keyword, statusCode).stream()
                .sorted(comparator(sort))
                .limit(normalizeSize(limit))
                .toList();
    }

    /**
     * offset 페이징은 작은 목록과 관리자성 조회에 적합합니다.
     */
    @Transactional(transactionManager = "cmnTransactionManager", readOnly = true)
    public XyzQueryPageResponse<XyzQueryEducationItem> findOffsetPage(
            String keyword,
            String statusCode,
            String sort,
            int page,
            int size) {
        int normalizedPage = Math.max(page, 1);
        int normalizedSize = normalizeSize(size);
        List<XyzQueryEducationItem> filtered = filtered(keyword, statusCode).stream()
                .sorted(comparator(sort))
                .toList();
        int fromIndex = Math.min((normalizedPage - 1) * normalizedSize, filtered.size());
        int toIndex = Math.min(fromIndex + normalizedSize, filtered.size());
        return new XyzQueryPageResponse<>(
                filtered.subList(fromIndex, toIndex),
                normalizedPage,
                normalizedSize,
                filtered.size(),
                toIndex < filtered.size());
    }

    /**
     * keyset 페이징은 대용량 목록에서 마지막으로 본 key 이후를 조회하는 방식입니다.
     */
    @Transactional(transactionManager = "cmnTransactionManager", readOnly = true)
    public XyzQueryKeysetResponse<XyzQueryEducationItem> findKeysetPage(Long cursorId, int size) {
        int normalizedSize = normalizeSize(size);
        List<XyzQueryEducationItem> page = seedItems.stream()
                .filter(item -> cursorId == null || item.itemId() > cursorId)
                .sorted(Comparator.comparing(XyzQueryEducationItem::itemId))
                .limit(normalizedSize + 1L)
                .toList();
        boolean hasNext = page.size() > normalizedSize;
        List<XyzQueryEducationItem> items = hasNext ? page.subList(0, normalizedSize) : page;
        Long nextCursorId = items.isEmpty() ? cursorId : items.get(items.size() - 1).itemId();
        return new XyzQueryKeysetResponse<>(items, nextCursorId, hasNext);
    }

    private List<XyzQueryEducationItem> filtered(String keyword, String statusCode) {
        Predicate<XyzQueryEducationItem> predicate = item -> true;
        if (hasText(keyword)) {
            String normalizedKeyword = keyword.trim().toLowerCase(Locale.ROOT);
            predicate = predicate.and(item -> item.itemName().toLowerCase(Locale.ROOT).contains(normalizedKeyword)
                    || item.categoryCode().toLowerCase(Locale.ROOT).contains(normalizedKeyword));
        }
        if (hasText(statusCode)) {
            String normalizedStatus = statusCode.trim().toUpperCase(Locale.ROOT);
            predicate = predicate.and(item -> item.statusCode().equalsIgnoreCase(normalizedStatus));
        }
        return seedItems.stream().filter(predicate).toList();
    }

    private Comparator<XyzQueryEducationItem> comparator(String sort) {
        if ("nameAsc".equalsIgnoreCase(sort)) {
            return Comparator.comparing(XyzQueryEducationItem::itemName);
        }
        if ("createdDesc".equalsIgnoreCase(sort)) {
            return Comparator.comparing(XyzQueryEducationItem::createdAt).reversed();
        }
        return Comparator.comparing(XyzQueryEducationItem::itemId);
    }

    private int normalizeSize(int size) {
        return Math.max(1, Math.min(size, 100));
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
