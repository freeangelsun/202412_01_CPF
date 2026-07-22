package com.cpf.core.common.security;

/**
 * 전자서명 생성/검증 port입니다.
 */
public interface CpfSignaturePort {

    byte[] sign(CpfCredentialRef credentialRef, byte[] payload);

    boolean verify(CpfCredentialRef credentialRef, byte[] payload, byte[] signature);
}
