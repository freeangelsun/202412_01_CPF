package cpf.xyz.edu.service;

import cpf.pfw.common.exception.CpfNotFoundException;
import cpf.xyz.edu.dto.XyzQueryEducationItem;
import cpf.xyz.edu.dto.XyzQueryKeysetResponse;
import cpf.xyz.edu.dto.XyzQueryPageResponse;
import cpf.xyz.edu.repository.XyzQueryEducationRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class XyzQueryEducationServiceTest {

    @Test
    void getItemThrowsCpfNotFoundWhenRepositoryReturnsEmpty() {
        XyzQueryEducationRepository repository = mock(XyzQueryEducationRepository.class);
        XyzQueryEducationService service = new XyzQueryEducationService(repository);
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getItem(999L))
                .isInstanceOf(CpfNotFoundException.class)
                .hasMessageContaining("itemId=999");
    }

    @Test
    void offsetPageUsesNormalizedPageAndSizeAndCalculatesHasNext() {
        XyzQueryEducationRepository repository = mock(XyzQueryEducationRepository.class);
        XyzQueryEducationService service = new XyzQueryEducationService(repository);
        when(repository.normalizePage(0)).thenReturn(1);
        when(repository.normalizeSize(2)).thenReturn(2);
        when(repository.countOffsetPageItems("query", "ACTIVE")).thenReturn(5L);
        when(repository.findOffsetPageItems("query", "ACTIVE", "idAsc", 1, 2))
                .thenReturn(List.of(item(1L), item(2L)));

        XyzQueryPageResponse<XyzQueryEducationItem> response =
                service.findOffsetPage("query", "ACTIVE", "idAsc", 0, 2);

        assertThat(response.items()).extracting(XyzQueryEducationItem::itemId).containsExactly(1L, 2L);
        assertThat(response.page()).isEqualTo(1);
        assertThat(response.size()).isEqualTo(2);
        assertThat(response.total()).isEqualTo(5L);
        assertThat(response.hasNext()).isTrue();
    }

    @Test
    void keysetPageTrimsExtraRowAndReturnsNextCursor() {
        XyzQueryEducationRepository repository = mock(XyzQueryEducationRepository.class);
        XyzQueryEducationService service = new XyzQueryEducationService(repository);
        when(repository.normalizeSize(2)).thenReturn(2);
        when(repository.findKeysetPageItems(10L, 2))
                .thenReturn(List.of(item(11L), item(12L), item(13L)));

        XyzQueryKeysetResponse<XyzQueryEducationItem> response = service.findKeysetPage(10L, 2);

        assertThat(response.items()).extracting(XyzQueryEducationItem::itemId).containsExactly(11L, 12L);
        assertThat(response.nextCursorId()).isEqualTo(12L);
        assertThat(response.hasNext()).isTrue();
    }

    private XyzQueryEducationItem item(Long itemId) {
        return new XyzQueryEducationItem(itemId, "조회 샘플 " + itemId, "QUERY", "ACTIVE", "M000000001", "2026-06-29T09:00:00");
    }
}
