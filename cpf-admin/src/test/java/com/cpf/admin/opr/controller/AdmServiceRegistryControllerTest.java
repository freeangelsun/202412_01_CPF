package com.cpf.admin.opr.controller;

import com.cpf.admin.opr.service.AdmAuditLogService;
import com.cpf.admin.opr.service.AdmServiceRegistryService;
import com.cpf.integration.api.servicecall.CpfServiceRegistryView;
import io.swagger.v3.oas.annotations.Hidden;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdmServiceRegistryControllerTest {
    private final AdmServiceRegistryService service = mock(AdmServiceRegistryService.class);
    private final AdmServiceRegistryController controller =
            new AdmServiceRegistryController(service, mock(AdmAuditLogService.class));

    @Test
    void findServicesDelegatesTypedResultToServiceLayer() {
        var item = new CpfServiceRegistryView.Service(
                "MBR", "회원 서비스", "INTERNAL", "cpf-member", "member", true, 3L, OffsetDateTime.now());
        when(service.findServices("MBR", "Y", 100)).thenReturn(List.of(item));

        ResponseEntity<List<CpfServiceRegistryView.Service>> response = controller.findServices("MBR", "Y", 100);

        assertThat(response.getBody()).containsExactly(item);
        verify(service).findServices("MBR", "Y", 100);
    }

    @Test
    void findCallHistoryDelegatesTypedResultToServiceLayer() {
        var item = new CpfServiceRegistryView.CallHistory(
                "call-1", "20260707120000000MBR00000010000001", "trace-1", "MBR", "MBR-HTTP", "mbr-1",
                "POST", "/members", "SUCCESS", 200, 12L, 0, null, null, OffsetDateTime.now());
        when(service.findCallHistory("MBR", item.transactionId(), 20)).thenReturn(List.of(item));

        ResponseEntity<List<CpfServiceRegistryView.CallHistory>> response =
                controller.findCallHistory("MBR", item.transactionId(), 20);

        assertThat(response.getBody()).containsExactly(item);
        verify(service).findCallHistory("MBR", item.transactionId(), 20);
    }

    @Test
    void directDangerousCommandsAreRetiredWithGoneAndHiddenFromPublicContract() throws Exception {
        assertGone(() -> controller.changeInstanceState("MBR", "MBR-HTTP", "mbr-1", null, null));
        assertGone(() -> controller.deleteService("MBR", null, null));
        assertGone(() -> controller.deleteEndpoint("MBR-HTTP", null, null));
        assertGone(() -> controller.deleteInstance("mbr-1", null, null));

        assertThat(AdmServiceRegistryController.class.getMethod(
                "changeInstanceState", String.class, String.class, String.class,
                AdmServiceRegistryController.InstanceStateRequest.class, jakarta.servlet.http.HttpServletRequest.class)
                .isAnnotationPresent(Hidden.class)).isTrue();
        assertThat(AdmServiceRegistryController.class.getMethod(
                "deleteService", String.class, com.cpf.integration.api.servicecall.CpfServiceRegistryControlPort.DeleteCommand.class,
                jakarta.servlet.http.HttpServletRequest.class).isAnnotationPresent(Hidden.class)).isTrue();
        assertThat(AdmServiceRegistryController.class.getMethod(
                "deleteEndpoint", String.class, com.cpf.integration.api.servicecall.CpfServiceRegistryControlPort.DeleteCommand.class,
                jakarta.servlet.http.HttpServletRequest.class).isAnnotationPresent(Hidden.class)).isTrue();
        assertThat(AdmServiceRegistryController.class.getMethod(
                "deleteInstance", String.class, com.cpf.integration.api.servicecall.CpfServiceRegistryControlPort.DeleteCommand.class,
                jakarta.servlet.http.HttpServletRequest.class).isAnnotationPresent(Hidden.class)).isTrue();
    }

    private static void assertGone(Runnable action) {
        assertThatThrownBy(action::run)
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode()).isEqualTo(HttpStatus.GONE));
    }
}
