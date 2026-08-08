package com.cpf.starter.security.secret;
import com.cpf.core.api.security.crypto.CpfKeyProvider;
import java.util.*;
/** Bean-name prefix 또는 single-provider 규칙으로 key provider를 결정합니다. */
final class CpfKeyProviderRouter {
 private final Map<String,CpfKeyProvider> providers;
 CpfKeyProviderRouter(Map<String,CpfKeyProvider> providers){if(providers==null||providers.isEmpty())throw new IllegalArgumentException("key providers required");this.providers=Map.copyOf(providers);}
 CpfKeyProvider provider(String keyId){if(keyId==null||keyId.isBlank())throw new IllegalArgumentException("keyId required");if(providers.size()==1)return providers.values().iterator().next();int colon=keyId.indexOf(':');if(colon>0){CpfKeyProvider p=providers.get(keyId.substring(0,colon));if(p!=null)return p;}throw new IllegalArgumentException("No CpfKeyProvider route for keyId");}
}
