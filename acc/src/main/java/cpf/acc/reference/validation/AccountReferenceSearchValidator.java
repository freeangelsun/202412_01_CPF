package cpf.acc.validation;

import cpf.acc.dto.AccountSearchRequest;
import cpf.pfw.common.exception.CpfValidationException;
import org.springframework.stereotype.Component;

/**
 * Account 조회 API 입력값을 검증합니다.
 */
@Component
public class AccountSearchValidator {
    public void validate(AccountSearchRequest request) {
        if (request == null) {
            throw new CpfValidationException("Account 조회 조건은 필수입니다.");
        }
        if (request.size() != null && request.size() > 200) {
            throw new CpfValidationException("페이지 크기는 200 이하여야 합니다.");
        }
    }
}