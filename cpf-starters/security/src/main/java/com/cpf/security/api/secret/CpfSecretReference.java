package com.cpf.security.api.secret;

/** 실제 Secret 값 대신 Provider/Key만 전달하는 공개 참조 계약. */
public record CpfSecretReference(String provider, String key) {
    public CpfSecretReference {
        if (provider == null || provider.isBlank()) throw new IllegalArgumentException("provider는 필수입니다.");
        if (key == null || key.isBlank()) throw new IllegalArgumentException("key는 필수입니다.");
        provider = provider.trim();
        key = key.trim();
    }

    @Override public String toString() { return provider + ":" + key; }
}
