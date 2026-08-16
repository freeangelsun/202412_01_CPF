package com.cpf.reliability.runtime;

import com.cpf.core.api.context.CpfContext;
import com.cpf.reliability.api.CpfIdempotencyException;
import com.cpf.reliability.api.CpfIdempotencyFingerprintResolver;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.temporal.TemporalAccessor;
import java.util.HexFormat;
import java.util.UUID;

/** Context fingerprint를 우선 사용하고, 없을 때는 안전 scalar 인자만 SHA-256으로 요약합니다. */
public final class CpfDefaultIdempotencyFingerprintResolver implements CpfIdempotencyFingerprintResolver {
    @Override
    public String resolve(Method method, Object[] arguments, CpfContext context) {
        if (context.operation() != null && context.operation().payloadFingerprint() != null) {
            return context.operation().payloadFingerprint();
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(method.toGenericString().getBytes(StandardCharsets.UTF_8));
            Object[] args = arguments == null ? new Object[0] : arguments;
            for (Object value : args) {
                if (!safeScalar(value)) {
                    throw new CpfIdempotencyException("CPF_IDEMPOTENCY_FINGERPRINT_REQUIRED",
                            "Complex payload requires a boundary-provided payloadFingerprint or custom resolver");
                }
                digest.update((byte) 0);
                digest.update(String.valueOf(value).getBytes(StandardCharsets.UTF_8));
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (CpfIdempotencyException e) {
            throw e;
        } catch (Exception e) {
            throw new CpfIdempotencyException("CPF_IDEMPOTENCY_FINGERPRINT_FAILED", "Unable to create fingerprint", e);
        }
    }

    private static boolean safeScalar(Object value) {
        return value == null || value instanceof CharSequence || value instanceof Number || value instanceof Boolean
                || value instanceof Enum<?> || value instanceof UUID || value instanceof TemporalAccessor;
    }
}
