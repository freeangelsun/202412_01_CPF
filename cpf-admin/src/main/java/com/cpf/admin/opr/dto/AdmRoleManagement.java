package cpf.adm.opr.dto;

/**
 * ADM 역할 관리 응답입니다.
 *
 * @param roleId      역할 ID
 * @param roleName    역할명
 * @param roleType    역할 유형
 * @param description 역할 설명
 * @param useYn       사용 여부
 * @param createdAt   등록일시
 * @param updatedAt   수정일시
 */
public record AdmRoleManagement(
        String roleId,
        String roleName,
        String roleType,
        String description,
        String useYn,
        String createdAt,
        String updatedAt) {
}
