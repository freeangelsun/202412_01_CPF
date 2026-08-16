package com.cpf.batch.scheduler;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class SchedulerAvailableWindowTest {
    @Test
    void ordinaryWindowIncludesItsBoundaries() {
        assertThat(SchedulerDispatchService.withinAvailableWindow(
                LocalTime.of(9, 0), LocalTime.of(9, 0), LocalTime.of(18, 0))).isTrue();
        assertThat(SchedulerDispatchService.withinAvailableWindow(
                LocalTime.of(18, 1), LocalTime.of(9, 0), LocalTime.of(18, 0))).isFalse();
    }

    @Test
    void overnightWindowCrossesMidnight() {
        assertThat(SchedulerDispatchService.withinAvailableWindow(
                LocalTime.of(23, 0), LocalTime.of(22, 0), LocalTime.of(2, 0))).isTrue();
        assertThat(SchedulerDispatchService.withinAvailableWindow(
                LocalTime.of(1, 30), LocalTime.of(22, 0), LocalTime.of(2, 0))).isTrue();
        assertThat(SchedulerDispatchService.withinAvailableWindow(
                LocalTime.NOON, LocalTime.of(22, 0), LocalTime.of(2, 0))).isFalse();
    }
}
