package com.cpf.core.api.data.encryption;
public interface CpfFieldEncryptionOperations {
    CpfEncryptedField encrypt(String fieldName, String value, CpfFieldClassification classification, boolean searchable);
    String decrypt(String fieldName, CpfEncryptedField field, String actorId, String reason);
    CpfEncryptedField rekey(String fieldName, CpfEncryptedField field, String targetKeyVersion, String actorId, String reason);
    String mask(String value, CpfFieldClassification classification);
}
