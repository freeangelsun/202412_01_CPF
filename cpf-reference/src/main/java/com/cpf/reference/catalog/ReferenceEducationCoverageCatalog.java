package cpf.xyz.catalog;

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
            "XYZ Reference-CRUD-001",
            "XYZ Reference-CRUD-002",
            "XYZ Reference-CRUD-003",
            "XYZ Reference-CRUD-004",
            "XYZ Reference-CRUD-005",
            "XYZ Reference-CRUD-006",
            "XYZ Reference-LIST-001",
            "XYZ Reference-LIST-002",
            "XYZ Reference-LIST-003",
            "XYZ Reference-LIST-004",
            "XYZ Reference-LIST-005",
            "XYZ Reference-LIST-006",
            "XYZ Reference-LIST-007",
            "XYZ Reference-PAGE-001",
            "XYZ Reference-PAGE-002",
            "XYZ Reference-PAGE-003",
            "XYZ Reference-PAGE-004",
            "XYZ Reference-DETAIL-001",
            "XYZ Reference-DETAIL-002",
            "XYZ Reference-DETAIL-003",
            "XYZ Reference-DETAIL-004",
            "XYZ Reference-TRX-001",
            "XYZ Reference-TRX-002",
            "XYZ Reference-TRX-003",
            "XYZ Reference-TRX-004",
            "XYZ Reference-TRX-005",
            "XYZ Reference-TRX-006",
            "XYZ Reference-TRX-007",
            "XYZ Reference-CALL-001",
            "XYZ Reference-CALL-002",
            "XYZ Reference-CALL-003",
            "XYZ Reference-CALL-004",
            "XYZ Reference-CALL-005",
            "XYZ Reference-CALL-006",
            "XYZ Reference-CALL-007",
            "XYZ Reference-CALL-008",
            "XYZ Reference-CALL-009",
            "XYZ Reference-CALL-010",
            "XYZ Reference-HEADER-001",
            "XYZ Reference-HEADER-002",
            "XYZ Reference-HEADER-003",
            "XYZ Reference-HEADER-004",
            "XYZ Reference-IDEMP-001",
            "XYZ Reference-IDEMP-002",
            "XYZ Reference-IDEMP-003",
            "XYZ Reference-FAIL-001",
            "XYZ Reference-FAIL-002",
            "XYZ Reference-FAIL-003",
            "XYZ Reference-FAIL-004",
            "XYZ Reference-SEC-001",
            "XYZ Reference-SEC-002",
            "XYZ Reference-SEC-003",
            "XYZ Reference-SEC-004",
            "XYZ Reference-SEC-005",
            "XYZ Reference-AUDIT-001",
            "XYZ Reference-AUDIT-002",
            "XYZ Reference-AUDIT-003",
            "XYZ Reference-VALID-001",
            "XYZ Reference-VALID-002",
            "XYZ Reference-VALID-003",
            "XYZ Reference-VALID-004",
            "XYZ Reference-OPER-001",
            "XYZ Reference-OPER-002",
            "XYZ Reference-AI-001",
            "XYZ Reference-AI-002",
            "XYZ Reference-AI-003",
            "XYZ Reference-AI-004",
            "XYZ Reference-AI-005",
            "XYZ Reference-AI-006",
            "XYZ Reference-ATTACH-001",
            "XYZ Reference-ATTACH-002"
    )));

    private XyzEducationCoverageCatalog() {
    }

    public static Set<String> requiredSampleIds() {
        return REQUIRED_SAMPLE_IDS;
    }
}
