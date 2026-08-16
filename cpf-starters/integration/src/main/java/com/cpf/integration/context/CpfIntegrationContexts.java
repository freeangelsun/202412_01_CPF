package com.cpf.integration.context;

import com.cpf.integration.internal.context.CpfIntegrationContextRuntime;

/**
 * Integration 실행 중 필요한 전문 Context 접근 Facade입니다.
 * 일반 업무 코드는 Core {@code CpfContexts}만 사용하고, 외부 연계 구현에서만 이 API를 사용합니다.
 */
public final class CpfIntegrationContexts {
    private CpfIntegrationContexts() { }
    public static CpfIntegrationContext current() { return CpfIntegrationContextRuntime.current(); }
    public static CpfIntegrationContext requireCurrent() {
        CpfIntegrationContext current=current();
        if(current==null) throw new IllegalStateException("Managed CPF integration execution has no bound context");
        return current;
    }
}
