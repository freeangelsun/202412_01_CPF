package cpf.pfw.common.runtime;

import java.time.Duration;
import java.util.List;

/**
 * heartbeat가 끊긴 runtime 후보를 찾는 port입니다.
 */
public interface CpfGhostDetectorPort {

    List<CpfRuntimeGhostCandidate> detect(Duration staleAfter);
}
