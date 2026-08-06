package com.cpf.core.api.security.crypto;
public interface CpfCryptoOperations {
    CpfEnvelopeCiphertext encrypt(byte[] plaintext, byte[] additionalAuthenticatedData);
    byte[] decrypt(CpfEnvelopeCiphertext ciphertext, byte[] additionalAuthenticatedData);
    CpfRekeyResult rekey(CpfEnvelopeCiphertext ciphertext, byte[] additionalAuthenticatedData, String targetKeyVersion);
    String searchableToken(byte[] normalizedValue, String keyVersion);
    String activeKeyVersion();
}
