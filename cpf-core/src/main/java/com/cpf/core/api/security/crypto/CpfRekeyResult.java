package com.cpf.core.api.security.crypto;
/** CpfRekeyResult 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public record CpfRekeyResult(String previousKeyVersion, String currentKeyVersion, CpfEnvelopeCiphertext ciphertext) {
    public CpfRekeyResult { if(previousKeyVersion==null||currentKeyVersion==null||ciphertext==null) throw new IllegalArgumentException("rekey result values are required"); }
}
