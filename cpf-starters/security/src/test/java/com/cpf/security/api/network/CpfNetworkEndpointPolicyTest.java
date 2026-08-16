package com.cpf.security.api.network;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class CpfNetworkEndpointPolicyTest {
    @Test
    void supportsPrivateOnlyGatewayPolicy() {
        CpfNetworkEndpointPolicy policy = new CpfNetworkEndpointPolicy(
                List.of("10.0.0.0/8"), List.of(443), true, false, false, true);
        assertDoesNotThrow(() -> policy.validateEndpoint("https://10.10.20.30"));
        assertThrows(IllegalArgumentException.class,
                () -> policy.validateEndpoint("https://8.8.8.8"));
    }

    @Test
    void alwaysRejectsSpecialUseEvenWhenPrivateAllowed() {
        CpfNetworkEndpointPolicy policy = new CpfNetworkEndpointPolicy(
                List.of(), List.of(443), true, true, false, true);
        assertThrows(IllegalArgumentException.class,
                () -> policy.validateEndpoint("https://127.0.0.1"));
        assertThrows(IllegalArgumentException.class,
                () -> policy.validateEndpoint("https://169.254.169.254"));
    }

    @Test
    void validatesIpv6AndDnsRebinding() {
        CpfNetworkEndpointPolicy policy = new CpfNetworkEndpointPolicy(
                List.of(), List.of(443), false, true, true, true);
        assertDoesNotThrow(() -> policy.validateEndpoint("https://example.internal"));
        assertThrows(IllegalArgumentException.class,
                () -> policy.validateResolvedAddresses("example.internal", List.of("10.0.0.10")));
        assertThrows(IllegalArgumentException.class,
                () -> policy.validateResolvedAddresses("example.internal", List.of("::1")));
    }

    @Test
    void rejectsMalformedCorpus() {
        CpfNetworkEndpointPolicy policy = CpfNetworkEndpointPolicy.secureDefault();
        for (String invalid : List.of("", "ftp://example.com", "https://user:pass@example.com", "https://300.1.1.1")) {
            assertThrows(RuntimeException.class, () -> policy.validateEndpoint(invalid));
        }
    }
}
