
package com.cpf.data.persistence.jdbc;

import com.cpf.data.persistence.api.database.CpfDatabaseVendor;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalogProvider;
import java.nio.file.Path;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/** Supplies deterministic, fail-closed SQL catalogs from the selected external Vendor Pack. */
@AutoConfiguration(after = CpfDomainDataSourceAutoConfiguration.class)
@ConditionalOnProperty(prefix = "cpf.db", name = "resource-root")
public class CpfVendorSqlCatalogAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(CpfVendorSqlCatalogProvider.class)
    CpfVendorSqlCatalogProvider cpfVendorSqlCatalogProvider(Environment environment) {
        CpfDatabaseVendor vendor = CpfDatabaseVendor.from(
                environment.getProperty("cpf.db.vendor", "mariadb"));
        String configured = environment.getProperty("cpf.db.resource-root");
        if (configured == null || configured.isBlank()) {
            throw new IllegalStateException(
                    "cpf.db.resource-root is required for the Vendor SQL catalog provider");
        }
        Path root = Path.of(configured.trim());
        return moduleCode -> CpfVendorSqlCatalogs.fromPack(vendor, moduleCode, root);
    }
}
