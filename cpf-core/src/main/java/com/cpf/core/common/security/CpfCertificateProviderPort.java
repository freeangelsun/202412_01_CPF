package cpf.pfw.common.security;

import java.security.cert.X509Certificate;
import java.util.List;

/**
 * mTLS나 전자서명에 사용하는 인증서 체인 조회 port입니다.
 */
public interface CpfCertificateProviderPort {

    List<X509Certificate> findCertificateChain(CpfCredentialRef credentialRef);
}
