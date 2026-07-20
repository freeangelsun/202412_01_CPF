package cpf.xyz.edu.query.dto;

/**
 * 조회 EDU 샘플의 Mapper 조건 DTO입니다.
 *
 * <p>Controller 파라미터를 그대로 SQL에 넘기지 않고, Repository에서 검색어 정리, 정렬 whitelist, 최대 조회 건수
 * 제한을 적용한 뒤 이 DTO로 Mapper에 전달합니다.</p>
 *
 * @param keyword    검색어
 * @param statusCode 상태 코드
 * @param sortCode   SQL XML에서 허용한 정렬 코드
 * @param limit      최대 조회 건수
 * @param offset     offset 페이징 시작 위치
 * @param cursorId   keyset 페이징 기준 ID
 */
public record XyzQueryEducationCriteria(
        String keyword,
        String statusCode,
        String sortCode,
        int limit,
        int offset,
        Long cursorId) {
}
