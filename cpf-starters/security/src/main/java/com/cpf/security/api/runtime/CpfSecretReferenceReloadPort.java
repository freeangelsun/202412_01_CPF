package com.cpf.security.api.runtime;
import java.util.Set;
/** 실제 Secret cache/provider reference를 reload하고 적용 hash를 반환하는 SPI입니다. */
public interface CpfSecretReferenceReloadPort { String reloadSecretReferences(Set<String> credentialReferences, long desiredVersion); }
