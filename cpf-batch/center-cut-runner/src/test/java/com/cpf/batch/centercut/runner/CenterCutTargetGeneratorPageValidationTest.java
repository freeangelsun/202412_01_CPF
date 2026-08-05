package com.cpf.batch.centercut.runner;

import com.cpf.batch.spi.CenterCutTargetProvider;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CenterCutTargetGeneratorPageValidationTest {
    @Test
    void acceptsAdvancingPageAndFinalPage() {
        assertEquals(2, CenterCutTargetGenerator.validatePage("c0", 2, List.of(
                new CenterCutTargetProvider.Target("A", "c1", "{}", false),
                new CenterCutTargetProvider.Target("B", "c2", "{}", false))).size());
        assertEquals(1, CenterCutTargetGenerator.validatePage("c2", 2, List.of(
                new CenterCutTargetProvider.Target("C", null, "{}", true))).size());
    }

    @Test
    void rejectsOversizedDuplicateAndNonAdvancingPages() {
        assertThrows(IllegalStateException.class, () -> CenterCutTargetGenerator.validatePage(null, 1, List.of(
                new CenterCutTargetProvider.Target("A", "c1", "{}", false),
                new CenterCutTargetProvider.Target("B", "c2", "{}", false))));
        assertThrows(IllegalArgumentException.class, () -> CenterCutTargetGenerator.validatePage(null, 2, List.of(
                new CenterCutTargetProvider.Target("A", "c1", "{}", false),
                new CenterCutTargetProvider.Target("A", "c2", "{}", false))));
        assertThrows(IllegalStateException.class, () -> CenterCutTargetGenerator.validatePage("same", 2, List.of(
                new CenterCutTargetProvider.Target("A", "same", "{}", false))));
    }

    @Test
    void rejectsEarlyLastMarkerAndMissingContinuationCursor() {
        assertThrows(IllegalArgumentException.class, () -> CenterCutTargetGenerator.validatePage(null, 2, List.of(
                new CenterCutTargetProvider.Target("A", null, "{}", true),
                new CenterCutTargetProvider.Target("B", "c2", "{}", false))));
        assertThrows(IllegalArgumentException.class, () -> CenterCutTargetGenerator.validatePage(null, 1, List.of(
                new CenterCutTargetProvider.Target("A", null, "{}", false))));
    }
}
