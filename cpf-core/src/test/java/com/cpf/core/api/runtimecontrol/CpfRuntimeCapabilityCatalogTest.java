package com.cpf.core.api.runtimecontrol;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CpfRuntimeCapabilityCatalogTest {

    @Test
    void exposesFourteenIndependentRuntimeCapabilities() {
        assertThat(CpfRuntimeCapabilityCatalog.capabilities()).hasSize(14);
        assertThat(CpfRuntimeCapabilityCatalog.resolve("GATEWAY_ROUTE_UPDATE"))
                .get()
                .extracting(CpfRuntimeCapabilityCatalog.Capability::code)
                .isEqualTo("GATEWAY");
    }

    @Test
    void unknownChangeTypeIsFailClosedForApproval() {
        assertThat(CpfRuntimeCapabilityCatalog.requiresApproval("CUSTOM_VENDOR_COMMAND")).isTrue();
    }
}
