package com.cpf.core.common.servicecall;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/** Routing weight/priority/zone/drain/failover가 실제 선택에 반영되는지 검증합니다. */
class CpfHealthAwareInstanceSelectorTest {
    private final CpfHealthAwareInstanceSelector selector = new CpfHealthAwareInstanceSelector();

    @Test
    void maintenanceDrainDownAndInactiveInstancesAreExcluded() {
        var maintenance = instance("maintenance", 100, 0, "Y", "N", "UP", "Y");
        var draining = instance("draining", 100, 0, "N", "Y", "UP", "Y");
        var down = instance("down", 100, 0, "N", "N", "DOWN", "Y");
        var inactive = instance("inactive", 100, 0, "N", "N", "UP", "N");
        var healthy = instance("healthy", 100, 0, "N", "N", "UP", "Y");

        assertThat(selector.select(List.of(maintenance, draining, down, inactive, healthy), null)
                .orElseThrow().get("instanceId")).isEqualTo("healthy");
    }

    @Test
    void lowerPriorityWinsBeforeWeight() {
        var rows = List.of(
                instance("high-weight-low-priority", 10_000, 20, "N", "N", "UP", "Y"),
                instance("low-weight-high-priority", 1, 10, "N", "N", "UP", "Y"));

        assertThat(selector.select(rows, null, Set.of(), context("tx-1", null, null))
                .orElseThrow().get("instanceId")).isEqualTo("low-weight-high-priority");
    }

    @Test
    void preferredZoneAndCellWinWhenSamePriority() {
        var a = instance("a", 100, 10, "N", "N", "UP", "Y");
        a.put("zoneCode", "AZ1"); a.put("cellCode", "CELL1");
        var b = instance("b", 100, 10, "N", "N", "UP", "Y");
        b.put("zoneCode", "AZ2"); b.put("cellCode", "CELL2");

        assertThat(selector.select(List.of(a, b), null, Set.of(), context("tx-1", "AZ2", "CELL2"))
                .orElseThrow().get("instanceId")).isEqualTo("b");
    }

    @Test
    void sameRoutingKeyIsStableAndExcludedTargetFailsOver() {
        var rows = List.of(
                instance("a", 50, 10, "N", "N", "UP", "Y"),
                instance("b", 100, 10, "N", "N", "UP", "Y"));
        var context = context("stable-key", null, null);
        String first = String.valueOf(selector.select(rows, null, Set.of(), context)
                .orElseThrow().get("instanceId"));
        for (int i = 0; i < 20; i++) {
            assertThat(String.valueOf(selector.select(rows, null, Set.of(), context)
                    .orElseThrow().get("instanceId"))).isEqualTo(first);
        }
        assertThat(String.valueOf(selector.select(rows, null, Set.of(first), context)
                .orElseThrow().get("instanceId"))).isNotEqualTo(first);
    }

    @Test
    void weightedRendezvousProducesStatisticalDistribution() {
        var light = instance("light", 100, 10, "N", "N", "UP", "Y");
        var heavy = instance("heavy", 400, 10, "N", "N", "UP", "Y");
        int lightSelected = 0;
        int heavySelected = 0;
        for (int i = 0; i < 10_000; i++) {
            String selected = String.valueOf(selector.select(
                    List.of(light, heavy), null, Set.of(), context("route-" + i, null, null))
                    .orElseThrow().get("instanceId"));
            if ("light".equals(selected)) lightSelected++;
            if ("heavy".equals(selected)) heavySelected++;
        }

        assertThat(heavySelected).isGreaterThan(lightSelected * 3);
        assertThat(heavySelected + lightSelected).isEqualTo(10_000);
    }

    @Test
    void explicitRequestedInstanceStillHonorsHealthAndDrainFence() {
        var draining = instance("requested", 100, 0, "N", "Y", "UP", "Y");
        var healthy = instance("healthy", 100, 0, "N", "N", "UP", "Y");

        assertThat(selector.select(List.of(draining, healthy), "requested")).isEmpty();
        assertThat(selector.select(List.of(draining, healthy), "healthy")).isPresent();
    }

    private CpfHealthAwareInstanceSelector.SelectionContext context(
            String routingKey, String zone, String cell) {
        return new CpfHealthAwareInstanceSelector.SelectionContext(routingKey, zone, cell, false, false);
    }

    private LinkedHashMap<String, Object> instance(
            String id,
            int weight,
            int priority,
            String maintenance,
            String drain,
            String status,
            String active) {
        LinkedHashMap<String, Object> row = new LinkedHashMap<>();
        row.put("instanceId", id);
        row.put("activeYn", active);
        row.put("instanceStatus", status);
        row.put("weight", weight);
        row.put("priorityNo", priority);
        row.put("maintenanceYn", maintenance);
        row.put("drainYn", drain);
        return row;
    }
}
