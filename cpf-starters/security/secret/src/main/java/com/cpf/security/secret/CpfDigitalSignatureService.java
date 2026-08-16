package com.cpf.security.secret;
import com.cpf.security.api.crypto.*;import java.time.*;import java.util.*;
/** KMS/HSM provider를 사용하고 private key 원문을 취급하지 않는 signature consumer입니다. */
public final class CpfDigitalSignatureService implements CpfDigitalSignatureOperations {
 private final CpfKeyProviderRouter router;private final CpfKeyTimeoutExecutor timeout;private final Clock clock;
 public CpfDigitalSignatureService(Map<String,CpfKeyProvider> providers,Duration operationTimeout){this(providers,new CpfKeyTimeoutExecutor(operationTimeout),Clock.systemUTC());}
 CpfDigitalSignatureService(Map<String,CpfKeyProvider> providers,CpfKeyTimeoutExecutor timeout,Clock clock){this.router=new CpfKeyProviderRouter(providers);this.timeout=Objects.requireNonNull(timeout);this.clock=Objects.requireNonNull(clock);}
 @Override public CpfDigitalSignature sign(String tx,String keyId,String algorithm,byte[] payload){required(tx,"transactionId");required(keyId,"keyId");Objects.requireNonNull(payload,"payload");CpfKeyProvider p=router.provider(keyId);CpfSigningKey k=timeout.call(()->p.key(keyId));if(k.status()!=CpfKeyStatus.ACTIVE&&k.status()!=CpfKeyStatus.ROTATING)throw new IllegalStateException("Signing key unavailable: "+k.status());byte[] sig=timeout.call(()->p.sign(keyId,algorithm,payload));return new CpfDigitalSignature(k.keyId(),k.keyVersion(),algorithm,k.certificateId(),sig,Instant.now(clock));}
 @Override public boolean verify(String tx,byte[] payload,CpfDigitalSignature sig){required(tx,"transactionId");Objects.requireNonNull(payload,"payload");Objects.requireNonNull(sig,"signature");return timeout.call(()->router.provider(sig.keyId()).verify(sig.keyId(),sig.algorithm(),payload,sig.signature()));}
 private static void required(String s,String n){if(s==null||s.isBlank())throw new IllegalArgumentException(n+" required");}
}
