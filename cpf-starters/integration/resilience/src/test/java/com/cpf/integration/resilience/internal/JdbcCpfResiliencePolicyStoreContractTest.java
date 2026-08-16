package com.cpf.integration.resilience.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import com.cpf.integration.resilience.api.CpfResiliencePolicy;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

class JdbcCpfResiliencePolicyStoreContractTest {

    @Test
    void requestSanitizesReasonBeforeBindingThePendingRow() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        AtomicReference<String> sql = new AtomicReference<>();
        AtomicReference<Object[]> bound = new AtomicReference<>();
        doAnswer(invocation -> {
            sql.set(invocation.getArgument(0));
            bound.set(invocation.getArgument(1));
            return 1;
        }).when(jdbc).update(anyString(), any(Object[].class));
        JdbcCpfResiliencePolicyStore store =
                new JdbcCpfResiliencePolicyStore(jdbc, mock(TransactionTemplate.class));

        String requestId = store.request(
                policy(),
                "requester@example.com",
                "incident email=user@example.com token=raw-secret");

        assertThat(requestId).isNotBlank();
        assertThat(sql.get()).contains("cpf_resilience_policy_request", "active_operation_key");
        assertThat(bound.get()).hasSize(7);
        assertThat(String.valueOf(bound.get()[5]))
                .doesNotContain("user@example.com", "raw-secret")
                .contains("[PII_REDACTED]", "[REDACTED]");
        assertThat(bound.get()[6]).isEqualTo("payment.post");
    }

    @Test
    void rejectEnforcesSeparationOfDutiesAndSanitizesTheReason() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        AtomicReference<String> sql = new AtomicReference<>();
        AtomicReference<Object[]> bound = new AtomicReference<>();
        doAnswer(invocation -> {
            sql.set(invocation.getArgument(0));
            bound.set(invocation.getArgument(1));
            return 1;
        }).when(jdbc).update(anyString(), any(Object[].class));
        JdbcCpfResiliencePolicyStore store =
                new JdbcCpfResiliencePolicyStore(jdbc, mock(TransactionTemplate.class));

        store.reject("request-1", "approver@example.com", "reject Bearer abc.def.ghi");

        assertThat(sql.get()).contains("requester_id<>?", "active_operation_key=null");
        assertThat(bound.get()).hasSize(4);
        assertThat(String.valueOf(bound.get()[1])).doesNotContain("abc.def.ghi").contains("[REDACTED]");
        assertThat(bound.get()[0]).isEqualTo(bound.get()[3]);
    }

    @Test
    void duplicatePendingRequestAndPagingOverflowFailClosed() {
        JdbcTemplate duplicateJdbc = mock(JdbcTemplate.class);
        doAnswer(invocation -> {
            throw new DuplicateKeyException("duplicate pending request");
        }).when(duplicateJdbc).update(anyString(), any(Object[].class));
        JdbcCpfResiliencePolicyStore duplicateStore =
                new JdbcCpfResiliencePolicyStore(duplicateJdbc, mock(TransactionTemplate.class));

        assertThatThrownBy(() -> duplicateStore.request(policy(), "requester", "reason"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("pending policy request already exists");

        JdbcCpfResiliencePolicyStore store = new JdbcCpfResiliencePolicyStore(
                mock(JdbcTemplate.class), mock(TransactionTemplate.class));
        assertThatThrownBy(() -> store.search("", Integer.MAX_VALUE, 500))
                .isInstanceOf(ArithmeticException.class);
    }

    private static CpfResiliencePolicy policy() {
        return new CpfResiliencePolicy(
                "payment.post",
                0L,
                Duration.ofSeconds(2),
                2,
                Duration.ofMillis(10),
                3,
                Duration.ofSeconds(5),
                10,
                100,
                Duration.ofMinutes(1),
                true,
                true);
    }
}
