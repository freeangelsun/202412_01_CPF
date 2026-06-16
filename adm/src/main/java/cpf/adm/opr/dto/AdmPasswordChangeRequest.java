package cpf.adm.opr.dto;

/**
 * ?댁쁺??鍮꾨?踰덊샇 蹂寃??붿껌?낅땲??
 *
 * @param newPassword ??鍮꾨?踰덊샇
 * @param requestUser ?붿껌?? */
public record AdmPasswordChangeRequest(String newPassword, String requestUser) {
}

