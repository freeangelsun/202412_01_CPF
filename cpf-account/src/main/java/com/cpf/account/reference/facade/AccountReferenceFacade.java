package com.cpf.account.reference.facade;

import com.cpf.account.reference.dto.AccountReferenceSearchRequest;
import com.cpf.account.reference.service.AccountReferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Controller와 업무 서비스를 분리하는 진입 Facade입니다.
 *
 * <p>여러 서비스 조합, 외부 호출, 거래 단위 조정이 필요하면 Facade에서 처리합니다.</p>
 */
@Component
@RequiredArgsConstructor
public class AccountReferenceFacade {
    private final AccountReferenceService service;

    public Map<String, Object> search(AccountReferenceSearchRequest request) {
        return service.search(request);
    }
}
