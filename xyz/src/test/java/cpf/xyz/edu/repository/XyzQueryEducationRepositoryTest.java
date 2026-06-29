package cpf.xyz.edu.repository;

import cpf.xyz.edu.dto.XyzQueryEducationCriteria;
import cpf.xyz.edu.mapper.XyzQueryEducationMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class XyzQueryEducationRepositoryTest {

    @Test
    void findItemsNormalizesSortKeywordStatusAndLimit() {
        XyzQueryEducationMapper mapper = mock(XyzQueryEducationMapper.class);
        XyzQueryEducationRepository repository = new XyzQueryEducationRepository(mapper);
        when(mapper.findItems(any())).thenReturn(List.of());

        repository.findItems("  query  ", " active ", "createdDesc", 500);

        ArgumentCaptor<XyzQueryEducationCriteria> captor = ArgumentCaptor.forClass(XyzQueryEducationCriteria.class);
        verify(mapper).findItems(captor.capture());
        assertThat(captor.getValue().keyword()).isEqualTo("query");
        assertThat(captor.getValue().statusCode()).isEqualTo("ACTIVE");
        assertThat(captor.getValue().sortCode()).isEqualTo(XyzQueryEducationRepository.SORT_CREATED_DESC);
        assertThat(captor.getValue().limit()).isEqualTo(XyzQueryEducationRepository.MAX_PAGE_SIZE);
    }

    @Test
    void unknownSortFallsBackToIdAsc() {
        XyzQueryEducationMapper mapper = mock(XyzQueryEducationMapper.class);
        XyzQueryEducationRepository repository = new XyzQueryEducationRepository(mapper);
        when(mapper.findItems(any())).thenReturn(List.of());

        repository.findItems(null, null, "itemName; drop table", 10);

        ArgumentCaptor<XyzQueryEducationCriteria> captor = ArgumentCaptor.forClass(XyzQueryEducationCriteria.class);
        verify(mapper).findItems(captor.capture());
        assertThat(captor.getValue().sortCode()).isEqualTo(XyzQueryEducationRepository.SORT_ID_ASC);
    }

    @Test
    void keysetQueryRequestsOneMoreRowForHasNextDecision() {
        XyzQueryEducationMapper mapper = mock(XyzQueryEducationMapper.class);
        XyzQueryEducationRepository repository = new XyzQueryEducationRepository(mapper);
        when(mapper.findKeysetPageItems(any())).thenReturn(List.of());

        repository.findKeysetPageItems(10L, 20);

        ArgumentCaptor<XyzQueryEducationCriteria> captor = ArgumentCaptor.forClass(XyzQueryEducationCriteria.class);
        verify(mapper).findKeysetPageItems(captor.capture());
        assertThat(captor.getValue().cursorId()).isEqualTo(10L);
        assertThat(captor.getValue().limit()).isEqualTo(21);
        assertThat(captor.getValue().sortCode()).isEqualTo(XyzQueryEducationRepository.SORT_ID_ASC);
    }
}
