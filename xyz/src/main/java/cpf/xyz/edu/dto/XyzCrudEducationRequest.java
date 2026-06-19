package cpf.xyz.edu.dto;

/**
 * XYZ CRUD 교육 항목 등록/수정 요청 DTO입니다.
 *
 * @param title 교육 항목 제목
 * @param description 교육 항목 설명
 * @param requestUser 요청 사용자
 */
public record XyzCrudEducationRequest(
        String title,
        String description,
        String requestUser) {
}
