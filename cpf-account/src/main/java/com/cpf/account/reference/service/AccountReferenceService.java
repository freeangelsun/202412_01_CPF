package cpf.acc.reference.service;

import cpf.acc.reference.dto.AccountReferenceSearchRequest;
import cpf.acc.reference.port.AccountReferenceQueryPort;
import cpf.acc.common.base.AccBaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * ACC 기준 조회 업무를 처리합니다.
 */
@Service
@RequiredArgsConstructor
public class AccountReferenceService extends AccBaseService {
    private final AccountReferenceQueryPort queryPort;

    @Transactional(readOnly = true)
    public Map<String, Object> search(AccountReferenceSearchRequest request) {
        return queryPort.search(request.normalized());
    }
}
