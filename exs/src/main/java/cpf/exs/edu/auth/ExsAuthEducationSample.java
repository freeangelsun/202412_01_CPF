package cpf.exs.edu.auth;

import cpf.pfw.common.security.PfwSecurityEducationSample;

/**
 * 대외 인증정보는 secret 원문이 아니라 PFW credential reference를 사용하는 샘플입니다.
 */
public class ExsAuthEducationSample {

    public PfwSecurityEducationSample.CredentialReference credential(String institutionCode) {
        return new PfwSecurityEducationSample().credentialReference("vault", "exs/" + institutionCode + "/client");
    }
}
