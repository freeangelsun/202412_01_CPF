package com.cpf.security.api.runtime;
import java.util.Set;
/** 실제 인증서 Provider/SSL Context를 reload하고 적용 hash를 반환하는 SPI입니다. */
public interface CpfCertificateReloadPort { String reloadCertificates(Set<String> credentialReferences, long desiredVersion); }
