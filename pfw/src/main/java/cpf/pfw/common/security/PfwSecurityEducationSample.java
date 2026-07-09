package cpf.pfw.common.security;

import java.util.Map;
import java.util.Set;

/**
 * secret 원문을 코드와 로그에 남기지 않는 보안 contract 샘플입니다.
 */
public class PfwSecurityEducationSample {

    public CredentialReference credentialReference(String provider, String keyAlias) {
        if (provider == null || provider.isBlank()) {
            throw new IllegalArgumentException("provider는 필수입니다.");
        }
        return new CredentialReference(provider, keyAlias, Set.of("READ"), Map.of("exposeRawSecret", "false"));
    }

    public record CredentialReference(
            String provider,
            String keyAlias,
            Set<String> scopes,
            Map<String, String> policy) {
    }
}
