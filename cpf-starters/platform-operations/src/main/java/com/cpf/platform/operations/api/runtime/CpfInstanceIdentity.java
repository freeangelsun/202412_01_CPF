package com.cpf.platform.operations.api.runtime;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;

/** 운영 로그/헤더에 사용할 실행 인스턴스 식별 Public API입니다. */
public final class CpfInstanceIdentity {
    private CpfInstanceIdentity() {}
    public static Identity current() {
        String host = host();
        String pid = pid();
        String configured = first(System.getenv("CPF_INSTANCE_ID"), System.getenv("SERVER_INSTANCE_ID"));
        return new Identity(text(configured) ? configured : host + ":" + pid, host, pid, Thread.currentThread().getName());
    }
    private static String host(){ try{return InetAddress.getLocalHost().getHostName();}catch(Exception ex){return "unknown-host";} }
    private static String pid(){ String value=ManagementFactory.getRuntimeMXBean().getName(); int at=value.indexOf('@'); return at>0?value.substring(0,at):value; }
    private static boolean text(String v){return v!=null&&!v.isBlank();}
    private static String first(String a,String b){return text(a)?a:b;}
    /** Identity 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
    public record Identity(String serverInstanceId,String hostName,String processId,String threadName) {}
}
