package com.cpf.security.api.crypto;

import java.util.Objects;

/** Provider-neutral envelope encrypted payload. */
/** CpfEnvelopeCiphertext 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public record CpfEnvelopeCiphertext(
        String algorithm,
        String provider,
        String keyVersion,
        byte[] encryptedDataKey,
        byte[] encryptedDataKeyNonce,
        byte[] payloadNonce,
        byte[] ciphertext,
        byte[] aadHash) {
    public CpfEnvelopeCiphertext {
        algorithm = require(algorithm, "algorithm");
        provider = require(provider, "provider");
        keyVersion = require(keyVersion, "keyVersion");
        encryptedDataKey = copy(encryptedDataKey, "encryptedDataKey");
        encryptedDataKeyNonce = copy(encryptedDataKeyNonce, "encryptedDataKeyNonce");
        payloadNonce = copy(payloadNonce, "payloadNonce");
        ciphertext = copy(ciphertext, "ciphertext");
        aadHash = copy(aadHash, "aadHash");
    }
    @Override public byte[] encryptedDataKey(){ return encryptedDataKey.clone(); }
    @Override public byte[] encryptedDataKeyNonce(){ return encryptedDataKeyNonce.clone(); }
    @Override public byte[] payloadNonce(){ return payloadNonce.clone(); }
    @Override public byte[] ciphertext(){ return ciphertext.clone(); }
    @Override public byte[] aadHash(){ return aadHash.clone(); }
    private static byte[] copy(byte[] value,String name){ Objects.requireNonNull(value,name); if(value.length==0) throw new IllegalArgumentException(name+" must not be empty"); return value.clone(); }
    private static String require(String value,String name){ if(value==null||value.isBlank()) throw new IllegalArgumentException(name+" must not be blank"); return value.trim(); }
}
