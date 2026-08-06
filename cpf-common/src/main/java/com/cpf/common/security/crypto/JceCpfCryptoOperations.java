package com.cpf.common.security.crypto;

import com.cpf.core.api.security.crypto.*;
import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;

/** JDK provider implementation; provider runtime remains outside cpf-core. */
public final class JceCpfCryptoOperations implements CpfCryptoOperations {
    private static final String TRANSFORMATION="AES/GCM/NoPadding";
    private static final int TAG_BITS=128;
    private final Map<String, SecretKey> keyEncryptionKeys;
    private final String activeKeyVersion;
    private final CpfCryptoPolicy policy;
    private final SecureRandom random;

    public JceCpfCryptoOperations(Map<String, byte[]> rawKeys, String activeKeyVersion, CpfCryptoPolicy policy) {
        this(rawKeys, activeKeyVersion, policy, new SecureRandom());
    }
    public JceCpfCryptoOperations(Map<String, byte[]> rawKeys, String activeKeyVersion, CpfCryptoPolicy policy, SecureRandom random) {
        Objects.requireNonNull(rawKeys,"rawKeys"); this.policy=Objects.requireNonNull(policy,"policy"); this.random=Objects.requireNonNull(random,"random");
        Map<String,SecretKey> keys=new LinkedHashMap<>();
        rawKeys.forEach((version,bytes)->{ if(version==null||version.isBlank()||bytes==null) throw new IllegalArgumentException("key version/value required"); policy.assertAllowed(TRANSFORMATION,"SUNJCE",bytes.length*8); keys.put(version,new SecretKeySpec(bytes.clone(),"AES")); });
        if(!keys.containsKey(activeKeyVersion)) throw new IllegalArgumentException("Active key version is not configured: "+activeKeyVersion);
        this.keyEncryptionKeys=Map.copyOf(keys); this.activeKeyVersion=activeKeyVersion;
    }
    @Override public CpfEnvelopeCiphertext encrypt(byte[] plaintext, byte[] aad){
        Objects.requireNonNull(plaintext,"plaintext"); byte[] safeAad=safe(aad); policy.assertAllowed(TRANSFORMATION,"SUNJCE",256);
        try {
            KeyGenerator generator=KeyGenerator.getInstance("AES"); generator.init(256,random); SecretKey dataKey=generator.generateKey();
            byte[] payloadNonce=nonce(); byte[] encryptedPayload=crypt(Cipher.ENCRYPT_MODE,dataKey,payloadNonce,safeAad,plaintext);
            byte[] keyNonce=nonce(); byte[] keyAad=concat(safeAad,activeKeyVersion.getBytes(StandardCharsets.UTF_8));
            byte[] encryptedKey=crypt(Cipher.ENCRYPT_MODE,keyEncryptionKeys.get(activeKeyVersion),keyNonce,keyAad,dataKey.getEncoded());
            return new CpfEnvelopeCiphertext(TRANSFORMATION,"SunJCE",activeKeyVersion,encryptedKey,keyNonce,payloadNonce,encryptedPayload,sha256(safeAad));
        } catch(GeneralSecurityException ex){ throw new IllegalStateException("Envelope encryption failed",ex); }
    }
    @Override public byte[] decrypt(CpfEnvelopeCiphertext value, byte[] aad){
        Objects.requireNonNull(value,"ciphertext"); byte[] safeAad=safe(aad); policy.assertAllowed(value.algorithm(),value.provider(),256);
        if(!MessageDigest.isEqual(value.aadHash(),sha256(safeAad))) throw new SecurityException("AAD mismatch");
        SecretKey kek=keyEncryptionKeys.get(value.keyVersion()); if(kek==null) throw new IllegalStateException("Unknown key version: "+value.keyVersion());
        try {
            byte[] keyAad=concat(safeAad,value.keyVersion().getBytes(StandardCharsets.UTF_8));
            byte[] rawDataKey=crypt(Cipher.DECRYPT_MODE,kek,value.encryptedDataKeyNonce(),keyAad,value.encryptedDataKey());
            return crypt(Cipher.DECRYPT_MODE,new SecretKeySpec(rawDataKey,"AES"),value.payloadNonce(),safeAad,value.ciphertext());
        } catch(AEADBadTagException ex){ throw new SecurityException("Ciphertext authentication failed",ex); }
        catch(GeneralSecurityException ex){ throw new IllegalStateException("Envelope decryption failed",ex); }
    }
    @Override public CpfRekeyResult rekey(CpfEnvelopeCiphertext value, byte[] aad, String targetKeyVersion){
        Objects.requireNonNull(targetKeyVersion,"targetKeyVersion"); SecretKey target=keyEncryptionKeys.get(targetKeyVersion); if(target==null) throw new IllegalArgumentException("Unknown target key version: "+targetKeyVersion);
        byte[] safeAad=safe(aad); SecretKey current=keyEncryptionKeys.get(value.keyVersion()); if(current==null) throw new IllegalStateException("Unknown current key version");
        try {
            byte[] raw=crypt(Cipher.DECRYPT_MODE,current,value.encryptedDataKeyNonce(),concat(safeAad,value.keyVersion().getBytes(StandardCharsets.UTF_8)),value.encryptedDataKey());
            byte[] nonce=nonce(); byte[] wrapped=crypt(Cipher.ENCRYPT_MODE,target,nonce,concat(safeAad,targetKeyVersion.getBytes(StandardCharsets.UTF_8)),raw);
            CpfEnvelopeCiphertext rekeyed=new CpfEnvelopeCiphertext(value.algorithm(),value.provider(),targetKeyVersion,wrapped,nonce,value.payloadNonce(),value.ciphertext(),value.aadHash());
            return new CpfRekeyResult(value.keyVersion(),targetKeyVersion,rekeyed);
        } catch(GeneralSecurityException ex){ throw new IllegalStateException("Envelope rekey failed",ex); }
    }
    @Override public String searchableToken(byte[] normalizedValue,String keyVersion){
        Objects.requireNonNull(normalizedValue,"normalizedValue"); SecretKey key=keyEncryptionKeys.get(keyVersion); if(key==null) throw new IllegalArgumentException("Unknown token key version: "+keyVersion);
        try { Mac mac=Mac.getInstance("HmacSHA256"); mac.init(new SecretKeySpec(key.getEncoded(),"HmacSHA256")); return HexFormat.of().formatHex(mac.doFinal(normalizedValue)); }
        catch(GeneralSecurityException ex){ throw new IllegalStateException("Search token generation failed",ex); }
    }
    @Override public String activeKeyVersion(){ return activeKeyVersion; }
    private byte[] nonce(){ byte[] n=new byte[12]; random.nextBytes(n); return n; }
    private static byte[] crypt(int mode,SecretKey key,byte[] nonce,byte[] aad,byte[] input) throws GeneralSecurityException { Cipher c=Cipher.getInstance(TRANSFORMATION); c.init(mode,key,new GCMParameterSpec(TAG_BITS,nonce)); if(aad.length>0)c.updateAAD(aad); return c.doFinal(input); }
    private static byte[] sha256(byte[] value){ try{return MessageDigest.getInstance("SHA-256").digest(value);}catch(NoSuchAlgorithmException e){throw new IllegalStateException(e);} }
    private static byte[] safe(byte[] v){ return v==null?new byte[0]:v.clone(); }
    private static byte[] concat(byte[] a,byte[] b){ return ByteBuffer.allocate(a.length+b.length).put(a).put(b).array(); }
}
