package com.cpf.security.api.crypto;
/** Key 조회·상태·회전·폐기를 제공하는 CPF Key Management API입니다. */
public interface CpfKeyManagementOperations {
    CpfSigningKey key(String keyId);
    CpfKeyStatus health(String keyId);
    CpfSigningKey rotate(String transactionId, String keyId);
    CpfSigningKey revoke(String transactionId, String keyId, String reason);
}
