package com.cpf.core.common.execution;

import com.cpf.core.api.execution.CpfExecutionCatalogPort;
import com.cpf.core.api.execution.CpfExecutionDefinition;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class CpfExecutionCatalogScannerTest {

    @Test
    void typeLevelSharedApiIsRegisteredOnceAtTheTypePath() {
        try (GenericApplicationContext context = new GenericApplicationContext()) {
            context.registerBean(TypeLevelSharedController.class);
            context.refresh();
            RecordingCatalogPort catalog = new RecordingCatalogPort();

            new CpfExecutionCatalogScanner(
                    context,
                    new MockEnvironment()
                            .withProperty("cpf.framework.module-id", "PAY")
                            .withProperty("cpf.framework.source-version", "test"),
                    catalog)
                    .afterSingletonsInstantiated();

            assertThat(catalog.definitions).singleElement().satisfies(definition -> {
                assertThat(definition.standardExecutionId()).isEqualTo("SPAYSM0001");
                assertThat(definition.sourceClass()).isEqualTo(TypeLevelSharedController.class.getName());
                assertThat(definition.sourceMethod()).isEmpty();
                assertThat(definition.httpMethod()).isEmpty();
                assertThat(definition.endpoint()).isEqualTo("/pay/internal/sample/operations");
            });
        }
    }

    @RequestMapping("/pay/internal/sample/operations")
    @com.cpf.core.api.execution.CpfSharedApi(
            id = "SPAYSM0001",
            name = "PaySampleOperations",
            ownerDomain = "PAY")
    static class TypeLevelSharedController {
        @PostMapping("/query")
        void query() {
        }

        @PostMapping("/command")
        void command() {
        }
    }

    private static final class RecordingCatalogPort implements CpfExecutionCatalogPort {
        private final List<CpfExecutionDefinition> definitions = new ArrayList<>();

        @Override
        public void upsertAll(Collection<CpfExecutionDefinition> definitions) {
            this.definitions.addAll(definitions);
        }

        @Override
        public List<CpfExecutionDefinition> findAll() {
            return List.copyOf(definitions);
        }

        @Override
        public Optional<CpfExecutionDefinition> findById(String standardExecutionId) {
            return definitions.stream()
                    .filter(item -> item.standardExecutionId().equals(standardExecutionId))
                    .findFirst();
        }
    }
}
