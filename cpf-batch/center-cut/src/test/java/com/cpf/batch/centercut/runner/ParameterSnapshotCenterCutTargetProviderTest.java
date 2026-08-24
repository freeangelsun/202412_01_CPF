package com.cpf.batch.centercut.runner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ParameterSnapshotCenterCutTargetProviderTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private final ParameterSnapshotCenterCutTargetProvider provider =
            new ParameterSnapshotCenterCutTargetProvider(mapper);

    @Test
    void pagesImmutableBusinessRequestsIntoCanonicalInvocationWorkItems() throws Exception {
        Map<String, Object> parameters = Map.of(
                "systemCode", "MBR",
                "operationId", "MBR_SAMPLE_TX_CREATE",
                "targets", List.of(
                        target("member-1", "idem-1"),
                        target("member-2", "idem-2")));

        var first = provider.next("job", "snapshot", null, 1, parameters);
        var second = provider.next("job", "snapshot", first.getFirst().cursor(), 1, parameters);

        assertThat(first).singleElement().satisfies(target -> {
            assertThat(target.businessKey()).isEqualTo("member-1");
            assertThat(target.cursor()).isEqualTo("offset:1");
            assertThat(target.last()).isFalse();
        });
        assertThat(second).singleElement().satisfies(target -> {
            assertThat(target.businessKey()).isEqualTo("member-2");
            assertThat(target.cursor()).isEqualTo("offset:2");
            assertThat(target.last()).isTrue();
        });
        @SuppressWarnings("unchecked")
        Map<String, Object> payload = mapper.readValue(second.getFirst().payload(), Map.class);
        assertThat(payload).containsEntry("systemCode", "MBR")
                .containsEntry("operationId", "MBR_SAMPLE_TX_CREATE");
        Map<String, Object> request = mapper.convertValue(payload.get("request"), new TypeReference<>() { });
        assertThat(request).containsEntry("idempotencyKey", "idem-2");
    }

    @Test
    void rejectsMissingRequestsAndForgedCursors() {
        assertThatThrownBy(() -> provider.next("job", "snapshot", null, 10,
                Map.of("systemCode", "MBR", "operationId", "OP", "targets", List.of())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must not be empty");
        assertThatThrownBy(() -> provider.next("job", "snapshot", "offset:99", 10,
                Map.of("systemCode", "MBR", "operationId", "OP", "targets", List.of(target("a", "b")))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cursor");
    }

    private static Map<String, Object> target(String businessKey, String idempotencyKey) {
        return Map.of("businessKey", businessKey, "request", Map.of(
                "sampleKey", businessKey,
                "itemName", "Center-Cut " + businessKey,
                "idempotencyKey", idempotencyKey));
    }
}
