package cpf.pfw.common.security;

import java.util.Optional;

/**
 * secret 저장소에서 값을 조회하는 port입니다.
 */
public interface CpfSecretProviderPort {

    Optional<char[]> findSecret(CpfCredentialRef credentialRef);
}
