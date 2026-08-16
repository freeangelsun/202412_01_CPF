package com.cpf.security.api.crypto;
/** 업무/감사 Consumer용 digital-signature API입니다. */
public interface CpfDigitalSignatureOperations { CpfDigitalSignature sign(String transactionId,String keyId,String algorithm,byte[] canonicalPayload); boolean verify(String transactionId,byte[] canonicalPayload,CpfDigitalSignature signature); }
