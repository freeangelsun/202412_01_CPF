package com.cpf.core.common.servicecall;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CpfHealthAwareInstanceSelectorTest {
    private final CpfHealthAwareInstanceSelector selector = new CpfHealthAwareInstanceSelector();

    @Test
    void maintenanceAndDrainInstancesAreExcluded() {
        var rows = List.of(instance("a", 100, 0, "Y", "N"), instance("b", 100, 0, "N", "N"));
        assertThat(selector.select(rows, null).orElseThrow().get("instanceId")).isEqualTo("b");
    }

    @Test
    void lowerPriorityWinsBeforeWeight() {
        var rows = List.of(instance("a", 10000, 20, "N", "N"), instance("b", 1, 10, "N", "N"));
        assertThat(selector.select(rows, null, Set.of(), new CpfHealthAwareInstanceSelector.SelectionContext("tx-1", null, null, false, false))
                .orElseThrow().get("instanceId")).isEqualTo("b");
    }

    @Test
    void preferredZoneWinsWhenSamePriority() {
        var a = instance("a", 100, 10, "N", "N"); a.put("zoneCode", "AZ1");
        var b = instance("b", 100, 10, "N", "N"); b.put("zoneCode", "AZ2");
        assertThat(selector.select(List.of(a, b), null, Set.of(),
                new CpfHealthAwareInstanceSelector.SelectionContext("tx-1", "AZ2", null, false, false))
                .orElseThrow().get("instanceId")).isEqualTo("b");
    }

    @Test
    void sameRoutingKeyIsStable() {
        var rows = List.of(instance("a", 50, 10, "N", "N"), instance("b", 100, 10, "N", "N"));
        var ctx = new CpfHealthAwareInstanceSelector.SelectionContext("stable-key", null, null, false, false);
        String first = String.valueOf(selector.select(rows, null, Set.of(), ctx).orElseThrow().get("instanceId"));
        for (int i = 0; i < 20; i++) {
            assertThat(String.valueOf(selector.select(rows, null, Set.of(), ctx).orElseThrow().get("instanceId"))).isEqualTo(first);
        }
    }

    private LinkedHashMap<String, Object> instance(String id, int weight, int priority, String maintenance, String drain) {
        LinkedHashMap<String, Object> row = new LinkedHashMap<>();
        row.put("instanceId", id); row.put("activeYn", "Y"); row.put("instanceStatus", "UP");
        row.put("weight", weight); row.put("priorityNo", priority); row.put("maintenanceYn", maintenance); row.put("drainYn", drain);
        return row;
    }
}
