package com.cpf.starter.messaging.reliability;
import java.util.Map;
import java.util.Set;
/** Enforces schema compatibility before a message is handed to a business consumer. */
public final class CpfMessageCompatibilityGuard {
 private final Map<String,Set<String>> acceptedVersions;
 public CpfMessageCompatibilityGuard(Map<String,Set<String>> acceptedVersions){this.acceptedVersions=Map.copyOf(acceptedVersions);}
 public void verify(Map<String,String> headers){String schema=headers.get("cpf-schema-id");String version=headers.get("cpf-schema-version");if(schema==null||version==null)throw new QuarantineException("Missing CPF message schema headers");Set<String> accepted=acceptedVersions.get(schema);if(accepted==null||!accepted.contains(version))throw new QuarantineException("Unsupported CPF message schema: "+schema+"/"+version);}
 public static final class QuarantineException extends RuntimeException{public QuarantineException(String m){super(m);}}
}
