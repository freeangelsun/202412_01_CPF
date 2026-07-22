package com.cpf.account.reference.adapter.local;

import com.cpf.account.reference.dto.AccountReferenceSearchRequest;
import com.cpf.account.reference.port.AccountReferenceQueryPort;
import com.cpf.account.reference.repository.AccountReferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 같은 주제영역 DB를 사용하는 기본 local adapter입니다.
 */
@Primary
@Component
@RequiredArgsConstructor
public class LocalAccountReferenceQueryAdapter implements AccountReferenceQueryPort {
    private final AccountReferenceRepository repository;

    @Override
    public Map<String, Object> search(AccountReferenceSearchRequest request) {
        return repository.search(request);
    }
}
