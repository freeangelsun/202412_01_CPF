package cpf.acc.account.dto;

import java.util.Map;

/**
 * ACC 계정 검색·정렬·페이징 조건입니다.
 *
 * <p>정렬 입력은 SQL 조각으로 직접 사용하지 않고 고정 whitelist의 실제 컬럼명으로 변환합니다.</p>
 */
public record AccAccountSearchCriteria(
        String accountNo,
        String accountName,
        String statusCode,
        String sortBy,
        String sortDirection,
        Integer page,
        Integer size,
        Long cursorId) {

    private static final Map<String, String> SORT_COLUMNS = Map.of(
            "accountId", "account_id",
            "accountNo", "account_no",
            "accountName", "account_name",
            "createdAt", "created_at",
            "updatedAt", "updated_at");

    public AccAccountSearchCriteria normalized() {
        String normalizedSort = SORT_COLUMNS.containsKey(sortBy) ? sortBy : "accountId";
        String normalizedDirection = "ASC".equalsIgnoreCase(sortDirection) ? "ASC" : "DESC";
        int normalizedPage = page == null || page < 0 ? 0 : page;
        int normalizedSize = size == null || size < 1 ? 20 : Math.min(size, 200);
        Long normalizedCursor = cursorId != null && cursorId > 0 ? cursorId : null;
        return new AccAccountSearchCriteria(
                trimToNull(accountNo), trimToNull(accountName), trimToNull(statusCode),
                normalizedSort, normalizedDirection, normalizedPage, normalizedSize, normalizedCursor);
    }

    public String sortColumn() {
        return SORT_COLUMNS.getOrDefault(sortBy, "account_id");
    }

    public int offset() {
        return Math.max(0, page == null ? 0 : page) * Math.max(1, size == null ? 20 : size);
    }

    private static String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
