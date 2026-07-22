package cpf.pfw.common.security;

import java.util.Optional;

/**
 * credential 참조값과 상태를 조회하는 port입니다.
 */
public interface CpfCredentialProviderPort {

    Optional<CpfCredentialStatus> findStatus(CpfCredentialRef credentialRef);

    CpfCredentialValidationResult validate(CpfCredentialRef credentialRef);
}
