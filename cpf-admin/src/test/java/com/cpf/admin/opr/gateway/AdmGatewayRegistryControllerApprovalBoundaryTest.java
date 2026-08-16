package com.cpf.admin.opr.gateway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.cpf.gateway.api.CpfGatewayRegistryPort;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class AdmGatewayRegistryControllerApprovalBoundaryTest {
    private final AdmGatewayRegistryController controller = new AdmGatewayRegistryController(null, null);

    @Test
    void directBlockIsRejectedBeforeOwnerInvocation() {
        assertConflict("BLOCKED");
    }

    @Test
    void directRetireIsRejectedBeforeOwnerInvocation() {
        assertConflict("RETIRED");
    }

    private void assertConflict(String targetState) {
        ResponseStatusException failure = assertThrows(ResponseStatusException.class,
                () -> controller.changeState(
                        "binding-1",
                        new CpfGatewayRegistryPort.BindingStateCommand(
                                "op-1", "binding-1", targetState, 3L,
                                "approval-1", "QA31 direct state boundary", "untrusted"),
                        null));
        assertEquals(HttpStatus.CONFLICT, failure.getStatusCode());
    }
}
