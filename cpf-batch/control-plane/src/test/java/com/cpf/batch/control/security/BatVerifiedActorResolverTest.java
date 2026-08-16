package com.cpf.batch.control.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.security.Principal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BatVerifiedActorResolverTest {
    private final BatVerifiedActorResolver resolver = new BatVerifiedActorResolver();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void bodyActorSpoofingIsRejected() {
        MockHttpServletRequest request = authenticatedRequest(
                new BatAuthenticatedIdentity(
                        "operator-a", "ADM", "adm-01", null, null, null));

        assertThatThrownBy(() -> resolver.actor(request, "operator-b", "requestUser"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("requestUser");
    }

    @Test
    void approvedActorsComeFromVerifiedDelegationNotBody() {
        MockHttpServletRequest request = authenticatedRequest(
                new BatAuthenticatedIdentity(
                        "approver-a",
                        "ADM",
                        "adm-01",
                        "CN=cpf-admin",
                        "approval-10",
                        "requester-a"));

        BatVerifiedActorResolver.ApprovedActors actors =
                resolver.approved(
                        request,
                        null,
                        null,
                        null);

        assertThat(actors.requestedBy()).isEqualTo("requester-a");
        assertThat(actors.approvedBy()).isEqualTo("approver-a");
        assertThat(actors.approvalRequestId()).isEqualTo("approval-10");
    }

    @Test
    void approvalBodyCannotReplaceVerifiedRequester() {
        MockHttpServletRequest request = authenticatedRequest(
                new BatAuthenticatedIdentity(
                        "approver-a",
                        "ADM",
                        "adm-01",
                        "CN=cpf-admin",
                        "approval-10",
                        "requester-a"));

        assertThatThrownBy(() -> resolver.approved(
                request,
                "requester-b",
                "approver-a",
                "approval-10"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("requestedBy");
    }

    private static MockHttpServletRequest authenticatedRequest(BatAuthenticatedIdentity identity) {
        UsernamePasswordAuthenticationToken authentication =
                UsernamePasswordAuthenticationToken.authenticated(
                        identity.operatorId(),
                        "verified",
                        List.of(new SimpleGrantedAuthority("BAT_AUTHENTICATED")));
        authentication.setDetails(identity);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setUserPrincipal((Principal) authentication);
        return request;
    }
}
