package com.cpf.security.api.crypto;
import java.util.Objects;
/** Private key material을 포함하지 않는 signing key metadata입니다. */
public record CpfSigningKey(String keyId,String keyVersion,String algorithm,String certificateId,CpfKeyStatus status){public CpfSigningKey{if(keyId==null||keyId.isBlank())throw new IllegalArgumentException("keyId required");keyVersion=Objects.requireNonNullElse(keyVersion,"");if(algorithm==null||algorithm.isBlank())throw new IllegalArgumentException("algorithm required");certificateId=Objects.requireNonNullElse(certificateId,"");status=Objects.requireNonNullElse(status,CpfKeyStatus.ACTIVE);}}
