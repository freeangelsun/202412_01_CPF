package com.cpf.reliability.api;

import com.cpf.core.api.context.CpfContext;
import java.lang.reflect.Method;

/** 민감 원문을 저장하지 않고 동일 요청 여부를 판정할 fingerprint를 생성하는 계약입니다. */
@FunctionalInterface
public interface CpfIdempotencyFingerprintResolver {
    String resolve(Method method, Object[] arguments, CpfContext context);
}
