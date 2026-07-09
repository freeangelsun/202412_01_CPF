package cpf.adm.edu;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * ADM 운영 교육 샘플의 필수 식별자를 관리한다.
 *
 * <p>ADM 샘플은 운영자가 조회하고 판단해야 하는 거래, 서비스 호출, 레지스트리,
 * 배치, 파일, broker, runtime 상태를 추적한다.</p>
 */
public final class AdmEducationCoverageCatalog {

    private static final Set<String> REQUIRED_SAMPLE_IDS = Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(
            "ADM-EDU-OPR-001",
            "ADM-EDU-OPR-002",
            "ADM-EDU-OPR-003",
            "ADM-EDU-OPR-004",
            "ADM-EDU-OPR-005",
            "ADM-EDU-OPR-006",
            "ADM-EDU-OPR-007",
            "ADM-EDU-OPR-008",
            "ADM-EDU-OPR-009",
            "ADM-EDU-OPR-010"
    )));

    private AdmEducationCoverageCatalog() {
    }

    public static Set<String> requiredSampleIds() {
        return REQUIRED_SAMPLE_IDS;
    }
}
