package com.cpf.platform.operations.api.runtime;

/**
 * @deprecated Runtime instance identity의 정본은 Foundation Runtime에 있습니다.
 *             기존 플랫폼 Consumer 호환을 위해 위임만 제공하며 신규 하위 모듈은 이 타입에 의존하지 않습니다.
 */
@Deprecated(forRemoval = true)
public final class CpfInstanceIdentity {
    private CpfInstanceIdentity() {}

    public static Identity current() {
        com.cpf.foundation.runtime.CpfInstanceIdentity.Identity value =
                com.cpf.foundation.runtime.CpfInstanceIdentity.current();
        return new Identity(value.instanceId(), value.hostName(), value.processId(), value.threadName());
    }

    /** instanceId 동작은 Foundation Runtime의 canonical instance identity를 기존 플랫폼 Consumer에 위임하는 호환 경계에서 필요한 공개 동작을 수행합니다. */
    public static String instanceId() { return com.cpf.foundation.runtime.CpfInstanceIdentity.instanceId(); }
    /** hostName 동작은 Foundation Runtime의 canonical instance identity를 기존 플랫폼 Consumer에 위임하는 호환 경계에서 필요한 공개 동작을 수행합니다. */
    public static String hostName() { return com.cpf.foundation.runtime.CpfInstanceIdentity.hostName(); }

    /** Identity는 Foundation Runtime의 canonical instance identity를 기존 플랫폼 Consumer에 위임하는 호환 경계입니다. */
    public record Identity(String instanceId, String hostName, String processId, String threadName) {}
}
