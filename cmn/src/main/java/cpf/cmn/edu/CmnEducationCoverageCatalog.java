package cpf.cmn.edu;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * CMN 공통 기능 교육 샘플의 필수 식별자를 관리한다.
 *
 * <p>CMN은 프로젝트 공통 영역이므로 프레임워크 엔진을 소유하지 않고,
 * 고정길이, 메시지, 코드, validation, helper 같은 업무 공통 예제를 제공한다.</p>
 */
public final class CmnEducationCoverageCatalog {

    private static final Set<String> REQUIRED_SAMPLE_IDS = Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(
            "CMN-EDU-FIXED-001",
            "CMN-EDU-FIXED-002",
            "CMN-EDU-FIXED-003",
            "CMN-EDU-FIXED-004",
            "CMN-EDU-FIXED-005",
            "CMN-EDU-FIXED-006",
            "CMN-EDU-FIXED-007",
            "CMN-EDU-FIXED-008",
            "CMN-EDU-FIXED-009",
            "CMN-EDU-FIXED-010",
            "CMN-EDU-FIXED-011",
            "CMN-EDU-MSG-001",
            "CMN-EDU-CODE-001",
            "CMN-EDU-VALID-001",
            "CMN-EDU-CONV-001",
            "CMN-EDU-FILE-001",
            "CMN-EDU-FILE-002",
            "CMN-EDU-FIXTURE-001",
            "CMN-EDU-MASK-001"
    )));

    private CmnEducationCoverageCatalog() {
    }

    public static Set<String> requiredSampleIds() {
        return REQUIRED_SAMPLE_IDS;
    }
}
