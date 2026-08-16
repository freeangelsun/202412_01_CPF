package com.cpf.platform.operations.runtimecontrol;

import java.time.Instant;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CpfRuntimeControlContractTest {
    @Test void deliveryCarriesTypedImmutablePayload() {
        CpfRuntimePayload payload = CpfRuntimePayload.parse("{\"id\":1}");
        CpfRuntimeDelivery delivery = new CpfRuntimeDelivery(
                "delivery-1", "change-1", "CACHE_REFRESH", "was-1",
                3, 7, "request-hash", "payload-hash", payload, 1,
                Instant.parse("2026-08-15T00:00:00Z"));
        assertThat(delivery.changeType()).isEqualTo("CACHE_REFRESH");
        assertThat(delivery.desiredVersion()).isEqualTo(3);
        assertThat(delivery.fencingToken()).isEqualTo(7);
        assertThat(delivery.payload().canonicalJson()).isEqualTo("{\"id\":1}");
        assertThat(delivery.payloadSchemaVersion()).isEqualTo(1);
    }

    @Test void failureDoesNotExposeArbitraryLongText() {
        CpfRuntimeApplyResult result = CpfRuntimeApplyResult.failure("ERR", "x".repeat(2000));
        assertThat(result.success()).isFalse();
        assertThat(result.failureMessage()).hasSize(1000);
    }

    @Test void payloadProvidesTypedAccessWithoutLeakingJacksonTypes() {
        CpfRuntimePayload payload = CpfRuntimePayload.parse("""
                {"enabled":true,"limit":12,"name":"gateway","tags":["a","b"],
                 "clients":[{"id":"one"}],"routes":{"r1":{"permits":3}}}
                """);

        assertThat(payload.contains("clients")).isTrue();
        assertThat(payload.fieldNames()).containsExactly("clients", "enabled", "limit", "name", "routes", "tags");
        assertThat(payload.text("name", null)).isEqualTo("gateway");
        assertThat(payload.longValue("limit", 0)).isEqualTo(12);
        assertThat(payload.booleanValue("enabled", false)).isTrue();
        assertThat(payload.stringList("tags")).containsExactly("a", "b");
        assertThat(payload.objectList("clients").getFirst().text("id", null)).isEqualTo("one");
        assertThat(payload.objectMap("routes").get("r1").longValue("permits", 0)).isEqualTo(3);
        assertThat(payload.text("missing", "fallback")).isEqualTo("fallback");
        assertThatThrownBy(() -> payload.longValue("name", 0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
