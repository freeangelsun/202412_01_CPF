package com.cpf.core.api.security.crypto;
/** CpfCryptoOperations 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public interface CpfCryptoOperations {
    CpfEnvelopeCiphertext encrypt(byte[] plaintext, byte[] additionalAuthenticatedData);
    byte[] decrypt(CpfEnvelopeCiphertext ciphertext, byte[] additionalAuthenticatedData);
    CpfRekeyResult rekey(CpfEnvelopeCiphertext ciphertext, byte[] additionalAuthenticatedData, String targetKeyVersion);
    String searchableToken(byte[] normalizedValue, String keyVersion);
    String activeKeyVersion();
}
