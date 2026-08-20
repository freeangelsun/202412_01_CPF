package com.cpf.backoffice.online.approval.service;

import com.cpf.backoffice.online.approval.repository.BackofficeApprovalPolicyRepository;
import com.cpf.core.api.error.CpfValidationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BackofficeApprovalPolicyServiceListTest {
    private final BackofficeApprovalPolicyRepository repository = mock(BackofficeApprovalPolicyRepository.class);
    private final BackofficeApprovalPolicyService service =
            new BackofficeApprovalPolicyService(repository, new ObjectMapper());

    @Test
    void submissionsAreRestrictedToAuthenticatedEmployeeAndBounded() {
        when(repository.findEmployeeNoByLoginId("operator")).thenReturn(Optional.of("E001"));
        when(repository.findSubmissions("E001", "IN_REVIEW", 1000))
                .thenReturn(List.of(Map.of("approvalId", 7L)));

        List<Map<String, Object>> result =
                service.findSubmissions("operator", " in_review ", 5000);

        assertEquals(7L, result.getFirst().get("approvalId"));
        verify(repository).findSubmissions("E001", "IN_REVIEW", 1000);
    }

    @Test
    void inboxIsRestrictedToAuthenticatedParticipantAndUsesDefaultLimit() {
        when(repository.findEmployeeNoByLoginId("operator")).thenReturn(Optional.of("E002"));
        when(repository.findInbox("E002", "WAITING", 100)).thenReturn(List.of());

        assertEquals(List.of(), service.findInbox("operator", "waiting", 0));
        verify(repository).findInbox("E002", "WAITING", 100);
    }

    @Test
    void missingEmployeeMappingFailsClosed() {
        when(repository.findEmployeeNoByLoginId("unknown")).thenReturn(Optional.empty());

        assertThrows(CpfValidationException.class,
                () -> service.findInbox("unknown", null, 100));
    }
}
