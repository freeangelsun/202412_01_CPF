package com.cpf.education.base;

import com.cpf.data.persistence.api.CpfBaseRepository;

/**
 * Education JDBC/MyBatis 예제가 공통으로 사용하는 2단계 Domain Base Repository입니다.
 *
 * <p>Framework {@link CpfBaseRepository}의 Context/Validation/실행 상관관계 기능을 재사용하고,
 * EDU 예제의 paging/offset 경계만 소유합니다. Vendor별 SQL 분기는 이 Base나 Business Repository에
 * 두지 않고 CPF Data Dialect/SQL Catalog가 소유합니다.</p>
 */
public abstract class EducationBaseRepository extends CpfBaseRepository {
    /** Education DB 예제의 기본 조회 건수입니다. */
    private static final int DEFAULT_PAGE_SIZE = 20;

    /** Education DB 예제의 최대 조회 건수입니다. */
    protected static final int MAX_PAGE_SIZE = 100;

    /** 요청 건수를 안전한 범위로 제한합니다. */
    protected final int normalizeEducationPageSize(int requested) {
        return boundedSize(requested, DEFAULT_PAGE_SIZE, MAX_PAGE_SIZE);
    }

    /**
     * 1부터 시작하는 페이지 번호를 DB offset으로 변환합니다.
     *
     * @param page 1부터 시작하는 페이지 번호
     * @param size 페이지 크기
     * @return 0 이상 offset
     */
    protected final int educationOffset(int page, int size) {
        requireRule(page >= 1, "page는 1 이상이어야 합니다.");
        return Math.multiplyExact(page - 1, normalizeEducationPageSize(size));
    }
}
