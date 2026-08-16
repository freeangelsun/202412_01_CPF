package com.cpf.education.web.crud.application;
import com.cpf.education.data.transaction.application.EducationTransactionEducationAuditService;
import com.cpf.core.api.error.CpfNotFoundException;
import com.cpf.education.web.crud.dto.EducationCrudEducationRequest;
import com.cpf.education.web.crud.dto.EducationCrudEducationResponse;
import com.cpf.education.web.crud.dto.EducationCrudEducationStatusRequest;
import com.cpf.education.data.query.dto.EducationQueryEducationItem;
import com.cpf.education.data.query.adapter.EducationQueryEducationRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EducationCrudEducationServiceTest {

    @Test
    void findEducationItemsUsesRepositoryAndConvertsToCrudResponse() {
        EducationQueryEducationRepository repository = mock(EducationQueryEducationRepository.class);
        EducationTransactionEducationAuditService auditService = mock(EducationTransactionEducationAuditService.class);
        EducationCrudEducationService service = new EducationCrudEducationService(repository, auditService);
        when(repository.findItems("검색", "ACTIVE", "nameAsc", 10))
                .thenReturn(List.of(item(91001L, "조회 샘플", "CRUD", "ACTIVE")));

        List<EducationCrudEducationResponse> responses =
                service.findEducationItems("검색", "ACTIVE", "nameAsc", 10);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).educationItemId()).isEqualTo(91001L);
        assertThat(responses.get(0).categoryCode()).isEqualTo("CRUD");
    }

    @Test
    void createEducationItemInsertsThroughRepositoryAndReadsBack() {
        EducationQueryEducationRepository repository = mock(EducationQueryEducationRepository.class);
        EducationTransactionEducationAuditService auditService = mock(EducationTransactionEducationAuditService.class);
        EducationCrudEducationService service = new EducationCrudEducationService(repository, auditService);
        when(repository.nextCrudItemId()).thenReturn(91010L);
        when(repository.normalizeRequestUser("tester")).thenReturn("tester");
        when(repository.normalizeCategoryCode("crud")).thenReturn("CRUD");
        when(repository.findById(91010L)).thenReturn(Optional.of(item(91010L, "등록 샘플", "CRUD", "ACTIVE")));

        EducationCrudEducationResponse response = service.createEducationItem(
                new EducationCrudEducationRequest("등록 샘플", "설명", "tester", "crud", "MBR-001"));

        verify(repository).insertCrudItem(91010L, "등록 샘플", "CRUD", "ACTIVE", "MBR-001", "tester");
        assertThat(response.educationItemId()).isEqualTo(91010L);
    }

    @Test
    void statusChangeUpdatesThroughRepository() {
        EducationQueryEducationRepository repository = mock(EducationQueryEducationRepository.class);
        EducationTransactionEducationAuditService auditService = mock(EducationTransactionEducationAuditService.class);
        EducationCrudEducationService service = new EducationCrudEducationService(repository, auditService);
        when(repository.findById(91011L))
                .thenReturn(Optional.of(item(91011L, "상태 샘플", "CRUD", "ACTIVE")))
                .thenReturn(Optional.of(item(91011L, "상태 샘플", "CRUD", "INACTIVE")));
        when(repository.updateCrudItemStatus(91011L, "INACTIVE", "tester")).thenReturn(1);
        when(repository.normalizeRequestUser("tester")).thenReturn("tester");

        EducationCrudEducationResponse response = service.changeEducationItemStatus(
                91011L,
                new EducationCrudEducationStatusRequest("INACTIVE", "tester"));

        assertThat(response.status()).isEqualTo("INACTIVE");
    }

    @Test
    void missingItemThrowsNotFound() {
        EducationQueryEducationRepository repository = mock(EducationQueryEducationRepository.class);
        EducationTransactionEducationAuditService auditService = mock(EducationTransactionEducationAuditService.class);
        EducationCrudEducationService service = new EducationCrudEducationService(repository, auditService);
        when(repository.findById(99999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getEducationItem(99999L))
                .isInstanceOf(CpfNotFoundException.class)
                .hasMessageContaining("educationItemId=99999");
    }

    private EducationQueryEducationItem item(Long itemId, String itemName, String categoryCode, String statusCode) {
        return new EducationQueryEducationItem(
                itemId, itemName, categoryCode, statusCode, "MBR-001",
                LocalDateTime.parse("2026-07-02T09:00:00"));
    }
}
