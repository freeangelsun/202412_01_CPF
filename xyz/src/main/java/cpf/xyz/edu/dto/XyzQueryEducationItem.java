package cpf.xyz.edu.dto;

/**
 * 조회 EDU 샘플에서 사용하는 읽기 전용 항목 DTO입니다.
 *
 * @param itemId        샘플 항목 ID
 * @param itemName      화면에 표시할 샘플 항목명
 * @param categoryCode  조회 패턴 분류 코드
 * @param statusCode    사용 상태 코드
 * @param ownerMemberNo 예시 담당 회원 번호. MBR 테이블과 직접 조인하지 않고 값만 보관합니다.
 * @param createdAt     ISO 형태로 변환한 생성 일시 문자열
 */
public record XyzQueryEducationItem(
        Long itemId,
        String itemName,
        String categoryCode,
        String statusCode,
        String ownerMemberNo,
        String createdAt) {
}
