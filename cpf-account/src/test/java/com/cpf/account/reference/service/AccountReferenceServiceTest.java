package com.cpf.account.reference.service;

import com.cpf.account.reference.adapter.local.LocalAccountReferenceQueryAdapter;
import com.cpf.account.reference.dto.AccountReferenceSearchRequest;
import com.cpf.account.reference.repository.AccountReferenceRepository;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class AccountReferenceServiceTest {
    private final AtomicReference<AccountReferenceSearchRequest> capturedRequest = new AtomicReference<>();
    private final AccountReferenceRepository repository = new AccountReferenceRepository(null) {
        @Override
        public Map<String, Object> search(AccountReferenceSearchRequest request) {
            // DB 없이도 Service가 정규화한 최종 조건을 검증할 수 있도록 요청을 보관합니다.
            capturedRequest.set(request);
            return Map.of("items", java.util.List.of(), "criteria", request);
        }
    };
    private final AccountReferenceService service =
            new AccountReferenceService(new LocalAccountReferenceQueryAdapter(repository));

    @Test
    void searchNormalizesPagingAndSort() {
        Map<String, Object> result = service.search(
                new AccountReferenceSearchRequest("keyword", "unsafe_column", "ASC", -1, 999));

        assertThat(result).containsKey("items");
        assertThat(capturedRequest.get().sortBy()).isEqualTo("created_at");
        assertThat(capturedRequest.get().sortDirection()).isEqualTo("ASC");
        assertThat(capturedRequest.get().page()).isZero();
        assertThat(capturedRequest.get().size()).isEqualTo(200);
    }

    @Test
    void searchAppliesDefaultsWhenOptionalQueryParametersAreMissing() {
        service.search(new AccountReferenceSearchRequest(null, null, null, null, null));

        assertThat(capturedRequest.get().sortBy()).isEqualTo("created_at");
        assertThat(capturedRequest.get().sortDirection()).isEqualTo("DESC");
        assertThat(capturedRequest.get().page()).isZero();
        assertThat(capturedRequest.get().size()).isEqualTo(20);
    }
}
