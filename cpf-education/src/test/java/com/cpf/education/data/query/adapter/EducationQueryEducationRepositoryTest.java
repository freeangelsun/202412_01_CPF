package com.cpf.education.data.query.adapter;
import com.cpf.education.data.query.dto.EducationQueryEducationCriteria;
import com.cpf.education.data.query.adapter.EducationQueryEducationMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EducationQueryEducationRepositoryTest {

    @Test
    void findItemsNormalizesSortKeywordStatusAndLimit() {
        EducationQueryEducationMapper mapper = mock(EducationQueryEducationMapper.class);
        EducationQueryEducationRepository repository = new EducationQueryEducationRepository(mapper);
        when(mapper.findItems(any())).thenReturn(List.of());

        repository.findItems("  query  ", " active ", "createdDesc", 500);

        ArgumentCaptor<EducationQueryEducationCriteria> captor = ArgumentCaptor.forClass(EducationQueryEducationCriteria.class);
        verify(mapper).findItems(captor.capture());
        assertThat(captor.getValue().keyword()).isEqualTo("query");
        assertThat(captor.getValue().statusCode()).isEqualTo("ACTIVE");
        assertThat(captor.getValue().sortCode()).isEqualTo(EducationQueryEducationRepository.SORT_CREATED_DESC);
        assertThat(captor.getValue().limit()).isEqualTo(EducationQueryEducationRepository.MAX_PAGE_SIZE);
    }

    @Test
    void unknownSortFallsBackToIdAsc() {
        EducationQueryEducationMapper mapper = mock(EducationQueryEducationMapper.class);
        EducationQueryEducationRepository repository = new EducationQueryEducationRepository(mapper);
        when(mapper.findItems(any())).thenReturn(List.of());

        repository.findItems(null, null, "itemName; drop table", 10);

        ArgumentCaptor<EducationQueryEducationCriteria> captor = ArgumentCaptor.forClass(EducationQueryEducationCriteria.class);
        verify(mapper).findItems(captor.capture());
        assertThat(captor.getValue().sortCode()).isEqualTo(EducationQueryEducationRepository.SORT_ID_ASC);
    }

    @Test
    void nameAscAliasIsNormalizedToWhitelistCode() {
        EducationQueryEducationMapper mapper = mock(EducationQueryEducationMapper.class);
        EducationQueryEducationRepository repository = new EducationQueryEducationRepository(mapper);
        when(mapper.findItems(any())).thenReturn(List.of());

        repository.findItems(null, null, "nameAsc", 10);

        ArgumentCaptor<EducationQueryEducationCriteria> captor = ArgumentCaptor.forClass(EducationQueryEducationCriteria.class);
        verify(mapper).findItems(captor.capture());
        assertThat(captor.getValue().sortCode()).isEqualTo(EducationQueryEducationRepository.SORT_NAME_ASC);
    }

    @Test
    void keysetQueryRequestsOneMoreRowForHasNextDecision() {
        EducationQueryEducationMapper mapper = mock(EducationQueryEducationMapper.class);
        EducationQueryEducationRepository repository = new EducationQueryEducationRepository(mapper);
        when(mapper.findKeysetPageItems(any())).thenReturn(List.of());

        repository.findKeysetPageItems(10L, 20);

        ArgumentCaptor<EducationQueryEducationCriteria> captor = ArgumentCaptor.forClass(EducationQueryEducationCriteria.class);
        verify(mapper).findKeysetPageItems(captor.capture());
        assertThat(captor.getValue().cursorId()).isEqualTo(10L);
        assertThat(captor.getValue().limit()).isEqualTo(21);
        assertThat(captor.getValue().sortCode()).isEqualTo(EducationQueryEducationRepository.SORT_ID_ASC);
    }
}
