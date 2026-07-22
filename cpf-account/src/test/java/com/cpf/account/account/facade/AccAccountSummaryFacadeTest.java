package com.cpf.account.account.facade;

import com.cpf.account.account.dto.AccAccountResponse;
import com.cpf.account.account.port.AccAccountRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AccAccountSummaryFacadeTest {
    @Test
    void ACC소유저장소결과를공유계약으로변환한다() {
        AccAccountRepository repository = mock(AccAccountRepository.class);
        when(repository.find(10L)).thenReturn(Optional.of(new AccAccountResponse(
                10L, "ACC-10", "교육 계정", "ed***@example.test", "ACTIVE", 3L,
                LocalDateTime.now(), LocalDateTime.now())));

        var summary = new AccAccountSummaryFacade(repository).findSummary(10L);

        assertThat(summary.accountNo()).isEqualTo("ACC-10");
        assertThat(summary.version()).isEqualTo(3L);
    }
}
