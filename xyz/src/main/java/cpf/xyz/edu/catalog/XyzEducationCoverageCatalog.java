package cpf.xyz.edu.catalog;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * XYZ 교육 샘플의 필수 식별자를 관리한다.
 *
 * <p>이 클래스는 EDU를 별도 배포 모듈로 만들지 않고 XYZ 내부 교육 자산으로 유지하기 위한 기준점이다.
 * 실제 동작 샘플이 보강될 때마다 이 목록, 샘플 소스, 테스트, 문서 매트릭스를 함께 갱신한다.</p>
 */
public final class XyzEducationCoverageCatalog {

    private static final Set<String> REQUIRED_SAMPLE_IDS = Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(
            "XYZ-EDU-CRUD-001",
            "XYZ-EDU-CRUD-002",
            "XYZ-EDU-CRUD-003",
            "XYZ-EDU-CRUD-004",
            "XYZ-EDU-CRUD-005",
            "XYZ-EDU-CRUD-006",
            "XYZ-EDU-LIST-001",
            "XYZ-EDU-LIST-002",
            "XYZ-EDU-LIST-003",
            "XYZ-EDU-LIST-004",
            "XYZ-EDU-LIST-005",
            "XYZ-EDU-LIST-006",
            "XYZ-EDU-LIST-007",
            "XYZ-EDU-PAGE-001",
            "XYZ-EDU-PAGE-002",
            "XYZ-EDU-PAGE-003",
            "XYZ-EDU-PAGE-004",
            "XYZ-EDU-DETAIL-001",
            "XYZ-EDU-DETAIL-002",
            "XYZ-EDU-DETAIL-003",
            "XYZ-EDU-DETAIL-004",
            "XYZ-EDU-TRX-001",
            "XYZ-EDU-TRX-002",
            "XYZ-EDU-TRX-003",
            "XYZ-EDU-TRX-004",
            "XYZ-EDU-TRX-005",
            "XYZ-EDU-TRX-006",
            "XYZ-EDU-TRX-007",
            "XYZ-EDU-CALL-001",
            "XYZ-EDU-CALL-002",
            "XYZ-EDU-CALL-003",
            "XYZ-EDU-CALL-004",
            "XYZ-EDU-CALL-005",
            "XYZ-EDU-CALL-006",
            "XYZ-EDU-CALL-007",
            "XYZ-EDU-CALL-008",
            "XYZ-EDU-CALL-009",
            "XYZ-EDU-CALL-010",
            "XYZ-EDU-HEADER-001",
            "XYZ-EDU-HEADER-002",
            "XYZ-EDU-HEADER-003",
            "XYZ-EDU-HEADER-004",
            "XYZ-EDU-IDEMP-001",
            "XYZ-EDU-IDEMP-002",
            "XYZ-EDU-IDEMP-003",
            "XYZ-EDU-FAIL-001",
            "XYZ-EDU-FAIL-002",
            "XYZ-EDU-FAIL-003",
            "XYZ-EDU-FAIL-004",
            "XYZ-EDU-SEC-001",
            "XYZ-EDU-SEC-002",
            "XYZ-EDU-SEC-003",
            "XYZ-EDU-SEC-004",
            "XYZ-EDU-SEC-005",
            "XYZ-EDU-AUDIT-001",
            "XYZ-EDU-AUDIT-002",
            "XYZ-EDU-AUDIT-003",
            "XYZ-EDU-VALID-001",
            "XYZ-EDU-VALID-002",
            "XYZ-EDU-VALID-003",
            "XYZ-EDU-VALID-004",
            "XYZ-EDU-OPER-001",
            "XYZ-EDU-OPER-002",
            "XYZ-EDU-AI-001",
            "XYZ-EDU-AI-002",
            "XYZ-EDU-AI-003",
            "XYZ-EDU-AI-004",
            "XYZ-EDU-AI-005",
            "XYZ-EDU-AI-006",
            "XYZ-EDU-ATTACH-001",
            "XYZ-EDU-ATTACH-002"
    )));

    private XyzEducationCoverageCatalog() {
    }

    public static Set<String> requiredSampleIds() {
        return REQUIRED_SAMPLE_IDS;
    }
}
