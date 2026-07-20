package cpf.xyz.edu.dto;

/**
 * XYZ CRUD 교육 항목 응답 DTO입니다.
 *
 * @param educationItemId 교육 항목 ID
 * @param title 항목명
 * @param status 상태 코드
 * @param description 설명
 * @param createdAt 생성 일시
 * @param categoryCode 분류 코드
 * @param ownerMemberNo 예시 소유 회원 번호
 */
public record XyzCrudEducationResponse(
        Long educationItemId,
        String title,
        String status,
        String description,
        String createdAt,
        String categoryCode,
        String ownerMemberNo) {
}
