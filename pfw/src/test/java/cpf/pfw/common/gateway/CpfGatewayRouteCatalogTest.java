package cpf.pfw.common.gateway;

import cpf.pfw.common.execution.CpfExecutionCatalogPort;
import cpf.pfw.common.execution.CpfExecutionDefinition;
import cpf.pfw.common.execution.CpfExecutionType;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CpfGatewayRouteCatalogTest {

    @Test
    void 공개온라인거래만GatewaySnapshot에포함한다() {
        CpfExecutionDefinition online = definition("OACCAC0001", CpfExecutionType.ONLINE, "PUBLIC", true);
        CpfExecutionDefinition shared = definition("SACCSH0001", CpfExecutionType.SHARED, "INTERNAL", false);
        CpfGatewayRouteCatalog catalog = new CpfGatewayRouteCatalog(new MemoryCatalog(List.of(online, shared)));

        var snapshot = catalog.loadPublicRoutes();

        assertThat(snapshot).containsOnlyKeys("OACCAC0001");
        assertThat(catalog.resolve(snapshot, "OACCAC0001").serviceId()).isEqualTo("ACC");
        assertThatThrownBy(() -> catalog.resolve(snapshot, "SACCSH0001"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private CpfExecutionDefinition definition(
            String id, CpfExecutionType type, String visibility, boolean gatewayAllowed) {
        return new CpfExecutionDefinition(id, "테스트", type, "ACC", "ACC",
                "cpf.acc.TestController", "execute", "POST", "/api/v1/test", "testOperation",
                "테스트 실행", "", false, visibility, true, gatewayAllowed, "test", Instant.now());
    }

    private record MemoryCatalog(List<CpfExecutionDefinition> definitions) implements CpfExecutionCatalogPort {
        @Override
        public void upsertAll(Collection<CpfExecutionDefinition> ignored) {
        }

        @Override
        public List<CpfExecutionDefinition> findAll() {
            return definitions;
        }

        @Override
        public Optional<CpfExecutionDefinition> findById(String standardExecutionId) {
            return definitions.stream().filter(item -> item.standardExecutionId().equals(standardExecutionId)).findFirst();
        }
    }
}
