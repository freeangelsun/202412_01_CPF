package com.cpf.education.data.query.application;
import com.cpf.core.api.error.CpfNotFoundException;
import com.cpf.education.data.query.dto.EducationQueryEducationItem;
import com.cpf.education.data.query.dto.EducationQueryKeysetResponse;
import com.cpf.education.data.query.dto.EducationQueryPageResponse;
import com.cpf.education.data.query.adapter.EducationQueryEducationRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EducationQueryEducationServiceTest {

    @Test
    void getItemThrowsCpfNotFoundWhenRepositoryReturnsEmpty() {
        EducationQueryEducationRepository repository = mock(EducationQueryEducationRepository.class);
        EducationQueryEducationService service = new EducationQueryEducationService(repository);
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getItem(999L))
                .isInstanceOf(CpfNotFoundException.class)
                .hasMessageContaining("itemId=999");
    }

    @Test
    void offsetPageUsesNormalizedPageAndSizeAndCalculatesHasNext() {
        EducationQueryEducationRepository repository = mock(EducationQueryEducationRepository.class);
        EducationQueryEducationService service = new EducationQueryEducationService(repository);
        when(repository.normalizePage(0)).thenReturn(1);
        when(repository.normalizeSize(2)).thenReturn(2);
        when(repository.countOffsetPageItems("query", "ACTIVE")).thenReturn(5L);
        when(repository.findOffsetPageItems("query", "ACTIVE", "idAsc", 1, 2))
                .thenReturn(List.of(item(1L), item(2L)));

        EducationQueryPageResponse<EducationQueryEducationItem> response =
                service.findOffsetPage("query", "ACTIVE", "idAsc", 0, 2);

        assertThat(response.items()).extracting(EducationQueryEducationItem::itemId).containsExactly(1L, 2L);
        assertThat(response.page()).isEqualTo(1);
        assertThat(response.size()).isEqualTo(2);
        assertThat(response.total()).isEqualTo(5L);
        assertThat(response.hasNext()).isTrue();
    }

    @Test
    void keysetPageTrimsExtraRowAndReturnsNextCursor() {
        EducationQueryEducationRepository repository = mock(EducationQueryEducationRepository.class);
        EducationQueryEducationService service = new EducationQueryEducationService(repository);
        when(repository.normalizeSize(2)).thenReturn(2);
        when(repository.findKeysetPageItems(10L, 2))
                .thenReturn(List.of(item(11L), item(12L), item(13L)));

        EducationQueryKeysetResponse<EducationQueryEducationItem> response = service.findKeysetPage(10L, 2);

        assertThat(response.items()).extracting(EducationQueryEducationItem::itemId).containsExactly(11L, 12L);
        assertThat(response.nextCursorId()).isEqualTo(12L);
        assertThat(response.hasNext()).isTrue();
    }

    private EducationQueryEducationItem item(Long itemId) {
        return new EducationQueryEducationItem(
                itemId, "조회 샘플 " + itemId, "QUERY", "ACTIVE", "M000000001",
                LocalDateTime.parse("2026-06-29T09:00:00"));
    }
}
