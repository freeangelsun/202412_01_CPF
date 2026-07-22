package cpf.pfw.common.security;

/**
 * payload 암복호화 port입니다.
 */
public interface CpfEncryptionPort {

    byte[] encrypt(CpfCredentialRef credentialRef, byte[] plain);

    byte[] decrypt(CpfCredentialRef credentialRef, byte[] encrypted);
}
