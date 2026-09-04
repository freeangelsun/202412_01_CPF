package com.cpf.web.runtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cpf.foundation.execution.api.CpfOnlineTransaction;
import com.cpf.foundation.execution.api.CpfOperationCatalogRegistry;
import com.cpf.web.context.CpfOperationOwnerResolver;
import com.cpf.web.context.CpfRuntimeIdentity;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/** Generated Domain Operation Catalog가 빈 도메인과 실제 업무 도메인을 구분하는 계약입니다. */
class CpfOperationCatalogBootstrapTest {
    @Test
    void emptyGeneratedDomainVerifiesManifestContractWithoutRequiringRegistry() {
        RequestMappingHandlerMapping mappings = mock(RequestMappingHandlerMapping.class);
        when(mappings.getHandlerMethods()).thenReturn(Map.of());

        assertThatCode(() -> bootstrap(mappings).afterSingletonsInstantiated()).doesNotThrowAnyException();
    }

    @Test
    void generatedDomainWithBusinessOperationStillRequiresExactlyOneRegistry() throws Exception {
        RequestMappingHandlerMapping mappings = mock(RequestMappingHandlerMapping.class);
        Method method = BusinessController.class.getDeclaredMethod("read");
        RequestMappingInfo route = RequestMappingInfo.paths("/business")
                .methods(org.springframework.web.bind.annotation.RequestMethod.GET)
                .build();
        when(mappings.getHandlerMethods()).thenReturn(Map.of(route, new HandlerMethod(new BusinessController(), method)));

        assertThatThrownBy(() -> bootstrap(mappings).afterSingletonsInstantiated())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("CPF_OPERATION_CATALOG_REGISTRY_UNAVAILABLE:0");
    }

    @Test
    void sameJvmTopologySynchronizesARealBusinessOwnerInsteadOfSkippingTheCatalog() throws Exception {
        RequestMappingHandlerMapping mappings = mock(RequestMappingHandlerMapping.class);
        Method method = BusinessController.class.getDeclaredMethod("read");
        RequestMappingInfo route = RequestMappingInfo.paths("/business")
                .methods(org.springframework.web.bind.annotation.RequestMethod.GET)
                .build();
        when(mappings.getHandlerMethods()).thenReturn(Map.of(route, new HandlerMethod(new BusinessController(), method)));
        CpfOperationCatalogRegistry registry = mock(CpfOperationCatalogRegistry.class);
        CpfOperationOwnerResolver owner = (handler, operation) ->
                new CpfOperationOwnerResolver.CpfOperationOwner("MBR", "member", "cpf-member-online", "test");
        MockEnvironment environment = new MockEnvironment().withProperty("cpf.operation-catalog.manifest-required", "false");
        CpfOperationCatalogBootstrap bootstrap = new CpfOperationCatalogBootstrap(mappings,
                new CpfRuntimeIdentity(null, "DEV", "cpf-local-runtime", "one-was-1"), environment, List.of(registry),
                new CpfBusinessOperationManifestVerifier(), owner);

        bootstrap.afterSingletonsInstantiated();

        org.mockito.ArgumentCaptor<CpfOperationCatalogRegistry.SyncRequest> request = org.mockito.ArgumentCaptor
                .forClass(CpfOperationCatalogRegistry.SyncRequest.class);
        verify(registry).synchronize(request.capture());
        assertThat(request.getValue().systemCode()).isEqualTo("MBR");
        assertThat(request.getValue().operations()).hasSize(1);
    }

    private static CpfOperationCatalogBootstrap bootstrap(RequestMappingHandlerMapping mappings) {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("cpf.generated-domain.name", "catalog-test")
                .withProperty("cpf.operation-catalog.manifest-required", "false");
        return new CpfOperationCatalogBootstrap(
                mappings,
                new CpfRuntimeIdentity("CAT", "catalog-test-online", "test-1"),
                environment,
                List.of());
    }

    static final class BusinessController {
        @CpfOnlineTransaction(operationId = "CAT_BUSINESS_READ", name = "조회", description = "업무 항목을 조회한다.")
        public void read() { }
    }
}
