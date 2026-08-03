package com.cpf.starter.platform.operations.feature.flag.openfeature.internal;

import com.cpf.core.api.featureflag.CpfFeatureFlagContext;
import com.cpf.core.api.featureflag.CpfFeatureFlagResult;
import com.cpf.core.api.featureflag.CpfFeatureFlagValue;
import com.cpf.core.spi.featureflag.CpfFeatureFlagProvider;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/** Internal OpenFeature SDK adapter. Reflection keeps every SDK type behind the CPF SPI. */
public final class CpfOpenFeatureProviderAdapter implements CpfFeatureFlagProvider {
    private final Object client;
    private final long revision;

    public CpfOpenFeatureProviderAdapter(String clientName, long revision) {
        try {
            Class<?> apiType = Class.forName("dev.openfeature.sdk.OpenFeatureAPI");
            Object api = apiType.getMethod("getInstance").invoke(null);
            Object resolved;
            try {
                resolved = apiType.getMethod("getClient", String.class).invoke(api, clientName);
            } catch (NoSuchMethodException ignored) {
                resolved = apiType.getMethod("getClient").invoke(api);
            }
            this.client = resolved;
            this.revision = revision;
        } catch (ReflectiveOperationException error) {
            throw new IllegalStateException("OpenFeature SDK initialization failed", error);
        }
    }

    @Override
    public CpfFeatureFlagResult<CpfFeatureFlagValue> evaluate(
            String key, CpfFeatureFlagValue fallback, CpfFeatureFlagContext context) {
        try {
            Object evaluationContext = toOpenFeatureContext(context);
            Object raw;
            if (fallback instanceof CpfFeatureFlagValue.BooleanValue value) {
                raw = invoke("getBooleanValue", key, value.value(), evaluationContext);
            } else if (fallback instanceof CpfFeatureFlagValue.StringValue value) {
                raw = invoke("getStringValue", key, value.value(), evaluationContext);
            } else if (fallback instanceof CpfFeatureFlagValue.IntegerValue value) {
                raw = invoke("getIntegerValue", key, Math.toIntExact(value.value()), evaluationContext);
            } else if (fallback instanceof CpfFeatureFlagValue.DecimalValue value) {
                raw = invoke("getDoubleValue", key, value.value(), evaluationContext);
            } else {
                throw new IllegalArgumentException("unsupported flag value");
            }
            CpfFeatureFlagValue evaluated = toCpfValue(raw, fallback);
            return new CpfFeatureFlagResult<>(key, evaluated, null, "OPENFEATURE",
                    CpfFeatureFlagResult.Source.PROVIDER, revision, Instant.now());
        } catch (ReflectiveOperationException error) {
            throw new IllegalStateException("OpenFeature evaluation failed", error);
        }
    }

    private static Object toOpenFeatureContext(CpfFeatureFlagContext context)
            throws ReflectiveOperationException {
        Class<?> valueType = Class.forName("dev.openfeature.sdk.Value");
        Constructor<?> stringValue = valueType.getConstructor(String.class);
        Map<String, Object> values = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : context.openFeatureAttributes().entrySet()) {
            values.put(entry.getKey(), stringValue.newInstance(entry.getValue()));
        }
        Class<?> contextType = Class.forName("dev.openfeature.sdk.ImmutableContext");
        return contextType.getConstructor(String.class, Map.class)
                .newInstance(context.targetingKey(), values);
    }

    private Object invoke(String name, String key, Object fallback, Object context)
            throws ReflectiveOperationException {
        Method twoArgument = null;
        for (Method method : client.getClass().getMethods()) {
            if (!method.getName().equals(name)) continue;
            if (method.getParameterCount() == 3
                    && method.getParameterTypes()[0].isAssignableFrom(String.class)
                    && wraps(method.getParameterTypes()[1]).isInstance(fallback)
                    && method.getParameterTypes()[2].isInstance(context)) {
                return method.invoke(client, key, fallback, context);
            }
            if (method.getParameterCount() == 2
                    && method.getParameterTypes()[0].isAssignableFrom(String.class)
                    && wraps(method.getParameterTypes()[1]).isInstance(fallback)) {
                twoArgument = method;
            }
        }
        if (twoArgument != null) {
            // SDK versions without invocation-context overload are accepted only as an explicit fallback.
            return twoArgument.invoke(client, key, fallback);
        }
        throw new NoSuchMethodException(name + "(String, defaultValue, EvaluationContext)");
    }

    private static Class<?> wraps(Class<?> type) {
        if (!type.isPrimitive()) return type;
        if (type == boolean.class) return Boolean.class;
        if (type == int.class) return Integer.class;
        if (type == long.class) return Long.class;
        if (type == double.class) return Double.class;
        return type;
    }

    private static CpfFeatureFlagValue toCpfValue(Object raw, CpfFeatureFlagValue fallback) {
        if (raw instanceof Boolean value) return new CpfFeatureFlagValue.BooleanValue(value);
        if (raw instanceof String value) return new CpfFeatureFlagValue.StringValue(value);
        if (raw instanceof Integer value) return new CpfFeatureFlagValue.IntegerValue(value.longValue());
        if (raw instanceof Long value) return new CpfFeatureFlagValue.IntegerValue(value);
        if (raw instanceof Number value) return new CpfFeatureFlagValue.DecimalValue(value.doubleValue());
        return fallback;
    }

    @Override
    public long revision() {
        return revision;
    }
}
