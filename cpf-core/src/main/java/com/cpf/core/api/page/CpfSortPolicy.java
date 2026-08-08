package com.cpf.core.api.page;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 외부 정렬 요청을 Repository의 명시적 allow-list와 대조하는 공통 정책입니다.
 * SQL ORDER BY 문자열 조립에는 검증된 반환값만 사용해야 합니다.
 */
public final class CpfSortPolicy {
    public static final int DEFAULT_MAX_COLUMNS = 5;

    private CpfSortPolicy() { }

    public static List<CpfSort> validate(List<CpfSort> requested, Set<String> allowedFields) {
        return validate(requested, allowedFields, DEFAULT_MAX_COLUMNS);
    }

    public static List<CpfSort> validate(List<CpfSort> requested, Set<String> allowedFields, int maxColumns) {
        if (maxColumns < 1) throw new IllegalArgumentException("maxColumns는 1 이상이어야 합니다.");
        Set<String> allowed = Objects.requireNonNull(allowedFields, "allowedFields");
        List<CpfSort> sorts = requested == null ? List.of() : List.copyOf(requested);
        if (sorts.size() > maxColumns) throw new IllegalArgumentException("정렬 컬럼은 최대 " + maxColumns + "개입니다.");
        LinkedHashSet<String> seen = new LinkedHashSet<>();
        ArrayList<CpfSort> validated = new ArrayList<>(sorts.size());
        for (CpfSort sort : sorts) {
            CpfSort required = Objects.requireNonNull(sort, "sort");
            if (!allowed.contains(required.field())) {
                throw new IllegalArgumentException("허용되지 않은 정렬 field입니다: " + required.field());
            }
            if (!seen.add(required.field())) {
                throw new IllegalArgumentException("중복 정렬 field입니다: " + required.field());
            }
            validated.add(required);
        }
        return List.copyOf(validated);
    }
}
