package com.cpf.core.spi.context;

import com.cpf.core.api.context.CpfContext;

/**
 * {@code CpfContexts} facade가 현재 lexical Context를 읽고 범위 바인딩하기 위한 최소 Core 교체점입니다.
 *
 * <p>이 SPI는 Registry/Descriptor/Factory/Component 저장소가 아닙니다. Runtime Provider는
 * nested scope에서 LIFO restore를 보장하고 exception/cancel/thread reuse 뒤 Context를 누출하지 않아야 합니다.
 * 일반 Application/Generated Domain은 이 타입을 직접 사용하지 않습니다.</p>
 */
public interface CpfContextRuntimeProvider {
    /** 동일 Classpath에 Testkit과 기본 Provider가 함께 있을 때 선택 우선순위입니다. */
    default int priority() { return 100; }

    /** 현재 lexical 실행 Context를 반환하며 바인딩이 없으면 {@code null}입니다. */
    CpfContext current();

    /** Context를 현재 범위에 바인딩하고 close 시 반드시 이전 값으로 복원합니다. */
    AutoCloseable bind(CpfContext context);
}
