package com.cpf.common.utils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 컬렉션 null 안전 처리 유틸리티입니다.
 *
 * <p>Spring의 CollectionUtils와 이름이 겹치지 않도록 {@code CollectionSafeUtils}라는
 * 이름을 사용합니다. 업무 코드는 null 컬렉션을 직접 순회하지 말고 이 유틸리티로
 * 빈 컬렉션 변환 또는 존재 여부를 확인합니다.</p>
 */
public final class CollectionSafeUtils {

    private CollectionSafeUtils() {
    }

    /**
     * 컬렉션이 null이거나 비어 있는지 확인합니다.
     *
     * @param source 검사할 컬렉션
     * @return null 또는 empty이면 true
     */
    public static boolean isEmpty(Collection<?> source) {
        return source == null || source.isEmpty();
    }

    /**
     * Map이 null이거나 비어 있는지 확인합니다.
     *
     * @param source 검사할 Map
     * @return null 또는 empty이면 true
     */
    public static boolean isEmpty(Map<?, ?> source) {
        return source == null || source.isEmpty();
    }

    /**
     * null List를 빈 List로 변환합니다.
     *
     * @param source 원본 List
     * @param <T>    항목 타입
     * @return 원본이 null이면 빈 List
     */
    public static <T> List<T> emptyIfNull(List<T> source) {
        return source == null ? List.of() : source;
    }
}

