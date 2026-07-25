package com.cpf.core.api.logging;

/** 로그/감사에 사용할 현재 서버 인스턴스 식별 정보를 제공하는 공개 facade입니다. */
public final class CpfServerIdentity {
    private CpfServerIdentity() { }

    public static Identity current() {
        com.cpf.core.common.logging.ServerInstanceIdentity.Identity current =
                com.cpf.core.common.logging.ServerInstanceIdentity.current();
        return new Identity(current.serverInstanceId(), current.hostName(), current.processId(), current.threadName());
    }

    public record Identity(String serverInstanceId, String hostName, String processId, String threadName) { }
}
