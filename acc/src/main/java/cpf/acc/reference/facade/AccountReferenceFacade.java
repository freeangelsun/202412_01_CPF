package cpf.acc.facade;

import cpf.acc.dto.AccountSearchRequest;
import cpf.acc.service.AccountService;
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
public class AccountFacade {
    private final AccountService service;

    public Map<String, Object> search(AccountSearchRequest request) {
        return service.search(request);
    }
}