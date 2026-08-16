package com.cpf.platform.operations.observability.api.logging.policy;

/** ENCRYPTED_BODY 모드에서 원문을 제품 Key Provider로 보호하는 확장 Port입니다. */
public interface CpfPayloadProtectionPort {
    ProtectedPayload protect(String plaintext, String maskingPolicyKey);
    record ProtectedPayload(String algorithm, String keyReference, String ciphertext) {
        public ProtectedPayload {
            if (algorithm == null || algorithm.isBlank() || keyReference == null || keyReference.isBlank()
                    || ciphertext == null || ciphertext.isBlank()) {
                throw new IllegalArgumentException("보호 Payload 결과가 완전하지 않습니다.");
            }
        }
    }
}
