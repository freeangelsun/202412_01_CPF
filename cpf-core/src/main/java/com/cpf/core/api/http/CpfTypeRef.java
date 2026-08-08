package com.cpf.core.api.http;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;

/** Provider-neutral generic type token used by CPF HTTP contracts. */
public abstract class CpfTypeRef<T> {
    private final Type type;

    protected CpfTypeRef() {
        Type parent = getClass().getGenericSuperclass();
        if (!(parent instanceof ParameterizedType parameterized)) {
            throw new IllegalStateException("CpfTypeRef requires a generic type parameter");
        }
        this.type = Objects.requireNonNull(parameterized.getActualTypeArguments()[0]);
    }

    public final Type type() { return type; }
}
