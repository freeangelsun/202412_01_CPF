package cpf.pfw.common.security;

/**
 * OAuth/JWT token 발급 port입니다.
 */
public interface CpfTokenProviderPort {

    CpfTokenResult issueToken(CpfTokenRequest tokenRequest);
}
