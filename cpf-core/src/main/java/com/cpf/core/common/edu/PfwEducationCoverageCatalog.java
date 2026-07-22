package cpf.pfw.common.edu;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * PFW 프레임워크 capability 교육 샘플의 필수 식별자를 관리한다.
 *
 * <p>PFW는 프레임워크 코어 영역이므로 broker, file transfer, security, runtime,
 * service call 같은 capability를 공통 API와 테스트 지원 자산으로 제공한다.</p>
 */
public final class PfwEducationCoverageCatalog {

    private static final Set<String> REQUIRED_SAMPLE_IDS = Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(
            "PFW-EDU-CALL-001",
            "PFW-EDU-CALL-002",
            "PFW-EDU-CALL-003",
            "PFW-EDU-CALL-004",
            "PFW-EDU-CALL-005",
            "PFW-EDU-CALL-006",
            "PFW-EDU-CALL-007",
            "PFW-EDU-CALL-008",
            "PFW-EDU-CALL-009",
            "PFW-EDU-CALL-010",
            "PFW-EDU-BROKER-001",
            "PFW-EDU-BROKER-002",
            "PFW-EDU-BROKER-003",
            "PFW-EDU-BROKER-004",
            "PFW-EDU-BROKER-005",
            "PFW-EDU-BROKER-006",
            "PFW-EDU-BROKER-007",
            "PFW-EDU-BROKER-008",
            "PFW-EDU-FILE-001",
            "PFW-EDU-FILE-002",
            "PFW-EDU-FILE-003",
            "PFW-EDU-FILE-004",
            "PFW-EDU-FILE-005",
            "PFW-EDU-FILE-006",
            "PFW-EDU-SEC-001",
            "PFW-EDU-SEC-002",
            "PFW-EDU-SEC-003",
            "PFW-EDU-SEC-004",
            "PFW-EDU-SEC-005",
            "PFW-EDU-RUNTIME-001",
            "PFW-EDU-RUNTIME-002",
            "PFW-EDU-RUNTIME-003",
            "PFW-EDU-RUNTIME-004",
            "PFW-EDU-RUNTIME-005",
            "PFW-EDU-RUNTIME-006",
            "PFW-EDU-ADMIN-001",
            "PFW-EDU-AUDIT-001",
            "PFW-EDU-MASK-001"
    )));

    private PfwEducationCoverageCatalog() {
    }

    public static Set<String> requiredSampleIds() {
        return REQUIRED_SAMPLE_IDS;
    }
}
