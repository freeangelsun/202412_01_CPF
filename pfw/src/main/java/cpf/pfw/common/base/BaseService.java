package cpf.pfw.common.base;

import cpf.pfw.common.exception.CpfValidationException;

/**
 * CPF 업무 Service가 사용할 수 있는 안정적인 최소 확장점입니다.
 *
 * <p>트랜잭션 범위와 업무 로직은 각 feature Service가 소유하며, 이 클래스에는 특정 업무나
 * 저장소 의존성을 두지 않습니다.</p>
 */
public abstract class BaseService {

    protected final String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new CpfValidationException(fieldName + " 값은 필수입니다.");
        }
        return value.trim();
    }
}
