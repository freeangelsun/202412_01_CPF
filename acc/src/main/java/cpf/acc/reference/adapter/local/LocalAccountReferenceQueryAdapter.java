package cpf.acc.adapter.local;

import cpf.acc.dto.AccountSearchRequest;
import cpf.acc.port.AccountQueryPort;
import cpf.acc.repository.AccountRepository;
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
public class LocalAccountQueryAdapter implements AccountQueryPort {
    private final AccountRepository repository;

    @Override
    public Map<String, Object> search(AccountSearchRequest request) {
        return repository.search(request);
    }
}