package com.cpf.web.context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockHttpServletRequest;

class CpfConfiguredIngressTrustResolverTest {
    @Test
    void callerHeaderAloneNeverCreatesInternalTrust() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.20.30.40");
        request.addHeader(CpfHttpHeaderNames.CALLER_SYSTEM_CODE, "FORGED");

        var decision = new CpfConfiguredIngressTrustResolver(new MockEnvironment()).resolve(request);

        assertEquals(CpfHttpIngressTrust.UNTRUSTED_EXTERNAL, decision.trust());
        assertEquals(null, decision.verifiedCallerSystemCode());
    }

    @Test
    void configuredPeerIdentityIgnoresForgedCallerHeader() {
        MockEnvironment environment = new MockEnvironment().withProperty(
                CpfConfiguredIngressTrustResolver.PEER_IDENTITIES_PROPERTY,
                "10.20.30.0/24=MBR;10.40.0.7=EXS");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.20.30.40");
        request.addHeader(CpfHttpHeaderNames.CALLER_SYSTEM_CODE, "FORGED");

        var decision = new CpfConfiguredIngressTrustResolver(environment).resolve(request);

        assertEquals(CpfHttpIngressTrust.TRUSTED_INTERNAL, decision.trust());
        assertEquals("MBR", decision.verifiedCallerSystemCode());
    }

    @Test
    void authenticatedCallerAttributeTakesPrecedenceOverNetworkMapping() {
        MockEnvironment environment = new MockEnvironment().withProperty(
                CpfConfiguredIngressTrustResolver.PEER_IDENTITIES_PROPERTY, "10.20.30.40=MBR");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.20.30.40");
        request.setAttribute(CpfHttpIngressTrustResolver.VERIFIED_INTERNAL_CALLER_ATTRIBUTE, "EXS");

        var decision = new CpfConfiguredIngressTrustResolver(environment).resolve(request);

        assertEquals(CpfHttpIngressTrust.TRUSTED_INTERNAL, decision.trust());
        assertEquals("EXS", decision.verifiedCallerSystemCode());
    }

    @Test
    void invalidMappingFailsStartup() {
        assertThrows(IllegalArgumentException.class, () -> new CpfConfiguredIngressTrustResolver(
                new MockEnvironment().withProperty(
                        CpfConfiguredIngressTrustResolver.PEER_IDENTITIES_PROPERTY, "10.20.0.0/99=MBR")));
    }
}
