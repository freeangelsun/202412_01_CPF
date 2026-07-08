package cpf.pfw.common.security;

import java.util.Map;

/**
 * OAuth/JWT token 발급 요청 후보 DTO입니다.
 */
public record CpfTokenRequest(
        CpfCredentialRef credentialRef,
        String grantType,
        String audience,
        String scope,
        Map<String, String> attributes) {

    public CpfTokenRequest {
        if (credentialRef == null) {
            throw new IllegalArgumentException("credentialRef는 필수입니다.");
        }
        grantType = grantType == null || grantType.isBlank() ? "client_credentials" : grantType;
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
