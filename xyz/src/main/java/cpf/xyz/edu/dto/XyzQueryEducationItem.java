package cpf.xyz.edu.dto;

/**
 * 조회 EDU에서 사용하는 읽기 전용 항목 DTO입니다.
 */
public record XyzQueryEducationItem(
        Long itemId,
        String itemName,
        String categoryCode,
        String statusCode,
        String ownerMemberNo,
        String createdAt) {
}
