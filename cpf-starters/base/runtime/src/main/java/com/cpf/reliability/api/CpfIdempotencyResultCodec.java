package com.cpf.reliability.api;

/** durable 멱등 결과 재생을 위한 명시적 codec 계약입니다. */
public interface CpfIdempotencyResultCodec {
    boolean supports(Class<?> declaredType);
    CpfIdempotencyStore.StoredResult encode(Object value, Class<?> declaredType);
    Object decode(CpfIdempotencyStore.StoredResult stored, Class<?> declaredType);
}
