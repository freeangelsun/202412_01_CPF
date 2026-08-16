package com.cpf.batch.control.compat;

import com.cpf.batch.control.security.BatAuthenticatedIdentity;
import com.cpf.batch.control.security.BatVerifiedActorResolver;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class BatCenterCutInternalControllerTest {
    @Test
    void authenticatedAdmCanUseTheWhitelistedTargetQuery() {
        BatCenterCutOperationsService operations = mock(BatCenterCutOperationsService.class);
        BatVerifiedActorResolver actors = mock(BatVerifiedActorResolver.class);
        MockHttpServletRequest request = new MockHttpServletRequest();
        when(actors.identity(request)).thenReturn(adm());
        when(operations.findTargets("CC-JOB", null, 100))
                .thenReturn(List.of(Map.of("targetId", 1L)));
        BatCenterCutInternalController controller =
                new BatCenterCutInternalController(operations, actors);

        Object result = controller.invoke(
                "findTargets",
                Map.of("centerCutJobId", "CC-JOB"),
                request);

        assertThat(result).isEqualTo(List.of(Map.of("targetId", 1L)));
        verify(operations).findTargets("CC-JOB", null, 100);
    }

    @Test
    void nonAdmCallerIsRejectedEvenWhenControllerIsInvokedWithoutTheFilterChain() {
        BatCenterCutOperationsService operations = mock(BatCenterCutOperationsService.class);
        BatVerifiedActorResolver actors = mock(BatVerifiedActorResolver.class);
        MockHttpServletRequest request = new MockHttpServletRequest();
        when(actors.identity(request)).thenReturn(new BatAuthenticatedIdentity(
                "runtime-a", "BAT", "worker-a", null, null, null));
        BatCenterCutInternalController controller =
                new BatCenterCutInternalController(operations, actors);

        assertThatThrownBy(() -> controller.invoke("findJobs", Map.of(), request))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("ADM");
        verifyNoInteractions(operations);
    }

    @Test
    void unknownOperationUnexpectedFieldAndOutOfRangeLimitFailClosed() {
        BatCenterCutOperationsService operations = mock(BatCenterCutOperationsService.class);
        BatVerifiedActorResolver actors = mock(BatVerifiedActorResolver.class);
        MockHttpServletRequest request = new MockHttpServletRequest();
        when(actors.identity(request)).thenReturn(adm());
        BatCenterCutInternalController controller =
                new BatCenterCutInternalController(operations, actors);

        assertThatThrownBy(() -> controller.invoke("dropEverything", Map.of(), request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported");
        assertThatThrownBy(() -> controller.invoke(
                "findJobs",
                Map.of("unexpected", "value"),
                request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("fields");
        assertThatThrownBy(() -> controller.invoke(
                "findJobDetail",
                Map.of(),
                request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("centerCutJobId");
        assertThatThrownBy(() -> controller.invoke(
                "findResults",
                Map.of("centerCutJobId", "CC-JOB", "limit", 501),
                request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("between 1 and 500");
        verifyNoInteractions(operations);
    }

    private static BatAuthenticatedIdentity adm() {
        return new BatAuthenticatedIdentity(
                "operator-a",
                "ADM",
                "adm-a",
                "CN=cpf-admin",
                null,
                null);
    }
}
