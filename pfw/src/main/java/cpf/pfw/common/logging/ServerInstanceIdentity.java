package cpf.pfw.common.logging;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;

/**
 * 거래와 배치 로그에 남길 서버 인스턴스 식별값을 제공합니다.
 */
public final class ServerInstanceIdentity {

    private ServerInstanceIdentity() {
    }

    public static Identity current() {
        String hostName = resolveHostName();
        String processId = resolveProcessId();
        String threadName = Thread.currentThread().getName();
        String configured = System.getenv("SERVER_INSTANCE_ID");
        String serverInstanceId = hasText(configured) ? configured : hostName + ":" + processId;
        return new Identity(serverInstanceId, hostName, processId, threadName);
    }

    private static String resolveHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception ex) {
            return "unknown-host";
        }
    }

    private static String resolveProcessId() {
        String runtimeName = ManagementFactory.getRuntimeMXBean().getName();
        int separator = runtimeName.indexOf('@');
        if (separator > 0) {
            return runtimeName.substring(0, separator);
        }
        return runtimeName;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    public record Identity(
            String serverInstanceId,
            String hostName,
            String processId,
            String threadName) {
    }
}
