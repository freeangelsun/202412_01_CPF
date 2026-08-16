package com.cpf.messaging.schema.runtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.cpf.messaging.schema.api.CpfEventSchemaCompatibility;
import com.cpf.messaging.schema.api.CpfEventSchemaDescriptor;
import com.cpf.messaging.schema.api.CpfEventSchemaFormat;
import com.cpf.messaging.schema.api.CpfEventSchemaRegistry;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CpfEventSchemaRegistryTest {
    private final CpfEventSchemaRegistry registry = new CpfInMemoryEventSchemaRegistry();

    @Test
    void registersFindsAndValidatesJsonSchema() {
        var v1 = json(1, "order-v1", "{\"type\":\"object\",\"required\":[\"id\"]}");
        assertThat(registry.register(v1)).isEqualTo(v1);
        assertThat(registry.latest("orders")).contains(v1);
        assertThat(registry.byId("order-v1")).contains(v1);
        registry.validate(v1, "{\"id\":\"O-1\"}".getBytes(StandardCharsets.UTF_8));
        assertThatThrownBy(() -> registry.validate(v1, "{}".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("required field missing");
    }

    @Test
    void rejectsBreakingAndOutOfOrderVersions() {
        registry.register(json(1, "order-v1", "{\"type\":\"object\",\"required\":[\"id\"]}"));
        var breaking = json(2, "order-v2", "{\"type\":\"object\",\"required\":[\"id\",\"newRequired\"]}");
        assertThatThrownBy(() -> registry.register(breaking))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("breaking schema");
        assertThatThrownBy(() -> registry.register(json(3, "order-v3", "{\"type\":\"object\",\"required\":[\"id\"]}")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("increment by one");
    }

    @Test
    void rejectsDuplicateSchemaIdAcrossSubjects() {
        registry.register(json(1, "shared-id", "{\"type\":\"object\"}"));
        var duplicate = new CpfEventSchemaDescriptor(
                "payments", 1, "shared-id", CpfEventSchemaFormat.JSON_SCHEMA,
                "application/json", "{\"type\":\"object\"}", CpfEventSchemaCompatibility.BACKWARD, Map.of());
        assertThatThrownBy(() -> registry.register(duplicate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("duplicate schemaId");
    }

    private static CpfEventSchemaDescriptor json(int version, String id, String definition) {
        return new CpfEventSchemaDescriptor(
                "orders", version, id, CpfEventSchemaFormat.JSON_SCHEMA,
                "application/json", definition, CpfEventSchemaCompatibility.BACKWARD, Map.of());
    }
}
