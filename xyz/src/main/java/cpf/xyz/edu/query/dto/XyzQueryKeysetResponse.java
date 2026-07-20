package cpf.xyz.edu.query.dto;

import java.util.List;

/**
 * keyset 기반 페이징 응답 샘플입니다.
 *
 * @param items        조회 결과
 * @param nextCursorId 다음 조회에 넘길 마지막 항목 ID
 * @param hasNext      다음 묶음 존재 여부
 */
public record XyzQueryKeysetResponse<T>(
        List<T> items,
        Long nextCursorId,
        boolean hasNext) {
}
