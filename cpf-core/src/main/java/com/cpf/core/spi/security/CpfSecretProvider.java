package com.cpf.core.spi.security;

/**
 * Resolves an opaque secret reference through a deployment-owned secret manager/KMS bridge.
 * Implementations must not log, cache to evidence, or expose resolved secret values.
 */
@FunctionalInterface
public interface CpfSecretProvider {
    String resolveSecret(String secretRef);
}
