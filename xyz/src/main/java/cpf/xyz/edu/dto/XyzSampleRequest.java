package cpf.xyz.edu.dto;

/**
 * XYZ CRUD 교육 샘플 등록/수정 요청 DTO입니다.
 *
 * @param title 샘플 제목
 * @param description 샘플 설명
 * @param requestUser 요청 사용자
 */
public record XyzSampleRequest(
        String title,
        String description,
        String requestUser) {
}
