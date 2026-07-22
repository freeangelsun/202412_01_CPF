package cpf.pfw.common.reliability;

import cpf.pfw.api.reliability.CpfReliabilityOperationsPort;
import cpf.pfw.common.exception.CpfValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
class CpfReliabilityOperationsFacadeTest {

    @Test
    void outboxSearchUsesBoundParametersAndSafeLimit() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        ObjectProvider<JdbcTemplate> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(jdbcTemplate);
        when(jdbcTemplate.queryForList(anyString(), any(Object[].class))).thenReturn(List.of());
        CpfReliabilityOperationsFacade facade = new CpfReliabilityOperationsFacade(provider);

        facade.findOutbox("FAILED", "TX-1", "cpf.topic", 1000);

        verify(jdbcTemplate).queryForList(
                contains("FROM pfw_broker_outbox"),
                eq(new Object[]{"FAILED", "TX-1", "cpf.topic", 500}));
    }

    @Test
    void replayRejectsAlreadyRequestedDlq() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        ObjectProvider<JdbcTemplate> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(jdbcTemplate);
        when(jdbcTemplate.queryForList(contains("FROM pfw_broker_dlq"), eq("M1")))
                .thenReturn(List.of(Map.of("message_id", "M1", "replay_status", "REQUESTED")));
        when(jdbcTemplate.update(anyString(), any(), any())).thenReturn(0);
        CpfReliabilityOperationsFacade facade = new CpfReliabilityOperationsFacade(provider);

        assertThatThrownBy(() -> facade.requestDlqReplay("M1", "OP1", "운영 재처리"))
                .isInstanceOf(CpfValidationException.class)
                .hasMessageContaining("재처리");
    }

    @Test
    void unknownResolutionReturnsBeforeAndAfter() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        ObjectProvider<JdbcTemplate> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(jdbcTemplate);
        when(jdbcTemplate.queryForList(contains("FROM pfw_unknown_result"), eq("U1")))
                .thenReturn(
                        List.of(Map.of("unknown_id", "U1", "unknown_status", "CHECK_PENDING")),
                        List.of(Map.of("unknown_id", "U1", "unknown_status", "CONFIRMED_SUCCESS")));
        when(jdbcTemplate.update(anyString(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(1);
        CpfReliabilityOperationsFacade facade = new CpfReliabilityOperationsFacade(provider);

        CpfReliabilityOperationsPort.ChangeResult result = facade.resolveUnknown(
                "U1", "CONFIRMED_SUCCESS", "OP1", "외부 조회 결과 성공 확인");

        assertThat(result.before()).containsEntry("unknown_status", "CHECK_PENDING");
        assertThat(result.after()).containsEntry("unknown_status", "CONFIRMED_SUCCESS");
    }
}
