package com.cpf.core.api.security.crypto;
import java.time.Instant;import java.util.Arrays;
/** 서명값과 공개 metadata. signature byte[]는 방어 복사됩니다. */
public record CpfDigitalSignature(String keyId,String keyVersion,String algorithm,String certificateId,byte[] signature,Instant signedAt){public CpfDigitalSignature{signature=signature==null?new byte[0]:Arrays.copyOf(signature,signature.length);signedAt=signedAt==null?Instant.now():signedAt;}@Override public byte[] signature(){return Arrays.copyOf(signature,signature.length);}}
