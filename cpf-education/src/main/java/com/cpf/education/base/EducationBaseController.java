package com.cpf.education.base;

import com.cpf.web.api.CpfBaseController;

/**
 * Education Web 예제가 공통으로 사용하는 2단계 Domain Base Controller입니다.
 *
 * <p>Framework {@link CpfBaseController}의 Context/Validation/응답 계약 위에 EDU 시스템 식별자와
 * 교육 API의 공통 paging 제한을 추가합니다. Business Controller는 이 타입을 직접 상속하여
 * Framework → Domain → Business의 3단 구조를 실제로 사용합니다.</p>
 */
public abstract class EducationBaseController extends CpfBaseController {
    /** Education 모듈의 표준 시스템 코드입니다. */
    protected static final String SYSTEM_CODE = "EDU";

    /** Education API의 기본 페이지 크기입니다. */
    private static final int DEFAULT_PAGE_SIZE = 20;

    /** Education API가 허용하는 최대 페이지 크기입니다. */
    private static final int MAX_PAGE_SIZE = 100;

    /**
     * 요청 페이지 크기를 EDU 운영 한도 안으로 정규화합니다.
     *
     * @param requested 요청된 페이지 크기. 0 이하이면 기본값을 사용합니다.
     * @return 1~100 범위로 정규화된 페이지 크기
     */
    protected final int normalizeEducationPageSize(int requested) {
        if (requested <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        requireRule(requested <= MAX_PAGE_SIZE, "size는 100 이하여야 합니다.");
        return requested;
    }
}
