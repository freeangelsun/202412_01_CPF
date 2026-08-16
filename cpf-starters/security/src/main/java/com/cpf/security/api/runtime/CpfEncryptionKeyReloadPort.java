package com.cpf.security.api.runtime;
import java.util.Set;
/** 실제 암복호화 key set을 reload하고 적용 hash를 반환하는 SPI입니다. */
public interface CpfEncryptionKeyReloadPort { String reloadEncryptionKeys(Set<String> credentialReferences, long desiredVersion); }
