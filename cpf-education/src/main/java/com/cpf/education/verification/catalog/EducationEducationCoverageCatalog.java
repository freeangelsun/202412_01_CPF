package com.cpf.education.verification.catalog;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * EDU 교육 샘플의 필수 식별자를 관리한다.
 *
 * <p>이 클래스는 EDU를 별도 배포 모듈로 만들지 않고 EDU 내부 교육 자산으로 유지하기 위한 기준점이다.
 * 실제 동작 샘플이 보강될 때마다 이 목록, 샘플 소스, 테스트, 문서 매트릭스를 함께 갱신한다.</p>
 */
public final class EducationEducationCoverageCatalog {

    private static final Set<String> REQUIRED_SAMPLE_IDS = Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(
            "EDU Education-CRUD-001",
            "EDU Education-CRUD-002",
            "EDU Education-CRUD-003",
            "EDU Education-CRUD-004",
            "EDU Education-CRUD-005",
            "EDU Education-CRUD-006",
            "EDU Education-LIST-001",
            "EDU Education-LIST-002",
            "EDU Education-LIST-003",
            "EDU Education-LIST-004",
            "EDU Education-LIST-005",
            "EDU Education-LIST-006",
            "EDU Education-LIST-007",
            "EDU Education-PAGE-001",
            "EDU Education-PAGE-002",
            "EDU Education-PAGE-003",
            "EDU Education-PAGE-004",
            "EDU Education-DETAIL-001",
            "EDU Education-DETAIL-002",
            "EDU Education-DETAIL-003",
            "EDU Education-DETAIL-004",
            "EDU Education-TRX-001",
            "EDU Education-TRX-002",
            "EDU Education-TRX-003",
            "EDU Education-TRX-004",
            "EDU Education-TRX-005",
            "EDU Education-TRX-006",
            "EDU Education-TRX-007",
            "EDU Education-CALL-001",
            "EDU Education-CALL-002",
            "EDU Education-CALL-003",
            "EDU Education-CALL-004",
            "EDU Education-CALL-005",
            "EDU Education-CALL-006",
            "EDU Education-CALL-007",
            "EDU Education-CALL-008",
            "EDU Education-CALL-009",
            "EDU Education-CALL-010",
            "EDU Education-HEADER-001",
            "EDU Education-HEADER-002",
            "EDU Education-HEADER-003",
            "EDU Education-HEADER-004",
            "EDU Education-IDEMP-001",
            "EDU Education-IDEMP-002",
            "EDU Education-IDEMP-003",
            "EDU Education-FAIL-001",
            "EDU Education-FAIL-002",
            "EDU Education-FAIL-003",
            "EDU Education-FAIL-004",
            "EDU Education-SEC-001",
            "EDU Education-SEC-002",
            "EDU Education-SEC-003",
            "EDU Education-SEC-004",
            "EDU Education-SEC-005",
            "EDU Education-AUDIT-001",
            "EDU Education-AUDIT-002",
            "EDU Education-AUDIT-003",
            "EDU Education-VALID-001",
            "EDU Education-VALID-002",
            "EDU Education-VALID-003",
            "EDU Education-VALID-004",
            "EDU Education-OPER-001",
            "EDU Education-OPER-002",
            "EDU Education-ATTACH-001",
            "EDU Education-ATTACH-002",
            "EDU Education-BATCH-TASKLET-001",
            "EDU Education-BATCH-CHUNK-001",
            "EDU Education-BATCH-RETRY-001",
            "EDU Education-BATCH-LOCK-001",
            "EDU Education-BATCH-RESTART-001",
            "EDU Education-BATCH-SCHEDULE-001",
            "EDU Education-BATCH-IDEMP-001",
            "EDU Education-BATCH-UNKNOWN-001",
            "EDU Education-BATCH-RECON-001",
            "EDU Education-BATCH-ADM-001",
            "EDU Education-CENTER-TARGET-001",
            "EDU Education-CENTER-RESULT-001"
    )));

    private EducationEducationCoverageCatalog() {
    }

    /** requiredSampleIds 작업을 CPF 표준 계약에 따라 수행한다. */
    public static Set<String> requiredSampleIds() {
        return REQUIRED_SAMPLE_IDS;
    }
}
