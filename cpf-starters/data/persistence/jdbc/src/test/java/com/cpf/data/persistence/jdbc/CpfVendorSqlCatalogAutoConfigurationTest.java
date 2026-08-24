package com.cpf.data.persistence.jdbc;

import com.cpf.data.persistence.api.database.CpfVendorSqlCatalogProvider;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class CpfVendorSqlCatalogAutoConfigurationTest {
    private final ApplicationContextRunner context = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(CpfVendorSqlCatalogAutoConfiguration.class))
            .withPropertyValues(
                    "cpf.db.vendor=mariadb",
                    "cpf.db.resource-root=.");

    @Test
    void suppliesExactlyOneCatalogForEveryJdbcBackedPersistenceStyle() {
        for (String provider : List.of("jdbc", "mybatis", "jpa")) {
            context.withPropertyValues("cpf.domain.persistence.provider=" + provider).run(application -> {
                assertThat(application).hasSingleBean(CpfVendorSqlCatalogProvider.class);
                assertThat(application).hasNotFailed();
            });
        }
    }

    @Test
    void suppliesCatalogToNonDomainDb3RuntimeWhenExternalPackIsSelected() {
        context.withPropertyValues("cpf.domain.persistence.enabled=false").run(application -> {
            assertThat(application).hasSingleBean(CpfVendorSqlCatalogProvider.class);
            assertThat(application).hasNotFailed();
        });
    }

    @Test
    void staysAbsentWhenNoExternalVendorPackIsSelected() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(CpfVendorSqlCatalogAutoConfiguration.class))
                .withPropertyValues("cpf.db.vendor=mariadb")
                .run(application ->
                        assertThat(application).doesNotHaveBean(CpfVendorSqlCatalogProvider.class));
    }
}
