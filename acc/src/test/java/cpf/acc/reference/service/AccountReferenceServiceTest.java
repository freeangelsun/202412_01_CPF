package cpf.acc.service;

import cpf.acc.dto.AccountSearchRequest;
import cpf.acc.adapter.local.LocalAccountQueryAdapter;
import cpf.acc.repository.AccountRepository;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class AccountServiceTest {
    private final AtomicReference<AccountSearchRequest> capturedRequest = new AtomicReference<>();
    private final AccountRepository repository = new AccountRepository(null) {
        @Override
        public Map<String, Object> search(AccountSearchRequest request) {
            // DB 없이도 Service가 정규화한 최종 조건을 검증할 수 있도록 요청을 보관합니다.
            capturedRequest.set(request);
            return Map.of("items", java.util.List.of(), "criteria", request);
        }
    };
    private final AccountService service = new AccountService(new LocalAccountQueryAdapter(repository));

    @Test
    void searchNormalizesPagingAndSort() {
        Map<String, Object> result = service.search(new AccountSearchRequest("keyword", "unsafe_column", "ASC", -1, 999));

        assertThat(result).containsKey("items");
        assertThat(capturedRequest.get().sortBy()).isEqualTo("created_at");
        assertThat(capturedRequest.get().sortDirection()).isEqualTo("ASC");
        assertThat(capturedRequest.get().page()).isZero();
        assertThat(capturedRequest.get().size()).isEqualTo(200);
    }
}