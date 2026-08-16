package com.cpf.integration.fixedlength.runtimecontrol;

import static org.assertj.core.api.Assertions.assertThat;

import com.cpf.integration.fixedlength.api.CpfFixedLengthLayout;
import com.cpf.integration.fixedlength.api.CpfFixedLengthLayoutRegistry;
import com.cpf.platform.operations.runtimecontrol.CpfRuntimeDelivery;
import com.cpf.platform.operations.runtimecontrol.CpfRuntimePayload;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class CpfFixedLengthRuntimeControlTest {

    @Test
    void fixedLayoutDeliveryDecodesAndRegistersCanonicalApiLayout() {
        CpfFixedLengthLayoutRegistry registry = new CpfFixedLengthLayoutRegistry();
        var result = new CpfFixedLayoutRuntimeApplier(registry).apply(delivery(
                "FIXED_LAYOUT",
                1L,
                """
                {
                  "layout": {
                    "layoutId": "PAY",
                    "version": "1",
                    "charset": "US-ASCII",
                    "totalLength": 4,
                    "fields": [
                      {"name":"code","start":1,"length":4,"type":"STRING","required":true}
                    ]
                  },
                  "expectedRegistryHash": ""
                }
                """));

        assertThat(result.applied()).isTrue();
        CpfFixedLengthLayout layout = registry.require("PAY", "1");
        assertThat(layout.totalLength()).isEqualTo(4);
        assertThat(layout.fields()).singleElement().satisfies(field -> {
            assertThat(field.name()).isEqualTo("code");
            assertThat(field.zeroBasedStart()).isZero();
        });
    }

    @Test
    void schemaRegistryDeliveryAtomicallyReplacesWithCanonicalApiLayouts() {
        CpfFixedLengthLayoutRegistry registry = new CpfFixedLengthLayoutRegistry();
        var result = new CpfSchemaRegistryRuntimeApplier(registry).apply(delivery(
                "SCHEMA_REGISTRY",
                7L,
                """
                {
                  "layouts": [
                    {
                      "layoutId": "ACC",
                      "version": "2",
                      "charset": "UTF-8",
                      "totalLength": 3,
                      "fields": [
                        {"name":"kind","start":1,"length":3,"type":"STRING"}
                      ]
                    }
                  ],
                  "compatibility": "NONE",
                  "expectedRegistryHash": ""
                }
                """));

        assertThat(result.applied()).isTrue();
        assertThat(registry.snapshot().version()).isEqualTo(7L);
        assertThat(registry.require("ACC", "2").charset().name()).isEqualTo("UTF-8");
    }

    private static CpfRuntimeDelivery delivery(String changeType, long desiredVersion, String payload) {
        return new CpfRuntimeDelivery(
                "delivery-1",
                "change-1",
                changeType,
                "instance-1",
                desiredVersion,
                1L,
                "request-hash",
                "payload-hash",
                CpfRuntimePayload.parse(payload),
                1,
                Instant.now().plusSeconds(60));
    }
}
