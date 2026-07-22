package cpf.adm.opr.dto;

/**
 * ADM MFA OTP 운영 요청입니다.
 *
 * @param secretRef   Vault/KMS 또는 환경변수 secret 참조
 * @param otpCode     운영자가 입력한 OTP 코드
 * @param requestUser 요청자 ID
 * @param reason      감사 사유
 */
public record AdmMfaOtpRequest(
        String secretRef,
        String otpCode,
        String requestUser,
        String reason) {
}
