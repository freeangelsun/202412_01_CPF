package cpf.acc.service;

import cpf.acc.dto.AccountSearchRequest;
import cpf.acc.port.AccountQueryPort;
import cpf.pfw.common.base.BaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Account 조회 업무를 처리합니다.
 */
@Service
@RequiredArgsConstructor
public class AccountService extends BaseService {
    private final AccountQueryPort queryPort;

    @Transactional(readOnly = true)
    public Map<String, Object> search(AccountSearchRequest request) {
        return queryPort.search(request.normalized());
    }
}