package cpf.xyz.edu.service;

import cpf.pfw.common.exception.CpfNotFoundException;
import cpf.xyz.edu.dto.XyzCrudEducationRequest;
import cpf.xyz.edu.dto.XyzCrudEducationResponse;
import cpf.xyz.edu.dto.XyzCrudEducationStatusRequest;
import cpf.xyz.edu.dto.XyzQueryEducationItem;
import cpf.xyz.edu.repository.XyzQueryEducationRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class XyzCrudEducationServiceTest {

    @Test
    void findEducationItemsUsesRepositoryAndConvertsToCrudResponse() {
        XyzQueryEducationRepository repository = mock(XyzQueryEducationRepository.class);
        XyzTransactionEducationAuditService auditService = mock(XyzTransactionEducationAuditService.class);
        XyzCrudEducationService service = new XyzCrudEducationService(repository, auditService);
        when(repository.findItems("검색", "ACTIVE", "nameAsc", 10))
                .thenReturn(List.of(item(91001L, "조회 샘플", "CRUD", "ACTIVE")));

        List<XyzCrudEducationResponse> responses =
                service.findEducationItems("검색", "ACTIVE", "nameAsc", 10);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).educationItemId()).isEqualTo(91001L);
        assertThat(responses.get(0).categoryCode()).isEqualTo("CRUD");
    }

    @Test
    void createEducationItemInsertsThroughRepositoryAndReadsBack() {
        XyzQueryEducationRepository repository = mock(XyzQueryEducationRepository.class);
        XyzTransactionEducationAuditService auditService = mock(XyzTransactionEducationAuditService.class);
        XyzCrudEducationService service = new XyzCrudEducationService(repository, auditService);
        when(repository.nextCrudItemId()).thenReturn(91010L);
        when(repository.normalizeRequestUser("tester")).thenReturn("tester");
        when(repository.normalizeCategoryCode("crud")).thenReturn("CRUD");
        when(repository.findById(91010L)).thenReturn(Optional.of(item(91010L, "등록 샘플", "CRUD", "ACTIVE")));

        XyzCrudEducationResponse response = service.createEducationItem(
                new XyzCrudEducationRequest("등록 샘플", "설명", "tester", "crud", "MBR-001"));

        verify(repository).insertCrudItem(91010L, "등록 샘플", "CRUD", "ACTIVE", "MBR-001", "tester");
        assertThat(response.educationItemId()).isEqualTo(91010L);
    }

    @Test
    void statusChangeUpdatesThroughRepository() {
        XyzQueryEducationRepository repository = mock(XyzQueryEducationRepository.class);
        XyzTransactionEducationAuditService auditService = mock(XyzTransactionEducationAuditService.class);
        XyzCrudEducationService service = new XyzCrudEducationService(repository, auditService);
        when(repository.findById(91011L))
                .thenReturn(Optional.of(item(91011L, "상태 샘플", "CRUD", "ACTIVE")))
                .thenReturn(Optional.of(item(91011L, "상태 샘플", "CRUD", "INACTIVE")));
        when(repository.updateCrudItemStatus(91011L, "INACTIVE", "tester")).thenReturn(1);
        when(repository.normalizeRequestUser("tester")).thenReturn("tester");

        XyzCrudEducationResponse response = service.changeEducationItemStatus(
                91011L,
                new XyzCrudEducationStatusRequest("INACTIVE", "tester"));

        assertThat(response.status()).isEqualTo("INACTIVE");
    }

    @Test
    void missingItemThrowsNotFound() {
        XyzQueryEducationRepository repository = mock(XyzQueryEducationRepository.class);
        XyzTransactionEducationAuditService auditService = mock(XyzTransactionEducationAuditService.class);
        XyzCrudEducationService service = new XyzCrudEducationService(repository, auditService);
        when(repository.findById(99999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getEducationItem(99999L))
                .isInstanceOf(CpfNotFoundException.class)
                .hasMessageContaining("educationItemId=99999");
    }

    private XyzQueryEducationItem item(Long itemId, String itemName, String categoryCode, String statusCode) {
        return new XyzQueryEducationItem(itemId, itemName, categoryCode, statusCode, "MBR-001", "2026-07-02T09:00:00");
    }
}
