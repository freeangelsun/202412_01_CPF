package cpf.exs.edu;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * EXS 대외 연계 교육 샘플의 필수 식별자를 관리한다.
 *
 * <p>EXS 샘플은 실제 기관 연계 런타임 없이도 adapter, endpoint, 인증 참조,
 * 재시도, unknown result, 대사, 파일 송수신 후보를 추적할 수 있어야 한다.</p>
 */
public final class ExsEducationCoverageCatalog {

    private static final Set<String> REQUIRED_SAMPLE_IDS = Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(
            "EXS-EDU-FIXED-001",
            "EXS-EDU-FIXED-002",
            "EXS-EDU-FIXED-003",
            "EXS-EDU-FIXED-004",
            "EXS-EDU-ENDPOINT-001",
            "EXS-EDU-ENDPOINT-002",
            "EXS-EDU-AUTH-001",
            "EXS-EDU-AUTH-002",
            "EXS-EDU-AUTH-003",
            "EXS-EDU-CALL-001",
            "EXS-EDU-RETRY-001",
            "EXS-EDU-RETRY-002",
            "EXS-EDU-RETRY-003",
            "EXS-EDU-RETRY-004",
            "EXS-EDU-UNKNOWN-001",
            "EXS-EDU-RECON-001",
            "EXS-EDU-LEDGER-001",
            "EXS-EDU-IDEMP-001",
            "EXS-EDU-FILE-001",
            "EXS-EDU-FILE-002"
    )));

    private ExsEducationCoverageCatalog() {
    }

    public static Set<String> requiredSampleIds() {
        return REQUIRED_SAMPLE_IDS;
    }
}
