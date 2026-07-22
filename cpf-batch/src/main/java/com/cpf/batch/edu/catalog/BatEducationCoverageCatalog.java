package cpf.bat.edu.catalog;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * BAT 교육 샘플의 필수 식별자를 관리한다.
 *
 * <p>배치 샘플은 단일 tasklet 예제로 끝내지 않고 job, transaction, service call,
 * center-cut, logging, idempotency, reconciliation 영역을 모두 추적한다.</p>
 */
public final class BatEducationCoverageCatalog {

    private static final Set<String> REQUIRED_SAMPLE_IDS = Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(
            "BAT-EDU-JOB-001",
            "BAT-EDU-JOB-002",
            "BAT-EDU-JOB-003",
            "BAT-EDU-JOB-004",
            "BAT-EDU-JOB-005",
            "BAT-EDU-JOB-006",
            "BAT-EDU-JOB-007",
            "BAT-EDU-JOB-008",
            "BAT-EDU-JOB-009",
            "BAT-EDU-TRX-001",
            "BAT-EDU-TRX-002",
            "BAT-EDU-TRX-003",
            "BAT-EDU-TRX-004",
            "BAT-EDU-CALL-001",
            "BAT-EDU-CALL-002",
            "BAT-EDU-CALL-003",
            "BAT-EDU-CALL-004",
            "BAT-EDU-CALL-005",
            "BAT-EDU-CALL-006",
            "BAT-EDU-CENTER-001",
            "BAT-EDU-CENTER-002",
            "BAT-EDU-CENTER-003",
            "BAT-EDU-CENTER-004",
            "BAT-EDU-CENTER-005",
            "BAT-EDU-CENTER-006",
            "BAT-EDU-CENTER-007",
            "BAT-EDU-LOG-001",
            "BAT-EDU-LOG-002",
            "BAT-EDU-LOG-003",
            "BAT-EDU-LOG-004",
            "BAT-EDU-LOG-005",
            "BAT-EDU-IDEMP-001",
            "BAT-EDU-IDEMP-002",
            "BAT-EDU-UNKNOWN-001",
            "BAT-EDU-RECON-001",
            "BAT-EDU-RECON-002"
    )));

    private BatEducationCoverageCatalog() {
    }

    public static Set<String> requiredSampleIds() {
        return REQUIRED_SAMPLE_IDS;
    }
}
