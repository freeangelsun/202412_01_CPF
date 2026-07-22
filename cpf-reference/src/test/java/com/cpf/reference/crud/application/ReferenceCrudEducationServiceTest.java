package com.cpf.reference.crud.application;

import com.cpf.reference.transaction.application.ReferenceTransactionEducationAuditService;
import com.cpf.core.common.exception.CpfNotFoundException;
import com.cpf.reference.crud.dto.ReferenceCrudEducationRequest;
import com.cpf.reference.crud.dto.ReferenceCrudEducationResponse;
import com.cpf.reference.crud.dto.ReferenceCrudEducationStatusRequest;
import com.cpf.reference.query.dto.ReferenceQueryEducationItem;
import com.cpf.reference.query.adapter.ReferenceQueryEducationRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReferenceCrudEducationServiceTest {

    @Test
    void findEducationItemsUsesRepositoryAndConvertsToCrudResponse() {
        ReferenceQueryEducationRepository repository = mock(ReferenceQueryEducationRepository.class);
        ReferenceTransactionEducationAuditService auditService = mock(ReferenceTransactionEducationAuditService.class);
        ReferenceCrudEducationService service = new ReferenceCrudEducationService(repository, auditService);
        when(repository.findItems("검색", "ACTIVE", "nameAsc", 10))
                .thenReturn(List.of(item(91001L, "조회 샘플", "CRUD", "ACTIVE")));

        List<ReferenceCrudEducationResponse> responses =
                service.findEducationItems("검색", "ACTIVE", "nameAsc", 10);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).educationItemId()).isEqualTo(91001L);
        assertThat(responses.get(0).categoryCode()).isEqualTo("CRUD");
    }

    @Test
    void createEducationItemInsertsThroughRepositoryAndReadsBack() {
        ReferenceQueryEducationRepository repository = mock(ReferenceQueryEducationRepository.class);
        ReferenceTransactionEducationAuditService auditService = mock(ReferenceTransactionEducationAuditService.class);
        ReferenceCrudEducationService service = new ReferenceCrudEducationService(repository, auditService);
        when(repository.nextCrudItemId()).thenReturn(91010L);
        when(repository.normalizeRequestUser("tester")).thenReturn("tester");
        when(repository.normalizeCategoryCode("crud")).thenReturn("CRUD");
        when(repository.findById(91010L)).thenReturn(Optional.of(item(91010L, "등록 샘플", "CRUD", "ACTIVE")));

        ReferenceCrudEducationResponse response = service.createEducationItem(
                new ReferenceCrudEducationRequest("등록 샘플", "설명", "tester", "crud", "MBR-001"));

        verify(repository).insertCrudItem(91010L, "등록 샘플", "CRUD", "ACTIVE", "MBR-001", "tester");
        assertThat(response.educationItemId()).isEqualTo(91010L);
    }

    @Test
    void statusChangeUpdatesThroughRepository() {
        ReferenceQueryEducationRepository repository = mock(ReferenceQueryEducationRepository.class);
        ReferenceTransactionEducationAuditService auditService = mock(ReferenceTransactionEducationAuditService.class);
        ReferenceCrudEducationService service = new ReferenceCrudEducationService(repository, auditService);
        when(repository.findById(91011L))
                .thenReturn(Optional.of(item(91011L, "상태 샘플", "CRUD", "ACTIVE")))
                .thenReturn(Optional.of(item(91011L, "상태 샘플", "CRUD", "INACTIVE")));
        when(repository.updateCrudItemStatus(91011L, "INACTIVE", "tester")).thenReturn(1);
        when(repository.normalizeRequestUser("tester")).thenReturn("tester");

        ReferenceCrudEducationResponse response = service.changeEducationItemStatus(
                91011L,
                new ReferenceCrudEducationStatusRequest("INACTIVE", "tester"));

        assertThat(response.status()).isEqualTo("INACTIVE");
    }

    @Test
    void missingItemThrowsNotFound() {
        ReferenceQueryEducationRepository repository = mock(ReferenceQueryEducationRepository.class);
        ReferenceTransactionEducationAuditService auditService = mock(ReferenceTransactionEducationAuditService.class);
        ReferenceCrudEducationService service = new ReferenceCrudEducationService(repository, auditService);
        when(repository.findById(99999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getEducationItem(99999L))
                .isInstanceOf(CpfNotFoundException.class)
                .hasMessageContaining("educationItemId=99999");
    }

    private ReferenceQueryEducationItem item(Long itemId, String itemName, String categoryCode, String statusCode) {
        return new ReferenceQueryEducationItem(itemId, itemName, categoryCode, statusCode, "MBR-001", "2026-07-02T09:00:00");
    }
}
