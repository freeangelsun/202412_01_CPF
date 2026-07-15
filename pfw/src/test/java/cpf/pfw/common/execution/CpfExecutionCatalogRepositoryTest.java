package cpf.pfw.common.execution;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
class CpfExecutionCatalogRepositoryTest {

    @Test
    void databaseFailureStopsFurtherWritesAndKeepsLocalCatalog() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        ObjectProvider<JdbcTemplate> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(jdbcTemplate).thenReturn(null);
        doThrow(new DataAccessResourceFailureException("테스트 DB 연결 실패"))
                .when(jdbcTemplate).update(anyString(), any(Object[].class));
        CpfExecutionCatalogRepository repository = new CpfExecutionCatalogRepository(provider);

        repository.upsertAll(List.of(
                definition("OXYZ-EDU-01-0001"),
                definition("OXYZ-EDU-01-0002")));

        verify(jdbcTemplate, times(1)).update(anyString(), any(Object[].class));
        assertThat(repository.findAll())
                .extracting(CpfExecutionDefinition::standardExecutionId)
                .containsExactly("OXYZ-EDU-01-0001", "OXYZ-EDU-01-0002");
    }

    private CpfExecutionDefinition definition(String id) {
        return new CpfExecutionDefinition(
                id,
                "표준 실행 카탈로그 테스트",
                CpfExecutionType.ONLINE,
                "XYZ",
                "XYZ",
                getClass().getName(),
                "databaseFailureStopsFurtherWritesAndKeepsLocalCatalog",
                "/test/catalog",
                "cpfExecutionCatalogRepositoryTest",
                "test",
                Instant.parse("2026-07-15T00:00:00Z"));
    }
}
