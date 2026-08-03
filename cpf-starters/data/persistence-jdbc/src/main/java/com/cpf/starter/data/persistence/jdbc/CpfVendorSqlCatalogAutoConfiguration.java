
package com.cpf.starter.data.persistence.jdbc;

import com.cpf.core.api.database.CpfDatabaseVendor;
import com.cpf.core.api.database.CpfVendorSqlCatalogProvider;
import com.cpf.core.api.database.CpfVendorSqlCatalogs;
import java.nio.file.Path;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/** Supplies deterministic, fail-closed SQL catalogs from the selected external Vendor Pack. */
@AutoConfiguration(after = CpfDomainDataSourceAutoConfiguration.class)
@ConditionalOnProperty(prefix = "cpf.domain.persistence", name = "provider", havingValue = "jdbc")
public class CpfVendorSqlCatalogAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(CpfVendorSqlCatalogProvider.class)
    CpfVendorSqlCatalogProvider cpfVendorSqlCatalogProvider(Environment environment) {
        CpfDatabaseVendor vendor = CpfDatabaseVendor.from(
                environment.getProperty("cpf.db.vendor", "mariadb"));
        String configured = environment.getProperty("cpf.db.resource-root");
        if (configured == null || configured.isBlank()) {
            throw new IllegalStateException(
                    "cpf.db.resource-root is required for the JDBC Generated Domain provider");
        }
        Path root = Path.of(configured.trim());
        return moduleCode -> CpfVendorSqlCatalogs.fromPack(vendor, moduleCode, root);
    }
}
