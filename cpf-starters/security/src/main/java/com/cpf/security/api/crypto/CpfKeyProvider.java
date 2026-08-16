package com.cpf.security.api.crypto;
/** Private Key 원문을 노출하지 않고 서명·검증·상태를 제공하는 KMS/HSM Provider 계약입니다. */
public interface CpfKeyProvider { CpfSigningKey key(String keyId); byte[] sign(String keyId,String algorithm,byte[] payload); boolean verify(String keyId,String algorithm,byte[] payload,byte[] signature); default CpfKeyStatus health(String keyId){return key(keyId).status();} }
