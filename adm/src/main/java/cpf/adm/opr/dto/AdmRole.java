package cpf.adm.opr.dto;

/**
 * ADM 역할 응답입니다.
 *
 * @param roleId      역할 ID
 * @param roleName    역할명
 * @param description 역할 설명
 */
public record AdmRole(String roleId, String roleName, String description) {
}
