package com.cpf.reference.online.foundation;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FoundationApiReferenceConsumerTest {
    private final FoundationApiReferenceConsumer consumer = new FoundationApiReferenceConsumer();

    @Test void pagingBoundary와outOfRange를공통계약으로처리한다() {
        assertEquals(List.of("C"), consumer.page(List.of("A","B","C"), 1, 2).items());
        assertTrue(consumer.page(List.of("A"), 10, 20).items().isEmpty());
        assertThrows(IllegalArgumentException.class, () -> consumer.page(List.of(), 0, 201));
    }

    @Test void fileTraversal을거부하고root하위만허용한다() {
        Path root = Path.of("/safe/root");
        assertEquals(root.resolve("a.txt"), consumer.uploadTarget(root, "a.txt"));
        assertThrows(IllegalArgumentException.class, () -> consumer.uploadTarget(root, "../secret.txt"));
    }

    @Test void systemCode와transactionId를failClosed한다() {
        assertTrue(consumer.temporaryId("adm").startsWith("ADM-"));
        assertThrows(IllegalArgumentException.class, () -> consumer.temporaryId("TOO-LONG"));
        String tx = "20260725123456789ADMABC12340000001";
        assertEquals(tx, consumer.transactionHeaders(tx).values().iterator().next());
        assertThrows(IllegalArgumentException.class, () -> consumer.transactionHeaders("spoof"));
    }
}
