package com.cpf.core.api.security.crypto;
public record CpfRekeyResult(String previousKeyVersion, String currentKeyVersion, CpfEnvelopeCiphertext ciphertext) {
    public CpfRekeyResult { if(previousKeyVersion==null||currentKeyVersion==null||ciphertext==null) throw new IllegalArgumentException("rekey result values are required"); }
}
