package com.cpf.common.runtime;

import com.cpf.common.management.JdbcCpfCommonManagementService;
import com.cpf.common.message.service.CmnCommonCatalogManagementService;
import com.cpf.common.runtime.cache.CpfCommonCacheRefreshEventRepository;
import java.lang.reflect.Modifier;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

class CpfCommonClassBasedAopContractTest {

    @Test
    void everyTransactionalCommonServiceIsProxyable() {
        for (Class<?> type : List.of(
                CmnCommonCatalogManagementService.class,
                JdbcCpfCommonManagementService.class)) {
            assertThat(type.isAnnotationPresent(Service.class)).as(type.getName()).isTrue();
            assertThat(Modifier.isFinal(type.getModifiers())).as(type.getName()).isFalse();
            assertThat(List.of(type.getDeclaredMethods()).stream()
                    .anyMatch(method -> method.isAnnotationPresent(Transactional.class)))
                    .as(type.getName())
                    .isTrue();
        }
    }

    @Test
    void commonCacheEventRepositoryIsProxyableForPersistenceExceptionTranslation() {
        Class<CpfCommonCacheRefreshEventRepository> type = CpfCommonCacheRefreshEventRepository.class;

        assertThat(type.isAnnotationPresent(Repository.class)).isTrue();
        assertThat(Modifier.isFinal(type.getModifiers())).isFalse();
    }
}
