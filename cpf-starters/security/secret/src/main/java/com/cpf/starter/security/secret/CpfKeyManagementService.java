package com.cpf.starter.security.secret;
import com.cpf.core.api.security.crypto.*;import java.time.Duration;import java.util.Map;
/** Key metadata/health/rotation/revocation convenience API with a bounded provider timeout. */
public final class CpfKeyManagementService implements CpfKeyManagementOperations {
 private final CpfKeyProviderRouter router;private final CpfKeyTimeoutExecutor timeout;
 public CpfKeyManagementService(Map<String,CpfKeyProvider> providers,Duration operationTimeout){this(providers,new CpfKeyTimeoutExecutor(operationTimeout));}
 CpfKeyManagementService(Map<String,CpfKeyProvider> providers,CpfKeyTimeoutExecutor timeout){router=new CpfKeyProviderRouter(providers);this.timeout=java.util.Objects.requireNonNull(timeout);}
 @Override public CpfSigningKey key(String keyId){return timeout.call(()->router.provider(keyId).key(keyId));}
 @Override public CpfKeyStatus health(String keyId){return timeout.call(()->router.provider(keyId).health(keyId));}
 @Override public CpfSigningKey rotate(String tx,String keyId){required(tx);CpfKeyProvider p=router.provider(keyId);if(!(p instanceof CpfKeyLifecycleProvider lifecycle))throw new UnsupportedOperationException("Key provider does not support rotation");return timeout.call(()->lifecycle.rotate(keyId));}
 @Override public CpfSigningKey revoke(String tx,String keyId,String reason){required(tx);if(reason==null||reason.isBlank())throw new IllegalArgumentException("revocation reason required");CpfKeyProvider p=router.provider(keyId);if(!(p instanceof CpfKeyLifecycleProvider lifecycle))throw new UnsupportedOperationException("Key provider does not support revocation");return timeout.call(()->lifecycle.revoke(keyId,reason));}
 private static void required(String tx){if(tx==null||tx.isBlank())throw new IllegalArgumentException("transactionId required");}
}
