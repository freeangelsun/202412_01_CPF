package com.cpf.platform.operations.observability.api.logging;

/** 로그/감사에 사용할 현재 서버 인스턴스 식별 정보를 제공하는 공개 facade입니다. */
public final class CpfServerIdentity {
    private CpfServerIdentity() { }

    public static Identity current() {
        com.cpf.platform.operations.observability.internal.logging.ServerInstanceIdentity.Identity current =
                com.cpf.platform.operations.observability.internal.logging.ServerInstanceIdentity.current();
        return new Identity(current.serverInstanceId(), current.hostName(), current.processId(), current.threadName());
    }

    /** Identity 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
    public record Identity(String serverInstanceId, String hostName, String processId, String threadName) { }
}
