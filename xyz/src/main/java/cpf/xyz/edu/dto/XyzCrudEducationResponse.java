package cpf.xyz.edu.dto;

/**
 * XYZ CRUD 교육 항목 응답 DTO입니다.
 *
 * @param educationItemId 교육 항목 ID
 * @param title 교육 항목 제목
 * @param status 처리 상태
 * @param description 교육 항목 설명
 * @param createdAt 생성 일시
 */
public record XyzCrudEducationResponse(
        Long educationItemId,
        String title,
        String status,
        String description,
        String createdAt) {
}
