package cpf.acc.reference.adapter.local;

import cpf.acc.reference.dto.AccountReferenceSearchRequest;
import cpf.acc.reference.port.AccountReferenceQueryPort;
import cpf.acc.reference.repository.AccountReferenceRepository;
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
