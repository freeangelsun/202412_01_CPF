package com.cpf.common.sample;

import java.util.List;

/**
 * Count query 없이 다음 항목 존재 여부를 제공하는 CMN Sample slice입니다.
 */
public record CmnSampleSlice(
        List<CmnSampleItem> items,
        boolean hasNext,
        Long nextCursor) {

    public CmnSampleSlice {
        items = List.copyOf(items);
    }
}
