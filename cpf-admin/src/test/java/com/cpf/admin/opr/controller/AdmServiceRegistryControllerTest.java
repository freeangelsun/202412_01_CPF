package com.cpf.admin.opr.controller;

import com.cpf.admin.opr.service.AdmServiceRegistryService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdmServiceRegistryControllerTest {
    private final AdmServiceRegistryService service = mock(AdmServiceRegistryService.class);
    private final AdmServiceRegistryController controller = new AdmServiceRegistryController(service);

    @Test
    void findServicesDelegatesToServiceLayer() {
        when(service.findServices("MBR", "Y", 100))
                .thenReturn(List.of(Map.of("serviceId", "MBR", "serviceName", "회원 서비스")));

        ResponseEntity<List<Map<String, Object>>> response = controller.findServices("MBR", "Y", 100);

        assertThat(response.getBody()).singleElement()
                .satisfies(row -> assertThat(row).containsEntry("serviceId", "MBR"));
        verify(service).findServices("MBR", "Y", 100);
    }

    @Test
    void findCallHistoryDelegatesToServiceLayer() {
        when(service.findCallHistory("MBR", "20260707120000000MBRlocal010000001", 20))
                .thenReturn(List.of(Map.of("callStatus", "SUCCESS")));

        ResponseEntity<List<Map<String, Object>>> response =
                controller.findCallHistory("MBR", "20260707120000000MBRlocal010000001", 20);

        assertThat(response.getBody()).singleElement()
                .satisfies(row -> assertThat(row).containsEntry("callStatus", "SUCCESS"));
        verify(service).findCallHistory("MBR", "20260707120000000MBRlocal010000001", 20);
    }
}
