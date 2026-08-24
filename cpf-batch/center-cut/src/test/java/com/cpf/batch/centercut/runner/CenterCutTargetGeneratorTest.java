package com.cpf.batch.centercut.runner;

import com.cpf.batch.spi.CenterCutTargetProvider.Target;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CenterCutTargetGeneratorTest {
    @Test
    void targetPreparationNeverAuthorizesBusinessProcessing() {
        assertEquals("TARGETING", CenterCutTargetGenerator.preparedState(false));
        assertEquals("TARGET_READY", CenterCutTargetGenerator.preparedState(true));
    }

    @Test
    void acceptsBoundedPageWithAdvancingContinuationCursor() {
        List<Target> validated = CenterCutTargetGenerator.validatePage(
                "cursor-0", 2, List.of(
                        new Target("customer-1", "cursor-1", "{}", false),
                        new Target("customer-2", "cursor-2", "{}", false)));

        assertEquals(2, validated.size());
    }

    @Test
    void rejectsOversizedDuplicateAndStalledPages() {
        assertThrows(IllegalStateException.class, () -> CenterCutTargetGenerator.validatePage(
                null, 1, List.of(
                        new Target("customer-1", "cursor-1", "{}", false),
                        new Target("customer-2", "cursor-2", "{}", false))));
        assertThrows(IllegalArgumentException.class, () -> CenterCutTargetGenerator.validatePage(
                null, 2, List.of(
                        new Target("customer-1", "cursor-1", "{}", false),
                        new Target("customer-1", "cursor-2", "{}", false))));
        assertThrows(IllegalStateException.class, () -> CenterCutTargetGenerator.validatePage(
                "same", 1, List.of(new Target("customer-1", "same", "{}", false))));
    }

    @Test
    void rejectsIntermediateLastMissingCursorAndNullTarget() {
        assertThrows(IllegalArgumentException.class, () -> CenterCutTargetGenerator.validatePage(
                null, 2, List.of(
                        new Target("customer-1", null, "{}", true),
                        new Target("customer-2", "cursor-2", "{}", false))));
        assertThrows(IllegalArgumentException.class, () -> CenterCutTargetGenerator.validatePage(
                null, 1, List.of(new Target("customer-1", null, "{}", false))));
        ArrayList<Target> nullPage = new ArrayList<>();
        nullPage.add(null);
        assertThrows(IllegalArgumentException.class, () ->
                CenterCutTargetGenerator.validatePage(null, 1, nullPage));
    }
}
