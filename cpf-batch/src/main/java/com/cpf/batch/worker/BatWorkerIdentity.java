package cpf.bat.worker;

import cpf.pfw.common.logging.ServerInstanceIdentity;

/** 독립 worker 프로세스의 안정적인 식별 정보를 제공합니다. */
public record BatWorkerIdentity(
        String workerId,
        String instanceId,
        String hostName,
        String processId,
        String version) {

    public static BatWorkerIdentity resolve(BatWorkerProperties properties) {
        ServerInstanceIdentity.Identity current = ServerInstanceIdentity.current();
        String workerId = properties.workerId().isBlank()
                ? current.serverInstanceId() + "-worker"
                : properties.workerId();
        return new BatWorkerIdentity(
                workerId,
                current.serverInstanceId(),
                current.hostName(),
                current.processId(),
                properties.version());
    }
}
