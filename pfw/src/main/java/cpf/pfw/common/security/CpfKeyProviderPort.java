package cpf.pfw.common.security;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Optional;

/**
 * 서명/암호화 key 조회 port입니다.
 */
public interface CpfKeyProviderPort {

    Optional<PrivateKey> findPrivateKey(CpfCredentialRef credentialRef);

    Optional<PublicKey> findPublicKey(CpfCredentialRef credentialRef);
}
