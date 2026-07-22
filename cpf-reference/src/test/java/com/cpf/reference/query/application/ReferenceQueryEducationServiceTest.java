package com.cpf.reference.query.application;

import com.cpf.core.common.exception.CpfNotFoundException;
import com.cpf.reference.query.dto.ReferenceQueryEducationItem;
import com.cpf.reference.query.dto.ReferenceQueryKeysetResponse;
import com.cpf.reference.query.dto.ReferenceQueryPageResponse;
import com.cpf.reference.query.adapter.ReferenceQueryEducationRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReferenceQueryEducationServiceTest {

    @Test
    void getItemThrowsCpfNotFoundWhenRepositoryReturnsEmpty() {
        ReferenceQueryEducationRepository repository = mock(ReferenceQueryEducationRepository.class);
        ReferenceQueryEducationService service = new ReferenceQueryEducationService(repository);
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getItem(999L))
                .isInstanceOf(CpfNotFoundException.class)
                .hasMessageContaining("itemId=999");
    }

    @Test
    void offsetPageUsesNormalizedPageAndSizeAndCalculatesHasNext() {
        ReferenceQueryEducationRepository repository = mock(ReferenceQueryEducationRepository.class);
        ReferenceQueryEducationService service = new ReferenceQueryEducationService(repository);
        when(repository.normalizePage(0)).thenReturn(1);
        when(repository.normalizeSize(2)).thenReturn(2);
        when(repository.countOffsetPageItems("query", "ACTIVE")).thenReturn(5L);
        when(repository.findOffsetPageItems("query", "ACTIVE", "idAsc", 1, 2))
                .thenReturn(List.of(item(1L), item(2L)));

        ReferenceQueryPageResponse<ReferenceQueryEducationItem> response =
                service.findOffsetPage("query", "ACTIVE", "idAsc", 0, 2);

        assertThat(response.items()).extracting(ReferenceQueryEducationItem::itemId).containsExactly(1L, 2L);
        assertThat(response.page()).isEqualTo(1);
        assertThat(response.size()).isEqualTo(2);
        assertThat(response.total()).isEqualTo(5L);
        assertThat(response.hasNext()).isTrue();
    }

    @Test
    void keysetPageTrimsExtraRowAndReturnsNextCursor() {
        ReferenceQueryEducationRepository repository = mock(ReferenceQueryEducationRepository.class);
        ReferenceQueryEducationService service = new ReferenceQueryEducationService(repository);
        when(repository.normalizeSize(2)).thenReturn(2);
        when(repository.findKeysetPageItems(10L, 2))
                .thenReturn(List.of(item(11L), item(12L), item(13L)));

        ReferenceQueryKeysetResponse<ReferenceQueryEducationItem> response = service.findKeysetPage(10L, 2);

        assertThat(response.items()).extracting(ReferenceQueryEducationItem::itemId).containsExactly(11L, 12L);
        assertThat(response.nextCursorId()).isEqualTo(12L);
        assertThat(response.hasNext()).isTrue();
    }

    private ReferenceQueryEducationItem item(Long itemId) {
        return new ReferenceQueryEducationItem(itemId, "조회 샘플 " + itemId, "QUERY", "ACTIVE", "M000000001", "2026-06-29T09:00:00");
    }
}
