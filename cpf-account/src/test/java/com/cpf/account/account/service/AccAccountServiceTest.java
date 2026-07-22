package com.cpf.account.account.service;

import com.cpf.account.account.dto.AccAccountCreateRequest;
import com.cpf.account.account.dto.AccAccountResponse;
import com.cpf.account.account.dto.AccAccountSearchCriteria;
import com.cpf.account.account.dto.AccAccountUpdateRequest;
import com.cpf.account.account.port.AccAccountRepository;
import com.cpf.core.common.exception.CpfBusinessException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccAccountServiceTest {
    private final MemoryRepository repository = new MemoryRepository();
    private final AccAccountService service = new AccAccountService(repository);

    @Test
    void 등록수정삭제가감사사유와낙관적잠금을적용한다() {
        AccAccountResponse created = service.create(
                new AccAccountCreateRequest("ACC-001", "테스트 계정", "tester@example.com"),
                "operator", "회귀 테스트 등록");

        assertThat(created.maskedEmail()).isEqualTo("te***@example.com");
        AccAccountResponse updated = service.update(created.accountId(),
                new AccAccountUpdateRequest("수정 계정", "tester@example.com", "ACTIVE", 0L),
                "operator", "회귀 테스트 수정");
        assertThat(updated.version()).isEqualTo(1L);

        assertThatThrownBy(() -> service.delete(created.accountId(), 0L, "operator", "구 버전 삭제"))
                .isInstanceOf(CpfBusinessException.class);
        service.delete(created.accountId(), 1L, "operator", "회귀 테스트 삭제");
        assertThat(repository.changes).extracting(Change::action).containsExactly("CREATE", "UPDATE", "DELETE");
    }

    @Test
    void 검색조건은정렬과크기를안전한값으로정규화한다() {
        service.search(new AccAccountSearchCriteria(null, null, null, "unsafe", "invalid", -1, 999, null));

        assertThat(repository.lastCriteria.sortColumn()).isEqualTo("account_id");
        assertThat(repository.lastCriteria.sortDirection()).isEqualTo("DESC");
        assertThat(repository.lastCriteria.size()).isEqualTo(200);
    }

    @Test
    void 검색조건이없으면안전한기본값을사용한다() {
        service.search(null);

        assertThat(repository.lastCriteria.sortColumn()).isEqualTo("account_id");
        assertThat(repository.lastCriteria.sortDirection()).isEqualTo("DESC");
        assertThat(repository.lastCriteria.page()).isZero();
        assertThat(repository.lastCriteria.size()).isEqualTo(20);
    }

    private static final class MemoryRepository implements AccAccountRepository {
        private AccAccountResponse account;
        private AccAccountSearchCriteria lastCriteria;
        private final List<Change> changes = new ArrayList<>();

        @Override
        public long create(AccAccountCreateRequest request, String actor) {
            account = new AccAccountResponse(1L, request.accountNo(), request.accountName(),
                    "te***@example.com", "ACTIVE", 0L, LocalDateTime.now(), LocalDateTime.now());
            return 1L;
        }

        @Override
        public Optional<AccAccountResponse> find(long accountId) {
            return account == null ? Optional.empty() : Optional.of(account);
        }

        @Override
        public List<AccAccountResponse> search(AccAccountSearchCriteria criteria) {
            lastCriteria = criteria;
            return account == null ? List.of() : List.of(account);
        }

        @Override
        public boolean update(long accountId, AccAccountUpdateRequest request, String actor) {
            if (account == null || request.version() != account.version()) {
                return false;
            }
            account = new AccAccountResponse(account.accountId(), account.accountNo(), request.accountName(),
                    account.maskedEmail(), request.statusCode(), account.version() + 1,
                    account.createdAt(), LocalDateTime.now());
            return true;
        }

        @Override
        public boolean logicalDelete(long accountId, long version, String actor) {
            if (account == null || version != account.version()) {
                return false;
            }
            account = null;
            return true;
        }

        @Override
        public void recordChange(long accountId, String actionCode, Object beforeValue, Object afterValue,
                                 String actor, String auditReason) {
            changes.add(new Change(actionCode, auditReason));
        }
    }

    private record Change(String action, String reason) {
    }
}
