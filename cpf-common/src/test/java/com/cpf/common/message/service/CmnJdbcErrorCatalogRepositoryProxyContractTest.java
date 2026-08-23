package com.cpf.common.message.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Modifier;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Repository;

class CmnJdbcErrorCatalogRepositoryProxyContractTest {
    @Test
    void repositoryRemainsInternalAndProxyableForPersistenceAdvice() {
        Class<CmnJdbcErrorCatalogRepository> type = CmnJdbcErrorCatalogRepository.class;

        assertThat(type.isAnnotationPresent(Repository.class)).isTrue();
        assertThat(Modifier.isPublic(type.getModifiers())).isFalse();
        assertThat(Modifier.isFinal(type.getModifiers())).isFalse();
        assertThat(CmnErrorCatalogRepository.class.isAssignableFrom(type)).isTrue();
    }
}
