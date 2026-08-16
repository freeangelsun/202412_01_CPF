package com.cpf.messaging.reliability.api.jdbc;
import java.util.Map;
import java.util.Set;
/** Enforces schema compatibility before a message is handed to a business consumer. */
/** CpfMessageCompatibilityGuard는 메시징 신뢰성 경계에서 중복 방지·재시도·결과불명 복구 책임을 명확히 수행합니다. */
public final class CpfMessageCompatibilityGuard {
 private final Map<String,Set<String>> acceptedVersions;
 /** CpfMessageCompatibilityGuard 작업을 CPF 메시징 신뢰성 정책과 상태 전이 규칙에 따라 수행합니다. */
 public CpfMessageCompatibilityGuard(Map<String,Set<String>> acceptedVersions){this.acceptedVersions=Map.copyOf(acceptedVersions);}
 /** verify 작업을 CPF 메시징 신뢰성 정책과 상태 전이 규칙에 따라 수행합니다. */
 public void verify(Map<String,String> headers){String schema=headers.get("cpf-schema-id");String version=headers.get("cpf-schema-version");if(schema==null||version==null)throw new QuarantineException("Missing CPF message schema headers");Set<String> accepted=acceptedVersions.get(schema);if(accepted==null||!accepted.contains(version))throw new QuarantineException("Unsupported CPF message schema: "+schema+"/"+version);}
 /** QuarantineException는 메시징 신뢰성 경계에서 중복 방지·재시도·결과불명 복구 책임을 명확히 수행합니다. */
 public static final class QuarantineException extends RuntimeException{public QuarantineException(String m){super(m);}}
}
