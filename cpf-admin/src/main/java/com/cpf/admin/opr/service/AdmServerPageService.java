package com.cpf.admin.opr.service;

import com.cpf.admin.opr.dto.AdmPageResponse;
import com.cpf.foundation.annotation.CpfService;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Owner Runtime이 반환한 운영 목록을 ADM 화면 계약에 맞게 안전하게 Paging·검색·정렬합니다.
 *
 * <p>정렬 키는 화면별 allow-list로 제한하며 원본 Map을 변경하지 않습니다. Owner Runtime이
 * Native Paging을 지원하는 경우에는 해당 Adapter가 같은 {@link AdmPageResponse} 계약으로
 * 교체될 수 있습니다.</p>
 */
@CpfService
public class AdmServerPageService {

    public AdmPageResponse<Map<String, Object>> page(
            List<Map<String, Object>> source,
            int page,
            int size,
            String query,
            String sort,
            String direction,
            Set<String> allowedSortKeys) {
        if (page < 0) throw new IllegalArgumentException("page must be >= 0");
        if (size < 1 || size > 200) throw new IllegalArgumentException("size must be between 1 and 200");
        List<Map<String, Object>> rows = source == null ? List.of() : source;
        String normalizedQuery = normalize(query);
        ArrayList<Map<String, Object>> filtered = new ArrayList<>();
        for (Map<String, Object> sourceRow : rows) {
            Map<String, Object> row = sourceRow == null ? Map.of() : new LinkedHashMap<>(sourceRow);
            if (normalizedQuery.isEmpty() || matches(row, normalizedQuery)) filtered.add(row);
        }
        String sortKey = normalize(sort);
        if (!sortKey.isEmpty()) {
            if (allowedSortKeys == null || !allowedSortKeys.contains(sortKey)) {
                throw new IllegalArgumentException("unsupported sort key: " + sortKey);
            }
            Comparator<Map<String, Object>> comparator = Comparator.comparing(
                    row -> comparable(row.get(sortKey)), Comparator.nullsLast(Comparator.naturalOrder()));
            if ("desc".equalsIgnoreCase(direction)) comparator = comparator.reversed();
            filtered.sort(comparator.thenComparing(row -> row.toString()));
        }
        long total = filtered.size();
        int from = Math.min(Math.multiplyExact(page, size), filtered.size());
        int to = Math.min(from + size, filtered.size());
        return new AdmPageResponse<>(filtered.subList(from, to), page, size, total,
                (long) to < total, Instant.now(), false, false, "");
    }

    private static boolean matches(Map<String, Object> row, String query) {
        for (Object value : row.values()) {
            if (value != null && normalize(String.valueOf(value)).contains(query)) return true;
        }
        return false;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private static ComparableValue comparable(Object value) {
        if (value == null) return null;
        if (value instanceof Number number) return new ComparableValue(new BigDecimal(number.toString()), "");
        String text = String.valueOf(value).trim();
        try { return new ComparableValue(new BigDecimal(text), ""); }
        catch (NumberFormatException ignored) { return new ComparableValue(null, text.toLowerCase(Locale.ROOT)); }
    }

    private record ComparableValue(BigDecimal number, String text) implements Comparable<ComparableValue> {
        @Override
        public int compareTo(ComparableValue other) {
            if (number != null && other.number != null) return number.compareTo(other.number);
            if (number != null) return -1;
            if (other.number != null) return 1;
            return text.compareTo(other.text);
        }
    }
}
