package com.cpf.core.api.data.encryption;
/** CpfFieldEncryptionOperations 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public interface CpfFieldEncryptionOperations {
    CpfEncryptedField encrypt(String fieldName, String value, CpfFieldClassification classification, boolean searchable);
    String decrypt(String fieldName, CpfEncryptedField field, String actorId, String reason);
    CpfEncryptedField rekey(String fieldName, CpfEncryptedField field, String targetKeyVersion, String actorId, String reason);
    String mask(String value, CpfFieldClassification classification);
}
