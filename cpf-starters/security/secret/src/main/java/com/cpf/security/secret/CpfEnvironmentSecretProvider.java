package com.cpf.security.secret;

import com.cpf.security.api.secret.CpfSecretMetadata;
import com.cpf.security.api.secret.CpfSecretProvider;
import com.cpf.security.api.secret.CpfSecretReference;
import com.cpf.security.api.secret.CpfSecretValue;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Pattern;

/** Read-only Secret Provider backed by exact process-environment variable names. */
public final class CpfEnvironmentSecretProvider implements CpfSecretProvider {
    public static final String PROVIDER_ID = "env";
    private static final Pattern SAFE_KEY = Pattern.compile("[A-Z][A-Z0-9_]{0,127}");
    private final Function<String, String> lookup;

    public CpfEnvironmentSecretProvider() {
        this(System::getenv);
    }

    CpfEnvironmentSecretProvider(Function<String, String> lookup) {
        this.lookup = Objects.requireNonNull(lookup, "lookup");
    }

    @Override
    public String providerId() {
        return PROVIDER_ID;
    }

    @Override
    public CpfSecretMetadata metadata(CpfSecretReference reference) {
        requireReference(reference);
        requireValue(reference.key());
        return new CpfSecretMetadata(reference, "PROCESS_ENVIRONMENT", null, null, false,
                Map.of("source", "process-environment", "mutable", "false"));
    }

    @Override
    public CpfSecretValue resolve(CpfSecretReference reference) {
        requireReference(reference);
        return new CpfSecretValue(requireValue(reference.key()).toCharArray());
    }

    private void requireReference(CpfSecretReference reference) {
        Objects.requireNonNull(reference, "reference");
        if (!PROVIDER_ID.equals(reference.provider())) {
            throw new IllegalArgumentException("Environment provider requires provider id 'env'.");
        }
        if (!SAFE_KEY.matcher(reference.key()).matches()) {
            throw new IllegalArgumentException("Environment secret key must be an exact upper-case variable name.");
        }
    }

    private String requireValue(String key) {
        String value = lookup.apply(key);
        if (value == null || value.isEmpty()) {
            throw new IllegalStateException("Environment secret is not configured: " + key);
        }
        return value;
    }
}
