package com.cpf.core.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.cpf.core.api.database.CpfDatabaseVendor;
import com.cpf.core.api.database.CpfVendorSqlCatalog;
import com.cpf.core.api.database.CpfVendorSqlCatalogProvider;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.mock.env.MockEnvironment;

import java.nio.file.Path;

class CpfPublicBoundaryAutoConfigurationTest {

    @Test
    void providesVendorSqlCatalogWithoutCoreDataSourceConfiguration() {
        try (AnnotationConfigApplicationContext context = contextWith(
                CpfPublicBoundaryAutoConfiguration.class)) {
            CpfVendorSqlCatalog catalog =
                    context.getBean(CpfVendorSqlCatalogProvider.class).forModule("bat");

            assertThat(catalog.vendor()).isEqualTo(CpfDatabaseVendor.MARIADB);
            assertThat(catalog.resourcePath("runtime-registry-find"))
                    .isEqualTo("runtime/bat/repository/runtime-registry-find.sql");
        }
    }

    @Test
    void preservesApplicationProvidedVendorSqlCatalogProvider() {
        CpfVendorSqlCatalogProvider customProvider = moduleCode -> null;
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.setEnvironment(testEnvironment());
            context.getBeanFactory().registerSingleton("customSqlCatalogProvider", customProvider);
            context.register(CpfPublicBoundaryAutoConfiguration.class);
            context.refresh();

            assertThat(context.getBeansOfType(CpfVendorSqlCatalogProvider.class))
                    .containsOnlyKeys("customSqlCatalogProvider");
            assertThat(context.getBean(CpfVendorSqlCatalogProvider.class)).isSameAs(customProvider);
        }
    }

    private AnnotationConfigApplicationContext contextWith(Class<?> configurationClass) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.setEnvironment(testEnvironment());
        context.register(configurationClass);
        context.refresh();
        return context;
    }

    private MockEnvironment testEnvironment() {
        return new MockEnvironment()
                .withProperty("cpf.db.vendor", "mariadb")
                .withProperty(
                        "cpf.db.resource-root",
                        Path.of("..", "cpf-tools", "db", "vendor", "mariadb")
                                .toAbsolutePath()
                                .normalize()
                                .toString());
    }
}
