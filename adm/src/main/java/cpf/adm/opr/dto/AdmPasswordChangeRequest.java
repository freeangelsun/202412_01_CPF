package cpf.adm.opr.dto;

/**
 * ADM operator password change request.
 */
public record AdmPasswordChangeRequest(String newPassword, String requestUser, String reason) {
}
