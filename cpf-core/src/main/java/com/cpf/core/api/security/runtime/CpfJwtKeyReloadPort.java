package com.cpf.core.api.security.runtime;
import java.util.Set;
/** 실제 JWT signer/verifier key set을 reload하고 적용 hash를 반환하는 SPI입니다. */
public interface CpfJwtKeyReloadPort { String reloadJwtKeys(Set<String> credentialReferences, long desiredVersion); }
