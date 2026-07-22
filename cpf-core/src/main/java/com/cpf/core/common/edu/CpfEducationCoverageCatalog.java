package com.cpf.core.common.edu;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * CPF 프레임워크 capability 교육 샘플의 필수 식별자를 관리한다.
 *
 * <p>CPF는 프레임워크 코어 영역이므로 broker, file transfer, security, runtime,
 * service call 같은 capability를 공통 API와 테스트 지원 자산으로 제공한다.</p>
 */
public final class CpfEducationCoverageCatalog {

    private static final Set<String> REQUIRED_SAMPLE_IDS = Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(
            "CPF-EDU-CALL-001",
            "CPF-EDU-CALL-002",
            "CPF-EDU-CALL-003",
            "CPF-EDU-CALL-004",
            "CPF-EDU-CALL-005",
            "CPF-EDU-CALL-006",
            "CPF-EDU-CALL-007",
            "CPF-EDU-CALL-008",
            "CPF-EDU-CALL-009",
            "CPF-EDU-CALL-010",
            "CPF-EDU-BROKER-001",
            "CPF-EDU-BROKER-002",
            "CPF-EDU-BROKER-003",
            "CPF-EDU-BROKER-004",
            "CPF-EDU-BROKER-005",
            "CPF-EDU-BROKER-006",
            "CPF-EDU-BROKER-007",
            "CPF-EDU-BROKER-008",
            "CPF-EDU-FILE-001",
            "CPF-EDU-FILE-002",
            "CPF-EDU-FILE-003",
            "CPF-EDU-FILE-004",
            "CPF-EDU-FILE-005",
            "CPF-EDU-FILE-006",
            "CPF-EDU-SEC-001",
            "CPF-EDU-SEC-002",
            "CPF-EDU-SEC-003",
            "CPF-EDU-SEC-004",
            "CPF-EDU-SEC-005",
            "CPF-EDU-RUNTIME-001",
            "CPF-EDU-RUNTIME-002",
            "CPF-EDU-RUNTIME-003",
            "CPF-EDU-RUNTIME-004",
            "CPF-EDU-RUNTIME-005",
            "CPF-EDU-RUNTIME-006",
            "CPF-EDU-ADMIN-001",
            "CPF-EDU-AUDIT-001",
            "CPF-EDU-MASK-001"
    )));

    private CpfEducationCoverageCatalog() {
    }

    public static Set<String> requiredSampleIds() {
        return REQUIRED_SAMPLE_IDS;
    }
}
