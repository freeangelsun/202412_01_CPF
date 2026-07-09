package cpf.bizadm.edu;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * BIZADM 업무관리 교육 샘플의 필수 식별자를 관리한다.
 *
 * <p>BIZADM 샘플은 업무관리 화면에서 자주 쓰는 메뉴, API, 버튼 권한,
 * 감사, 다운로드, 마스킹 정책을 추적한다.</p>
 */
public final class BizAdmEducationCoverageCatalog {

    private static final Set<String> REQUIRED_SAMPLE_IDS = Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(
            "BIZADM-EDU-AUTH-001",
            "BIZADM-EDU-AUTH-002",
            "BIZADM-EDU-AUTH-003",
            "BIZADM-EDU-AUDIT-001",
            "BIZADM-EDU-DOWNLOAD-001",
            "BIZADM-EDU-MASK-001"
    )));

    private BizAdmEducationCoverageCatalog() {
    }

    public static Set<String> requiredSampleIds() {
        return REQUIRED_SAMPLE_IDS;
    }
}
