package cpf.pfw.common.runtime;

import java.util.List;

/**
 * runtime 상태 확인 port입니다.
 */
public interface CpfHealthCheckPort {

    CpfRuntimeHealthStatus check(String componentId);

    List<CpfRuntimeHealthStatus> checkAll(String componentType);
}
