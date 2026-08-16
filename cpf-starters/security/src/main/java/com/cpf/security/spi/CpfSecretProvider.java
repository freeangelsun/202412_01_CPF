package com.cpf.security.spi;

/**
 * Resolves an opaque secret reference through a deployment-owned secret manager/KMS bridge.
 * Implementations must not log, cache to evidence, or expose resolved secret values.
 */
@FunctionalInterface
/** CpfSecretProvider 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public interface CpfSecretProvider {
    String resolveSecret(String secretRef);
}
