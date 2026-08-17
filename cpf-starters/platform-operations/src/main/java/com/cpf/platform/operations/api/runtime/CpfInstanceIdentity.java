package com.cpf.platform.operations.api.runtime;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;

/**
 * 현재 CPF Runtime instance의 안정적인 식별 정보를 제공하는 Public API입니다.
 *
 * <p>instanceId는 프로세스 기동 시 한 번 확정합니다. {@code cpf.runtime.instance-id}/{@code CPF_RUNTIME_INSTANCE_ID}가 명시되면
 * 그 값을 사용하고, 없으면 현재 Runtime hostname을 사용합니다. processId와 현재 threadName은
 * 관측 보조정보일 뿐 instanceId 구성요소가 아닙니다.</p>
 */
public final class CpfInstanceIdentity {
    private static final String HOST_NAME = resolveHostName();
    private static final String PROCESS_ID = resolveProcessId();
    private static final String INSTANCE_ID = resolveInstanceId();

    private CpfInstanceIdentity() {
    }

    /** Process 생명주기 동안 동일한 Runtime Identity를 조회합니다. */
    public static Identity current() {
        return new Identity(INSTANCE_ID, HOST_NAME, PROCESS_ID, Thread.currentThread().getName());
    }

    /** 현재 Process/WAS의 확정된 instanceId를 반환합니다. */
    public static String instanceId() {
        return INSTANCE_ID;
    }

    /** 현재 Runtime Hostname을 반환합니다. */
    public static String hostName() {
        return HOST_NAME;
    }

    private static String resolveInstanceId() {
        String configured = System.getProperty("cpf.runtime.instance-id");
        if (!hasText(configured)) configured = System.getenv("CPF_RUNTIME_INSTANCE_ID");
        return hasText(configured) ? configured.trim() : HOST_NAME;
    }

    private static String resolveHostName() {
        try {
            String host = InetAddress.getLocalHost().getHostName();
            if (hasText(host)) return host.trim();
        // Hostname 확보 실패를 조용히 합성값으로 대체하지 않고 상위 Runtime Identity 초기화에서 fail-closed로 처리합니다.
        } catch (Exception ignored) {
            // Environment fallbacks below cover common container/Windows runtime host exposure.
        }
        String environmentHost = System.getenv("HOSTNAME");
        if (!hasText(environmentHost)) environmentHost = System.getenv("COMPUTERNAME");
        if (hasText(environmentHost)) return environmentHost.trim();
        throw new IllegalStateException("CPF Runtime hostname을 확인할 수 없습니다. cpf.runtime.instance-id 또는 CPF_RUNTIME_INSTANCE_ID를 명시해야 합니다.");
    }

    private static String resolveProcessId() {
        String runtimeName = ManagementFactory.getRuntimeMXBean().getName();
        int separator = runtimeName.indexOf('@');
        return separator > 0 ? runtimeName.substring(0, separator) : runtimeName;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    /** 기동 시 확정된 instanceId와 Host/Process 정보를 변경 없이 전달하는 Runtime Identity 값입니다. */
    public record Identity(String instanceId, String hostName, String processId, String threadName) {
    }
}
