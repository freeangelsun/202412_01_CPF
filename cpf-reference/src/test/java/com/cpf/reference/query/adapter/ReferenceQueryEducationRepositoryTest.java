package com.cpf.reference.query.adapter;

import com.cpf.reference.query.dto.ReferenceQueryEducationCriteria;
import com.cpf.reference.query.adapter.ReferenceQueryEducationMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReferenceQueryEducationRepositoryTest {

    @Test
    void findItemsNormalizesSortKeywordStatusAndLimit() {
        ReferenceQueryEducationMapper mapper = mock(ReferenceQueryEducationMapper.class);
        ReferenceQueryEducationRepository repository = new ReferenceQueryEducationRepository(mapper);
        when(mapper.findItems(any())).thenReturn(List.of());

        repository.findItems("  query  ", " active ", "createdDesc", 500);

        ArgumentCaptor<ReferenceQueryEducationCriteria> captor = ArgumentCaptor.forClass(ReferenceQueryEducationCriteria.class);
        verify(mapper).findItems(captor.capture());
        assertThat(captor.getValue().keyword()).isEqualTo("query");
        assertThat(captor.getValue().statusCode()).isEqualTo("ACTIVE");
        assertThat(captor.getValue().sortCode()).isEqualTo(ReferenceQueryEducationRepository.SORT_CREATED_DESC);
        assertThat(captor.getValue().limit()).isEqualTo(ReferenceQueryEducationRepository.MAX_PAGE_SIZE);
    }

    @Test
    void unknownSortFallsBackToIdAsc() {
        ReferenceQueryEducationMapper mapper = mock(ReferenceQueryEducationMapper.class);
        ReferenceQueryEducationRepository repository = new ReferenceQueryEducationRepository(mapper);
        when(mapper.findItems(any())).thenReturn(List.of());

        repository.findItems(null, null, "itemName; drop table", 10);

        ArgumentCaptor<ReferenceQueryEducationCriteria> captor = ArgumentCaptor.forClass(ReferenceQueryEducationCriteria.class);
        verify(mapper).findItems(captor.capture());
        assertThat(captor.getValue().sortCode()).isEqualTo(ReferenceQueryEducationRepository.SORT_ID_ASC);
    }

    @Test
    void nameAscAliasIsNormalizedToWhitelistCode() {
        ReferenceQueryEducationMapper mapper = mock(ReferenceQueryEducationMapper.class);
        ReferenceQueryEducationRepository repository = new ReferenceQueryEducationRepository(mapper);
        when(mapper.findItems(any())).thenReturn(List.of());

        repository.findItems(null, null, "nameAsc", 10);

        ArgumentCaptor<ReferenceQueryEducationCriteria> captor = ArgumentCaptor.forClass(ReferenceQueryEducationCriteria.class);
        verify(mapper).findItems(captor.capture());
        assertThat(captor.getValue().sortCode()).isEqualTo(ReferenceQueryEducationRepository.SORT_NAME_ASC);
    }

    @Test
    void keysetQueryRequestsOneMoreRowForHasNextDecision() {
        ReferenceQueryEducationMapper mapper = mock(ReferenceQueryEducationMapper.class);
        ReferenceQueryEducationRepository repository = new ReferenceQueryEducationRepository(mapper);
        when(mapper.findKeysetPageItems(any())).thenReturn(List.of());

        repository.findKeysetPageItems(10L, 20);

        ArgumentCaptor<ReferenceQueryEducationCriteria> captor = ArgumentCaptor.forClass(ReferenceQueryEducationCriteria.class);
        verify(mapper).findKeysetPageItems(captor.capture());
        assertThat(captor.getValue().cursorId()).isEqualTo(10L);
        assertThat(captor.getValue().limit()).isEqualTo(21);
        assertThat(captor.getValue().sortCode()).isEqualTo(ReferenceQueryEducationRepository.SORT_ID_ASC);
    }
}
