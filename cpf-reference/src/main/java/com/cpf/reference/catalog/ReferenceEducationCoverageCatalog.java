package com.cpf.reference.catalog;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * REF 교육 샘플의 필수 식별자를 관리한다.
 *
 * <p>이 클래스는 EDU를 별도 배포 모듈로 만들지 않고 REF 내부 교육 자산으로 유지하기 위한 기준점이다.
 * 실제 동작 샘플이 보강될 때마다 이 목록, 샘플 소스, 테스트, 문서 매트릭스를 함께 갱신한다.</p>
 */
public final class ReferenceEducationCoverageCatalog {

    private static final Set<String> REQUIRED_SAMPLE_IDS = Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(
            "REF Reference-CRUD-001",
            "REF Reference-CRUD-002",
            "REF Reference-CRUD-003",
            "REF Reference-CRUD-004",
            "REF Reference-CRUD-005",
            "REF Reference-CRUD-006",
            "REF Reference-LIST-001",
            "REF Reference-LIST-002",
            "REF Reference-LIST-003",
            "REF Reference-LIST-004",
            "REF Reference-LIST-005",
            "REF Reference-LIST-006",
            "REF Reference-LIST-007",
            "REF Reference-PAGE-001",
            "REF Reference-PAGE-002",
            "REF Reference-PAGE-003",
            "REF Reference-PAGE-004",
            "REF Reference-DETAIL-001",
            "REF Reference-DETAIL-002",
            "REF Reference-DETAIL-003",
            "REF Reference-DETAIL-004",
            "REF Reference-TRX-001",
            "REF Reference-TRX-002",
            "REF Reference-TRX-003",
            "REF Reference-TRX-004",
            "REF Reference-TRX-005",
            "REF Reference-TRX-006",
            "REF Reference-TRX-007",
            "REF Reference-CALL-001",
            "REF Reference-CALL-002",
            "REF Reference-CALL-003",
            "REF Reference-CALL-004",
            "REF Reference-CALL-005",
            "REF Reference-CALL-006",
            "REF Reference-CALL-007",
            "REF Reference-CALL-008",
            "REF Reference-CALL-009",
            "REF Reference-CALL-010",
            "REF Reference-HEADER-001",
            "REF Reference-HEADER-002",
            "REF Reference-HEADER-003",
            "REF Reference-HEADER-004",
            "REF Reference-IDEMP-001",
            "REF Reference-IDEMP-002",
            "REF Reference-IDEMP-003",
            "REF Reference-FAIL-001",
            "REF Reference-FAIL-002",
            "REF Reference-FAIL-003",
            "REF Reference-FAIL-004",
            "REF Reference-SEC-001",
            "REF Reference-SEC-002",
            "REF Reference-SEC-003",
            "REF Reference-SEC-004",
            "REF Reference-SEC-005",
            "REF Reference-AUDIT-001",
            "REF Reference-AUDIT-002",
            "REF Reference-AUDIT-003",
            "REF Reference-VALID-001",
            "REF Reference-VALID-002",
            "REF Reference-VALID-003",
            "REF Reference-VALID-004",
            "REF Reference-OPER-001",
            "REF Reference-OPER-002",
            "REF Reference-ATTACH-001",
            "REF Reference-ATTACH-002"
    )));

    private ReferenceEducationCoverageCatalog() {
    }

    public static Set<String> requiredSampleIds() {
        return REQUIRED_SAMPLE_IDS;
    }
}
